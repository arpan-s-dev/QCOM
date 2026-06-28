package com.medic.app.nav

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AirplaneModeMonitor(context: Context) {

    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isAirplaneModeOn(): Boolean =
        Settings.Global.getInt(appContext.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1

    fun signals(): Flow<Boolean> = callbackFlow {
        fun emitCurrentState() {
            trySend(isAirplaneModeOn())
        }

        val airplaneReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                emitCurrentState()
            }
        }

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = emitCurrentState()
            override fun onLost(network: Network) = emitCurrentState()
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) = emitCurrentState()
        }

        emitCurrentState()
        appContext.registerReceiver(
            airplaneReceiver,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        )
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback
        )

        awaitClose {
            appContext.unregisterReceiver(airplaneReceiver)
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}
