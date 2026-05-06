package com.example.api_gateway.filter;


import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

// THis class contain all the endpoint that doesn't need any token validation. These requests will
// completely bypass the api gateway.

@Component
public class RouteValidator {

    public static final List<String> openApiEndPoints=List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/eureka"
    );
    // This predicate checks if the incoming request path is NOT in the open endpoints list
    public Predicate<ServerHttpRequest> isSecured=
            request-> openApiEndPoints.stream()
                    .noneMatch(uri-> request.getURI().getPath().contains(uri));
}
