package com.chow.gmall.payment.service.impl;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.chow.gmall.bean.PaymentInfo;
import com.chow.gmall.conf.ActiveMQUtil;
import com.chow.gmall.payment.mapper.PaymentMapper;
import com.chow.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentMapper paymentMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",paymentInfo.getOutTradeNo());
        paymentMapper.updateByExampleSelective(paymentInfo, example);
    }

    @Override
    public void sendPaymentSuccess(String out_trade_no, String paymentStatus, String trackingNo) {
        // 整合了ActiveMQUtil和ActiveMQConfig后，使用ActiveMQUtil的连接池更加规范。使用那个boss的测试代码也可以
        try {
            ConnectionFactory connect = activeMQUtil.getConnectionFactory();
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_SUCCESS_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage = new ActiveMQMapMessage();// TextMessage对象底层是tcp协议的字符串（还有另外一种map型的消息）
            mapMessage.setString("out_trade_no",out_trade_no);
            mapMessage.setString("payment_status",paymentStatus);
            mapMessage.setString("tracking_no",trackingNo);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage); // producer发送消息（consumer是监听消息）
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void sendDelayPaymentResult(String outTradeNo, int count) {
        try {
            Connection connection = activeMQUtil.getConnectionFactory().createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue paymentResultQueue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(paymentResultQueue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            MapMessage mapMessage= new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setInt("count",count);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,20*1000);
            producer.send(mapMessage);

            session.commit();
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public PaymentInfo getPaymentStatus(String outTradeNo) { // 因为当交易创建成功后，要修改paymentInfo，所以返回PaymentInfo

        PaymentInfo paymentInfo = new PaymentInfo();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        Map<String,Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no",outTradeNo);
        String s = JSON.toJSONString(requestMap);
        request.setBizContent(s);

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        // 等待付款、付款成功、付款失败、已经结束
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setCallbackContent(response.getMsg());
        if(response.isSuccess()){
            System.out.println("交易已创建");
            paymentInfo.setPaymentStatus(response.getTradeStatus());
            paymentInfo.setAlipayTradeNo(response.getTradeNo());

        } else {
            System.out.println("交易未创建");
        }

        return paymentInfo;
    }

    @Override
    public boolean checkPaymentStatues(String out_trade_no) {

        boolean b = false;

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        paymentInfo = paymentMapper.selectOne(paymentInfo);
        String paymentStatus = paymentInfo.getPaymentStatus();
        if("已支付".equals(paymentStatus)){
            b = true;
        }


        return b;
    }
}
