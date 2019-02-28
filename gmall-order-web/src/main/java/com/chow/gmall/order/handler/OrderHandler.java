package com.chow.gmall.order.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chow.gmall.annotation.LoginRequire;
import com.chow.gmall.bean.*;
import com.chow.gmall.bean.enums.PaymentWay;
import com.chow.gmall.service.CartService;
import com.chow.gmall.service.OrderService;
import com.chow.gmall.service.SkuService;
import com.chow.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class OrderHandler {

    @Reference
    UserInfoService userInfoService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    @LoginRequire(isNeedLogin = true)
    @RequestMapping("submitOrder")
    public String submitOrder(String deliveryAddressId,String tradeCode,HttpServletRequest request, HttpServletResponse response, ModelMap map) {

        String userId = (String)request.getAttribute("userId");
        String nickName = (String)request.getAttribute("nickName");


        boolean b = orderService.checkTradeCode(tradeCode,userId);
        if(b){

            UserAddress userAddress = userInfoService.getAddressById(deliveryAddressId);
            List<CartInfo> cartInfos = cartService.getCartInfoFromCache(userId);

            // 订单保存业务(订单数据的一致性校验，库存价格)
            // 对订单对象进行封装
            OrderInfo orderInfo = new OrderInfo();

            orderInfo.setProcessStatus("订单已提交");
            orderInfo.setOrderStatus("订单已提交");

            String outTradeNo = "chow" + userId;
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = simpleDateFormat.format(date);
            outTradeNo = outTradeNo + format + System.currentTimeMillis();
            orderInfo.setOutTradeNo(outTradeNo);

            orderInfo.setUserId(userId);
            PaymentWay online = PaymentWay.ONLINE;
            String comment = online.getComment();
            orderInfo.setPaymentWay(comment);
            orderInfo.setTotalAmount(getMySum(cartInfos));
            orderInfo.setOrderComment(nickName);
            orderInfo.setDeliveryAddress(userAddress.getUserAddress());
            orderInfo.setCreateTime(new Date());
            orderInfo.setConsignee(userAddress.getConsignee());
            orderInfo.setConsigneeTel(userAddress.getPhoneNum());
            // 过期时间
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            orderInfo.setExpireTime(c.getTime());

            // 对订单详情进行封装
            List<OrderDetail> orderDetailList = new ArrayList<>();
            List<String> delCartIds = new ArrayList<>();
            for (CartInfo cartInfo : cartInfos) {
                if(cartInfo.getIsChecked().equals("1")){
                    // 验价
                    SkuInfo skuInfo = skuService.getSkuById(cartInfo.getSkuId());
                    int i = skuInfo.getPrice().compareTo(cartInfo.getSkuPrice());
                    if(i==0){
                        OrderDetail orderDetail = new OrderDetail();
                        orderDetail.setSkuId(cartInfo.getSkuId());
                        orderDetail.setSkuNum(cartInfo.getSkuNum());
                        orderDetail.setImgUrl(cartInfo.getImgUrl());
                        orderDetail.setSkuName(cartInfo.getSkuName());
                        // orderDetail.setOrderId();
                        orderDetail.setOrderPrice(cartInfo.getCartPrice());
                        orderDetailList.add(orderDetail);
                        delCartIds.add(cartInfo.getId());
                    }else {
                        return "tradeFail";
                    }
                    // 验库存
                }
            }

            orderInfo.setOrderDetailList(orderDetailList);

            orderService.saveOrder(orderInfo);

            return "redirect:http://payment.gmall.com:8087/paymentindex.html?outTradeNo=" + outTradeNo + "&totalAmount=" + getMySum(cartInfos);

        }else {
            return "tradeFail";
        }

    }



    @LoginRequire(isNeedLogin = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap map){

        String userId = (String) request.getAttribute("userId");

        // 根据userId查询缓存中的购物车数据
        List<CartInfo> cartInfoList = cartService.getCartInfoFromCache(userId);

        // 将购物车数据转化为订单列表数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            if(cartInfo.getIsChecked().equals("1")){
                OrderDetail orderDetail = new OrderDetail();

                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setHasStock("1");

                orderDetailList.add(orderDetail);
            }
        }


        // 获取送货地址信息
        List<UserAddress> userAddressList = userInfoService.getUserAddress(userId);

        if(userAddressList==null){} // 添加收获地址，没有页面，先不做

        map.put("userAddressList",userAddressList);
        map.put("orderDetailList",orderDetailList);
        map.put("totalAmount",getMySum(cartInfoList));

        String tradeCode = UUID.randomUUID().toString();
        map.put("tradeCode",tradeCode);
        orderService.genTradeCode(tradeCode,userId);

        return "trade";
    }


    private BigDecimal getMySum(List<CartInfo> cartList) {
        BigDecimal b = new BigDecimal("0");
        for (CartInfo cartInfo : cartList) {
            String isChecked = cartInfo.getIsChecked();

            if (isChecked.equals("1")) {
                b = b.add(cartInfo.getCartPrice());
            }
        }
        return b;
    }
}
