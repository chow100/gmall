package com.chow.gmall.service;

import com.chow.gmall.bean.PaymentInfo;

public interface PaymentService {
    void savePayment(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendPaymentSuccess(String out_trade_no, String paymentStatus, String trackingNo);

    void sendDelayPaymentResult(String outTradeNo, int count);

    PaymentInfo getPaymentStatus(String outTradeNo);

    boolean checkPaymentStatues(String out_trade_no);
}
