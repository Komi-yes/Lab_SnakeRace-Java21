package co.eci.snake.concurrency;

import co.eci.snake.core.Board;
import co.eci.snake.core.Direction;
import co.eci.snake.core.GameState;
import co.eci.snake.core.Snake;
import co.eci.snake.core.engine.PauseController;

import java.util.concurrent.ThreadLocalRandom;

public final class SnakeRunner implements Runnable {
    private final Snake snake;
    private final Board board;
    private final PauseController pause;
    private int turboTicks = 0;

    public SnakeRunner(Snake snake, Board board, PauseController pause) {
        this.snake = snake;
        this.board = board;
        this.pause = pause;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                pause.awaitIfPaused();

                if (pause.get() == GameState.STOPPED)
                    break;

                maybeTurn();
                pause.awaitIfPaused();
                var res = board.step(snake);

                if (res == Board.MoveResult.HIT_OBSTACLE) {
                    randomTurn();
                } else if (res == Board.MoveResult.ATE_TURBO) {
                    turboTicks = 100;
                }

                int turboSleepMs = 40;
                int baseSleepMs = 80;
                int sleep = (turboTicks > 0) ? turboSleepMs : baseSleepMs;
                if (turboTicks > 0) turboTicks--;
                Thread.sleep(sleep);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

  private void maybeTurn() {
    double p = (turboTicks > 0) ? 0.05 : 0.10;
    if (ThreadLocalRandom.current().nextDouble() < p) randomTurn();
  }

  private void randomTurn() {
    var dirs = Direction.values();
    snake.turn(dirs[ThreadLocalRandom.current().nextInt(dirs.length)]);
  }
}
