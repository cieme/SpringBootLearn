package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hello")
@Tag(name = "Hello", description = "Hello相关的API")
public class HelloController {
    public static void main(String[] args) {
        System.out.println("main方法");
    }

    private static final double PI = 3.14;

    @GetMapping("/hello")
    /*
     * String 返回string 的意思
     * */
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
    /*
     * void 无返回的意思
     * */
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
        double area = HelloController.PI * r * r;
        System.out.println(area);
    }

    @GetMapping("/hello3")
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
    public Integer sayHello4() {
        ClearConsole.clearScreen();
        short s = 1234;
        int i = 123456;
        // 如果参与运算的两个数类型不一致，那么计算结果为较大类型的整型
        var x = s + i; // s自动转型为int
        System.out.println(x);
        return x;
    }
}
