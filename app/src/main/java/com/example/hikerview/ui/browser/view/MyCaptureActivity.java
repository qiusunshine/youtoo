package com.example.hikerview.ui.browser.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.hikerview.R;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.UriUtils;
import com.google.zxing.Result;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.decode.DecodeImgCallback;
import com.yzq.zxinglibrary.decode.DecodeImgThread;

import java.io.File;

/**
 * 作者：By 15968
 * 日期：On 2023/3/27
 * 时间：At 12:07
 */

public class MyCaptureActivity extends com.yzq.zxinglibrary.android.CaptureActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.flashLightLayout) {
            super.onClick(view);
        } else if (id == R.id.albumLayout) {
            /*打开相册*/
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "选择图片"), Constant.REQUEST_IMAGE);
        } else if (id == R.id.backIv) {
            finish();
        }
    }

    public Context getContext() {
        return this;
    }

    public Activity getActivity() {
        return this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode + 1, resultCode, data);
        if (requestCode == Constant.REQUEST_IMAGE && resultCode == RESULT_OK) {
            Uri uri = data == null ? null : data.getData();
            if (uri == null) {
                return;
            }
            String fileName = "_fileSelect_" + UriUtils.getFileName(uri);
            String copyTo = UriUtils.getRootDir(getContext()) + File.separator + "cache" + File.separator + fileName;
            FileUtil.makeSureDirExist(copyTo);
            UriUtils.getFilePathFromURI(getContext(), uri, copyTo, new UriUtils.LoadListener() {
                @Override
                public void success(String s) {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        new DecodeImgThread(s, new DecodeImgCallback() {
                            @Override
                            public void onImageDecodeSuccess(Result result) {
                                handleDecode(result);
                                new File(s).delete();
                            }

                            @Override
                            public void onImageDecodeFailed() {
                                Toast.makeText(MyCaptureActivity.this, R.string.scan_failed_tip, Toast.LENGTH_SHORT).show();
                                new File(s).delete();
                            }
                        }).run();
                    }
                }

                @Override
                public void failed(String msg) {
                    Toast.makeText(MyCaptureActivity.this, "提取图片失败，可能是权限问题", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}