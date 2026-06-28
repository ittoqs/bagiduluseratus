package com.example.offlinefileshare

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val activity: MainActivity
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action) {
            val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wi-Fi Direct is enabled
            } else {
                // Wi-Fi Direct is not enabled
                Toast.makeText(context, "Wi-Fi Direct is not enabled", Toast.LENGTH_SHORT).show()
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action) {
            // Request available peers from the wifi p2p manager.
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                manager.requestPeers(channel, activity.peerListListener)
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
            if (networkInfo?.isConnected == true) {
                manager.requestConnectionInfo(channel) { info ->
                    val groupOwnerAddress = info.groupOwnerAddress?.hostAddress
                    if (info.groupFormed && info.isGroupOwner) {
                        activity.connectionStatus.text = "Status: Connected (Server)"
                        activity.serverAddress = null // Server doesn't send to itself in this simple implementation
                        FileServerAsyncTask(context).executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR)
                    } else if (info.groupFormed) {
                        activity.connectionStatus.text = "Status: Connected (Client)"
                        activity.serverAddress = groupOwnerAddress
                    }
                }
            } else {
                activity.connectionStatus.text = "Status: Disconnected"
                activity.serverAddress = null
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            // Respond to this device's wifi state changing
        }
    }
}
