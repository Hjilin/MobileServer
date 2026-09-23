package com.mobileserver.core

import android.content.Context
import android.content.SharedPreferences

/**
 * 全局设置持久化（SharedPreferences）。
 * 设置页写入，服务/引擎读取。
 */
object SettingsManager {
    private const val PREFS = "mobile_server_prefs"

    // key
    private const val KEY_AUTO_START = "auto_start"
    private const val KEY_KEEP_ALIVE = "keep_alive"
    private const val KEY_NOTIFICATION = "notification"
    private const val KEY_LOG_DAYS = "log_days"
    private const val KEY_N2N_ENABLED = "n2n_enabled"
    private const val KEY_N2N_SUPERNODE = "n2n_supernode"
    private const val KEY_N2N_COMMUNITY = "n2n_community"
    private const val KEY_N2N_PASSWORD = "n2n_password"
    private const val KEY_N2N_VIP = "n2n_vip"
    private const val KEY_WEB_DIR_MAPPING = "web_dir_mapping"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // 开机自启
    var autoStart: Boolean
        get(ctx: Context) = prefs(ctx).getBoolean(KEY_AUTO_START, true)
        set(ctx, v) = prefs(ctx).edit().putBoolean(KEY_AUTO_START, v).apply()

    // 后台保活
    var keepAlive: Boolean
        get(ctx: Context) = prefs(ctx).getBoolean(KEY_KEEP_ALIVE, true)
        set(ctx, v) = prefs(ctx).edit().putBoolean(KEY_KEEP_ALIVE, v).apply()

    // 前台通知
    var notification: Boolean
        get(ctx: Context) = prefs(ctx).getBoolean(KEY_NOTIFICATION, true)
        set(ctx, v) = prefs(ctx).edit().putBoolean(KEY_NOTIFICATION, v).apply()

    // 日志保留天数
    var logDays: Int
        get(ctx: Context) = prefs(ctx).getInt(KEY_LOG_DAYS, 7)
        set(ctx, v) = prefs(ctx).edit().putInt(KEY_LOG_DAYS, v).apply()

    // n2n
    var n2nEnabled: Boolean
        get(ctx: Context) = prefs(ctx).getBoolean(KEY_N2N_ENABLED, false)
        set(ctx, v) = prefs(ctx).edit().putBoolean(KEY_N2N_ENABLED, v).apply()

    var n2nSupernode: String
        get(ctx: Context) = prefs(ctx).getString(KEY_N2N_SUPERNODE, "wang.switchy.hin2n.stars:10086")!!
        set(ctx, v) = prefs(ctx).edit().putString(KEY_N2N_SUPERNODE, v).apply()

    var n2nCommunity: String
        get(ctx: Context) = prefs(ctx).getString(KEY_N2N_COMMUNITY, "nas-network")!!
        set(ctx, v) = prefs(ctx).edit().putString(KEY_N2N_COMMUNITY, v).apply()

    var n2nPassword: String
        get(ctx: Context) = prefs(ctx).getString(KEY_N2N_PASSWORD, "nas123456")!!
        set(ctx, v) = prefs(ctx).edit().putString(KEY_N2N_PASSWORD, v).apply()

    var n2nVirtualIp: String
        get(ctx: Context) = prefs(ctx).getString(KEY_N2N_VIP, "192.168.100.1")!!
        set(ctx, v) = prefs(ctx).edit().putString(KEY_N2N_VIP, v).apply()

    // 网盘目录映射网站目录
    var webDirMapping: Boolean
        get(ctx: Context) = prefs(ctx).getBoolean(KEY_WEB_DIR_MAPPING, true)
        set(ctx, v) = prefs(ctx).edit().putBoolean(KEY_WEB_DIR_MAPPING, v).apply()
}
