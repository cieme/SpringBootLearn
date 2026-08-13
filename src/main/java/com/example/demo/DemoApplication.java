package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 而 @SpringBootApplication 内部包含了 @ComponentScan，它的默认行为是：
// 扫描主启动类所在包及其所有子包，把带有 @Component 及其衍生注解的类都注册成 Bean
// 如果包名不同可以手动指定
// @SpringBootApplication(scanBasePackages = {"com.example.demo", "com.other.package"})

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
