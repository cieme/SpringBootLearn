package com.example.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/Learn3")
@Tag(name = "Learn3", description = "循环")
public class Learn3Controller {

    @GetMapping("/hello1")
    @Operation(summary = "while")
    @Order(1)
    public void sayHello() {
        ClearConsole.clearScreen();
        int start = 1;
        int end = 100;
        int sum = 0;
        while (start <= end) {
            sum += start;
            start++;
        }
        System.out.println(sum); // 5050
    }

    @GetMapping("/hello2")
    @Operation(summary = "do while")
    @Order(2)
    public void sayHello2() {
        ClearConsole.clearScreen();
        int sum = 0;
        int n = 1;
        int end = 100;
        int end2 = 0;
        do {
            sum = sum + n;
            n++;
//        } while (n <= end); // 5050
        } while (n <= end2); // 1
        System.out.println(sum); // 5050
    }

    @GetMapping("/hello3")
    @Operation(summary = "排序")
    @Order(3)
    public void sayHello3() {
        ClearConsole.clearScreen();
        int[] arr = {1, 5, 3, 7, 9};
        for (int item : arr) {
            System.out.println(item);
        }
        /*打印数组 会输出在jvm中的引用地址*/
        System.out.println(arr); // [I@4f5d1c39
        System.out.println(Arrays.toString(arr));  // [1, 5, 3, 7, 9]

        /*正序,只能升序*/
        Arrays.sort(arr); // 会改数组
        System.out.println(Arrays.toString(arr));  // [1, 3, 5, 7, 9]


        Integer[] arr2 = {1, 5, 3, 7, 9};
        //引用类型才能用第二个参数,int[] 就不可以用
        Arrays.sort(arr2, (a, b) -> b - a);
        System.out.println(Arrays.toString(arr2));  // [[9, 7, 5, 3, 1]

        /*倒序*/
        int[] reversedArr = IntStream.range(0, arr.length)
            .map(i -> arr[arr.length - 1 - i])  // 从最后一个元素开始映射
            .toArray();

    }

    @GetMapping("/hello4")
    @Operation(summary = "多维数组")
    @Order(4)
    public void sayHello4() {
        ClearConsole.clearScreen();
        int[][] arr = {
            {1, 5, 3, 7, 9},
            {2, 4, 8, 6, 10}
        };

    }

    @GetMapping("/hello5")
    @Operation(summary = "多维数组")
    @Order(5)
    public void sayHello5() {
        ClearConsole.clearScreen();
        /*因为有函数重载,所以可以选择要不要传递参数*/
        City city = new City();
        City city2 = new City(1);

        city.name = "北京";
        city.latitude = 1.0;
        city.longitude = 2.0;
        city.setSex(Sex.MALE);
        city.setNameAndAge("上海",Sex.MALE);
        city.setNames("北京","上海");

        System.out.println(city); // com.example.demo.City@14283c85
        System.out.println(city.name); // 上海
        System.out.println(city.latitude);// 1.0
        System.out.println(city.longitude);// 2.0
        System.out.println(city.getSex()); // MALE
        System.out.println(Arrays.toString(city.names)); // [北京, 上海]


    }
}
