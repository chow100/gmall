package com.chow.gmall.service;

import com.chow.gmall.bean.UserAddress;

import java.util.List;

public interface UserAddressService {

    List<UserAddress> getUserAddressAllByUserId();
}
