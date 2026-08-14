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
