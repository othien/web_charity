package com.example.utecharity_project.Config;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor{
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Kiá»ƒm tra náº¿u ngÆ°á»i dÃ¹ng chÆ°a Ä‘Äƒng nháº­p (session khÃ´ng chá»©a "username")
        if (request.getSession().getAttribute("username") == null) {
            response.sendRedirect("/login-siteadmin");  // Chuyá»ƒn hÆ°á»›ng Ä‘áº¿n trang Ä‘Äƒng nháº­p náº¿u chÆ°a Ä‘Äƒng nháº­p
            return false; // Dá»«ng xá»­ lÃ½ request
        }
        return true; // Cho phÃ©p xá»­ lÃ½ request náº¿u Ä‘Ã£ Ä‘Äƒng nháº­p
    }
}

