package com.example.utecharity_project.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Ãp dá»¥ng Interceptor cho cÃ¡c route cáº§n kiá»ƒm tra Ä‘Äƒng nháº­p
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/manager/**", "/dashboard_programmanagement", "/dashboard_campaignmanagement",
                        "/dashboard_newsmanagement", "/dashboard_servicemanagement", "/sendmessage_dashboard",
                        "/dashboard_revenuemanagement", "/dashboard_note", "/dashboard_contact",
                        "/dashboard_statistical")
                .excludePathPatterns("/login-siteadmin");
    }

    @Override
    public void addResourceHandlers(
            org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        // Serve uploaded files from /uploads/ URL
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
