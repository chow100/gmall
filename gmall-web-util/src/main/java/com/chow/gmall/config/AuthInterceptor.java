package com.chow.gmall.config;

import com.chow.gmall.annotation.LoginRequire;
import com.chow.gmall.util.CookieUtil;
import com.chow.gmall.util.HttpClientUtil;
import com.chow.gmall.util.JwtUtil;
import com.chow.gmall.util.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = "";
        String newToken = request.getParameter("newToken");
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);

        if(StringUtils.isNotBlank(oldToken)){
            token = oldToken;
        }
        if(StringUtils.isNotBlank(newToken)){
            token = newToken;
        }

        HandlerMethod hm = (HandlerMethod) handler;

        // 判断是否有注解
        LoginRequire methodAnnotation = hm.getMethodAnnotation(LoginRequire.class);

        // 注解不为空
        if(methodAnnotation!=null){

            // 校验
            boolean loginCheck = false;
            String nip = "";
            // token不为空,对token经行校验
            if(StringUtils.isNotBlank(token)){
                nip = request.getHeader("request-forwared-for");
                if(StringUtils.isBlank(nip)){
                   nip = request.getRemoteAddr();
                   if(StringUtils.isBlank(nip)){
                       nip = "127.0.0.1";
                   }
                }
                String result = HttpClientUtil.doGet("http://passport.gmall.com:8085/verify?token="+token+"&nip="+nip);
                if(result!=null&&"success".equals(result)){
                    // 校验通过
                    loginCheck = true;
                    CookieUtil.setCookie(request, response,"oldToken",token,60*60*24,true);
                    Map userMap = JwtUtil.decode("chowter", token, MD5Utils.md5(nip));
                    String userId = (String) userMap.get("userId");
                    String nickName = (String) userMap.get("nickName");
                    request.setAttribute("userId",userId);
                    request.setAttribute("nickName",nickName);

                }


            }

            // 如果校验不通过，然而注解校验又必须为true时，携带一个returnUrl重定向到登录页面
            if(loginCheck == false && methodAnnotation.isNeedLogin() == true){
                response.sendRedirect("http://passport.gmall.com:8085/index?returnUrl="+request.getRequestURL());
                return false;
            }

        }

        return true;

    }
}
