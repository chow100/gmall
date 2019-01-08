package com.chow.gmall.user.handler;

import com.chow.gmall.user.bean.UserInfo;
import com.chow.gmall.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserInfoHandler {

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("index")
    @ResponseBody
    public List<UserInfo> getUserInfo(){

        List<UserInfo> userInfoList = userInfoService.getUserInfoAll();

        return userInfoList;
    }


}
