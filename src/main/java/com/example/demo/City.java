package com.example.demo;


public class City {
    public String name;
    public double latitude;
    public double longitude;
    public int age;


    /*函数重载*/
    public City(){
    }
    public City(Integer age){
        this.age = age;
    }

    /**
     * 性别 0 1
     */
    private Sex sex;

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        // 方法内部，可以使用一个隐含的变量this，它始终指向当前实例
        // 如果没有命名冲突，可以省略this
        this.sex = sex;
    }

    public void setNameAndAge(String name, Sex sex) {
        this.name = name;
        this.sex = sex;
    }

    public String[] names;

    /**
     * 可变参数 由类型...定义
     * @param names
     */
    public void setNames(String... names) {
        this.names = names;
    }
}
