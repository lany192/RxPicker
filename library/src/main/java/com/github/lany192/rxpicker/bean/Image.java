package com.github.lany192.rxpicker.bean;

import java.io.Serializable;


public class Image implements Serializable, Comparable<Image> {
    private int id;
    private String path;
    private String name;
    private long addTime;

    public Image(int id, String path, String name, long addTime) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.addTime = addTime;
    }

    public Image() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int compareTo(Image o) {
        long time = o.getAddTime() - getAddTime();
        if (time > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if (time < -Integer.MAX_VALUE) return -Integer.MAX_VALUE;
        return (int) time;
    }
}
