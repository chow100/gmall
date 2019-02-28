package com.chow.gmall.service;

import com.chow.gmall.bean.SkuLsInfo;
import com.chow.gmall.bean.SkuLsParam;

import java.util.List;

public interface ListService {
    List<SkuLsInfo> list(SkuLsParam skuLsParam);
}
