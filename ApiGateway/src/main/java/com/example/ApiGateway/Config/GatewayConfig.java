package com.example.ApiGateway.Config;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // Auth Service
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri("lb://IDENTITYSERVICE"))

                // Menu + Table Service
                .route("table-menu-service", r -> r
                        .path("/api/v1/items/**",
                                "/api/v1/upload/**",
                                "/api/v1/categories/**",
                                "/api/v1/sessions/**",
                                "/api/v1/tables/**")
                        .uri("lb://MENUSERVICE"))

                // Order Service
                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .uri("lb://ORDERSERVICE"))

                // Kitchen Service
                .route("kitchen-service", r -> r
                        .path("/api/v1/kitchens/**")
                        .uri("lb://KITCHENSERVICE"))

                // WebSocket Kitchen
                .route("websocket-service", r -> r
                        .path("/ws/**")
                        .uri("lb:ws://KITCHENSERVICE"))

                // Calendar Service
                .route("calendar-service", r -> r
                        .path("/api/v1/calendars/**")
                        .uri("lb://CALENDARSERVICE"))

                .build();
    }
}