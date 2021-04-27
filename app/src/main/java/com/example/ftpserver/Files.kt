package com.example.ftpserver


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ftpserver.adapters.FileAdapter
import org.apache.commons.net.ftp.FTP
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


class Files : AppCompatActivity(), FileAdapter.OnItemClickListener{

    lateinit var recyclerView : RecyclerView
    private lateinit var mProgressBar: ProgressBar

    private lateinit var fileAdapter : FileAdapter
    private lateinit var listener: FileAdapter.OnItemClickListener
    private lateinit var context: Context
    private lateinit var fileName : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.files)

        mProgressBar = findViewById(R.id.progressbar)
        recyclerView = findViewById(R.id.recycler)

        context = applicationContext
        listener = this

        mProgressBar.visibility = View.GONE
        progressBarAction()
        setAdapter()
    }
    override fun onItemClick(position: Int) {
        Log.d(":::PERMISSIONS", MainActivity.filesList[position].toString().split(" 1")[0])
        fileName = MainActivity.filesList[position].name.toString()
        downloadFile()
    }

    private fun downloadFile() {
        Thread(Runnable {
            MainActivity.thread.join()
            val ftpClient = MainActivity.myThread.getFtpClient()
            ftpClient.enterLocalPassiveMode()
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            val remoteFile = "/$fileName"
            val downloadFile = File("/storage/emulated/0/Download/$fileName")
            if (!downloadFile.exists())
                downloadFile.mkdir()
            try {
                val inputStream = ftpClient.retrieveFileStream(remoteFile)
                val fileOutputStream = FileOutputStream(downloadFile)
                val bufferedOutputStream = BufferedOutputStream(fileOutputStream, 1024)
                val data = ByteArray(1024)
                val x = 0
                while ((inputStream.read(data, 0, 1024)) >= 0)
                    bufferedOutputStream.write(data, 0, x)
                val success = ftpClient.retrieveFile(remoteFile, fileOutputStream)
                inputStream.close()
                fileOutputStream.close()
                bufferedOutputStream.close()
                if (success) {
                    Log.d(":::", "Archivo descargado exitosamente")
                    ftpClient.completePendingCommand()
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }

//            try {
//                val outputStream = BufferedOutputStream(FileOutputStream(downloadFile))
//                val success = ftpClient.retrieveFile(remoteFile, outputStream)
//                outputStream.close()
//                if (success) {
//                    Log.d(":::", "Archivo descargado exitosamente")
//                    ftpClient.completePendingCommand()
//                }
//            } catch (e : Exception) {
//                e.printStackTrace()
//            }
        }).start()
    }

//    private fun downloadFile(position: Int) {
//        Thread(Runnable {
//            val ftpClient = FTPClient()
//            ftpClient.connect("172.20.255.12", 21)
//            ftpClient.login("paco", "jones")
//            val filesList = ftpClient.listFiles()
//            ftpClient.enterLocalPassiveMode()
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
//            fileName = filesList[position].name.toString()
//            val remoteFile = "/$fileName"
//            val downloadFile = File("/storage/emulated/0/Download/$fileName")
//            val outputStream = BufferedOutputStream(FileOutputStream(downloadFile))
//            val success = ftpClient.retrieveFile(remoteFile, outputStream)
//            outputStream.close()
//            if (success) {
//                Log.d(":::", "Archivo descargado exitosamente")
//                ftpClient.completePendingCommand()
//            }
//        })
//    }

    private fun progressBarAction(){
        mProgressBar.visibility = View.VISIBLE
        Handler().postDelayed({
            mProgressBar.visibility = View.GONE
        }, 1000)
    }

    private fun setAdapter() {
        Log.d(":::Files", MainActivity.filesMap.size.toString())
        fileAdapter = FileAdapter(this, MainActivity.filesList, MainActivity.filesMap)
        fileAdapter.setData()

        runOnUiThread {
            recyclerView.adapter = fileAdapter
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = GridLayoutManager(context, 2)
            fileAdapter.notifyDataSetChanged()
        }
    }
}