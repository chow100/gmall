package com.chow.gmall.service;

import com.chow.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    CartInfo getCartInfo(CartInfo cartExists);

    void savaCartInfo(CartInfo cartInfo);

    void updateCartInfo(CartInfo cartInfoFromDB);

    void flushCartCacheByUser(String userId);

    List<CartInfo> getCartInfoFromCache(String userId);

    List<CartInfo> getcartInfoByUserId(String userId);

    void mergeCache(String listCartCookie, String id);
}

