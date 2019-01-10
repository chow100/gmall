package com.chow.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chow.gmall.bean.BaseAttrInfo;
import com.chow.gmall.bean.BaseCatalog1;
import com.chow.gmall.bean.BaseCatalog2;
import com.chow.gmall.bean.BaseCatalog3;
import com.chow.gmall.manage.mapper.BaseAttrInfoMapper;
import com.chow.gmall.manage.mapper.BaseCatalog1Mapper;
import com.chow.gmall.manage.mapper.BaseCatalog2Mapper;
import com.chow.gmall.manage.mapper.BaseCatalog3Mapper;
import com.chow.gmall.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {

        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);

        return baseCatalog2Mapper.select(baseCatalog2);

    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);

        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);

        return baseAttrInfoMapper.select(baseAttrInfo);
    }
}
