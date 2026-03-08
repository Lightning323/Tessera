/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.progress;

/**
 *
 * @author zipCoder933
 */
public class Bulletin {

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }
    private String title;
    private String body;

    public Bulletin(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public Bulletin(Throwable t) {
        this.title = "Error";
        String message = t.getMessage();
        if (message == null) {
            this.body = "Unknown error";
        } else {
            this.body = message;
        }
    }
}
