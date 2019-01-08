package com.chow.gmall.user.service.impl;

import com.chow.gmall.user.bean.UserInfo;
import com.chow.gmall.user.mapper.UserInfoMapper;
import com.chow.gmall.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
