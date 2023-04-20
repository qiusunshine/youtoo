package com.example.hikerview.ui.browser.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.function.Consumer;
import com.example.hikerview.R;
import com.example.hikerview.ui.view.PopImageLoader;
import com.example.hikerview.ui.view.popup.MyXpopup;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import java.io.File;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class ImagesViewerPopup extends BottomPopupView {
    private List<String> list;
    private ImagesViewerAdapter adapter;
    private Activity activity;
    private Consumer<String> homeBgSetter;

    public ImagesViewerPopup(@NonNull Context context) {
        super(context);
    }

    public ImagesViewerPopup(Activity activity, List<String> list, Consumer<String> homeBgSetter) {
        super(activity);
        this.activity = activity;
        this.list = list;
        this.homeBgSetter = homeBgSetter;
    }


    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_images_viewer;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ImagesViewerAdapter(activity, list, new ImagesViewerAdapter.OnClickListener() {
            @Override
            public void click(ImageView view, int pos) {
                new MyXpopup().Builder(getContext())
                        .asImageViewer(view, list.get(pos), new PopImageLoader(view, null))
                        .show();
            }

            @Override
            public void longClick(View view, int pos) {
                new XPopup.Builder(getContext())
                        .asCenterList(null, new String[]{"删除图片", "设为壁纸"}, (position, text) -> {
                            switch (text) {
                                case "删除图片":
                                    File file = new File(list.get(pos));
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    list.remove(pos);
                                    adapter.notifyItemRemoved(pos);
                                    ToastMgr.shortCenter(getContext(), "已删除");
                                    break;
                                case "设为壁纸":
                                    if (homeBgSetter != null) {
                                        homeBgSetter.accept(list.get(pos));
                                        ToastMgr.shortCenter(getContext(), "设置成功");
                                    }
                                    break;
                            }
                        }).show();
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(adapter.getDividerItem());
    }


    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .85f);
    }
}
