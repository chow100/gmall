package com.chow.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chow.gmall.bean.SkuLsInfo;
import com.chow.gmall.bean.SkuLsParam;
import com.chow.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;


    @Override
    public List<SkuLsInfo> list(SkuLsParam skuLsParam) {
        File file = new File("aa");
        file.length();
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();

        String dsl = getMyDsl(skuLsParam);
        System.err.println(dsl);

        // 建立查询
        Search build = new Search.Builder(dsl).addIndex("gmall").addType("SkuLsInfo").build();
        // 执行查询
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Long total = execute.getTotal();

        if(total>0) {
            // 获取多个查询结果列表
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);

            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                // 获取每一个查询结果的信息
                SkuLsInfo source = hit.source;


                Map<String, List<String>> highlight = hit.highlight;
                if(highlight!=null) {
                    List<String> skuName = highlight.get("skuName");

                    String s = skuName.get(0);
                    source.setSkuName(s);
                }
                // 将查询到的每个商品单元结果放入集合
                skuLsInfos.add(source);
            }
        }


        // 取聚合值
//        List<String> attrValueIdList=new ArrayList<>();
//        MetricAggregation aggregations = execute.getAggregations();
//        TermsAggregation groupby_attr = aggregations.getTermsAggregation("valueIdAggs");
//        if(groupby_attr!=null){
//            List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
//            for (TermsAggregation.Entry bucket : buckets) {
//                attrValueIdList.add( bucket.getKey()) ;
//            }
//        }

        return skuLsInfos;
    }

    // 查询语句
    private String getMyDsl(SkuLsParam skuLsParam) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        String catalog3Id = skuLsParam.getCatalog3Id();
        if(StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        String keyword = skuLsParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        String[] valueId = skuLsParam.getValueId();
        if(valueId != null && valueId.length>0){
            for (String id : valueId) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", id);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        searchSourceBuilder.query(boolQueryBuilder);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;font-weight:bolder;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);


        // 查询数量
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);

        // 属性值id的聚合
//        TermsBuilder groupby_attr = AggregationBuilders.terms("valueIdAggs").field("skuAttrValueList.valueId");
//        searchSourceBuilder.aggregation(groupby_attr);


        return searchSourceBuilder.toString();

    }
}
