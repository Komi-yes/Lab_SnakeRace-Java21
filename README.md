## ╰┈➤ ┆Laboratorio II: Snake Race┆

---

Daniel Palacio s Moreno

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


### Ejecutar programa [Prime Finder](./src/main/java/primefinder/Main.java) con Maven

```bash

# Desde la raíz del proyecto
mvn compile exec:java -Dexec.mainClass="primefinder.Main"
```


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


# - Parte 2

## 1) Analisis de concurrencia codigo original

### Explicacion de autonomia serpientes

- El código original, tiene dos clases principales que administran las serpientes concurrentemente. Primero está [SnakeRunner.java](src/main/java/co/eci/snake/concurrency/SnakeRunner.java), que implementa Runnable y es la que se encarga de realizar, dentro de un hilo virtual, los movimientos de los Snake y llamar a las funciones de los mismos. Por otra parte, está [GameClock.java](src/main/java/co/eci/snake/core/engine/GameClock.java), el cual implementa la interfaz AutoCloseable y es el que se encarga de los ticks para realizar el repaint del tablero dentro del UI. Estas dos funciones son llamadas por [SnakeApp.java](src/main/java/co/eci/snake/ui/legacy/SnakeApp.java), el cual, al ser creado, crea un GameClock que funciona como un reloj corriendo en su propio hilo para generar los repaints. Después, este declara con un executor la cantidad de hilos a generar, igual al total de Snakes declaradas al momento de ejecutar el programa, dándole así "autonomía" a cada serpiente en Threads independientes.

### Analisis posibles fallas

- Posibles condiciones de carrera:
  - El body de [Snake.java](src/main/java/co/eci/snake/core/Snake.java) genera condicion de carrera debido a que este no esta protegido de ninguna manera y este esta siendo modificado, y leido desde diferentes hilos se puede ver especificamente desde la funciones usadas dentro de [Board.java](src/main/java/co/eci/snake/core/Board.java) como advance o head que son funciones de Snake o directamente desde funciones como snapshot el cual crea una copia de Body, por otra parte al usar una coleccion como lo es ArrayDeque que no es thread-safe lo que puede generar lecturas inconsistentes, excepciones en tiempo de ejecucion o valores intermedios
  - El movimiento de las Snakes que los usuarios pueden mover se pueden solapar con los movimientos que genera el programa aleatoreamente lo que genera una mala representacion del movimiento y tambien puede generar acciones indebidas como moverse en 180º
  - Que el boton que permite pausar geneera inconsistencia debido a que los [SnakeRunner.java](src/main/java/co/eci/snake/concurrency/SnakeRunner.java) de las Snakes no se detienen lo que al "reanudar" las Snakes aparecen en sus posiciones "futuras" pareciendo asi que se hubieran teletransportado
- Colecciones o estructuras no seguras en contexto concurrente:
  - el ArrayDeque no es Thread-Safe usado en el body de [Snake.java](src/main/java/co/eci/snake/core/Snake.java).
  - Teoricamente el HashSet o el HashMap los cuales son usados en [Board.java](src/main/java/co/eci/snake/core/Board.java) no Thread-Safe pero debido  al comportamiento actual funcionan de manera adecuada
  - Teoricamente ArrayList el cual es usado en [SnakeApp.java](src/main/java/co/eci/snake/ui/legacy/SnakeApp.java) no es Thread-Safe pero en el contexto del uso que se le da no es realmente peligroso amenos que se realicen mutaciones dentro de la lista dentro del codigo
- Ocurrencias de espera activa (busy-wait) o de sincronización innecesaria:
  - No existe por lo revisado y analizado esperas activas
  - una sincronizacion potencialmente innecesaria o costosa es los metodos obstacles(), mice(), teleports() y turbo() de [Board.java](src/main/java/co/eci/snake/core/Board.java) debido a que se realizan copias constantes para realizar el repaint del tablero el cual bloquea el hilo que esta haciendo step() y tambien genera objetos basura que se dejan de usar una vez se les da un unico uso


