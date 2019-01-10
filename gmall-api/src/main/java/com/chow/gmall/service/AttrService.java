package com.chow.gmall.service;

import com.chow.gmall.bean.BaseAttrInfo;
import com.chow.gmall.bean.BaseCatalog1;
import com.chow.gmall.bean.BaseCatalog2;
import com.chow.gmall.bean.BaseCatalog3;

import java.util.List;

public interface AttrService {

    List<BaseCatalog1> getCatalog1();

    List<BaseCatalog2> getCatalog2(String catalog1Id);

    List<BaseCatalog3> getCatalog3(String catalog2Id);

    List<BaseAttrInfo> getAttrList(String catalog3Id);
}
