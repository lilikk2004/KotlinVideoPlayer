package oscar.kotlinvideoplayer.util

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

/**
 * 检查权限的工具类
 *
 *
 * Created by wangchenlong on 16/1/26.
 */
class PermissionsChecker(context: Context) {
    private val mContext: Context

    init {
        mContext = context.applicationContext
    }

    // 判断权限集合
    fun lacksPermissions(vararg permissions: String): Boolean {
        return permissions.any { lacksPermission(it) }
    }

    // 判断是否缺少权限
    private fun lacksPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_DENIED
    }
}
