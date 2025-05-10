package com.example.handartracker.models;

public class HandLandmark {
    private float x;
    private float y;
    private float z;
    private int index;

    public HandLandmark(float x, float y, float z, int index) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.index = index;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public int getIndex() {
        return index;
    }
}