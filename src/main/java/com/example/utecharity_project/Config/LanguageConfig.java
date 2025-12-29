package com.example.utecharity_project.Config;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class LanguageConfig implements WebMvcConfigurer {
    @Bean
    public SessionLocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(new Locale("vi", "VN"));  // Máº·c Ä‘á»‹nh lÃ  tiáº¿ng Viá»‡t
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");  // Tham sá»‘ dÃ¹ng Ä‘á»ƒ chuyá»ƒn ngÃ´n ngá»¯ trong URL
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("home");  // TÃªn tá»‡p .properties
        source.setDefaultEncoding("UTF-8");  // Äáº£m báº£o sá»­ dá»¥ng mÃ£ hÃ³a UTF-8
        return source;
    }
}

