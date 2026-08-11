package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller

public class PageController {
    @Value("${server.port}")  // 从配置文件读取端口
    private int port;

    @GetMapping("/")
    public String page(Model model) {
        model.addAttribute("name", "root");
        model.addAttribute("message", "src/main/resources/templates/root.html！");
        model.addAttribute("port", port );
        return "root";
    }
}
