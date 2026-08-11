package com.example.demo;

public class ClearConsole {
    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                // Windows: 命令和参数分开传，更安全
                pb = new ProcessBuilder("cmd", "/c", "cls");
            } else {
                // Linux / macOS
                pb = new ProcessBuilder("clear");
            }

            // 让子进程的输入输出与当前Java进程保持一致
            pb.inheritIO();
            // 启动进程并等待它执行完毕
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
