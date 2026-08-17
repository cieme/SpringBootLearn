package com.example.demo;

public enum Sex {
    MALE(0), FEMALE(1);

    private final int value;

    Sex(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
