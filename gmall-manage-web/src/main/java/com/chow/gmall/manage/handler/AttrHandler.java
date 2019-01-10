package com.chow.gmall.manage.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chow.gmall.bean.BaseAttrInfo;
import com.chow.gmall.bean.BaseCatalog1;
import com.chow.gmall.bean.BaseCatalog2;
import com.chow.gmall.bean.BaseCatalog3;
import com.chow.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AttrHandler {

    @Reference
    private AttrService attrService;

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1s(){

        List<BaseCatalog1> baseCatalog1s = attrService.getCatalog1();

        return baseCatalog1s;
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2s(String catalog1Id){

        List<BaseCatalog2> baseCatalog2s = attrService.getCatalog2(catalog1Id);

        return baseCatalog2s;
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3s(String catalog2Id){

        List<BaseCatalog3> baseCatalog3s = attrService.getCatalog3(catalog2Id);

        return baseCatalog3s;
    }

    @RequestMapping("getAttrList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrList(String catalog3Id){

        List<BaseAttrInfo> baseAttrInfo = attrService.getAttrList(catalog3Id);

        return baseAttrInfo;
    }

}
