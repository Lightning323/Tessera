/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.progress;

import com.tessera.engine.client.ClientWindow;

/**
 *
 * @author zipCoder933
 */
public class ProgressData {

    public final String title;
    private String task = "";
    public final ProgressBar bar;
    public int stage;

    private boolean done = false;

    public ProgressData(String title) {
        this.title = title;
        done = false;
        taskAborted = false;
        bar = new ProgressBar();
        stage = 0;
    }

    /**
     * @return the done
     */
    public boolean isFinished() {
        return done;
    }

    /**
     * Sets the progressBar to finished
     */
    public void finish() {
        this.done = true;
    }

    /**
     * @return the progressDesc
     */
    public String getTask() {
        return task + " (" + bar.getIncrements() + "/" + bar.getMax() + ")";
    }

    /**
     * @param progressDesc the progressDesc to set
     */
    public void setTask(String progressDesc) {
        this.task = progressDesc;
    }

    boolean taskAborted = false;

    public void abort() {
        taskAborted = true;
    }

    public void abort(String message) {
        ClientWindow.popupMessage.message(title + " Failed", message, () -> taskAborted = true);
    }


    public boolean isAborted() {
        return taskAborted;
    }

    @Override
    public String toString() {
        return "ProgressData{" + "title=" + title + ", progressDesc=" + task + ", bar=" + bar + ", done=" + done + ", Aborted=" + taskAborted + '}';
    }
}
