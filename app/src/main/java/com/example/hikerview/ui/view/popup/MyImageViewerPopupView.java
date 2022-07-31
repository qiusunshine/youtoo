package com.example.hikerview.ui.view.popup;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.hikerview.utils.ImgUtil;
import com.lxj.xpopup.core.ImageViewerPopupView;

import java.util.List;


/**
 * Description: 大图预览的弹窗，使用Transition实现
 * Create by lxj, at 2019/1/22
 */
public class MyImageViewerPopupView extends ImageViewerPopupView {

    public MyImageViewerPopupView(@NonNull Context context) {
        super(context);
    }

    private Runnable onShowTask;

    public Runnable getOnShowTask() {
        return onShowTask;
    }

    public void setOnShowTask(Runnable onShowTask) {
        this.onShowTask = onShowTask;
    }

    @Override
    protected void onShow() {
        super.onShow();
        if (onShowTask != null) {
            onShowTask.run();
        }
    }

    public TextView getSaveView(){
        return tv_save;
    }

    public int getPos(){
        return position;
    }

    @Override
    protected void save() {
        Object pic = urls.get(isInfinite ? position % urls.size() : position);
        ImgUtil.savePic2Gallery(container.getContext(), imageLoader, pic, new ImgUtil.OnSaveListener() {
            @Override
            public void success(List<String> paths) {
                Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failed(String msg) {
                Toast.makeText(getContext(), "保存失败：" + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
