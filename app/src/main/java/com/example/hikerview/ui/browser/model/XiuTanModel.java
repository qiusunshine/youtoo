package com.example.hikerview.ui.browser.model;

import android.content.Context;

import com.example.hikerview.model.XiuTanFavor;
import com.example.hikerview.ui.browser.util.CollectionUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2019/10/3
 * 时间：At 22:26
 */
public class XiuTanModel {

    public synchronized static List<XiuTanFavor> getXiuTanLiked() {
        List<XiuTanFavor> xiuTanFavors = null;
        try {
            xiuTanFavors = LitePal.findAll(XiuTanFavor.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!CollectionUtil.isEmpty(xiuTanFavors)) {
            return xiuTanFavors;
        }
        return new ArrayList<>();
    }

    public static synchronized void saveXiuTanLiked(Context context, String dom, String url) {
        List<XiuTanFavor> xiuTanFavors = null;
        try {
            xiuTanFavors = LitePal.where("dom = ?", dom).limit(1).find(XiuTanFavor.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!CollectionUtil.isEmpty(xiuTanFavors)) {
            if ("www.fy-sys.cn".equals(url)) {
                //删除
                xiuTanFavors.get(0).delete();
            } else {
                //更新
                xiuTanFavors.get(0).setUrl(url);
                xiuTanFavors.get(0).save();
            }
        } else {
            //新增
            XiuTanFavor xiuTanFavor = new XiuTanFavor();
            xiuTanFavor.setDom(dom);
            xiuTanFavor.setUrl(url);
            xiuTanFavor.save();
        }
    }
}
