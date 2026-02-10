## ╰┈➤ ┆Laboratorio II: Snake Race┆

---

Daniel Palacios Moreno

 ---

# Parte 1
### Actividades
1. Toma el programa [Prime Finder](./src/main/java/primefinder/Main.java).
2. Modifícalo para que cada t milisegundos:
    * Se pausen todos los hilos trabajadores.
    * Se muestre cuántos números primos se han encontrado.
    * El programa espere ENTER para reanudar.

La sincronización debe usar synchronized, wait(), notify() / notifyAll() sobre el mismo monitor (sin busy-waiting).
Entrega en el reporte de laboratorio las observaciones y/o comentarios explicando tu diseño de sincronización (qué lock, qué condición, cómo evitas lost wakeups).


# Parte 2
### 1) Análisis de concurrencia
* Explica cómo el código usa hilos para dar autonomía a cada serpiente.
* Identifica y documenta en el reporte de laboratorio:
    * Posibles condiciones de carrera.
    * Colecciones o estructuras no seguras en contexto concurrente.
    * Ocurrencias de espera activa (busy-wait) o de sincronización innecesaria.
---
### 2) Correcciones mínimas y regiones críticas
* Elimina esperas activas reemplazándolas por señales / estados o mecanismos de la librería de concurrencia.
* Protege solo las regiones críticas estrictamente necesarias (evita bloqueos amplios).
* Justifica en el reporte de laboratorio cada cambio: cuál era el riesgo y cómo lo resuelves.

---
### 3) Control de ejecución seguro (UI)
Implementa la UI con Iniciar / Pausar / Reanudar (ya existe el botón Action y el reloj GameClock).
Al Pausar, muestra de forma consistente (sin tearing):
La serpiente viva más larga.
La peor serpiente (la que primero murió).
Considera que la suspensión no es instantánea; coordina para que el estado mostrado no quede “a medias”.

---
### 4) Robustez bajo carga
Ejecuta con N alto (-Dsnakes=20 o más) y/o aumenta la velocidad.
El juego no debe romperse: sin ConcurrentModificationException, sin lecturas inconsistentes, sin deadlocks.
Si habilitas teleports y turbo, verifica que las reglas no introduzcan carreras.
   
---

# ➤ ┆Reporte de laboratorio┆

---

# - Parte 1 

## Observaciones del ejercicio

En el ejercicio se creó un reloj [Timer.java](src/main/java/primefinder/Timer.java), el cual funciona en un hilo aparte de la lógica para calcular los números primos dentro de [PrimeFinderThread.java](src/main/java/primefinder/PrimeFinderThread.java); pero en este caso, para llevar el tiempo de manera "paralela" con los otros hilos, se crea un AtomicBoolean que sabe si el temporizador se acabó y, por lo tanto, sabe si tiene que parar el cálculo de los números primos y parar los hilos de PrimeFinderThread hasta que reciba la señal de reanudar mediante la entrada de ENTER del usuario, hasta llegar al máximo de números que se quieren verificar, en este caso, del 1 al 30000000.# - Parte 2

## 1) Analisis de concurrencia codigo original

### Explicacion de autonomia serpientes

- El código original, tiene dos clases principales que administran las serpientes concurrentemente. Primero está [SnakeRunner.java](src/main/java/co/eci/snake/concurrency/SnakeRunner.java), que implementa Runnable y es la que se encarga de realizar, dentro de un hilo virtual, los movimientos de los Snake y llamar a las funciones de los mismos. Por otra parte, está [GameClock.java](src/main/java/co/eci/snake/core/engine/GameClock.java), el cual implementa la interfaz AutoCloseable y es el que se encarga de los ticks para realizar el repaint del tablero dentro del UI. Estas dos funciones son llamadas por [SnakeApp.java](src/main/java/co/eci/snake/ui/legacy/SnakeApp.java), el cual, al ser creado, crea un GameClock que funciona como un reloj corriendo en su propio hilo para generar los repaints. Después, este declara con un executor la cantidad de hilos a generar, igual al total de Snakes declaradas al momento de ejecutar el programa, dándole así "autonomía" a cada serpiente en Threads independientes.

### Analisis posibles fallas

- Posibles condiciones de carrera:
    - El body de [Snake.java](src/main/java/co/eci/snake/core/Snake.java) genera condición de carrera debido a que no está protegido de ninguna manera y está siendo modificado y leído desde diferentes hilos. Se puede ver específicamente desde las funciones usadas dentro de [Board.java](src/main/java/co/eci/snake/core/Board.java) como advance o head, que son funciones de Snake, o directamente desde funciones como snapshot, el cual crea una copia de Body. Por otra parte, al usar una colección como ArrayDeque, que no es thread-safe, se pueden generar lecturas inconsistentes, excepciones en tiempo de ejecución o valores intermedios.
    - El movimiento de las snakes que los usuarios pueden mover se puede solapar con los movimientos que genera el programa aleatoriamente, lo que genera una mala representación del movimiento y también puede generar acciones indebidas como moverse en 180º.
    - Que el botón que permite pausar genere inconsistencia debido a que los [SnakeRunner.java](src/main/java/co/eci/snake/concurrency/SnakeRunner.java) de las snakes no se detienen, lo que al "reanudar" hace que las snakes aparezcan en sus posiciones "futuras", pareciendo así que se hubieran teletransportado.

- Colecciones o estructuras no seguras en contexto concurrente:
    - El ArrayDeque no es thread-safe, usado en el body de [Snake.java](src/main/java/co/eci/snake/core/Snake.java).
    - Teóricamente, el HashSet o el HashMap, los cuales son usados en [Board.java](src/main/java/co/eci/snake/core/Board.java), no son thread-safe, pero debido al comportamiento actual funcionan de manera adecuada.
    - Teóricamente, ArrayList, el cual es usado en [SnakeApp.java](src/main/java/co/eci/snake/ui/legacy/SnakeApp.java), no es thread-safe, pero en el contexto del uso que se le da no es realmente peligroso, a menos que se realicen mutaciones dentro de la lista dentro del código.

- Ocurrencias de espera activa (busy-wait) o de sincronización innecesaria:
    - No existe, por lo revisado y analizado, esperas activas.
    - Una sincronización potencialmente innecesaria o costosa son los métodos obstacles(), mice(), teleports() y turbo() de [Board.java](src/main/java/co/eci/snake/core/Board.java), debido a que se realizan copias constantes para realizar el repaint del tablero, lo cual bloquea el hilo que está haciendo step() y también genera objetos basura que se dejan de usar una vez se les da un único uso.

## 2) Correcciones 

- Para corregir el problema de que la UI se pausaba pero la lógica del juego no, se realizaron unos pequeños cambios, principalmente una clase que funciona como monitor, la cual es [PauseController.java](src/main/java/co/eci/snake/core/engine/PauseController.java); pero dentro de este cambio también se agregó una parte dentro del while del SnakeRunner, el cual verifica el estado del [GameState.java](src/main/java/co/eci/snake/core/GameState.java) y para si está en PAUSED o sale completamente del run si está en STOPPED.
- Para corregir el problema del body de [Snake.java](src/main/java/co/eci/snake/core/Snake.java), simplemente se agregó synchronized en los métodos que podían generar condiciones de carrera, como head(), snapshot(), advance() y, por último, turn(), que también generaba una condición en la cual el movimiento podía sobreponerse y generar una mala respuesta al usuario.
- Para corregir el problema de la basura generada por los métodos obstacles(), mice(), teleports() y turbo() dentro de [Board.java](src/main/java/co/eci/snake/core/Board.java), lo que se hace ahora es crear un record volatile el cual contiene la información necesaria que estos métodos buscan, para que de esta manera no se bloquee el método step() por culpa de la generación de una copia de lo que se pida con el método específico de los ya mencionados.

## 3) Pausa con mensaje

- Aquí en el video se puede ver cómo interactúa el pausar con el mensaje de la snake más larga y más corta, que, debido a la ambigüedad del ejercicio, consideré como la "muerta".

[20260210-0259-30.4099626.mp4](images/20260210-0259-30.4099626.mp4)

## 4) Rendimiento con muchas (+20) Snakes

- Aquí en el video se puede ver el rendimiento del programa en su estado actual de entrega con 25 Snakes, el cual, como se puede ver, no contiene errores de lógica ni fallos en los movimientos o velocidades.

[20260210-0304-22.7123541.mp4](images/20260210-0304-22.7123541.mp4)

### Ejecutar programa [Snake](src/main/java/co/eci/snake/app/Main.java) con Maven

```bash

 mvn clean install
 mvn -q -DskipTests exec:java -Dsnakes=25    
```