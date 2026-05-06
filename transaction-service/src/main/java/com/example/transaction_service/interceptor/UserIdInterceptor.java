package com.example.transaction_service.interceptor;

import com.example.transaction_service.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserIdInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler) throws Exception{
        // 1. get the userid from the header passed by the api gateway
        String userId = request.getHeader("X-User-Id");

        // 2. If it exists, save it to thread local memory
        if(userId!=null && !userId.isEmpty()){
            UserContext.setUserId(userId);

        }

        // 3. Let the request continue to the controller
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request,HttpServletResponse response,Object handler,Exception exception){
        // CRITICAL: Always clear the ThreadLocal after the request is finished!
        // Tomcat reuses threads (Thread Pooling). If you don't clear this,
        // the next user who gets this thread might accidentally use the old user's ID!
        UserContext.clear();
    }
}
