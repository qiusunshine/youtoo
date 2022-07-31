package com.example.hikerview.ui.browser.model;

import com.example.hikerview.constants.Media;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.base.BaseModel;

/**
 * 作者：By 15968
 * 日期：On 2019/10/2
 * 时间：At 14:47
 */
public class MediaListModel extends BaseModel<DetectedMediaResult> {
    private static final String TAG = "ArticleListRuleModel";

    @Override
    public void process(String actionType, final BaseCallback<DetectedMediaResult> baseCallback) {
        try {
            if (mParams[0] == null) {
                baseCallback.bindArrayToView(actionType, DetectorManager.getInstance().getDetectedMediaResults((Media) null));
                return;
            }
            Media mediaType = (Media) mParams[0];
            baseCallback.bindArrayToView(actionType, DetectorManager.getInstance().getDetectedMediaResults(mediaType));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
