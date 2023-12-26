package com.aidata.springboard2.config;

import com.aidata.springboard2.util.SessionIntercepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    //세션 인터셉터 클래스 자동 등록
    @Autowired
    private SessionIntercepter intercepter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(intercepter) //인터셉터 등록
                .addPathPatterns("/**") //인터셉터 범위 지정
                .excludePathPatterns("/","/css/**","/js/**","/images/**")
                .excludePathPatterns("/joinForm","/loginForm","/idCheck")
                .excludePathPatterns("/loginProc","/joinProc","/error/**");
    }           //excludePathPatterns : 인터셉터 제외 url 지정.
}
