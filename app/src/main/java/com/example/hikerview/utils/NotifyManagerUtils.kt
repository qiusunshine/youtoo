package com.example.hikerview.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.lxj.xpopup.XPopup


/**
 * 作者：By 15968
 * 日期：On 2022/6/17
 * 时间：At 9:49
 */
class NotifyManagerUtils {
    companion object {
        var task: Runnable? = null

        /**
         * 打开通知权限
         *
         * @param context
         */
        private fun openNotificationSettingsForApp(context: Context) {
            // Links to this app's notification settings.
            val intent = Intent()
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
            // for Android 8 and above
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
            context.startActivity(intent)
        }

        fun openNotification(context: Context, runnable: Runnable) {
            task = null
            val notification = NotificationManagerCompat.from(context)
            val isEnabled = notification.areNotificationsEnabled()
            if (!isEnabled) {
                if (context is Activity) {
                    XPopup.Builder(context)
                        .asConfirm(
                            "温馨提示",
                            "检测到没有授予软件通知权限，通知权限仅用于避免本地视频播放、投屏、下载被杀后台，以及音乐通知栏等场景，请手动授予一下"
                        ) {
                            task = runnable
                            openNotificationSettingsForApp(context)
                        }.show()
                } else {
                    ToastMgr.shortBottomCenter(context, "请授予软件通知权限避免被杀后台")
                    task = runnable
                    openNotificationSettingsForApp(context)
                }
            } else {
                runnable.run()
            }
        }

        fun checkNotificationOnResume(context: Context) {
            if (task == null) {
                return
            }
            val notification = NotificationManagerCompat.from(context)
            val isEnabled = notification.areNotificationsEnabled()
            if (isEnabled) {
                task?.run()
                task = null
            }
        }
    }
}