package primefinder;

public class Timer extends Thread {
    private long msToWait;

    public Timer(long msToWait){
        this.msToWait = msToWait;
    }


    public void run(){
        try {
            this.sleep(this.msToWait);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            Control.pause();
        }
    }
}
