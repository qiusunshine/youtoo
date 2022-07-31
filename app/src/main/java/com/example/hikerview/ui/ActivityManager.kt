package com.example.hikerview.ui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.*

/**
 * 作者：By 15968
 * 日期：On 2022/3/28
 * 时间：At 17:07
 */
/**
 * Activity管理类<br></br>
 * 需要在Application中调用registerActivityLifecycleCallbacks注册
 */
class ActivityManager private constructor() : Application.ActivityLifecycleCallbacks {
    /**
     * 获取Activity任务栈
     * @return activity stack
     */
    //Activity栈
    val activityStack = Stack<Activity>()

    /**
     * Activity 入栈
     * @param activity Activity
     */
    fun addActivity(activity: Activity) {
        activityStack.add(activity)
    }

    /**
     * Activity出栈
     * @param activity Activity
     */
    fun removeActivity(activity: Activity?) {
        if (activity != null) {
            activityStack.remove(activity)
        }
    }
    /**
     * 结束某Activity
     * @param activity Activity
     */
    /**
     * 结束当前Activity
     */
    @JvmOverloads
    fun finishActivity(activity: Activity? = activityStack.lastElement()) {
        if (activity != null) {
            removeActivity(activity)
            activity.finish()
        }
    }

    /**
     * 获取当前Activity
     * @return current activity
     */
    val currentActivity: Activity
        get() = activityStack.lastElement()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        addActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        removeActivity(activity)
    }

    companion object {
        private var activityManager: ActivityManager? = null

        /**
         * 单例
         * @return activityManager instance
         */
        @JvmStatic
        val instance: ActivityManager
            get() {
                if (activityManager == null) {
                    synchronized(ActivityManager::class.java) {
                        if (activityManager == null) {
                            activityManager = ActivityManager()
                        }
                    }
                }
                return activityManager!!
            }
    }
}