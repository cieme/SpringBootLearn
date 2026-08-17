package com.example.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Scanner;

@RestController
@RequestMapping("/Learn2")
@Tag(name = "Learn2", description = "数组等")
public class Learn2Controller {

    @GetMapping("/hello1")
    @Operation(summary = "固定长度数组,数组是引用类型")
    @Order(1)
    public void sayHello() {
        ClearConsole.clearScreen();
        // 5位同学的成绩:
        int[] ns = new int[5];
        ns[0] = 68;
        ns[1] = 79;
        ns[2] = 91;
        ns[3] = 85;
        ns[4] = 62;
        System.out.println(ns.length); // 5

//        System.out.println(ns[5]); // 索引n不能超出范围
    }

    @GetMapping("/hello2")
    @Operation(summary = "自动推测数组长度,数组长度是固定的,一旦创建就不能追加或删除元素。")
    @Order(2)
    public void sayHello2() {
        ClearConsole.clearScreen();
        int[] ns = new int[]{68, 79, 91, 85, 62};
        int[] ns2 = {68, 79, 91, 85, 62}; // 简写

        System.out.println(ns.length); //5
        System.out.println(ns2.length);

        // 5位同学的成绩:
        int[] ns3;
        ns3 = new int[]{68, 79, 91, 85, 62};
        System.out.println(ns.length); // 5
        ns3 = new int[]{1, 2, 3};
        System.out.println(ns3.length); // 3
    }

    @GetMapping("/hello3")
    @Operation(summary = "2.4.1输入")
    @Order(3)
    public void sayHello3() {
        ClearConsole.clearScreen();
        // System.out代表标准输出流，而System.in代表标准输入流。
        // 通过Scanner就可以简化后续的代码。
        Scanner scanner = new Scanner(System.in); // 创建Scanner对象
        System.out.print("Input your name: "); // 打印提示
        String name = scanner.nextLine(); // 读取一行输入并获取字符串
        System.out.print("Input your age: "); // 打印提示
        int age = scanner.nextInt(); // 读取一行输入并获取整数
        System.out.printf("Hi, %s, you are %d\n", name, age); // 格式化输出
    }

    @GetMapping("/hello4")
    @Operation(summary = "2.4.1输入")
    @Order(4)
    public void sayHello4() {
        ClearConsole.clearScreen();
        // System.out代表标准输出流，而System.in代表标准输入流。
        // 通过Scanner就可以简化后续的代码。
        Scanner scanner = new Scanner(System.in); // 创建Scanner对象
        System.out.print("输入上次成绩: ");
        int prev = scanner.nextInt(); //
        System.out.print("输入本次成绩: ");
        int current = scanner.nextInt();
        /*这个地方不应该使用 scanner.close
         * 本Scanner实例是在web控制器中,这是持续运行的服务,System.in是全局共享,如果关闭,第二次请求或者其他方法使用Scanner(System.in) 会直接报错
         * 推荐参考单例模式
         *  */
//        scanner.close();
        float x = (float) (current - prev) / prev * 100;
        System.out.printf("成绩提高了%.2f%%\n", x); // 格式化输出
    }

    @GetMapping("/hello5")
    @Operation(summary = "条件判断,")
    @Order(5)
    public void sayHello5() {
        ClearConsole.clearScreen();
        String str = "hello";
        String str2 = "hello"; // 会有字符串常量池机制,str和str2在堆内存中复用同一个对象
        String s2 = "HELLO".toLowerCase(); // 会创建新的对象
        String s3 = "HELLO".toLowerCase().intern();// 主动将字符串对象放入字符串常量池

        boolean isSameWithIntern = s3 == str2; // true
        System.out.printf("isSameWithIntern-%s\n", isSameWithIntern); // true

        // == 比较是否是同一个对象
        // equals比较字符串序列是否相同
        boolean isSame = str == str2; // true
        boolean isSame2 = str.equals(str2); // true

        boolean isSame3 = str2 == s2; // false
        boolean isSame4 = str2.equals(s2); // true

        System.out.printf("isSame-%s\n", isSame); // true  字符串常量池机制
        System.out.printf("isSame2-%s\n", isSame2);// true
        System.out.printf("str2 == s2-%s\n", isSame3);// false
        System.out.printf("isSame4-%s\n", isSame4);// true



        String s4 = null;
        // NullPointerException
//        s4.equals(str);
        /*需要判断是否为null*/
        if (s4 != null && s4.equals("hello")) {
            System.out.println("hello");
        }

    }
}
