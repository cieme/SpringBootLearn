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
