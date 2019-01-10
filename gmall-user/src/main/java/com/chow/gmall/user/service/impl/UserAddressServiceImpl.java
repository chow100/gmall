package com.chow.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chow.gmall.bean.UserAddress;
import com.chow.gmall.service.UserAddressService;
import com.chow.gmall.user.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;

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
