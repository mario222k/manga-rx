package de.mario222k.mangarx.plugin

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import de.mario222k.mangarx.R
import de.mario222k.mangarxinterface.provider.IProviderInterface

class PluginConnection : ServiceConnection {

    interface Listener {
        fun onConnected()
        fun onDisconnected()
    }

    private var intent: Intent? = null
    var binder: IProviderInterface? = null
        private set
    var listener: Listener? = null

    fun connect(appContext: Context, packageName: String) {
        if (isConnected()) {
            return
        }

        intent = Intent()
        intent?.`package` = packageName
        intent?.action = appContext.getString(R.string.provider_name)
        appContext.bindService(intent, this, Service.BIND_AUTO_CREATE)
    }

    fun disconnect(appContext: Context) {
        if (!isConnected()) {
            return
        }
        listener?.onDisconnected()
        if (intent != null) {
            appContext.stopService(intent);
        }
        appContext.unbindService(this)
        cleanUp()
    }

    fun isConnected() = binder != null

    private fun cleanUp() {
        binder = null
        intent = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (service != null) {
            binder = IProviderInterface.Stub.asInterface(service)
            listener?.onConnected()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        listener?.onDisconnected()
        cleanUp()
    }
}