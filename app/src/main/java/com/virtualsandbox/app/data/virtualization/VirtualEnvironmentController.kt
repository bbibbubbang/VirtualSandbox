package com.virtualsandbox.app.data.virtualization

import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.virtualsandbox.app.domain.model.SandboxSpace
import com.virtualsandbox.app.domain.model.VirtualApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VirtualEnvironmentController @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun isVirtualizationSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_VIRTUALIZATION_FRAMEWORK)
    }

    fun canCloneApp(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openAppDetail(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ContextCompat.startActivity(context, intent, null)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun buildVirtualLaunchIntent(space: SandboxSpace, app: VirtualApp): Intent {
        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            ?: throw ActivityNotFoundException("앱을 실행할 수 없습니다: ${app.packageName}")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("virtual_sandbox_space_id", space.id)
        intent.putExtra("virtual_sandbox_profile", space.profile.name)
        return intent
    }

    fun launchVirtualApp(space: SandboxSpace, app: VirtualApp): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            try {
                val intent = buildVirtualLaunchIntent(space, app)
                val options = ActivityOptions.makeBasic()
                ContextCompat.startActivity(context, intent, options.toBundle())
                true
            } catch (_: Exception) {
                false
            }
        } else {
            false
        }
    }
}
