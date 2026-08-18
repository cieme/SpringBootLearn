package com.example.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

@RestController
@RequestMapping("/Learn5")
@Tag(name = "Learn5", description = "StringBuilder")
public class Learn5Controller {

    @GetMapping("/hello1")
    @Operation(summary = "字符串的一点方法")
    @Order(1)
    public String sayHello() {
        ClearConsole.clearScreen();
        // 指定容量可以避免多次扩容
        StringBuilder sb = new StringBuilder(1024);
        sb.append("Mr ")
            .append("Bob")
            .append("!")
            .insert(0, "Hello, ");
        String s = sb.toString(); // Hello, Mr Bob!
        System.out.println(s);
        return s;
    }

    @GetMapping("/hello2")
    @Operation(summary = "buildInsertSql连接字符等功能")
    @Order(2)
    public String sayHello2() {
        ClearConsole.clearScreen();
        String[] fields = {"name", "position", "salary"};
        String table = "employee";
        String insert = Learn5Controller.buildInsertSql2(table, fields);
        System.out.println(insert);
        String s = "INSERT INTO employee (name, position, salary) VALUES (?, ?, ?)";
        System.out.println(s.equals(insert) ? "测试成功" : "测试失败");
        return "测试东西";
    }

    static @NotNull String buildInsertSql(String table, String[] fields) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("INSERT INTO ").append(table).append(" (");
        for (int i = 0; i < fields.length; i++) {
            sb.append(fields[i]);
            if (i < fields.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(") VALUES (");
        for (int i = 0; i < fields.length; i++) {
            sb.append("?");
            if (i < fields.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    static @NotNull String buildInsertSql2(String table, String[] fields) {

        String columns = String.join(", ", fields);

        List<String> list = Collections.nCopies(fields.length, "?");
        String placeholders = String.join(", ", list);

//        System.out.println(list);  // [?, ?, ?, ?, ?]

        return "INSERT INTO " + table +
            " (" +
            columns +
            ") VALUES (" +
            placeholders +
            ")";
//        return new StringBuilder(1024)
//            .append("INSERT INTO ").append(table)
//            .append(" (")
//            .append(columns)
//            .append(") VALUES (")
//            .append(placeholders)
//            .append(")")
//            .toString();
    }

    @GetMapping("/hello3")
    @Operation(summary = "buildInsertSql连接字符等功能")
    @Order(3)
    public String sayHello3() {
        ClearConsole.clearScreen();
        String[] names = {"Bob", "Alice", "Grace"};
        var sb = new StringBuilder();
        sb.append("Hello ");
        for (String name : names) {
            sb.append(name).append(", ");
        }

        // 注意去掉最后的", " 从后往前数2个,删到最后, 所以就是删除最后2个
        sb.delete(sb.length() - 2, sb.length());
        sb.append("!");
        System.out.println(sb.toString());
        return sb.toString();
    }

    @GetMapping("/hello4")
    @Operation(summary = "StringJoiner 和 String.join连接字符等功能")
    @Order(4)
    public String sayHello4() {
        ClearConsole.clearScreen();
        String[] names = {"Bob", "Alice", "Grace"};
        var sj = new StringJoiner(", ", "Hello ", "!");
        for (String name : names) {
            sj.add(name);
        }
        System.out.println(sj.toString());

        String[] names2 = {"Bob", "Alice", "Grace"};
        String s = "Hello " + String.join(", ", names2) + "!";

        System.out.println(s);
        System.out.println(s.equals(sj.toString()));
        return sj.toString();
    }

    @GetMapping("/hello5")
    @Operation(summary = "Integer")
    @Order(5)
    public Integer sayHello5() {
        ClearConsole.clearScreen();
        int i = 100;
        Integer num = Integer.valueOf(i);
        int num2 = (num).intValue();

        System.out.println(num);
        System.out.println(num2);

        BigDecimal price = new BigDecimal(num);
        BigInteger bigInteger = price.toBigInteger();

        return num;
    }
    @GetMapping("/hello6")
    @Operation(summary = "Introspector,要枚举一个JavaBean的所有属性")
    @Order(6)
    public void sayHello6() throws Exception {
        ClearConsole.clearScreen();
        BeanInfo info = Introspector.getBeanInfo(Person.class);
        PropertyDescriptor[] s = info.getPropertyDescriptors();
        for (PropertyDescriptor pd : s) {
            System.out.println(pd.getName());
            System.out.println("  " + pd.getReadMethod());
            System.out.println("  " + pd.getWriteMethod());
        }

        System.out.println(Weekday.FRI); // 星期五 因为重写了toString, 因为toString可重写so判断枚举常量的名字，要始终使用name()方法
        System.out.println(Weekday.FRI.dayValue); // 5
        System.out.println(Weekday.FRI.chinese); // 星期五
    }

    enum Weekday {
        MON(1, "星期一"), TUE(2, "星期二"), WED(3, "星期三"), THU(4, "星期四"), FRI(5, "星期五"), SAT(6, "星期六"), SUN(0, "星期日");

        public final int dayValue;
        private final String chinese;

        private Weekday(int dayValue, String chinese) {
            this.dayValue = dayValue;
            this.chinese = chinese;
        }

        @Override
        public String toString() {
            return this.chinese;
        }
    }
}
