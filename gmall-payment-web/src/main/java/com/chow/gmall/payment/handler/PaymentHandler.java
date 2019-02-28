package com.chow.gmall.payment.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.chow.gmall.annotation.LoginRequire;
import com.chow.gmall.bean.OrderInfo;
import com.chow.gmall.bean.PaymentInfo;
import com.chow.gmall.payment.config.AlipayConfig;
import com.chow.gmall.service.OrderService;
import com.chow.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentHandler {

    @Reference
    OrderService orderService;

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

    @RequestMapping("/alipay/callback/return")
    public String callback(HttpServletRequest request, Map<String, String> paramsMap) {

        String out_trade_no = request.getParameter("out_trade_no");
        String trade_no = request.getParameter("trade_no");

        boolean b = paymentService.checkPaymentStatues(out_trade_no);
        if (!b) {
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setCallbackContent(request.getQueryString());
            paymentService.updatePayment(paymentInfo);


            // 支付成功后，开始发送消息，多个服务并发执行
            paymentService.sendPaymentSuccess(out_trade_no, paymentInfo.getPaymentStatus(), trade_no);
        }

        return "success";
    }

    @LoginRequire(isNeedLogin = true)
    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String toPay(HttpServletRequest httpRequset, HttpServletResponse httpResponse, String outTradeNo, BigDecimal totalAmount) throws IOException {

        OrderInfo orderInfo = orderService.getOrderInfo(outTradeNo);
        String skuName = orderInfo.getOrderDetailList().get(0).getSkuName();

        // 所有的公共参数封装到alipayClient中，所有的接口参数/请求参数封装到alipayRequest中，
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址o

        Map<String, Object> skuMap = new HashMap<>();
        skuMap.put("out_trade_no", outTradeNo);
        skuMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        skuMap.put("total_amount", 0.01);
        skuMap.put("subject", skuName);
        alipayRequest.setBizContent(JSON.toJSONString(skuMap));//填充业务参数

        // 根据公共参数和接口参数生成form表单，返回给页面，页面经行重定向
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // 生成(保存)支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setSubject(skuName);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setCreateTime(new Date());
        paymentService.savePayment(paymentInfo);

//        httpResponse.setContentType("text/html;charset=" + AlipayConfig.charset);
//        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
//        httpResponse.getWriter().flush();
//        httpResponse.getWriter().close();

        // 支付系统 发送延迟检查消息
        paymentService.sendDelayPaymentResult(outTradeNo, 4);

        return form;
    }

    @LoginRequire(isNeedLogin = true)
    @RequestMapping("paymentindex")
    public String paymentindex(HttpServletRequest request, String outTradeNo, BigDecimal totalAmount, ModelMap map) {

        String userId = (String) request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");

        map.addAttribute("outTradeNo", outTradeNo);
        map.addAttribute("nickName", nickName);
        map.addAttribute("totalAmount", totalAmount);

        return "paymentindex";
    }
}
