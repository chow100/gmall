package com.chow.gmall.cart.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.chow.gmall.annotation.LoginRequire;
import com.chow.gmall.bean.CartInfo;
import com.chow.gmall.bean.SkuInfo;
import com.chow.gmall.service.CartService;
import com.chow.gmall.service.SkuService;
import com.chow.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartHandler {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;


    @LoginRequire(isNeedLogin = false)
    @RequestMapping("addToCart")
    public String cart(HttpServletRequest request, HttpServletResponse response, String skuId, int num){

        String userId = (String) request.getAttribute("userId");
        SkuInfo skuInfo = skuService.getSkuInfo(skuId);
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setSkuNum(num);
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(num)));
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setIsChecked("1");
        cartInfo.setSkuName(skuInfo.getSkuName());
        if(StringUtils.isNotBlank(userId)) {
            cartInfo.setUserId(userId);
        }
        List<CartInfo> cartInfoList = new ArrayList<>();
        if(StringUtils.isBlank(userId)){
            // 用户未登录，存到cookie
            String cookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartInfoList = JSON.parseArray(cookie, CartInfo.class);
            if(StringUtils.isBlank(cookie)){
                // 直接添加到cookie
                cartInfoList = new ArrayList<>();
                cartInfoList.add(cartInfo);

            }else {
                // 更新cookie
//                for (CartInfo info : cartInfoList) {
//                    String skuIdFromCookie = info.getSkuId();
//                    if(skuIdFromCookie.equals(skuId)){
//                        // 该商品已添加，更新数量
//                        info.setSkuNum(info.getSkuNum()+cartInfo.getSkuNum());
//                        info.setSkuPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
//                    }else {
//                        cartInfoList.add(cartInfo);
//                    }
//                }
                // 不能用上面的方法，对cartInfoList遍历的同时向这个集合里添加数据，会导致死循环，报错
                boolean b = if_new_cart(cartInfoList,cartInfo);

                if(b){
                    // 新车
                    cartInfoList.add(cartInfo);
                }else{
                    // 老车
                    for (CartInfo info : cartInfoList) {
                        info.setSkuNum(info.getSkuNum()+cartInfo.getSkuNum());
                        info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                    }
                }

            }

            // 用户没有登录，将商品信息存放到cookie中
            CookieUtil.setCookie(request,response,"listCartCookie", JSON.toJSONString(cartInfoList),60*60*24,true);
        }else{
            // 用户已登录，存到db
            CartInfo cartExists = new CartInfo();
            cartExists.setSkuId(skuId);
            cartExists.setUserId(userId);
            CartInfo cartInfoFromDB = cartService.getCartInfo(cartExists);

            if(cartInfoFromDB == null){
                // 数据库中没有这个cartInfo，直接插入
                cartService.savaCartInfo(cartInfo);
            }else{
                // 数据库中有这个cartInfo，直接修改数量，价格
                cartInfoFromDB.setSkuNum(cartInfoFromDB.getSkuNum() + num);
                cartInfoFromDB.setCartPrice(cartInfoFromDB.getSkuPrice().multiply(new BigDecimal(cartInfoFromDB.getSkuNum())));
                cartService.updateCartInfo(cartInfoFromDB);
            }

            // 用户已经登录，将商品信息存放到redis中
            cartService.flushCartCacheByUser(userId);
        }

        return "redirect:http://payment.gmall.com:8084/success.html";
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
    //        SkuInfo skuInfo = (SkuInfo) map.get("skuInfo");

    //    @RequestMapping("addToCart2")

    @LoginRequire(isNeedLogin = false)
    @RequestMapping("logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, CartInfo cartInfo, ModelMap map){
        CookieUtil.deleteCookie(request,response,"oldToken");
        return "redirect:cartList";
    }

    @LoginRequire(isNeedLogin = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, ModelMap map){

        String userId = (String) request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");
        Integer totalNum = 0;
        List<CartInfo> cartList = new ArrayList<>();
        // 如果没有登录就从cookie中获取，否则就从redis中获取
        if(StringUtils.isBlank(userId)) {
            // 没有登录，从cookie中获取cartInfo信息
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartList = JSON.parseArray(listCartCookie,CartInfo.class);

        }else {
            // 已经登录，从redis中获取cartInfo信息
            cartList = cartService.getCartInfoFromCache(userId);
        }
        if(cartList!=null) {
            map.put("cartList", cartList);
            BigDecimal totalPrice = getTotalPrice(cartList);
            map.put("totalPrice", totalPrice);
            for (CartInfo cartInfo : cartList) {
                totalNum += cartInfo.getSkuNum();
            }
            map.put("totalNum", totalNum);
        }

        map.put("userId",userId);
        map.put("nickName",nickName);

        return "cartList";
    }


    @LoginRequire(isNeedLogin = false)
    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest request, HttpServletResponse response, CartInfo cartInfo, ModelMap map){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = new ArrayList<>();

        if(StringUtils.isBlank(userId)) {
            // userId为空，先从cookie中获取数据
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartList = JSON.parseArray(listCartCookie, CartInfo.class);
            // 修改cookie
            for (CartInfo info : cartList) {
                if (info.getSkuId().equals(cartInfo.getSkuId())) {
                    info.setIsChecked(cartInfo.getIsChecked());
                }
            }
            CookieUtil.setCookie(request,response,"listCartCookie", JSON.toJSONString(cartList),60*60*24,true);

        }else {
            cartList = cartService.getCartInfoFromCache(userId);
            for (CartInfo cart : cartList) {
                if(userId.equals(cart.getUserId())) {
                    if (cart.getSkuId().equals(cartInfo.getSkuId())) {
                        // 修改数据库，刷新redis缓存
                        cart.setIsChecked(cartInfo.getIsChecked());
                        cartService.updateCartInfo(cart);
                        cartService.flushCartCacheByUser(userId);
                    }
                }
            }
        }
        // 上面的方法代码冗余，可优化成下面的
//        String userId = "2";
//        List<CartInfo> cartList = new ArrayList<>();
//        if (StringUtils.isNotBlank(userId)) {
//            cartList = cartService.getCartInfoFromCache(userId);
//        } else {
//            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
//            cartList = JSON.parseArray(listCartCookie, CartInfo.class);
//        }
//
//        // 修改购物车状态
//        for (CartInfo info : cartList) {
//            if (info.getSkuId().equals(cartInfo.getSkuId())) {
//                info.setIsChecked(cartInfo.getIsChecked());
//                if(StringUtils.isNotBlank(userId)){
//                    cartService.updateCartInfo(info);
//                    // 刷新缓存
//                    cartService.flushCartCacheByUser(userId);
//                }else{
//                    // 覆盖cookie
//                    CookieUtil.setCookie(request, response, "listCartCookie", JSON.toJSONString(cartList), 60 * 60 * 24, true);
//                }
//            }
//        }

        map.put("cartList",cartList);

        BigDecimal totalPrice = getTotalPrice(cartList);
        map.put("totalPrice",totalPrice);

        return "cartListInner";
    }

    private BigDecimal getTotalPrice(List<CartInfo> cartList){
        BigDecimal totalPrice = new BigDecimal("0");
        for (CartInfo cart : cartList) {
            BigDecimal cartPrice = cart.getCartPrice();
            if(cart.getIsChecked().equals("1")) {
                totalPrice = totalPrice.add(cartPrice);
            }
        }
        return totalPrice;
    }


}
