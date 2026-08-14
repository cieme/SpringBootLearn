// 声明当前类所属的包。只有放在 com.example.demo 包下，Spring Boot 才能从启动类所在包自动扫描到它。
package com.example.demo;

// Operation 表示 OpenAPI 文档中的“一个接口操作”，例如 GET /Learn1/hello。
import io.swagger.v3.oas.models.Operation;
// PathItem 表示“一个 URL 路径”，里面可以同时包含 GET、POST、PUT 等多个 Operation。
import io.swagger.v3.oas.models.PathItem;
// Paths 是 OpenAPI 文档中保存所有 URL 路径的容器，它会保留路径被放进去时的先后顺序。
import io.swagger.v3.oas.models.Paths;
// OpenApiCustomizer 会在整份 OpenAPI 文档生成后执行，适合对所有路径做最终排序。
import org.springdoc.core.customizers.OpenApiCustomizer;
// OperationCustomizer 会在 springdoc 处理每个 Controller 方法时执行，能同时拿到 Operation 和对应的 Java 方法。
import org.springdoc.core.customizers.OperationCustomizer;
// @Bean 告诉 Spring：把下面方法返回的对象放进 Spring 容器，供 springdoc 自动使用。
import org.springframework.context.annotation.Bean;
// @Configuration 告诉 Spring：这个类是配置类，类中的 @Bean 方法需要被扫描和执行。
import org.springframework.context.annotation.Configuration;
// Ordered.LOWEST_PRECEDENCE 的值是 Integer.MAX_VALUE，用来表示“最低优先级，也就是排在最后”。
import org.springframework.core.Ordered;
// 读取 Controller 方法上现有的 @Order(1)、@Order(2) 等注解。
import org.springframework.core.annotation.Order;

// Comparator 用来定义两条路径之间应该怎样比较和排序。
import java.util.Comparator;
// Map.Entry 表示 Paths 中的一组“路径字符串 -> PathItem”，例如 "/Learn1/hello" -> 对应接口信息。
import java.util.Map;
// Objects::nonNull 用于过滤掉空值，避免后续读取扩展字段时出现空指针异常。
import java.util.Objects;

/**
 * 让 Swagger UI 按照 Controller 方法上的 {@link Order @Order} 数值显示接口。
 *
 * <p>整个过程分为两步：</p>
 * <ol>
 *     <li>生成每个接口时，把 Java 方法上的 {@code @Order} 数值保存到 OpenAPI 的 {@code x-order} 扩展字段。</li>
 *     <li>整份 OpenAPI 文档生成后，读取每个接口的 {@code x-order}，重新排列 Paths。</li>
 * </ol>
 *
 * <p>之所以分两步，是因为处理单个方法时知道它的 {@code @Order}，但还拿不到全部路径；
 * 拿到全部路径准备排序时，又没有对应的 Java 方法，所以需要用 {@code x-order} 在两步之间传递序号。</p>
 */
// 把当前类注册成 Spring 配置类。应用启动时，Spring 会自动发现它。
@Configuration
public class SwaggerOrderConfig {

    // OpenAPI 规定自定义扩展字段必须以 "x-" 开头；这里统一使用 "x-order" 保存排序数字。
    // 定义成常量可以避免在多个位置重复手写字符串时发生拼写不一致。
    private static final String ORDER_EXTENSION = "x-order";

    /**
     * 创建“单个接口处理器”，负责把 Java 方法上的 {@code @Order} 复制到 OpenAPI Operation。
     *
     * @return springdoc 在生成每个接口时都会调用的 OperationCustomizer
     */
    // 把返回的 OperationCustomizer 注册到 Spring 容器，springdoc 会自动找到并调用它。
    @Bean // @Bean 的意思是： Spring，请调用这个方法，并把返回的对象保存起来。
    OperationCustomizer operationOrderCustomizer() {
        // lambda 的 operation 是正在生成的 Swagger 接口；handlerMethod 是它对应的 Controller 方法。
        return (operation, handlerMethod) -> {
            // 从当前 Controller 方法读取 @Order；没有写注解时，getMethodAnnotation 会返回 null。
            Order order = handlerMethod.getMethodAnnotation(Order.class);

            // 只有确实写了 @Order 的接口才添加 x-order；没写的接口保持原样，后面统一排到最后。
            if (order != null) {
                // order.value() 取得 @Order(...) 括号里的整数，例如 @Order(2) 得到 2。
                // addExtension 会在生成的 OpenAPI JSON 中加入 "x-order": 2，供第二步排序使用。
                operation.addExtension(ORDER_EXTENSION, order.value());
            }

            // springdoc 要求返回处理后的 Operation；这里返回的仍是刚才那个对象，只是可能多了 x-order。
            return operation;
        };
    }

    /**
     * 创建“整份文档处理器”，在所有接口生成完成后按照 {@code x-order} 重排路径。
     *
     * @return springdoc 在 OpenAPI 文档生成末尾调用的 OpenApiCustomizer
     */
    // 把返回的 OpenApiCustomizer 注册到 Spring 容器，让 springdoc 在最终阶段调用它。
    @Bean
    OpenApiCustomizer orderedPathsOpenApiCustomizer() {
        // lambda 的 openApi 代表整份 OpenAPI 文档，其中 getPaths() 包含所有接口路径。
        return openApi -> {
            // 如果文档还没有 Paths，或者项目一个接口都没有，就不需要排序，直接结束当前 lambda。
            // 先判断 null 再调用 isEmpty()，可以避免 NullPointerException（空指针异常）。
            if (openApi.getPaths() == null || openApi.getPaths().isEmpty()) {
                return;
            }

            // 新建一个空的 Paths，稍后按正确顺序把旧路径逐个放进来。
            // 不能只对旧 Paths“原地排序”，因为它本质上是按插入顺序保存数据的 Map。
            Paths orderedPaths = new Paths();

            // entrySet() 把 Paths 转成一组“路径字符串 -> PathItem”，这样既能比较路径，也能保留接口详情。
            openApi.getPaths().entrySet().stream()
                    // sorted(...) 对所有路径进行排序，但不会修改原来的 Paths，而是产生一个有序数据流。
                    .sorted(Comparator
                            // 第一排序条件：调用 pathOrder(...) 得到路径的 @Order 数字，数字越小越靠前。
                            .comparingInt((Map.Entry<String, PathItem> entry) -> pathOrder(entry.getValue()))
                            // 第二排序条件：如果两个路径的 @Order 相同，就按 URL 字符串升序排列，保证结果稳定。
                            .thenComparing(Map.Entry::getKey))
                    // 按排好的顺序，把路径字符串和对应 PathItem 逐个放入新的 orderedPaths。
                    .forEach(entry -> orderedPaths.addPathItem(entry.getKey(), entry.getValue()));

            // 用排序后的 Paths 替换原 Paths；Swagger UI 接收到 JSON 后就会按照这个顺序展示接口。
            openApi.setPaths(orderedPaths);
        };
    }

    /**
     * 计算一个 URL 路径应该使用的排序数字。
     *
     * <p>通常一个 PathItem 只有一个 GET 接口；但同一路径也可能同时有 GET 和 POST。
     * 如果同一路径有多个接口，本方法取其中最小的 {@code x-order} 作为整条路径的位置。</p>
     *
     * @param pathItem 一个 URL 路径及其包含的 GET、POST 等接口
     * @return 该路径的排序数字；没有任何 {@code @Order} 时返回最低优先级，使它排在最后
     */
    private static int pathOrder(PathItem pathItem) {
        // readOperations() 取出这个路径下的所有接口操作，stream() 让我们可以逐步筛选和转换数据。
        return pathItem.readOperations().stream()
                // 把每个 Operation 转换成它的扩展字段 Map；有 @Order 时，其中会包含 x-order。
                .map(Operation::getExtensions)
                // 没有任何扩展字段时 getExtensions() 会返回 null；先过滤掉 null，避免下一步报错。
                .filter(Objects::nonNull)
                // 从扩展字段 Map 中只取出 x-order 对应的值；没有 x-order 时会得到 null。
                .map(extensions -> extensions.get(ORDER_EXTENSION))
                // 只保留数字类型。这样即使其他代码错误地写入了字符串，也不会导致强制转换异常。
                .filter(Number.class::isInstance)
                // 前一步已经确认值是 Number，这里把 Object 安全转换成 Number，才能读取整数值。
                .map(Number.class::cast)
                // 把 Number 转换成 int，例如 Integer(2) 最终变成基本类型 int 2。
                .mapToInt(Number::intValue)
                // 同一路径有多个 HTTP 方法时取最小序号，例如 GET=8、POST=3，则整条路径按 3 排序。
                .min()
                // min() 的结果可能为空；没有 @Order 时使用 Integer.MAX_VALUE，使该路径排在有序接口之后。
                .orElse(Ordered.LOWEST_PRECEDENCE);
    }
}

/*
 * ==================== 为什么这里不需要手动调用？ ====================
 *
 * 一、不是没有人调用，而是 Spring 和 springdoc 框架自动调用
 *
 * 1. 项目启动时，@SpringBootApplication 会扫描 com.example.demo 包。
 * 2. 扫描到本类的 @Configuration 后，Spring 知道这是一个配置类。
 * 3. Spring 会自动执行本类中标有 @Bean 的方法，并保存它们返回的对象：
 *
 *    operationOrderCustomizer()        -> 返回 OperationCustomizer
 *    orderedPathsOpenApiCustomizer()   -> 返回 OpenApiCustomizer
 *
 * 这些对象会被保存在 Spring 容器中，所以业务代码不需要自己 new，也不需要自己调用。
 *
 * 二、return 后面的 lambda 并不是立即执行
 *
 * operationOrderCustomizer() 中的：
 *
 *    return (operation, handlerMethod) -> { ... };
 *
 * 表示创建一个 OperationCustomizer 对象，并把“以后被调用时要执行的代码”交给它。
 * 它等价于创建一个实现 OperationCustomizer 接口的普通 Java 类，并重写 customize 方法。
 *
 * 三、什么时候真正执行？
 *
 * 浏览器打开 Swagger UI 后，会请求 /v3/api-docs。
 * springdoc 开始扫描所有 Controller 方法并生成 OpenAPI 文档，这时会：
 *
 * 1. 每生成一个接口，就自动调用 OperationCustomizer。
 * 2. 所有接口都生成完成后，再自动调用 OpenApiCustomizer。
 *
 * 可以把 springdoc 内部过程简单理解成下面的伪代码：
 *
 *    for (HandlerMethod controllerMethod : 所有Controller方法) {
 *        Operation operation = 根据Controller方法生成接口信息(controllerMethod);
 *
 *        for (OperationCustomizer customizer : Spring容器中的所有OperationCustomizer) {
 *            operation = customizer.customize(operation, controllerMethod);
 *        }
 *    }
 *
 *    OpenAPI openApi = 组合成完整的OpenAPI文档();
 *
 *    for (OpenApiCustomizer customizer : Spring容器中的所有OpenApiCustomizer) {
 *        customizer.customise(openApi);
 *    }
 *
 * 四、为什么必须分成两个 Customizer？
 *
 * OperationCustomizer 一次只处理一个 Controller 方法：
 *
 *    sayHello()  上有 @Order(1) -> 给对应 Operation 写入 x-order = 1
 *    sayHello2() 上有 @Order(2) -> 给对应 Operation 写入 x-order = 2
 *
 * 这个阶段知道当前 Java 方法的 @Order，但还看不到最终的全部接口，所以不能统一排序。
 *
 * OpenApiCustomizer 执行时已经能看到整份 OpenAPI 文档和全部 Paths，
 * 但这时没有方便的 Controller 方法信息。因此第一步先用 x-order 保存数字，
 * 第二步再读取 x-order 排序。x-order 就是两个阶段之间传递排序数字的“标签”。
 *
 * 五、最终是怎样排好顺序的？
 *
 * 假设 springdoc 原来生成的路径顺序是：
 *
 *    /Learn1/hello3 -> x-order = 3
 *    /Learn1/hello  -> x-order = 1
 *    /Learn1/hello2 -> x-order = 2
 *
 * orderedPathsOpenApiCustomizer() 会调用 pathOrder() 取得每条路径的数字，
 * 然后按数字从小到大排序，得到：
 *
 *    /Learn1/hello  -> 1
 *    /Learn1/hello2 -> 2
 *    /Learn1/hello3 -> 3
 *
 * 排好后，代码按这个顺序把路径逐个放入新的 orderedPaths，最后执行：
 *
 *    openApi.setPaths(orderedPaths);
 *
 * 因此 /v3/api-docs 返回的 JSON 中，paths 已经是排好顺序的。
 * Swagger UI 默认按照服务端返回的顺序展示，所以页面上的接口也就排好了。
 *
 * 六、完整调用链
 *
 * 打开 Swagger UI
 *      -> Swagger UI 请求 /v3/api-docs
 *      -> springdoc 扫描 Controller
 *      -> 自动调用 OperationCustomizer
 *      -> 读取 @Order 并写入 x-order
 *      -> springdoc 生成完整 OpenAPI
 *      -> 自动调用 OpenApiCustomizer
 *      -> 根据 x-order 重新排列 Paths
 *      -> 返回排好顺序的 OpenAPI JSON
 *      -> Swagger UI 按返回顺序显示接口
 *
 * 最需要记住的一句话：
 *
 * @Bean 负责把对象注册给框架；框架会在合适的时间自动调用这个对象的方法。
 */
