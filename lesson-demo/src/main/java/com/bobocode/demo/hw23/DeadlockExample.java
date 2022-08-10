package com.bobocode.demo.hw23;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

public class DeadlockExample {
    public static void main(String[] args) {
        Synchronized1 synchronized1 = new Synchronized1();
        Synchronized2 synchronized2 = new Synchronized2();

        Thread thread1 = new Thread(() -> synchronized1.synchronizedMethod1(synchronized2));
        Thread thread2 = new Thread(() -> synchronized2.synchronizedMethod1(synchronized1));

        thread1.start();
        thread2.start();
    }
}

class Synchronized1 {
    @SneakyThrows
    public synchronized void synchronizedMethod1(Synchronized2 synchronized2) {
        System.out.println("Synchronized1#synchronizedMethod1() start");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Synchronized1#synchronizedMethod1() try to call Synchronized2#synchronizedMethod2()");
        synchronized2.synchronizedMethod2();
        System.out.println("Synchronized1#synchronizedMethod() end");
    }

    @SneakyThrows
    public synchronized void synchronizedMethod2() {
        System.out.println("Synchronized1#synchronizedMethod2() start");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Synchronized1#synchronizedMethod2() end");
    }
}

class Synchronized2 {
    @SneakyThrows
    public synchronized void synchronizedMethod1(Synchronized1 synchronized1) {
        System.out.println("Synchronized2#synchronizedMethod1() start");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Synchronized2#synchronizedMethod1() try to call Synchronized1#synchronizedMethod2()");
        synchronized1.synchronizedMethod2();
        System.out.println("Synchronized2#synchronizedMethod1() end");
    }

    @SneakyThrows
    public synchronized void synchronizedMethod2() {
        System.out.println("Synchronized2#synchronizedMethod2() start");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Synchronized2#synchronizedMethod2() end");
    }
}
