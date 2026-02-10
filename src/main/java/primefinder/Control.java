/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package primefinder;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class Control extends Thread {
    private final static int NTHREADS = 3;
    private final static int MAXVALUE = 30000000;
    private final static int TMILISECONDS = 1000;
    private static AtomicBoolean isPaused = new AtomicBoolean(true);
    private static AtomicInteger amountPrimes = new AtomicInteger(0);
    private static AtomicInteger verifiedNumbers = new AtomicInteger(0);
    private static Timer timer = new Timer(TMILISECONDS);

    private final int NDATA = MAXVALUE / NTHREADS;

    private static PrimeFinderThread pft[];
    
    private Control() {
        super();
        this.pft = new  PrimeFinderThread[NTHREADS];

        int i;
        for(i = 0;i < NTHREADS - 1; i++) {
            PrimeFinderThread elem = new PrimeFinderThread(i*NDATA, (i+1)*NDATA);
            pft[i] = elem;
        }
        pft[i] = new PrimeFinderThread(i*NDATA, MAXVALUE + 1);
    }
    
    public static Control newControl() {
        return new Control();
    }

    @Override
    public void run() {
        pause();
        for (int i = 0; i < NTHREADS; i++) {
            pft[i].start();
        }
        Scanner scanner = new Scanner(System.in);
        while(verifiedNumbers.get() < MAXVALUE) {
            if(verifiedNumbers.get() ==0){
                System.out.println("Presiona Enter para comenzar");
            }else {
                System.out.println("Presiona Enter para seguir");
            }
            scanner.nextLine();
            unpause();
            try {
                sleep(TMILISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Se han encontrado "+ amountPrimes.get() + " primes from " + verifiedNumbers.get() + " verified");
        }
        System.out.println("Se verificaron los "+MAXVALUE+ " numeros");

    }

    public static void pause(){
        isPaused.set(true);
    }

    public static void unpause(){
        isPaused.set(false);
        for(PrimeFinderThread thread: pft){
            synchronized (thread){
                thread.notify();
            }
        }
        timer = new Timer(TMILISECONDS);
        timer.start();
    }

    public static void addCountedNumber(){
        verifiedNumbers.incrementAndGet();
    }

    public static void addAmountPrimes(){
        amountPrimes.incrementAndGet();
    }

    public static AtomicBoolean getIsPaused() {
        return isPaused;
    }
}
