package com.chow.gmall.manage.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chow.gmall.bean.SkuInfo;
import com.chow.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuHandler {

    @Reference
    SkuService skuService;

//    @RequestMapping("saveSku")
//    @ResponseBody
//    public String saveSku(SkuInfo skuInfo){
//
//        return "success";
//    }

    @RequestMapping("skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo> skuInfoListBySpu(String spuId){
        List<SkuInfo>  skuInfoList = skuService.skuInfoListBySpu(spuId);
        return skuInfoList;
    }

    @RequestMapping("saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){
        skuService.saveSku(skuInfo);
        return "success";
    }

}
