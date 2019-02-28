package com.chow.gmall.manage.mapper;

import com.chow.gmall.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr>{
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("skuId") String skuId, @Param("spuId") String spuId);
}
