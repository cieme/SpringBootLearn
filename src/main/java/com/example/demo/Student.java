package com.example.demo;

// 只要某个class没有final修饰符，那么任何类都可以从该class继承
public class Student extends Person {
    private int score;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
    }

    public String getName() {
        return super.getName();
    }
    // Override 是非必需的
    @Override
    public int getAge() {
        return 123;
    }
}
