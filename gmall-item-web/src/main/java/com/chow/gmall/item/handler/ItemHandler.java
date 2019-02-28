package com.chow.gmall.item.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.chow.gmall.bean.SkuInfo;
import com.chow.gmall.bean.SkuSaleAttrValue;
import com.chow.gmall.bean.SpuSaleAttr;
import com.chow.gmall.service.SkuService;
import com.chow.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemHandler {

    @Reference
    private SkuService skuService;

    @Reference
    private SpuInfoService spuInfoService;

    @RequestMapping("index")
    public String test(){
        return "item";
    }

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, ModelMap map, HttpServletRequest request){

        SkuInfo skuInfo = skuService.getSkuInfo(skuId);

        map.put("skuInfo",skuInfo);

        List<SpuSaleAttr> spuSaleAttrListCheckBySku = new ArrayList<>();
        String spuId = skuInfo.getSpuId();
        spuSaleAttrListCheckBySku = spuInfoService.getSpuSaleAttrListCheckBySku(skuId,spuId);

        map.put("spuSaleAttrListCheckBySku",spuSaleAttrListCheckBySku);

        // 隐藏一个map，sku的属性结合成键，skuId为值
        List<SkuInfo> skuInfoList = skuService.getSkuSaleAttrValueListBySpu(spuId);

        Map<String, String> skuMap = new HashMap<>();
        for (SkuInfo info : skuInfoList) {
            String v = info.getId();

            String k = "";
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            for (SkuSaleAttrValue skuSaleAttrValue  : skuSaleAttrValueList) {
                String valueId = skuSaleAttrValue.getSaleAttrValueId();
                k = k + "|" + valueId;
            }

            skuMap.put(k,v);
        }

        map.put("skuMap", JSON.toJSONString(skuMap));

        return "item";

    }


}

