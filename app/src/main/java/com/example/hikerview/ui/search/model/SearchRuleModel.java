package com.example.hikerview.ui.search.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.model.SearchEngineDO;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.base.BaseModel;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.utils.StringUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 作者：By 15968
 * 日期：On 2019/10/31
 * 时间：At 21:10
 */
public class SearchRuleModel extends BaseModel<SearchEngineDO> {
    @Override
    public void process(String actionType, BaseCallback<SearchEngineDO> baseCallback) {
        List<SearchEngineDO> searchEngineDOS = getSortedSearchEngines();
        baseCallback.bindArrayToView(actionType, searchEngineDOS);
    }

    public static List<SearchEngineDO> getSortedSearchEngines() {
        List<SearchEngineDO> searchEngineDOS = null;
        try {
            searchEngineDOS = LitePal.findAll(SearchEngineDO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!CollectionUtil.isEmpty(searchEngineDOS)) {
            //不允许超过500条
            if (searchEngineDOS.size() > 501) {
                searchEngineDOS.subList(501, searchEngineDOS.size()).clear();
            }
        }
        if (searchEngineDOS == null) {
            return new ArrayList<>();
        }
        Map<String, Integer> orderMap = loadOrderMapValue();
        if (orderMap != null && !orderMap.isEmpty()) {
            for (int i = 0; i < searchEngineDOS.size(); i++) {
                if (orderMap.containsKey(searchEngineDOS.get(i).getTitle())) {
                    searchEngineDOS.get(i).setOrder(orderMap.get(searchEngineDOS.get(i).getTitle()));
                }
            }
            Collections.sort(searchEngineDOS);
        }
        return searchEngineDOS;
    }

    private static Map<String, Integer> loadOrderMapValue() {
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.SEARCH_LIST_ORDER_KEY).findFirst(BigTextDO.class);
        if (bigTextDO != null) {
            String value = bigTextDO.getValue();
            if (StringUtil.isNotEmpty(value)) {
                return JSON.parseObject(value, new TypeReference<Map<String, Integer>>() {
                });
            }
        }
        return null;
    }
}
