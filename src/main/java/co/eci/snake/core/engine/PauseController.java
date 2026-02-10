
package co.eci.snake.core.engine;

import co.eci.snake.core.GameState;
import java.util.concurrent.atomic.AtomicReference;

public final class PauseController {
    private final AtomicReference<GameState> state = new AtomicReference<>(GameState.RUNNING);
    private final Object monitor = new Object();

    public GameState get() { return state.get(); }

    public void pause() {
        state.set(GameState.PAUSED);
    }

    public void resume() {
        state.set(GameState.RUNNING);
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    public void stop() {
        state.set(GameState.STOPPED);
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    public void awaitIfPaused() throws InterruptedException {
        synchronized (monitor) {
            while (state.get() == GameState.PAUSED) {
                monitor.wait();
            }
        }
    }
}
