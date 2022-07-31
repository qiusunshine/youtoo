package com.example.hikerview.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Toast;

import com.annimon.stream.function.Consumer;
import com.bumptech.glide.Glide;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.view.PopImageLoaderNoView;
import com.lxj.xpopup.interfaces.XPopupImageLoader;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

/**
 * 作者：By hdy
 * 日期：On 2018/6/21
 * 时间：At 18:54
 */

public class ImgUtil {
    private static final String TAG = ImgUtil.class.getSimpleName();

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 300) {  //循环判断如果压缩后图片是否大于3000kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null);
    }

    /**
     * 对图片进行毛玻璃化
     *
     * @param sentBitmap       位图
     * @param radius           虚化程度
     * @param canReuseInBitmap 是否重用
     * @return 位图
     */
    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int[] vmin = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

//        print("虚化后 ",bitmap);
        return (bitmap);
    }

    /**
     * 对图片进行毛玻璃化
     *
     * @param originBitmap 位图
     * @param scaleRatio   缩放比率
     * @param blurRadius   毛玻璃化比率，虚化程度
     * @return 位图
     */
    public static Bitmap doBlur(Bitmap originBitmap, int scaleRatio, int blurRadius) {
//        print("原图：：",originBitmap);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originBitmap,
                originBitmap.getWidth() / scaleRatio,
                originBitmap.getHeight() / scaleRatio,
                false);
        Bitmap blurBitmap = doBlur(scaledBitmap, blurRadius, false);
        scaledBitmap.recycle();
        return blurBitmap;
    }

//    private static void print(String tag, Bitmap originBitmap) {
//        StringBuilder sb = new StringBuilder(tag);
//        sb.append( String.format("  width=%s,",originBitmap.getWidth()));
//        sb.append( String.format(" height=%s,",originBitmap.getHeight()));
//        Log.i(TAG,sb.toString());
//    }

    /**
     * 对图片进行 毛玻璃化，虚化
     *
     * @param originBitmap 位图
     * @param width        缩放后的期望宽度
     * @param height       缩放后的期望高度
     * @param blurRadius   虚化程度
     * @return 位图
     */
    public static Bitmap doBlur(Bitmap originBitmap, int width, int height, int blurRadius) {
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(originBitmap, width, height);
        Bitmap blurBitmap = doBlur(thumbnail, blurRadius, true);
        thumbnail.recycle();
        return blurBitmap;
    }

    /**
     * 获取手机状态栏的高度
     *
     * @return 状态栏的高度
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c;
        Object obj;
        Field field;
        int x, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 保存到相册
     *
     * @param context context
     * @param picUrl  picUrl
     * @param baseUrl baseUrl
     */
    public static void savePic2Gallery(Context context, String picUrl, String baseUrl, OnSaveListener listener) {
        com.lxj.xpopup.util.XPermission.create(context, com.lxj.xpopup.util.PermissionConstants.STORAGE)
                .callback(new com.lxj.xpopup.util.XPermission.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        //save bitmap to album.
                        saveBmpToAlbum(context, new PopImageLoaderNoView(baseUrl), new ArrayList<>(Collections.singletonList(picUrl)), c -> {
                            if (listener != null) {
                                listener.success(c);
                            }
                        });
                    }

                    @Override
                    public void onDenied() {
                        Toast.makeText(context, "没有保存权限，保存功能无法使用！", Toast.LENGTH_SHORT).show();
                    }
                }).request();

    }

    public static void savePic2Gallery(Context context, XPopupImageLoader imageLoader, Object picUrl, OnSaveListener listener) {
        com.lxj.xpopup.util.XPermission.create(context, com.lxj.xpopup.util.PermissionConstants.STORAGE)
                .callback(new com.lxj.xpopup.util.XPermission.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        //save bitmap to album.
                        saveBmpToAlbum(context, imageLoader, new ArrayList<>(Collections.singletonList(picUrl)), c -> {
                            if (listener != null) {
                                listener.success(c);
                            }
                        });
                    }

                    @Override
                    public void onDenied() {
                        Toast.makeText(context, "没有保存权限，保存功能无法使用！", Toast.LENGTH_SHORT).show();
                    }
                }).request();

    }

    /**
     * 保存到相册
     *
     * @param context context
     * @param picUrls picUrls
     * @param baseUrl baseUrl
     */
    public static void savePic2Gallery(Context context, List<Object> picUrls, String baseUrl, Consumer<List<String>> completeListener) {
        com.lxj.xpopup.util.XPermission.create(context, com.lxj.xpopup.util.PermissionConstants.STORAGE)
                .callback(new com.lxj.xpopup.util.XPermission.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        //save bitmap to album.
                        saveBmpToAlbum(context, new PopImageLoaderNoView(baseUrl), picUrls, completeListener);
                    }

                    @Override
                    public void onDenied() {
                        Toast.makeText(context, "没有保存权限，保存功能无法使用！", Toast.LENGTH_SHORT).show();
                    }
                }).request();

    }


    public static void saveBmpToAlbum(final Context context, final XPopupImageLoader imageLoader, List<Object> uriList,
                                      Consumer<List<String>> completeListener) {
        if (CollectionUtil.isEmpty(uriList)) {
            return;
        }
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        HeavyTaskUtil.executeNewTask(() -> {
            List<String> paths = new ArrayList<>();
            for (int i = 0; i < uriList.size(); i++) {
                Object url = uriList.get(i);
                File source = imageLoader.getImageFile(context, url);
                if (source == null) {
                    continue;
                }
                //1. create path
                String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_PICTURES;
                File dirFile = new File(dirPath);
                if (!dirFile.exists()) {
                    dirFile.mkdirs();
                }
                try {
                    String ext = getImageType(source);
                    final File target = new File(dirPath, (url instanceof String ? StringUtil.md5((String) url) : System.currentTimeMillis()) + "." + ext);
                    if (target.exists()) {
                        //删除不了，只能不再创建
                        paths.add(target.getAbsolutePath());
                        continue;
//                        target.delete();
                    }
                    //3. notify
                    //再更新图库
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, target.getName());
                        values.put(MediaStore.MediaColumns.MIME_TYPE, ShareUtil.getMimeType(ext));
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                        ContentResolver contentResolver = context.getContentResolver();
                        Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        if (uri == null) {
                            return;
                        }
                        try (OutputStream outputStream = contentResolver.openOutputStream(uri)) {
                            FileInputStream fileInputStream = new FileInputStream(source);
                            FileUtils.copy(fileInputStream, outputStream);
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!new File(target.getAbsolutePath()).exists()) {
                            target.createNewFile();
                            //2. save
                            writeFileFromIS(target, new FileInputStream(source));
                        }
                    } else {
                        target.createNewFile();
                        //2. save
                        writeFileFromIS(target, new FileInputStream(source));
                        MediaScannerConnection.scanFile(context, new String[]{target.getAbsolutePath()},
                                new String[]{"image/" + ext}, (path, uri1) -> mainHandler.post(() -> {

                                }));
                    }
                    paths.add(target.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    mainHandler.post(() -> {
                        Toast.makeText(context, "没有保存权限，保存功能无法使用！", Toast.LENGTH_SHORT).show();
                    });
                    break;
                }
            }
            if (completeListener != null) {
                mainHandler.post(() -> completeListener.accept(paths));
            }
        });
    }

    public static String getImageType(final File file) {
        if (file == null) return "";
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] bytes = new byte[12];
            if (is.read(bytes) != -1) {
                String type = bytes2HexString(bytes, true).toUpperCase();
                if (type.contains("FFD8FF")) {
                    return "jpg";
                } else if (type.contains("89504E47")) {
                    return "png";
                } else if (type.contains("47494638")) {
                    return "gif";
                } else if (type.contains("49492A00") || type.contains("4D4D002A")) {
                    return "tiff";
                } else if (type.contains("424D")) {
                    return "bmp";
                } else if (type.startsWith("52494646") && type.endsWith("57454250")) {//524946461c57000057454250-12个字节
                    return "webp";
                } else if (type.contains("00000100") || type.contains("00000200")) {
                    return "ico";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static final char[] HEX_DIGITS_UPPER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] HEX_DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String bytes2HexString(final byte[] bytes, boolean isUpperCase) {
        if (bytes == null) return "";
        char[] hexDigits = isUpperCase ? HEX_DIGITS_UPPER : HEX_DIGITS_LOWER;
        int len = bytes.length;
        if (len <= 0) return "";
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = hexDigits[bytes[i] >> 4 & 0x0f];
            ret[j++] = hexDigits[bytes[i] & 0x0f];
        }
        return new String(ret);
    }

    private static boolean writeFileFromIS(final File file, final InputStream is) {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            byte data[] = new byte[8192];
            int len;
            while ((len = is.read(data, 0, 8192)) != -1) {
                os.write(data, 0, len);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnSaveListener {
        void success(List<String> paths);

        void failed(String msg);
    }

    private static void save(Context context, String picUrl, String picName) throws Exception {
        URL url = new URL(picUrl);
        //打开输入流
        InputStream inputStream = url.openStream();
        //对网上资源进行下载转换位图图片
        Bitmap bmp = BitmapFactory.decodeStream(inputStream);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileName = null;
        //系统相册目录
        String galleryPath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;
        // 声明文件对象
        File file = null;
        // 声明输出流
        FileOutputStream outStream = null;
        try {
            // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
            file = new File(galleryPath, picName + ".jpg");
            // 获得文件相对路径
            fileName = file.toString();
            // 获得输出流，如果文件中有内容，追加内容
            outStream = new FileOutputStream(fileName);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //通知相册更新
//        MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                bmp, fileName, null);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    public static void downloadImgByGlide(Context context, String urls, String filePathList) {
        if (context == null) {
            return;
        }
        HeavyTaskUtil.executeNewTask(() -> {
            String[] ors = urls.split("\\|\\|");
            for (String url : ors) {
                if (StringUtil.isNotEmpty(url)) {
                    try {
                        downloadImgByGlideSync(context, url, filePathList);
                        //只要有一个下载成功就结束
                        return;
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            }
        });
    }


    private static void downloadImgByGlideSync(Context context, String url, String filePath) throws ExecutionException, InterruptedException, IOException {
        File file = Glide.with(context).downloadOnly().load(url).submit().get();
        if (file != null && file.exists()) {
            File out = new File(filePath);
            if (out.exists()) {
                out.delete();
            } else if (!out.getParentFile().exists()) {
                out.getParentFile().mkdirs();
            }
            FileUtil.copy(file, out);
        } else {
            throw new IOException("file not found");
        }
    }
}
