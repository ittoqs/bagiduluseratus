package com.example.offlinefileshare

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class FileTransferService : IntentService("FileTransferService") {

    override fun onHandleIntent(intent: Intent?) {
        val context = applicationContext
        if (intent?.action == ACTION_SEND_FILE) {
            val fileUri = intent.extras?.getString(EXTRAS_FILE_PATH)
            val host = intent.extras?.getString(EXTRAS_GROUP_OWNER_ADDRESS)
            val port = intent.extras?.getInt(EXTRAS_GROUP_OWNER_PORT)

            val socket = Socket()

            try {
                Log.d(TAG, "Opening client socket - ")
                socket.bind(null)
                socket.connect(InetSocketAddress(host, port ?: 8988), SOCKET_TIMEOUT)

                Log.d(TAG, "Client socket - " + socket.isConnected)
                val stream: OutputStream = socket.getOutputStream()
                val cr = context.contentResolver
                var inputStream: InputStream? = null
                try {
                    inputStream = cr.openInputStream(Uri.parse(fileUri))
                } catch (e: FileNotFoundException) {
                    Log.d(TAG, e.toString())
                }

                if (inputStream != null) {
                    copyFile(inputStream, stream)
                    Log.d(TAG, "Client: Data written")
                }
            } catch (e: IOException) {
                Log.e(TAG, e.message ?: "IOException")
            } finally {
                if (socket.isConnected) {
                    try {
                        socket.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun copyFile(inputStream: InputStream, out: OutputStream): Boolean {
        val buf = ByteArray(1024)
        var len: Int
        try {
            while (inputStream.read(buf).also { len = it } != -1) {
                out.write(buf, 0, len)
            }
            out.close()
            inputStream.close()
        } catch (e: IOException) {
            Log.d(TAG, e.toString())
            return false
        }
        return true
    }

    companion object {
        private const val SOCKET_TIMEOUT = 5000
        const val ACTION_SEND_FILE = "com.example.offlinefileshare.SEND_FILE"
        const val EXTRAS_FILE_PATH = "file_url"
        const val EXTRAS_GROUP_OWNER_ADDRESS = "go_host"
        const val EXTRAS_GROUP_OWNER_PORT = "go_port"
        const val TAG = "FileTransferService"

        fun startActionSendFile(context: Context, fileUri: String, host: String, port: Int) {
            val intent = Intent(context, FileTransferService::class.java).apply {
                action = ACTION_SEND_FILE
                putExtra(EXTRAS_FILE_PATH, fileUri)
                putExtra(EXTRAS_GROUP_OWNER_ADDRESS, host)
                putExtra(EXTRAS_GROUP_OWNER_PORT, port)
            }
            context.startService(intent)
        }
    }
}
