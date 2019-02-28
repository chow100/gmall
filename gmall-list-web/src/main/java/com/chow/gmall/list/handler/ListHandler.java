package com.chow.gmall.list.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chow.gmall.bean.*;
import com.chow.gmall.service.AttrService;
import com.chow.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class ListHandler {

    @Reference
    ListService listService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    // skuLsParam 是根据url的请求地址获得的参数
    public String list(SkuLsParam skuLsParam, ModelMap map){

        List<SkuLsInfo> skuLsInfoList = listService.list(skuLsParam);

        Set<String> valueIds = new HashSet<>();


        for (SkuLsInfo skuLsInfo : skuLsInfoList) {
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                valueIds.add(valueId);
            }
        }

        String join = StringUtils.join(valueIds, ",");
        List<BaseAttrInfo> attrList = attrService.getAttrInfoList(join);

        // 将查询到的每个商品单元结果放入map
        map.put("skuLsInfoList",skuLsInfoList);

        // 每个商品单元抽取出来的平台属性经过set去重后的集合
        map.put("attrList",attrList);

        String urlParam = "";

        List<BaseAttrValue> attrValueSelectedList = new ArrayList<>();
        // valueId是所有请求参数的数组
        String[] valueId = skuLsParam.getValueId();
        if(valueId!=null&&valueId.length>0){

            // 获取展示出来的平台属性的迭代器
            Iterator<BaseAttrInfo> iterator = attrList.iterator();
            while (iterator.hasNext()) {
                // 通过迭代获取平台属性值的集合
                List<BaseAttrValue> attrValueList = iterator.next().getAttrValueList();
                // 遍历集合
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    // 列表中的属性值id
                    String id = baseAttrValue.getId();
                    for (String sid : valueId) {
                        // 如果sid与id匹配，则去掉id所在的属性
                        if(id.equals(sid)){

                            // 去掉平台属性之前，将选中的属性值放入面包屑中
                            BaseAttrValue attrValue = new BaseAttrValue();
                            attrValue.setValueName(baseAttrValue.getValueName());

                            urlParam = getMyUrlParam(skuLsParam,sid);
                            attrValue.setUrlParam(urlParam);
                            attrValueSelectedList.add(attrValue);


                            iterator.remove();
                        }
                    }
                }
            }
        }

        map.put("attrValueSelectedList",attrValueSelectedList);


        // 根据当前请求参数对象，生成当前请求参数字符串
        urlParam = getMyUrlParam(skuLsParam);
        map.put("urlParam",urlParam);

        return "list";
    }

    // 如果

    private String getMyUrlParam(SkuLsParam skuLsParam, String... crumdId) {
        String urlParam = "";
        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueId = skuLsParam.getValueId();

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (valueId != null && valueId.length > 0) {
            for (String id : valueId) {
                if (crumdId.length>0 && !crumdId[0].equals(id)) {
                    // 拼接面包屑的地址
                    urlParam = urlParam + "&valueId=" + id;
                }else if (crumdId.length==0){
                    // 拼接列表中平台属性的地址
                    urlParam = urlParam + "&valueId=" + id;
                }
                //总结：注意else和else if的差别，可变形参有值和无值的差别
            }
        }
        return urlParam;
    }




//    private String getMyUrlParam(SkuLsParam skuLsParam) {
//        String urlParam = "";
//        String catalog3Id = skuLsParam.getCatalog3Id();
//        String keyword = skuLsParam.getKeyword();
//        String[] valueId = skuLsParam.getValueId();
//
//        if(StringUtils.isNotBlank(catalog3Id)){
//            if(StringUtils.isNotBlank(urlParam)){
//                urlParam = urlParam + "&";
//            }
//            urlParam = urlParam + "catalog3Id=" +catalog3Id;
//        }
//
//        if(StringUtils.isNotBlank(keyword)){
//            if(StringUtils.isNotBlank(urlParam)){
//                urlParam = urlParam + "&";
//            }
//            urlParam = urlParam + "keyword=" +keyword;
//        }
//
//        if(valueId!=null&&valueId.length>0){
//            for (String id : valueId) {
//                    urlParam = urlParam + "&valueId=" + id;
//            }
//        }
//
//
//        return urlParam;
//    }

}
