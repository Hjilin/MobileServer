package com.mobileserver.core

import android.content.Context
import android.content.SharedPreferences

/**
 * 全局设置持久化（SharedPreferences）。
 * 设置页写入，服务/引擎读取。
 */
object SettingsManager {
    private const val PREFS = "mobile_server_prefs"

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
    private const val KEY_FRP_SERVER = "frp_server"
    private const val KEY_FRP_TOKEN = "frp_token"
    private const val KEY_FRP_REMOTE_PORT = "frp_remote_port"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // 开机自启
    fun getAutoStart(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_AUTO_START, true)
    fun setAutoStart(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean(KEY_AUTO_START, v).apply()

    // 后台保活
    fun getKeepAlive(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_KEEP_ALIVE, true)
    fun setKeepAlive(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean(KEY_KEEP_ALIVE, v).apply()

    // 前台通知
    fun getNotification(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_NOTIFICATION, true)
    fun setNotification(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean(KEY_NOTIFICATION, v).apply()

    // 日志保留天数
    fun getLogDays(ctx: Context): Int = prefs(ctx).getInt(KEY_LOG_DAYS, 7)
    fun setLogDays(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_LOG_DAYS, v).apply()

    // n2n
    fun getN2nEnabled(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_N2N_ENABLED, false)
    fun setN2nEnabled(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean(KEY_N2N_ENABLED, v).apply()

    fun getN2nSupernode(ctx: Context): String = prefs(ctx).getString(KEY_N2N_SUPERNODE, "wang.switchy.hin2n.stars:10086")!!
    fun setN2nSupernode(ctx: Context, v: String) = prefs(ctx).edit().putString(KEY_N2N_SUPERNODE, v).apply()

    fun getN2nCommunity(ctx: Context): String = prefs(ctx).getString(KEY_N2N_COMMUNITY, "nas-network")!!
    fun setN2nCommunity(ctx: Context, v: String) = prefs(ctx).edit().putString(KEY_N2N_COMMUNITY, v).apply()

    fun getN2nPassword(ctx: Context): String = prefs(ctx).getString(KEY_N2N_PASSWORD, "nas123456")!!
    fun setN2nPassword(ctx: Context, v: String) = prefs(ctx).edit().putString(KEY_N2N_PASSWORD, v).apply()

    fun getN2nVirtualIp(ctx: Context): String = prefs(ctx).getString(KEY_N2N_VIP, "192.168.100.1")!!
    fun setN2nVirtualIp(ctx: Context, v: String) = prefs(ctx).edit().putString(KEY_N2N_VIP, v).apply()

    // 网盘目录映射网站目录
    fun getWebDirMapping(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_WEB_DIR_MAPPING, true)
    fun setWebDirMapping(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean(KEY_WEB_DIR_MAPPING, v).apply()

    // frp
    fun getFrpServer(ctx: Context): String = prefs(ctx).getString(KEY_FRP_SERVER, "")!!
    fun setFrpServer(ctx: Context, v: String) = prefs(ctx).edit().putString(KEY_FRP_SERVER, v).apply()

    fun getFrpToken(ctx: Context): String = prefs(ctx).getString(KEY_FRP_TOKEN, "")!!
    fun setFrpToken(ctx: Context, v: String) = prefs(ctx).edit().putString(KEY_FRP_TOKEN, v).apply()

    fun getFrpRemotePort(ctx: Context): Int = prefs(ctx).getInt(KEY_FRP_REMOTE_PORT, 8080)
    fun setFrpRemotePort(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_FRP_REMOTE_PORT, v).apply()
}
