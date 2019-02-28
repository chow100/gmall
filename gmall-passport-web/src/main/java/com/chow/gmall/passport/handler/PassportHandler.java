package com.chow.gmall.passport.handler;


import com.alibaba.dubbo.config.annotation.Reference;
import com.chow.gmall.bean.UserInfo;
import com.chow.gmall.service.CartService;
import com.chow.gmall.service.UserInfoService;
import com.chow.gmall.util.CookieUtil;
import com.chow.gmall.util.JwtUtil;
import com.chow.gmall.util.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportHandler {

    @Reference
    UserInfoService userInfoService;

    @Reference
    CartService cartService;

    @RequestMapping("index")
    public String toTrade(String returnUrl, ModelMap map){

        map.put("originUrl",returnUrl);

        return "index";
    }

    // 登录成功后验证中心颁发token
    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response){

        UserInfo userInfoFromDb = userInfoService.getUserInfo(userInfo);

        if(userInfoFromDb != null) {
            HashMap<String, String> userMap = new HashMap<>();
            userMap.put("userId", userInfoFromDb.getId());
            userMap.put("nickName", userInfoFromDb.getNickName());

            String nip = request.getHeader("request-forwared-for");// 从nginx中获取
            if(StringUtils.isBlank(nip)){
                nip = request.getRemoteAddr(); // 从servlet中获取
                if (StringUtils.isBlank(nip)){
                    nip = "127.0.0.1";
                }
            }

            String token = JwtUtil.encode("chowter", userMap, MD5Utils.md5(nip));
            // 将用户信息放入redis中，因为后面还会用到用户的其他信息，方便后面调用
            userInfoService.setUserInfoCache(userInfoFromDb);

            // 登录成功后，要将cookie缓存中的订单同步到db中
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            // List<CartInfo> cartInfoListFromCookie = JSON.parseArray(listCartCookie,CartInfo.class);
            if(StringUtils.isNotBlank(listCartCookie)){
                // 缓存中有数据就与db合并
                cartService.mergeCache(listCartCookie,userInfoFromDb.getId());
            }else {
                // 缓存中没有数据就db中的数据刷新到redis中
                cartService.flushCartCacheByUser(userInfoFromDb.getId());
            }

            CookieUtil.deleteCookie(request, response, "listCartCookie");

            return token;
        }
        return "err";
    }

    // 验证token
    @RequestMapping("verify")
    @ResponseBody
    public String verify(String nip,String token){

        Map chowter = JwtUtil.decode("chowter", token, MD5Utils.md5(nip));
        if(chowter!=null){
            // token验证成功
            return "success";
        }
        return "failue";
    }

}
