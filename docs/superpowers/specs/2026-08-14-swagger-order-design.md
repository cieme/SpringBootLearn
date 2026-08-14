# Swagger 接口顺序设计

## 目标

`Learn1Controller` 中的接口在 Swagger UI 内按照方法上的 Spring `@Order` 数值升序展示。现有 `@Order(1)` 至 `@Order(8)` 应依次对应 `/hello` 至 `/hello8`；以后调整注解数值即可调整展示顺序。

## 根因

项目使用 springdoc-openapi 2.6.0。springdoc 在生成 OpenAPI 文档时不会把方法上的 Spring `@Order` 当作接口展示顺序，Swagger UI 也只会使用 OpenAPI 文档中的顺序或其自身的排序器。因此，当前 `@Order` 注解没有影响 Swagger UI。

## 设计

新增一个独立的 Swagger 排序配置类，分两个阶段处理：

1. 使用 `OperationCustomizer` 读取每个 Controller 方法的 `@Order`，并将数值写入对应 OpenAPI Operation 的 `x-order` 扩展字段。
2. 使用 `OpenApiCustomizer` 读取 `x-order`，重新构造 OpenAPI `Paths` 的插入顺序，使 Swagger UI 按该顺序接收接口。

排序规则如下：

- `@Order` 数值较小的接口排在前面。
- 没有 `@Order` 的接口排在所有显式排序接口之后。
- `@Order` 数值相同时按接口路径升序排列，保证输出稳定。
- 当前控制器中的接口均为不同路径；如果同一路径包含多个 HTTP 方法，则该路径使用其中最小的 `@Order` 值确定位置。

Swagger UI 保持默认的服务端顺序，不配置 `operations-sorter: alpha` 或 `operations-sorter: method`，避免覆盖服务端排好的顺序。

## 影响范围

- 保留 `Learn1Controller` 现有 URL、方法实现及 `@Order` 注解。
- 排序能力对项目中的所有 Controller 生效，但只有写了 `@Order` 的接口会获得显式优先级。
- OpenAPI JSON 中会出现内部使用的 `x-order` 扩展字段，不改变接口调用行为。

## 验证

1. 通过单元测试构造带有不同 `x-order` 的 OpenAPI Paths，验证显式顺序、无注解后置及同序号路径兜底排序。
2. 运行 Maven 测试或至少执行编译检查。
3. 启动应用并查看 `/v3/api-docs` 与 Swagger UI，确认 `/hello` 至 `/hello8` 按 `@Order(1)` 至 `@Order(8)` 展示。

当前环境未配置 Java/JAVA_HOME，若实施时环境仍然如此，将明确记录无法执行的编译和运行时验证，不把静态检查表述为完整验证。
