package com.chow.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chow.gmall.bean.CartInfo;
import com.chow.gmall.cart.mapper.CartInfoMapper;
import com.chow.gmall.service.CartService;
import com.chow.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo getCartInfo(CartInfo cartExists) {
        return cartInfoMapper.selectOne(cartExists);
    }

    @Override
    public void savaCartInfo(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public void updateCartInfo(CartInfo cartInfoFromDB) {
        Example e = new Example(CartInfo.class);
        e.createCriteria().andEqualTo("userId",cartInfoFromDB.getUserId()).andEqualTo("skuId",cartInfoFromDB.getSkuId());
        cartInfoMapper.updateByExampleSelective(cartInfoFromDB,e);
    }

    // 刷新redis中的缓存数据
    @Override
    public void flushCartCacheByUser(String userId) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);

        Jedis jedis = redisUtil.getJedis();

        if(cartInfos != null) {
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            for (CartInfo info : cartInfos) {
                stringStringHashMap.put(info.getId(), JSON.toJSONString(info));
            }

            // 新增或覆盖掉原来的缓存
            jedis.hmset("cart:" + userId + ":info", stringStringHashMap);

            jedis.close();
        }
    }


    // 从redis缓存中获取cartInfo数据
    @Override
    public List<CartInfo> getCartInfoFromCache(String userId) {

        Jedis jedis = redisUtil.getJedis();

        List<CartInfo> cartInfos = new ArrayList<>();

        List<String> stringList = jedis.hvals("cart:" + userId + ":info");
        for (String s : stringList) {
            CartInfo cartInfo = JSON.parseObject(s, CartInfo.class);
                cartInfos.add(cartInfo);
        }

        jedis.close();

        return cartInfos;
    }

    @Override
    public List<CartInfo> getcartInfoByUserId(String userId) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        return cartInfoMapper.select(cartInfo);

    }

    // 登录后将缓存中的购物车和db的购物车合并
    @Override
    public void mergeCache(String listCartCookie, String userId) {
        List<CartInfo> cartInfoListFromCookie = JSON.parseArray(listCartCookie, CartInfo.class);

        List<CartInfo> cartInfoListFromDb = getcartInfoByUserId(userId);

        for (CartInfo cartInfo : cartInfoListFromCookie) {
            boolean b = if_new_cart(cartInfoListFromDb, cartInfo);

            if (!b) {
                // 缓存中的数据在db中存在，则更新数量总价
                for (CartInfo info : cartInfoListFromDb) {
                    if(cartInfo.getSkuId().equals(info.getSkuId())) {
                        info.setSkuNum(info.getSkuNum() + cartInfo.getSkuNum());
                        info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        cartInfoMapper.updateByPrimaryKeySelective(info);
                    }
                }
            } else {
                // 缓存中的数据在db中不存在，则添加
                cartInfo.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfo);
            }
        }
        flushCartCacheByUser(userId);
    }

    private boolean if_new_cart(List<CartInfo> cartInfoList, CartInfo cartInfo) {
        boolean b = true;
        for (CartInfo info : cartInfoList) {
            String skuId = info.getSkuId();
            if(skuId.equals(cartInfo.getSkuId())){
                b = false;
            }
        }
        return b;
    }
}
