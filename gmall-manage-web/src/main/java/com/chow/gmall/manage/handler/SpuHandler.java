package com.chow.gmall.manage.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chow.gmall.bean.BaseSaleAttr;
import com.chow.gmall.bean.SpuImage;
import com.chow.gmall.bean.SpuInfo;
import com.chow.gmall.bean.SpuSaleAttr;
import com.chow.gmall.manage.util.ManageUploadUtil;
import com.chow.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class SpuHandler {

    @Reference
    private SpuInfoService spuInfoService;

    @RequestMapping("getSpuList")
    @ResponseBody
    public List<SpuInfo> getSpuList(String catalog3Id){

        List<SpuInfo> spuInfoList = spuInfoService.getSpuInfoList(catalog3Id);

        return spuInfoList;
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList(){

        List<BaseSaleAttr> getBaseSaleAttrList = spuInfoService.getBaseSaleAttrList();

        return getBaseSaleAttrList;
    }

    @RequestMapping("saveSpu")
    @ResponseBody
    public String saveSpuSaleAttr(SpuInfo spuInfo){

        spuInfoService.saveSpuSaleAttr(spuInfo);

        return "SUCCESS";
    }

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){

        String imgUrl = ManageUploadUtil.imgUpload(multipartFile);

        return imgUrl;
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrGroup(String spuId){

        List<SpuSaleAttr> spuSaleAttrList = spuInfoService.getSpuSaleAttrList(spuId);

        return spuSaleAttrList;

    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){

        List<SpuImage> spuImageList = spuInfoService.getSpuImageList(spuId);

        return spuImageList;

    }
}
