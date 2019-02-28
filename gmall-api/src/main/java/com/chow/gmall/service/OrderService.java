package com.chow.gmall.service;

import com.chow.gmall.bean.OrderInfo;

public interface OrderService {
    boolean checkTradeCode(String tradeCode, String userId);

    void genTradeCode(String tradeCode, String userId);

    void saveOrder(OrderInfo orderInfo);

    OrderInfo getOrderInfo(String outTradeNo);

    void updateOrderInfo(String out_trade_no, String trade_no);

    void sendOrderSuccess(String out_trade_no);
}
