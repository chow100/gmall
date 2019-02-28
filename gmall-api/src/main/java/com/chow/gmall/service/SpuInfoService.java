package com.chow.gmall.service;

import com.chow.gmall.bean.BaseSaleAttr;
import com.chow.gmall.bean.SpuImage;
import com.chow.gmall.bean.SpuInfo;
import com.chow.gmall.bean.SpuSaleAttr;

import java.util.List;

public interface SpuInfoService {
    List<SpuInfo> getSpuInfoList(String catalog3Id);

    List<BaseSaleAttr> getBaseSaleAttrList();

    void saveSpuSaleAttr(SpuInfo spuInfo);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    List<SpuImage> getSpuImageList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String skuId, String spuId);
}
