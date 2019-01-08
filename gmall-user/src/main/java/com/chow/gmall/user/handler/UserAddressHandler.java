package com.chow.gmall.user.handler;

import com.chow.gmall.user.bean.UserAddress;
import com.chow.gmall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserAddressHandler {

    @Autowired
    private UserAddressService userAddressService;

    @RequestMapping("get/user/address")
    @ResponseBody
    public List<UserAddress> getUserAddressAllByUserId(){

        List<UserAddress> userAddressList = userAddressService.getUserAddressAllByUserId();

        return userAddressList;
    }

}
