package com.chow.gmall.payment.paymentListener;

import com.chow.gmall.bean.PaymentInfo;
import com.chow.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;

@Component
public class delayCheckListener {

    @Autowired
    PaymentService paymentService;

    // 支付系统 消费延迟检查消息
    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAYMENT_CHECK_QUEUE")
    public void consumeCheckResult(MapMessage mapMessage){

        try {

            String outTradeNo = mapMessage.getString("outTradeNo");
            int count = mapMessage.getInt("count");

                if(count>0){
                    System.err.println("正在进行第" + (5 - count) + "次检查！");

                    // 获取支付状态
                    PaymentInfo paymentInfo = paymentService.getPaymentStatus(outTradeNo);
                    String paymentStatus = paymentInfo.getPaymentStatus();

                    if(paymentInfo.getPaymentStatus()!=null && (paymentStatus.equals("TRADE_SUCCESS") || paymentInfo.equals("TRADE_FINISHED"))){
                        System.err.println("交易支付成功，更新交易状态。。。");

                        boolean b = paymentService.checkPaymentStatues(paymentInfo.getOutTradeNo());
                        if(!b) {
                            PaymentInfo paymentInfoByMq = new PaymentInfo();
                            paymentInfoByMq.setOutTradeNo(paymentInfo.getOutTradeNo());
                            paymentInfoByMq.setAlipayTradeNo(paymentInfo.getAlipayTradeNo());
                            paymentInfoByMq.setPaymentStatus("已支付");
                            paymentInfoByMq.setCallbackTime(new Date());
                            paymentInfoByMq.setCallbackContent(paymentInfo.getCallbackContent());
                            paymentService.updatePayment(paymentInfoByMq);

                            // 支付成功后，支付系统 发送支付成功消息，接下来由 订单系统 消费该消息
                            paymentService.sendPaymentSuccess(paymentInfo.getOutTradeNo(), paymentInfo.getPaymentStatus(), paymentInfo.getAlipayTradeNo());
                        }

                    }else {
                        paymentService.sendDelayPaymentResult(outTradeNo,--count);
                        System.err.println("支付交易未成功，继续进行检查交易状态");
                        System.err.println();
                    }


                }else {
                    System.err.println("检查次数完毕，检查未果。。。");
                }

        }catch (JMSException e){
            e.printStackTrace();
        }


    }

}

