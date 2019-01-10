package com.chow.gmall.user.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chow.gmall.bean.UserAddress;
import com.chow.gmall.service.UserAddressService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserAddressHandler {

    @Reference
    private UserAddressService userAddressService;

    @RequestMapping("get/user/address")
    @ResponseBody
    public List<UserAddress> getUserAddressAllByUserId(){

        List<UserAddress> userAddressList = userAddressService.getUserAddressAllByUserId();

        return userAddressList;
    }

}
