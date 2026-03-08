/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.utils.preformance;

/**
 *
 * @author zipCoder933
 */
public class SimpleWaitLock {

    private boolean isLocked = false;

    public synchronized void lock() throws InterruptedException {
        isLocked = true;
        while (isLocked) {
            wait();
        }
    }

    public synchronized void unlock() {
        isLocked = false;
        notify();
    }
}
