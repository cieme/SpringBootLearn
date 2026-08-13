package com.example.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

// 1. Spring 容器在启动完成后，会主动查找所有实现了 ApplicationRunner 接口的 Bean
// Bean 就是由 Spring 容器创建、管理和销毁的 Java 对象。 自己 new 出来的对象不叫 Bean，只有交给 Spring 管理的才叫 Bean
// 2. 找到后，自动调用它们的 run 方法
// 2.1 如果有多个 可以使用 @Order(1) @Order(2) 顺序执行
// 2.2 或者 用 @DependsOn 控制 Bean 的创建顺序
@Component
public class StartUpInfoPrinter implements ApplicationRunner {

    private final Environment environment;

    public StartUpInfoPrinter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String port = environment.getProperty("local.server.port");
        String contextPath = environment.getProperty("server.servlet.context-path", "");

        System.out.println("================================================");
        System.out.println("🚀 应用启动成功！访问地址：");

        // 固定标签宽度为14个字符，保证所有 http:// 对齐
        String localLabel = "[本地]";
        System.out.printf("  %-14s http://localhost:%s%s%n", localLabel, port, contextPath);

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String ip = addr.getHostAddress();
                    // 只要 IPv4
                    if (ip.indexOf(':') == -1) {
                        String type = getNetworkType(ni);
                        String label = "[" + type + "]";
                        // 使用 %-14s 保证标签固定宽度，实现 http:// 对齐
                        System.out.printf("  %-14s http://%s:%s%s%n", label, ip, port, contextPath);
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("  ⚠️ 无法获取 IP 地址");
        }

        System.out.println("================================================");
    }

    /**
     * 判断网卡类型（返回固定长度的简称）
     */
    private String getNetworkType(NetworkInterface ni) {
        String name = ni.getName().toLowerCase();
        String displayName = ni.getDisplayName().toLowerCase();

        // 虚拟网卡
        if (displayName.contains("vmware")) return "VMware";
        if (displayName.contains("virtualbox") || name.contains("vbox")) return "VBox";
        if (displayName.contains("vethernet") || displayName.contains("hyper-v")) return "Hyper-V";
        if (name.contains("docker") || name.startsWith("veth") || name.startsWith("br-")) return "Docker";
        if (name.contains("wsl")) return "WSL";

        // VPN
        if (name.contains("ppp") || name.contains("tun") || name.contains("tap") ||
            displayName.contains("vpn") || displayName.contains("wireguard") ||
            name.contains("wintun")) return "VPN";

        // 蓝牙
        if (displayName.contains("bluetooth") || displayName.contains("蓝牙")) return "蓝牙";

        // WiFi
        if (name.startsWith("wlan") || name.startsWith("wlp") ||
            displayName.contains("wi-fi") || displayName.contains("wifi") ||
            displayName.contains("wireless") || displayName.contains("无线")) return "WiFi";

        // 有线网卡
        if (name.startsWith("eth") || name.startsWith("enp") || name.startsWith("eno") ||
            displayName.contains("ethernet") || displayName.contains("有线") ||
            displayName.contains("gigabit") || displayName.contains("realtek")) return "有线";

        return "其他";
    }
}
