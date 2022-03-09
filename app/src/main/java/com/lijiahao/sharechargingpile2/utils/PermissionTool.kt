package com.lijiahao.sharechargingpile2.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

import androidx.core.content.ContextCompat

import androidx.core.app.ActivityCompat

import android.app.Activity
import android.content.Intent

import android.content.DialogInterface
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lijiahao.sharechargingpile2.ui.MapActivity
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.lang.ref.WeakReference


/**
 * https://cloud.tencent.com/developer/article/1677302
 * 使用方法：
 *     1. 在OnCreate中调用usedOnCreate
 *     2. 在OnRequestPermissionsResult中调用usedOnRequestPermissionsResult
 * TODO: 有很多权限其实最终要从设置中打开的开关是同一个，可以想着把这些权限的提示合并起来
 * TODO: 设置一个map, 将权限和系统版本作对应，申请权限时根据当前版本作对应权限的申请
 * 具体请求逻辑：
 * 1. 给定一个权限数组，先判断是否都已经授权  （isAllPermissionGranted）
 *      2. 如果是，则执行自己的事物
 *      3. 如果否，判断是否被拒绝过 shouldShowRequestReason
 *          4. 如果没有拒绝过，则请求权限 （requestNecessaryPermissions）， 然后在Activity的onRequestPermissionsResult中调用（isAllPermissionGranted）判断是否全部都已经被允许
 *              5. 如果是，则执行需要的操作
 *              6. 如果否，获取拒绝权限，并显示对话框 （getDeniedHintString） -> （showDeniedDialog）。 这个对话框点击确认的话会跳转到系统设置那
 *          7. 如果拒绝过，则提示用户再次申请的理由（getDeniedHint）-> （showDeniedDialog）
 */


class PermissionTool {
    companion object {

        /**
         *  只需要在Activity的Oncreate中使用该方法即可完成权限的请求
         *  @param activity: 要申请权限的Activity
         *  @param permissions: Array<String> 要申请的权限
         *  @param permissionsHint: 当用户不授权时，使用该文字来做出提醒
         *
         */
        @JvmStatic
        fun usedOnCreate(
            activity: Activity,
            permissions: Array<String>,
            permissionsHint: Array<String>
        ) :Boolean{


            // 权限请求
            return if (!isAllPermissionGranted(activity, permissions)) {
                // 如果没有全部授权则查看是否有权限被拒绝过
                if (shouldShowRequestReason(activity, permissions)) {
                    // 如果拒绝过，则显示对话框
                    val hint =
                        getDeniedHintStr(activity, permissions, permissionsHint)
                    showDeniedDialog(activity, hint)
                } else {
                    // 没有拒绝过，则申请权限
                    requestNecessaryPermissions(
                        activity,
                        permissions,
                        1
                    )
                }
                false
            } else {
                true
            }
        }

        /**
         * 只需要在Activity的onRequestPermissionsResult方法中调用该方法即可
         * @params requestCode, permissions, grantResults：OnRequestPermissionsResult中的参数
         * @param activity : 要申请权限的Activity
         * @param permissions： 当用户不授权的时候，使用该文字来做出提醒
         * @param callback: 当授权后执行的操作
         * @return 返回是否所有的权限都已经授权了。 如果true则已经全部授权，否则没有群不授权
         */
        @JvmStatic
        fun usedOnRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray,
            activity: Activity,
            permissionsHint: Array<String>,
        ): Boolean {
            return if (!isAllPermissionGranted(grantResults)) {

                // 然后显示对话框
                if (shouldShowRequestReason(activity, permissions)) {
                    val deniedHintStr =
                        getDeniedHintStr(activity, permissions, permissionsHint)
                    showDeniedDialog(activity, deniedHintStr)
                }
                isAllPermissionGranted(activity, permissions)
            } else {
                true
            }
        }

        /**
         * 检查单个权限是否已经被允许
         *
         * @param permission 要申请的权限
         */
        @JvmStatic
        fun checkCurPermissionStatus(context: Context, permission: String?): Boolean {
            return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                permission!!
            )
        }

        /**
         * 检查一组权限的授权状态。
         *
         * @param permissions 要申请的权限
         * @return 权限的状态数组
         */
        @JvmStatic
        fun checkCurPermissionsStatus(context: Context, permissions: Array<String>): BooleanArray {
            return if (permissions.isNotEmpty()) {
                val permissionsStatus = BooleanArray(permissions.size)
                for (i in permissions.indices) {
                    val permissionStatus = checkCurPermissionStatus(context, permissions[i])
                    permissionsStatus[i] = permissionStatus
                }
                permissionsStatus
            } else {
                throw IllegalArgumentException("参数不能为空，且必须有元素")
            }
        }


        /**
         * 获取被拒绝的权限
         *
         * @param permissions 要申请的全部权限
         * @return 被拒绝的权限
         */
        @JvmStatic
        fun getDeniedPermissions(context: Context, permissions: Array<String>): Array<String> {
            return if (permissions.isNotEmpty()) {
                val deniedPermissionList: MutableList<String> = ArrayList()
                val permissionsStatus = checkCurPermissionsStatus(context, permissions)
                for (i in permissions.indices) {
                    if (!permissionsStatus[i]) {
                        deniedPermissionList.add(permissions[i])
                    }
                }
                val deniedPermissions = deniedPermissionList.toTypedArray()
                deniedPermissions
            } else {
                throw IllegalArgumentException("参数不能为空，且必须有元素")
            }
        }

        /**
         * 获取被拒绝的权限对应的提示文本字符串数组
         *
         * @param permissions 要申请的全部权限
         * @param hints       权限被拒绝时的提示文本
         */
        @JvmStatic
        fun getDeniedHint(
            context: Context,
            permissions: Array<String>,
            hints: Array<String>
        ): Array<String> {
            return if (permissions.isEmpty() || hints.isEmpty() || permissions.size != hints.size) {
                throw IllegalArgumentException("参数不能为空、必须有元素，且两个参数的长度必须一致")
            } else {
                val deniedHintList: MutableList<String> = ArrayList()
                val permissionsStatus = checkCurPermissionsStatus(context, permissions)
                for (i in permissions.indices) {
                    if (!permissionsStatus[i]) {
                        deniedHintList.add(hints[i])
                    }
                }
                val deniedPermissions = deniedHintList.toTypedArray()
                deniedPermissions
            }
        }

        /**
         * 获取被拒绝的权限对应的提示文本字符串
         *
         * @param permissions 要申请的全部权限
         * @param hints       权限被拒绝时的提示文本
         */
        @JvmStatic
        fun getDeniedHintStr(
            context: Context,
            permissions: Array<String>,
            hints: Array<String>
        ): String {
            return if (permissions.isEmpty() || hints.isEmpty() || permissions.size != hints.size) {
                throw IllegalArgumentException("参数不能为空、必须有元素，且两个参数的长度必须一致")
            } else {
                val hintStr = StringBuilder()
                val permissionsStatus = checkCurPermissionsStatus(context, permissions)
                for (i in permissions.indices) {
                    if (!permissionsStatus[i]) {
                        hintStr.append(hints[i]).append("\n")
                    }
                }
                hintStr.toString()
            }
        }

        /**
         * 是否所有权限都已经被允许
         *
         * @param permissions 申请的权限
         * @return true 全被允许，false 有没有被允许的权限
         */
        @JvmStatic
        fun isAllPermissionGranted(context: Context, permissions: Array<String>): Boolean {
            return getDeniedPermissions(context, permissions).isEmpty()
        }

        /**
         * 是否所有权限都已经被允许
         *
         * @param grantResults 权限申请的结果
         * @return true 全被允许，false 有没有被允许的权限
         */
        @JvmName("isAllPermissionGranted1")
        @JvmStatic
        fun isAllPermissionGranted(grantResults: IntArray): Boolean {
            var isAllGranted = PackageManager.PERMISSION_GRANTED
            for (grantResult in grantResults) {
                isAllGranted = isAllGranted or grantResult
            }
            return isAllGranted == 0
        }

        /**
         * 请求权限
         *
         * @param activity    Activity
         * @param permissions 权限
         * @param requestCode 请求码
         */
        @JvmStatic
        fun requestNecessaryPermissions(
            activity: Activity,
            permissions: Array<String>,
            requestCode: Int
        ) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }


        /**
         * 检测之前是否已经拒绝过。 拒绝过返回true，否则返回false
         *
         * @param activity    activity
         * @param permissions 请求的权限
         */
        @JvmStatic
        fun shouldShowRequestReason(activity: Activity, permissions: Array<String>): Boolean {
            var showReason = false
            for (i in permissions.indices) {
                // 权限中是否有一个被拒绝过
                showReason = showReason or ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permissions[i]
                )
            }
            return showReason
        }


        /**
         * 展示被拒绝的弹窗
         */
        @JvmStatic
        fun showDeniedDialog(context: Context, message: String) {

            MaterialAlertDialogBuilder(context)
                .setTitle("提示")
                .setMessage(message)
                .setNegativeButton("拒绝", null)
                .setPositiveButton("授权") { _, _ ->
                    openSysSettingPage(context)
                }
                .setCancelable(false)
                .show()
        }

        /**
         * 打开系统设置界面
         */
        @JvmStatic
        private fun openSysSettingPage(context: Context) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }

    }
}
