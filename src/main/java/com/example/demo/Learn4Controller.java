package com.example.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Learn4")
@Tag(name = "Learn4", description = "循环")
public class Learn4Controller {

    @GetMapping("/hello1")
    @Operation(summary = "字符串的一点方法")
    @Order(1)
    public String sayHello() {
        ClearConsole.clearScreen();
        // 前面好像不算\n,后面好像算\n
        String message = """
            茕茕孑兔,
            东奔西顾,
            衣不如新,
            人不如故
            """;

        int index = message.indexOf("兔"); //3
        System.out.println(index);
        int lastIndex = message.lastIndexOf("不");
        System.out.println(lastIndex);

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            System.out.println(c); // 茕 茕 孑 兔 , \n 东 奔 西 顾 , \n 衣 不 如 新 , \n 人 不 如 故 \n
        }

        boolean hasChar = message.contains("东奔西顾"); // true
        boolean startChar = message.startsWith("茕茕孑兔"); // true
        boolean endChar = message.endsWith("人不如故\n"); // true

        System.out.println(hasChar);
        System.out.println(startChar);
        System.out.println(endChar);

        String message2 = message.trim(); //strip也移除首尾空格但包含中文空格
        System.out.println(message2);

        String message3 = message.substring(2, 4); // 从哪开始,(可选)到哪结束
        System.out.println(message3); // 孑兔

        String message4 = message3.replace("兔", "鸭");
        System.out.println(message3); //孑兔
        System.out.println(message4); //孑鸭

        String message5 = message3.replaceAll("兔", "牛");
        System.out.println(message3); //孑兔
        System.out.println(message5); //孑牛
        return message;
    }
}
