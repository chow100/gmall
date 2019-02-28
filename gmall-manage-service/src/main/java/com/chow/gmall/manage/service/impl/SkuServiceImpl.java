package com.chow.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chow.gmall.bean.SkuAttrValue;
import com.chow.gmall.bean.SkuImage;
import com.chow.gmall.bean.SkuInfo;
import com.chow.gmall.bean.SkuSaleAttrValue;
import com.chow.gmall.manage.mapper.SkuAttrValueMapper;
import com.chow.gmall.manage.mapper.SkuImageMapper;
import com.chow.gmall.manage.mapper.SkuInfoMapper;
import com.chow.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.chow.gmall.service.SkuService;
import com.chow.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<SkuInfo> skuInfoListBySpu(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);

        List<SkuInfo> skuInfoList = skuInfoMapper.select(skuInfo);

        return skuInfoList;
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {
        
        // 保存skuinfo
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();

        // 保存图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insertSelective(skuImage);
        }


        // 保存平台属性信息
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }


        // 保存销售属性信息
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }

    }

    @Override
    public SkuInfo getSkuInfoFromDB(String skuId) {

        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo info = skuInfoMapper.selectOne(skuInfo);

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);

        info.setSkuImageList(skuImageList);

        return info;
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

        SkuInfo skuInfo = null;
        Jedis jedis = redisUtil.getJedis();

        String skuInfoStr = jedis.get("sku:" + skuId + ":info");
        skuInfo = JSON.parseObject(skuInfoStr, SkuInfo.class);

        if(skuInfo == null){
            skuInfo = getSkuInfoFromDB(skuId);
            jedis.set("sku:"+skuId+":info",JSON.toJSONString(skuInfo));
        }

        jedis.close();

        return skuInfo;
    }



    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public List<SkuInfo> getSkuListByCatalog3Id(String catalog3Id) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setCatalog3Id(catalog3Id);
        List<SkuInfo> skuInfoList = skuInfoMapper.select(skuInfo);
        for (SkuInfo info : skuInfoList) {
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(info.getId());
            List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
            info.setSkuAttrValueList(skuAttrValueList);
        }

        return skuInfoList;
    }

    @Override
    public SkuInfo getSkuById(String skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        return skuInfoMapper.selectOne(skuInfo);
    }
}
