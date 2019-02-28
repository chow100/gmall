package com.chow.gmall.service;

import com.chow.gmall.bean.SkuInfo;

import java.util.List;

public interface SkuService {

    List<SkuInfo> skuInfoListBySpu(String spuId);

    void saveSku(SkuInfo skuInfo);

    SkuInfo getSkuInfoFromDB(String skuId);

    SkuInfo getSkuInfo(String skuId);

    List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId);

    List<SkuInfo> getSkuListByCatalog3Id(String s);

    SkuInfo getSkuById(String skuId);
}
