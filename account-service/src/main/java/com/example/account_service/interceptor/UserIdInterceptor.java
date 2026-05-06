package com.example.account_service.interceptor;

import com.example.account_service.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserIdInterceptor implements HandlerInterceptor {



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("--- INCOMING REQUEST TO ACCOUNT SERVICE: {} ---", request.getRequestURI());

        // PRINT EVERY HEADER TO THE CONSOLE
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.info("Header: '{}' = '{}'", headerName, request.getHeader(headerName));
        }
        String userId = request.getHeader("X-user-Id");
        if(userId!=null && !userId.isEmpty()){
            UserContext.setUserId(userId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserContext.clear();
    }


}
