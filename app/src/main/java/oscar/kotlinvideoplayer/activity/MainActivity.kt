package oscar.kotlinvideoplayer.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.startActivity
import oscar.kotlinvideoplayer.R
import oscar.kotlinvideoplayer.util.PermissionsChecker

class MainActivity : AppCompatActivity() {

    // 所需的全部权限
    private val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val PERMISSION_REQUEST_CODE = 233//权限请求回调
    private var bRequireCheck = true // 是否需要系统权限检测, 防止和系统提示框重叠

    var permissionsChecker: PermissionsChecker? = null

    private val PACKAGE_URL_SCHEME = "package:" // 方案

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionsChecker = PermissionsChecker(this)

        jump_play_btn.onClick {
            startActivity<PlayerActivity>()
        }
    }

    override fun onResume() {
        super.onResume()

        //初始化工作(获取系统权限 调用anyoffice接口)
        if (bRequireCheck) {
            bRequireCheck = false
            if (permissionsChecker!!.lacksPermissions(*permissions)) {//判断是否有所需要的权限
                requestPermissions(*permissions) // 请求权限
            }
        }
    }


    // 请求权限兼容低版本
    private fun requestPermissions(vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) {
            return
        }
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
        } else {
            showMissingPermissionDialog()
        }
    }


    // 判断是否所有权限都通过
    private fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
        return !grantResults.contains(PackageManager.PERMISSION_DENIED)
    }


    // 显示缺失权限提示
    private fun showMissingPermissionDialog() {
        val builder = AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
        builder.setTitle(R.string.help)
        builder.setMessage(R.string.permission_request_tip)

        // 拒绝, 退出应用
        builder.setNegativeButton(R.string.quit, { dialog, which -> finish() })

        builder.setPositiveButton(R.string.action_settings, { dialog, which ->
            bRequireCheck = true
            startAppSettings()
        })

        builder.setCancelable(false)

        builder.show()
    }



    // 启动应用的设置
    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse(PACKAGE_URL_SCHEME + packageName)
        startActivity(intent)
    }

}
