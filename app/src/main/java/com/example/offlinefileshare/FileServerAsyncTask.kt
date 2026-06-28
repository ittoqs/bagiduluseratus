package com.example.offlinefileshare

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.ServerSocket

class FileServerAsyncTask(
    private val context: Context
) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void): String? {
        try {
            val serverSocket = ServerSocket(8988)
            Log.d("FileServer", "Server: Socket opened")
            val client = serverSocket.accept()
            Log.d("FileServer", "Server: connection done")

            val f = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "shared_file-${System.currentTimeMillis()}.bin")

            val dirs = File(f.parent)
            if (!dirs.exists()) {
                dirs.mkdirs()
            }
            f.createNewFile()

            val inputstream: InputStream = client.getInputStream()
            copyFile(inputstream, FileOutputStream(f))
            serverSocket.close()
            return f.absolutePath
        } catch (e: IOException) {
            Log.e("FileServer", e.message ?: "IOException")
            return null
        }
    }

    override fun onPostExecute(result: String?) {
        if (result != null) {
            Toast.makeText(context, "File copied to - $result", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyFile(inputStream: InputStream, out: FileOutputStream): Boolean {
        val buf = ByteArray(1024)
        var len: Int
        try {
            while (inputStream.read(buf).also { len = it } != -1) {
                out.write(buf, 0, len)
            }
            out.close()
            inputStream.close()
        } catch (e: IOException) {
            Log.d("FileServer", e.toString())
            return false
        }
        return true
    }
}
