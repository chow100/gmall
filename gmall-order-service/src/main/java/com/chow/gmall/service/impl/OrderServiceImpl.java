package com.chow.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chow.gmall.bean.OrderDetail;
import com.chow.gmall.bean.OrderInfo;
import com.chow.gmall.conf.ActiveMQUtil;
import com.chow.gmall.order.mapper.OrderDetailMapper;
import com.chow.gmall.order.mapper.OrderInfoMapper;
import com.chow.gmall.service.OrderService;
import com.chow.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public boolean checkTradeCode(String tradeCode, String userId) {
        boolean b = false;

        Jedis jedis = redisUtil.getJedis();

        String tradeCodeFromCache = jedis.get("user:" + userId + ":tradeCode");

        if(tradeCode.equals(tradeCodeFromCache)){
            b = true;
            jedis.del("user:"+userId+":tradeCode");
        }

        jedis.close();

        return b;
    }

    @Override
    public void genTradeCode(String tradeCode, String userId) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:"+userId+":tradeCode",60*30,tradeCode);

        jedis.close();
    }

    @Override
    public void saveOrder(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);

        String orderId = orderInfo.getId();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insertSelective(orderDetail);
        }

    }

    @Override
    public OrderInfo getOrderInfo(String outTradeNo) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);

        orderInfo= orderInfoMapper.selectOne(orderInfo);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);

        orderInfo.setOrderDetailList(orderDetailList);

        return orderInfo;

    }

    @Override
    public void updateOrderInfo(String out_trade_no, String trade_no) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(out_trade_no);
        orderInfo.setTrackingNo(trade_no);
        orderInfo.setOrderStatus("已支付");
        orderInfo.setProcessStatus("已支付");
        orderInfo.setPaymentWay("支付宝");

        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);

        orderInfoMapper.updateByExampleSelective(orderInfo, example);
    }

    // 发送订单成功后库存消费的队列
    @Override
    public void sendOrderSuccess(String out_trade_no) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(out_trade_no);
        orderInfo = orderInfoMapper.selectOne(orderInfo);

        String orderInfoId = orderInfo.getId();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfoId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);

        orderInfo.setOrderDetailList(orderDetails);


        try {
            Connection connection = activeMQUtil.getConnectionFactory().createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue paymentResultQueue = session.createQueue("ORDER_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(paymentResultQueue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            TextMessage textMessage= new ActiveMQTextMessage();
            textMessage.setText(JSON.toJSONString(orderInfo));

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);

            session.commit();

            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
