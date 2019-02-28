package com.chow.gmall.service;


import com.chow.gmall.bean.UserAddress;
import com.chow.gmall.bean.UserInfo;

import java.util.List;

public interface UserInfoService {


    List<UserInfo> getUserInfoAll();

    UserInfo getUserInfo(UserInfo userInfo);

    void setUserInfoCache(UserInfo userInfoFromDb);

    List<UserAddress> getUserAddress(String userId);

    UserAddress getAddressById(String deliveryAddressId);
}


