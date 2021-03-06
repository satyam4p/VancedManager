package com.vanced.manager.core.installer

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.vanced.manager.R
import com.vanced.manager.ui.MainActivity
import com.vanced.manager.utils.MiuiHelper.isMiui
import com.vanced.manager.utils.NotificationHelper.createBasicNotif

class SplitInstallerService: Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notifId = 666
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                Toast.makeText(this, "Installing...", Toast.LENGTH_SHORT).show()
                createBasicNotif(getString(R.string.installing_app, "Vanced"), notifId, this)
                Log.d(TAG, "Requesting user confirmation for installation")
                val confirmationIntent =
                    intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                confirmationIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    startActivity(confirmationIntent)
                } catch (e: Exception) {
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Log.d(TAG, "Installation succeed")
                getSharedPreferences("installPrefs", Context.MODE_PRIVATE).edit().putBoolean("isInstalling", false).apply()
                val mIntent = Intent(MainActivity.INSTALL_COMPLETED)
                mIntent.action = MainActivity.INSTALL_COMPLETED
                mIntent.putExtra("package", "split")
                LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent)
                createBasicNotif(
                    getString(R.string.successfully_installed, "Vanced"),
                    notifId,
                    this
                )
            }
            else -> {
                sendFailure(intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999))
                createBasicNotif(
                    getErrorMessage(intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)),
                    notifId,
                    this
                )
            }
        }
        stopSelf()
        return START_NOT_STICKY
    }

    private fun sendFailure(status: Int) {
        val mIntent = Intent(MainActivity.INSTALL_FAILED)
        mIntent.action = MainActivity.INSTALL_FAILED
        mIntent.putExtra("errorMsg", getErrorMessage(status))
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent)
    }

    private fun getErrorMessage(status: Int): String {
        return when (status) {
            PackageInstaller.STATUS_FAILURE_ABORTED -> getString(R.string.installation_aborted)
            PackageInstaller.STATUS_FAILURE_BLOCKED -> getString(R.string.installation_blocked)
            PackageInstaller.STATUS_FAILURE_STORAGE -> getString(R.string.installation_storage)
            PackageInstaller.STATUS_FAILURE_INVALID -> getString(R.string.installation_invalid)
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> getString(R.string.installation_incompatible)
            PackageInstaller.STATUS_FAILURE_CONFLICT -> getString(R.string.installation_conflict)
            else ->
                if (isMiui())
                    getString(R.string.installation_miui)
                else
                    getString(R.string.installation_failed)
        }
    }

    private fun startForegroundNotif(text: String) {
        val notifBuilder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Notification.Builder(this, 666.toString()).setChannelId("69420")
            else
                Notification.Builder(this).setPriority(Notification.PRIORITY_DEFAULT)

        notifBuilder.apply {
            setContentTitle(getString(R.string.app_name))
            setContentText(text)
            setSmallIcon(R.drawable.ic_stat_name)
        }

        val notif = notifBuilder.build()
        startForeground(666, notif)
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object{
        const val TAG = "VMInstall"
    }

}