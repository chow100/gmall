package com.chow.gmall.user.service.impl;

import com.chow.gmall.user.bean.UserAddress;
import com.chow.gmall.user.mapper.UserAddressMapper;
import com.chow.gmall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> getUserAddressAllByUserId() {
        return userAddressMapper.selectAll();
    }

}
