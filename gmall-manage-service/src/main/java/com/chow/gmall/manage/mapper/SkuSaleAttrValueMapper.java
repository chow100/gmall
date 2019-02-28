package com.chow.gmall.manage.mapper;

import com.chow.gmall.bean.SkuInfo;
import com.chow.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue>{
    List<SkuInfo> selectSkuSaleAttrValueListBySpu(String spuId);
}
