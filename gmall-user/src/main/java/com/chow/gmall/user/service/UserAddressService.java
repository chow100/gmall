package com.chow.gmall.user.service;

import com.chow.gmall.user.bean.UserAddress;

import java.util.List;

public interface UserAddressService {

    List<UserAddress> getUserAddressAllByUserId();
}
