package com.chow.gmall.orderListener;

import com.chow.gmall.order.mapper.OrderInfoMapper;
import com.chow.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderMqListener {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderService orderService;

    // 订单系统 消费支付成功消息
    @JmsListener(containerFactory = "jmsQueueListener", destination = "PAYMENT_SUCCESS_QUEUE")
    public void consumePaymentResult(MapMessage mapMessage){
        try {
            String out_trade_no = mapMessage.getString("out_trade_no");
            String trade_no = mapMessage.getString("tracking_no");

            System.err.println("out_trade_no = " + out_trade_no + ",trade_no = " + trade_no);

            // 更新orderInfo
            orderService.updateOrderInfo(out_trade_no,trade_no);

            // 订单系统 发送一个订单成功的消息队列，由库存系统消费（这个库存系统不在一个服务内，就要调用库存接口）
            orderService.sendOrderSuccess(out_trade_no);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
