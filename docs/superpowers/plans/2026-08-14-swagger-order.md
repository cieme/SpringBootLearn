# Swagger @Order Sorting Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Swagger UI display controller operations in ascending Spring `@Order` order.

**Architecture:** Add a focused Spring configuration that first copies each handler method's `@Order` value into the generated OpenAPI Operation as `x-order`, then rebuilds OpenAPI `Paths` in ascending order before serialization. Swagger UI keeps its default server-provided ordering, so no custom UI page or JavaScript is required.

**Tech Stack:** Java 21, Spring Boot 4.1.0, springdoc-openapi 2.6.0, Swagger OpenAPI models, JUnit 5

## Global Constraints

- Existing controller URLs, method implementations, and `@Order` annotations must remain unchanged.
- Smaller `@Order` values appear first; operations without `@Order` appear last.
- Equal order values use ascending path as a deterministic tie-breaker.
- For multiple HTTP methods on one path, the path uses the smallest operation order.
- Do not configure Swagger UI `operations-sorter`, because it would override the server-provided order.
- Do not add dependencies or replace the springdoc version as part of this change.

## File Structure

- Create `src/main/java/com/example/demo/SwaggerOrderConfig.java`: reads handler annotations and sorts generated OpenAPI paths.
- Create `src/test/java/com/example/demo/SwaggerOrderConfigTest.java`: verifies annotation extraction, missing annotation behavior, explicit order, unordered-last behavior, equal-order fallback, and multi-operation paths.

---

### Task 1: Apply `@Order` to generated OpenAPI paths

**Files:**
- Create: `src/main/java/com/example/demo/SwaggerOrderConfig.java`
- Test: `src/test/java/com/example/demo/SwaggerOrderConfigTest.java`

**Interfaces:**
- Consumes: Spring `HandlerMethod`, Spring `@Order`, springdoc `OperationCustomizer`, springdoc `OpenApiCustomizer`, Swagger `OpenAPI`, `Paths`, `PathItem`, and `Operation`.
- Produces: package-private bean methods `OperationCustomizer operationOrderCustomizer()` and `OpenApiCustomizer orderedPathsOpenApiCustomizer()`; generated Operations may contain numeric extension `x-order`.

- [x] **Step 1: Write the failing test**

Create `src/test/java/com/example/demo/SwaggerOrderConfigTest.java`:

```java
package com.example.demo;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SwaggerOrderConfigTest {

    private final SwaggerOrderConfig config = new SwaggerOrderConfig();

    @Test
    void copiesMethodOrderToOpenApiExtension() throws Exception {
        Operation operation = customize("ordered");

        assertEquals(2, operation.getExtensions().get("x-order"));
    }

    @Test
    void leavesOperationWithoutOrderUnchanged() throws Exception {
        Operation operation = customize("unordered");

        assertNull(operation.getExtensions());
    }

    @Test
    void sortsOrderedPathsFirstAndUsesPathAsTieBreaker() {
        OpenAPI openApi = new OpenAPI().paths(new Paths()
                .addPathItem("/unordered", getOperation(null))
                .addPathItem("/second", getOperation(2))
                .addPathItem("/first", getOperation(1))
                .addPathItem("/also-first", getOperation(1)));

        config.orderedPathsOpenApiCustomizer().customise(openApi);

        assertEquals(
                List.of("/also-first", "/first", "/second", "/unordered"),
                List.copyOf(openApi.getPaths().keySet())
        );
    }

    @Test
    void usesSmallestOrderWhenPathContainsMultipleOperations() {
        Operation postOperation = new Operation();
        postOperation.addExtension("x-order", 3);
        PathItem mixedPath = getOperation(8).post(postOperation);
        OpenAPI openApi = new OpenAPI().paths(new Paths()
                .addPathItem("/order-five", getOperation(5))
                .addPathItem("/mixed", mixedPath));

        config.orderedPathsOpenApiCustomizer().customise(openApi);

        assertEquals(List.of("/mixed", "/order-five"), List.copyOf(openApi.getPaths().keySet()));
    }

    private Operation customize(String methodName) throws Exception {
        Method method = OrderedController.class.getDeclaredMethod(methodName);
        HandlerMethod handlerMethod = new HandlerMethod(new OrderedController(), method);
        return config.operationOrderCustomizer().customize(new Operation(), handlerMethod);
    }

    private PathItem getOperation(Integer order) {
        Operation operation = new Operation();
        if (order != null) {
            operation.addExtension("x-order", order);
        }
        return new PathItem().get(operation);
    }

    private static class OrderedController {
        @Order(2)
        void ordered() {
        }

        void unordered() {
        }
    }
}
```

- [x] **Step 2: Run the focused test and confirm the expected failure**

Run:

```powershell
.\mvnw.cmd -Dtest=SwaggerOrderConfigTest test
```

Expected: compilation fails because `SwaggerOrderConfig` does not exist. If Java/JAVA_HOME is unavailable, record that environment blocker before continuing with source inspection.

- [x] **Step 3: Implement the minimal Swagger ordering configuration**

Create `src/main/java/com/example/demo/SwaggerOrderConfig.java`:

```java
package com.example.demo;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

@Configuration
public class SwaggerOrderConfig {

    private static final String ORDER_EXTENSION = "x-order";

    @Bean
    OperationCustomizer operationOrderCustomizer() {
        return (operation, handlerMethod) -> {
            Order order = handlerMethod.getMethodAnnotation(Order.class);
            if (order != null) {
                operation.addExtension(ORDER_EXTENSION, order.value());
            }
            return operation;
        };
    }

    @Bean
    OpenApiCustomizer orderedPathsOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null || openApi.getPaths().isEmpty()) {
                return;
            }

            Paths orderedPaths = new Paths();
            openApi.getPaths().entrySet().stream()
                    .sorted(Comparator
                            .comparingInt((Map.Entry<String, PathItem> entry) -> pathOrder(entry.getValue()))
                            .thenComparing(Map.Entry::getKey))
                    .forEach(entry -> orderedPaths.addPathItem(entry.getKey(), entry.getValue()));
            openApi.setPaths(orderedPaths);
        };
    }

    private static int pathOrder(PathItem pathItem) {
        return pathItem.readOperations().stream()
                .map(Operation::getExtensions)
                .filter(Objects::nonNull)
                .map(extensions -> extensions.get(ORDER_EXTENSION))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .mapToInt(Number::intValue)
                .min()
                .orElse(Ordered.LOWEST_PRECEDENCE);
    }
}
```

- [x] **Step 4: Run the focused test and confirm it passes**

Run:

```powershell
.\mvnw.cmd -Dtest=SwaggerOrderConfigTest test
```

Expected: all four tests in `SwaggerOrderConfigTest` pass.

- [x] **Step 5: Run regression checks**

Run:

```powershell
.\mvnw.cmd test
```

Expected: `SwaggerOrderConfigTest` and `DemoApplicationTests` pass with zero failures and zero errors.

- [x] **Step 6: Verify generated OpenAPI and Swagger UI when Java is available**

Start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

Then inspect `http://localhost:8080/v3/api-docs` and `http://localhost:8080/swagger-ui/index.html`. Expected: the `paths` keys and Swagger UI operations appear as `/Learn1/hello`, `/Learn1/hello2`, `/Learn1/hello3`, `/Learn1/hello4`, `/Learn1/hello5`, `/Learn1/hello6`, `/Learn1/hello7`, `/Learn1/hello8`.

- [x] **Step 7: Commit the tested implementation without including unrelated worktree changes**

```powershell
git add -- src/main/java/com/example/demo/SwaggerOrderConfig.java src/test/java/com/example/demo/SwaggerOrderConfigTest.java
git commit --only -m "fix: order Swagger operations by annotation" -- src/main/java/com/example/demo/SwaggerOrderConfig.java src/test/java/com/example/demo/SwaggerOrderConfigTest.java
```
