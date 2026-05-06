package com.example.api_gateway.filter;

import com.example.api_gateway.util.JwtUtil;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouteValidator validator, JwtUtil jwtUtil) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(AuthenticationFilter.Config config) {
     return(((exchange, chain) -> {
         ServerHttpRequest request = exchange.getRequest();
         //1. Is this a protected route?
         if(validator.isSecured.test(request)){

             // 2. Does the authorization header exist?
             if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                 exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                 return exchange.getResponse().setComplete();
             }

             // 3. Extract the token
             String authHeader = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
             if(authHeader!=null && authHeader.startsWith("Bearer ")){
                 authHeader=authHeader.substring(7);
             } else{
                 exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                 return exchange.getResponse().setComplete();
             }

             try{
                 // 4. validate the token
                 jwtUtil.validateToken(authHeader);

                 // 5. extract userId
                 String userId = jwtUtil.extractUserId(authHeader);

                 // 6. mutate the request
                 request=exchange.getRequest().mutate().header("X-User-Id",userId)
                         .build();

             } catch (Exception e) {
                 System.out.println("Invalid access "+e.getMessage());
                 exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                 return exchange.getResponse().setComplete();
             }



         }
         // 7. Forward the modified request to the destination microservice
         return chain.filter(exchange.mutate().request(request).build());
     }));


    }

    public static class Config {
        // Empty class required by AbstractGatewayFilterFactory
    }
}
