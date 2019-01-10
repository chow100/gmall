package com.chow.gmall.user.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chow.gmall.bean.UserInfo;
import com.chow.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserInfoHandler {

    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("userList")
    @ResponseBody
    public List<UserInfo> getUserInfo(){

        List<UserInfo> userInfoList = userInfoService.getUserInfoAll();

        return userInfoList;
    }


}
