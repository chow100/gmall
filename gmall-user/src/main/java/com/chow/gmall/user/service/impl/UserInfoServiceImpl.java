package com.chow.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chow.gmall.bean.UserAddress;
import com.chow.gmall.bean.UserInfo;
import com.chow.gmall.service.UserInfoService;
import com.chow.gmall.user.mapper.UserAddressMapper;
import com.chow.gmall.user.mapper.UserInfoMapper;
import com.chow.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserAddressMapper userAddressMapper;


    @Override
    public List<UserInfo> getUserInfoAll() {
//        return userInfoMapper.selectUserInfoAll();

        return userInfoMapper.selectAll();


    }

    @Override
    public UserInfo getUserInfo(UserInfo userInfo) {
        return userInfoMapper.selectOne(userInfo);
    }

    // 用户登录后将用户信息存入redis
    @Override
    public void setUserInfoCache(UserInfo userInfoFromDb) {
        Jedis jedis = redisUtil.getJedis();

        String userId = userInfoFromDb.getId();
        jedis.setex("user:"+userId+":info",60*60*24,JSON.toJSONString(userInfoFromDb));

        jedis.close();

    }

    // 获取收货地址列表
    @Override
    public List<UserAddress> getUserAddress(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);
        return userAddressList;
    }

    // 获取邮寄地址
    @Override
    public UserAddress getAddressById(String deliveryAddressId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(deliveryAddressId);
        return userAddressMapper.selectOne(userAddress);
    }

}
