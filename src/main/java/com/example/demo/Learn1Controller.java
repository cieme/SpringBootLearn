package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.annotation.Order;
import io.swagger.v3.oas.annotations.Operation;

import java.math.BigDecimal;

@RestController
@RequestMapping("/Learn1")
@Tag(name = "Learn1", description = "基础相关的API")
public class Learn1Controller {
    public static void main(String[] args) {
        System.out.println("main方法");
    }

    private static final double PI = 3.14;

    @GetMapping("/hello")
    @Operation(summary = "多行字符串和打印")
    @Order(1)  // 数字越小越靠前
    public String sayHello() {
        String hello = "Hello World1";
        System.out.println(hello);
        hello += '2';
        System.out.println(hello);
        hello = hello.concat("3");
        System.out.println(hello);
        return """
            <code style='white-space: pre;'>%s
            Java 15+代码块写的
            </code>
            """.formatted(hello);
    }

    @GetMapping("/hello2")
    @Operation(summary = "final 常量和int double ")
    @Order(2)  // 数字越小越靠前
    public void sayHello2() {
        ClearConsole.clearScreen();

        int x = 100;
        System.out.println(x);
        x = 200;
        System.out.println(x);

        int y = x + 100;
        y += 100;
        System.out.println(y);
        System.out.println(x);
        // 由于增加了final PI是一个常量

        double r = 5.0;
        double area = Learn1Controller.PI * r * r;
        System.out.println(area);
    }

    @GetMapping("/hello3")
    @Order(3)
    public String sayHello3() {
        ClearConsole.clearScreen();
        /*var 就不用指定类型了 */
        var sb = new StringBuilder();
        sb.append("GetMapping Hello World");
        StringBuilder append = sb.append('3');

        var str = sb.toString();
        System.out.println(sb);
        return str;
    }

    @GetMapping("/hello4")
    @Order(4)
    public Integer sayHello4() {
        ClearConsole.clearScreen();
        short s = 1234;
        int i = 123456;
        // 如果参与运算的两个数类型不一致，那么计算结果为较大类型的整型
        var x = s + i; // s自动转型为int 等于 int x = s + i;
        System.out.println(x);
        return x;
    }

    @GetMapping("/hello5")
    @Order(5)
    public String sayHello5() {
        ClearConsole.clearScreen();
        /*数据类型转型*/
        int i = 12345;
        short s = (short) i; // 12345
        System.out.println(s);

        float a = 31.14F;
        int b = (int) a; // 31
        System.out.println(b);

        char c = (char) a;
        System.out.println(c); //看起来没输出  Unicode 编码 31 对应的是一个不可打印的控制字符（Unit Separator，单位分隔符）

        char d = (char) 65.63F;
        System.out.println(d); //A

        return "数据类型转型";
    }

    @GetMapping("/hello6")
    @Order(6)
    public Integer sayHello6() {
        ClearConsole.clearScreen();
        int start = 1;
        int end = 100;
        int sum = 0;
        for (int i = start; i <= end; i++) {
            sum += i;
        }
        System.out.println(sum);
        return sum;
    }

    @GetMapping("/hello7")
    @Order(7)
    public Double sayHello7() {
        ClearConsole.clearScreen();
        double a = 1.0 / 10;
        double b = 9.0 / 10;  //
        double c = 1 - b;
        double d = 1 - 9.0 / 10;
        // 观察x和y是否相等:
        System.out.println(a); // 0.1
        System.out.println(b); // 0.9
        System.out.println(c); // 0.09999999999999998
        System.out.println(d); // 0.09999999999999998
        System.out.println(new BigDecimal(a)); // 0.1000000000000000055511151231257827021181583404541015625
        System.out.println(new BigDecimal(b)); // 0.90000000000000002220446049250313080847263336181640625

        // println 帮你进行了最短十进制显示。?
        return d;
    }

    @GetMapping("/hello8")
    @Order(8)
    public void sayHello8() {
        ClearConsole.clearScreen();

        String s = "hello";
        String t = s;
        s = "world";
        System.out.println(t); // hello
        System.out.println(s); // world

        // 引用类型的变量可以指向一个空值null
        String s1 = null; // s1是null
        System.out.println(s1); // null


        // 请将下面一组int值视为字符的Unicode码，把它们拼成一个字符串：
        int a = 72;
        int b = 105;
        int c = 65281;
        // FIXME:
        String str = "" + (char) a + (char) b + (char) c; //Hi！
        System.out.println(str);
    }
}
