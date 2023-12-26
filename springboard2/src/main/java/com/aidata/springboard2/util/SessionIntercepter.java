package com.aidata.springboard2.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@Slf4j
public class SessionIntercepter
        implements AsyncHandlerInterceptor {
    @Override //로그인 시점의 처리할 내용을 작성.
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        log.info("preHandle()");

        //세션에 로그인 정보가 있는지 확인
        HttpSession session = request.getSession();

        if(session.getAttribute("member") == null){
            //로그인을 안한 상태
            log.info("인터셉트! - 로그인 안함");
            //로그인 정보가 있으면 요청 페이지로, 없으면 첫페이지로 이동.
            response.sendRedirect("/");
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView)
            throws Exception {
        log.info("postHandle()");
        //로그아웃 후 브라우저의 back 버튼으로
        //인터셉터 대상 페이지로 들어가는 것을 막기.
        //막는 방식은 사용자 컴퓨터의 캐시를 제거.
        //현재 사용하는 웹 프로토콜(http) 버전은 1.1과 1.0 혼용
        if(request.getProtocol().equals("HTTP/1.1")){
            //1.1버전 캐시 제거
            response.setHeader("Cache-Control",
                    "no-cache, no-store, must-revalidate");
        } else {//1.0버전 캐시 제거
            response.setHeader("Pragma", "no-cache");
        }
    }
}
