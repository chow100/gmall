package com.chow.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chow.gmall.bean.UserInfo;
import com.chow.gmall.service.UserInfoService;
import com.chow.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public List<UserInfo> getUserInfoAll() {
//        return userInfoMapper.selectUserInfoAll();

        return userInfoMapper.selectAll();


    }

}
