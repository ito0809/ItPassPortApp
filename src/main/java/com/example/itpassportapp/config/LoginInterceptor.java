package com.example.itpassportapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                                throws  Exception{

        // /loginなどの識別子を取得
        String path= request.getRequestURI();

        //ログイン不要画面は表示
        if(path.equals("/") || path.equals("/login") || path.equals("/signUp") || path.equals("/index") || path.startsWith("/css") || path.startsWith("/images")){
            return true;
        }

        //セッションの確認　request.getSession有はsessionに代入、ないときは作らないだからnullを代入
        HttpSession session = request.getSession(false);
        if(session != null && session.getAttribute("loginUser") != null){
            return true; //ログイン済み→通す
        }

        //未ログイン
        response.sendRedirect("/index?openLoginModal=1&needLogin=1");
        return false;//stop 先に進ませない
    }
}
