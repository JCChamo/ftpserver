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
import java.io.*


class Files : AppCompatActivity(), FileAdapter.OnItemClickListener{

    lateinit var recyclerView : RecyclerView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mProgressBar2: ProgressBar

    private lateinit var fileAdapter : FileAdapter
    private lateinit var listener: FileAdapter.OnItemClickListener
    private lateinit var context: Context
    private lateinit var fileName : String

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.files)

        mProgressBar = findViewById(R.id.progressbar)
        mProgressBar2 = findViewById(R.id.progressbar2)
        recyclerView = findViewById(R.id.recycler)

        context = applicationContext
        listener = this

        mProgressBar.visibility = View.GONE
        mProgressBar2.visibility = View.GONE
        progressBarAction()
        setAdapter()
    }
    override fun onItemClick(position: Int) {
        verifyStoragePermissions(this)
        Log.d(":::FILE PERMISSIONS", MainActivity.filesList[position].toString().split(" 1")[0])
        fileName = MainActivity.filesList[position].name.toString()
        downloadFile()
    }
    private fun downloadFile() {
        Thread(Runnable {
            MainActivity.thread.join()
            val ftpClient = MainActivity.myThread.getFtpClient()
            ftpClient.enterLocalPassiveMode()
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            val remoteFile = fileName
//            val absolutePath = "/storage/emulated/0/Download"
            val absolutePath = getExternalFilesDir(null).toString()
            val downloadFile = File("$absolutePath/$fileName")
            Log.d(":::", "$absolutePath/$fileName")
            if (!downloadFile.exists()){
                downloadFile.createNewFile()
                Log.d(":::", "El archivo no existía, se acaba de crear")
            }
            Log.d(":::FILE IS FILE", downloadFile.isFile.toString())
            Log.d(":::FILE EXISTS", downloadFile.exists().toString())
            try {
                val inputStream = ftpClient.retrieveFileStream(remoteFile)
                val fileOutputStream = FileOutputStream(downloadFile, false)
                val bufferedOutputStream = BufferedOutputStream(fileOutputStream, 1024)

                inputStream.copyTo(bufferedOutputStream)
                ftpClient.completePendingCommand()

                val fileSize = downloadFile.length()
                Log.d(":::", fileSize.toString())

                val success = ftpClient.retrieveFile(remoteFile, fileOutputStream)
                if (success)
                    Log.d(":::", "Archivo descargado exitosamente")

                inputStream.close()
                fileOutputStream.close()
                bufferedOutputStream.close()

            } catch (e : Exception) {
                e.printStackTrace()
            }
        }).start()
    }

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

    private fun verifyStoragePermissions(activity: Activity?) {
        val permission = ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    private fun setProgress(maxValue: Int, bytesCopied : Long){
        mProgressBar2.max = maxValue
        mProgressBar2.visibility = View.VISIBLE
        mProgressBar2.progress = bytesCopied.toInt()
    }
}