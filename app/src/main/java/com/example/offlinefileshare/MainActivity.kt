package com.example.offlinefileshare

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: Channel
    private lateinit var receiver: BroadcastReceiver
    private val intentFilter = IntentFilter()

    private val peers = mutableListOf<WifiP2pDevice>()
    private lateinit var peerNames: Array<String>
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var btnDiscover: Button
    private lateinit var listView: ListView
    lateinit var connectionStatus: TextView
    private lateinit var btnSendFile: Button

    var serverAddress: String? = null

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            serverAddress?.let { host ->
                FileTransferService.startActionSendFile(this, it.toString(), host, 8988)
            } ?: run {
                Toast.makeText(this, "Not connected to a server", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            // Permissions granted
            discoverPeers()
        } else {
            Toast.makeText(this, "Permissions required for Wi-Fi Direct", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnDiscover = findViewById(R.id.btnDiscover)
        listView = findViewById(R.id.peerListView)
        connectionStatus = findViewById(R.id.connectionStatus)
        btnSendFile = findViewById(R.id.btnSendFile)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        btnDiscover.setOnClickListener {
            checkPermissionsAndDiscover()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val device = peers[position]
            val config = WifiP2pConfig().apply {
                deviceAddress = device.deviceAddress
            }

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                manager.connect(channel, config, object : ActionListener {
                    override fun onSuccess() {
                        Toast.makeText(this@MainActivity, "Connected to ${device.deviceName}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(reason: Int) {
                        Toast.makeText(this@MainActivity, "Connect failed. Retry.", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        btnSendFile.setOnClickListener {
            if (serverAddress != null) {
                pickFileLauncher.launch("*/*")
            } else {
                Toast.makeText(this, "Not connected to a device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionsAndDiscover() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val ungranted = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (ungranted.isNotEmpty()) {
            requestPermissionLauncher.launch(ungranted.toTypedArray())
        } else {
            discoverPeers()
        }
    }

    private fun discoverPeers() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED) {

            manager.discoverPeers(channel, object : ActionListener {
                override fun onSuccess() {
                    connectionStatus.text = "Discovery Started"
                }

                override fun onFailure(reasonCode: Int) {
                    connectionStatus.text = "Discovery Failed : $reasonCode"
                }
            })
        }
    }

    val peerListListener = PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers != peers) {
            peers.clear()
            peers.addAll(refreshedPeers)

            adapter.clear()
            adapter.addAll(peers.map { it.deviceName })
            adapter.notifyDataSetChanged()
        }

        if (peers.isEmpty()) {
            Toast.makeText(this@MainActivity, "No devices found", Toast.LENGTH_SHORT).show()
            return@PeerListListener
        }
    }

    override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(manager, channel, this)
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }
}
