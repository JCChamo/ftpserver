package com.example.ftpserver


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ftpserver.adapters.FileAdapter
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.*
import kotlin.properties.Delegates


class Files : AppCompatActivity(), FileAdapter.OnItemClickListener, View.OnClickListener{

    lateinit var recyclerView : RecyclerView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mProgressBar2: ProgressBar
    private lateinit var textPercentage: TextView
    private lateinit var pauseButton : Button

    private lateinit var fileAdapter : FileAdapter
    private lateinit var listener: FileAdapter.OnItemClickListener
    private lateinit var context: Context
    private lateinit var file : FTPFile
    private lateinit var fileName : String
    private var switch : Boolean = false
    private var exitWhile : Boolean = false
    private var visibility : Boolean = false
    private lateinit var myThread2 : MyThread2

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
        textPercentage = findViewById(R.id.textPercentage)
        pauseButton = findViewById(R.id.pauseButton)

        context = applicationContext
        listener = this

        pauseButton.setOnClickListener(this)

        mProgressBar.visibility = View.GONE
        mProgressBar2.visibility = View.GONE
        progressBarAction()
        setAdapter()
    }
    override fun onItemClick(position: Int) {
        verifyStoragePermissions(this)
        Log.d(":::FILE PERMISSIONS", MainActivity.filesList[position].toString().split(" 1")[0])
        file = MainActivity.filesList[position]
        fileName = file.name.toString()
//        downloadFile()
        myThread2 = MyThread2()
        Thread(myThread2).start()
    }

    inner class MyThread2 : Runnable {
        private lateinit var inputStream : InputStream
        private lateinit var bufferedOutputStream : BufferedOutputStream
        private var fileSize : Long = 0L
        private var valueToStartFrom : Long = 0L
        private lateinit var downloadFile : File

        override fun run() {
            Log.d(":::PATATA", "PATATA")
            val ftpClient = MainActivity.myThread.getFtpClient()
            ftpClient.enterLocalPassiveMode()
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            val remoteFile = fileName
//            val absolutePath = "/storage/emulated/0/Download"
            val absolutePath = getExternalFilesDir(null).toString()
            downloadFile = File("$absolutePath/$fileName")
            Log.d(":::", "$absolutePath/$fileName")
            fileSize = file.size
            Log.d(":::", fileSize.toString())
            valueToStartFrom = 0L

            if (!downloadFile.exists()){
                downloadFile.createNewFile()
                Log.d(":::", "El archivo no existía, se acaba de crear")
            }

            Log.d(":::FILE IS FILE", downloadFile.isFile.toString())
            Log.d(":::FILE EXISTS", downloadFile.exists().toString())
            try {
                inputStream = ftpClient.retrieveFileStream(remoteFile)
                val fileOutputStream = FileOutputStream(downloadFile, true)
                bufferedOutputStream = BufferedOutputStream(fileOutputStream, 1024)

                inputStream.inputToFile(bufferedOutputStream, fileSize, valueToStartFrom)
                ftpClient.completePendingCommand()

                val success = ftpClient.retrieveFile(remoteFile, fileOutputStream)
                if (success)
                    Log.d(":::", "Archivo descargado exitosamente")

                inputStream.close()
                fileOutputStream.close()
                bufferedOutputStream.close()

            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
        fun getInputStream() : InputStream{
            return inputStream
        }

        fun getBufferedOutputStream() : BufferedOutputStream{
            return bufferedOutputStream
        }

        fun getFileSize() : Long{
            return fileSize
        }

        fun getDownloadFile() : File{
            return downloadFile
        }

        fun setValueFromStart(valueToStartFrom: Long){
            this.valueToStartFrom = valueToStartFrom
        }

    }
    /*private fun downloadFile() {
        Thread(Runnable {
            MainActivity.thread.join()
            val ftpClient = MainActivity.myThread.getFtpClient()
            ftpClient.enterLocalPassiveMode()
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            val remoteFile = fileName
            val absolutePath = "/storage/emulated/0/Download"
            val absolutePath = getExternalFilesDir(null).toString()
            val downloadFile = File("$absolutePath/$fileName")
            Log.d(":::", "$absolutePath/$fileName")
            val fileSize = file.size
            Log.d(":::", fileSize.toString())
            var valueToStartFrom = 0L
            if (!downloadFile.exists()){
                downloadFile.createNewFile()
                Log.d(":::", "El archivo no existía, se acaba de crear")
            } else {
                valueToStartFrom = downloadFile.length()
            }
            Log.d(":::FILE IS FILE", downloadFile.isFile.toString())
            Log.d(":::FILE EXISTS", downloadFile.exists().toString())
            try {
                val inputStream = ftpClient.retrieveFileStream(remoteFile)
                val fileOutputStream = FileOutputStream(downloadFile, false)
                val bufferedOutputStream = BufferedOutputStream(fileOutputStream, 1024)

                inputStream.inputToFile(bufferedOutputStream, fileSize, valueToStartFrom)
                ftpClient.completePendingCommand()

                mProgressBar2.max = fileSize.toInt()

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
    }*/

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

    private fun InputStream.inputToFile(out: OutputStream, maxValue : Long, valueToStartFrom : Long = 0, bufferSize: Int = DEFAULT_BUFFER_SIZE){
        var bytesCopied: Long = valueToStartFrom
        Log.d(":::VALUE TO START FROM", valueToStartFrom.toString())
        val buffer = ByteArray(bufferSize)
        var bytes = read(buffer)
        val y = maxValue + valueToStartFrom
        runOnUiThread {
            mProgressBar2.max = maxValue.toInt()
            if(!visibility){
                mProgressBar2.visibility = View.VISIBLE
                textPercentage.visibility = View.VISIBLE
                pauseButton.visibility = View.VISIBLE
            }
        }
        while (bytes >= 0 && !exitWhile) {
            out.write(buffer, 0, bytes)
            bytesCopied += bytes
            bytes = read(buffer)
            runOnUiThread {
                mProgressBar2.progress = bytesCopied.toInt()
                textPercentage.text = String.format("%.1f%%", (bytesCopied.toDouble() / y * 100))
            }
        }
        Log.d(":::BYTES COPIED", bytesCopied.toString())
        myThread2.setValueFromStart(bytesCopied)
        runOnUiThread {
            if (bytesCopied == maxValue) {
                mProgressBar2.visibility = View.INVISIBLE
                textPercentage.visibility = View.INVISIBLE
                pauseButton.visibility = View.INVISIBLE
            }
        }
    }

    override fun onClick(p0: View?) {
        if (!switch) {
            switch = true
            exitWhile = true
            pauseButton.text = "REANUD DESCARGA"
            Log.d(":::", "DESCARGA PAUSADA")
        } else {
            switch = false
            exitWhile = false
            pauseButton.text = "PAUSAR DESCARGA"
            Log.d(":::", "DESCARGA REANUDADA")
            Thread(Runnable {
                Log.d(":::", "DENTRO DEL HILO")
                Log.d(":::", myThread2.getFileSize().toString())
                Log.d(":::", myThread2.getDownloadFile().length().toString())
                myThread2.getInputStream().inputToFile(myThread2.getBufferedOutputStream(), myThread2.getFileSize(), myThread2.getDownloadFile().length())
            }).start()
        }
    }
}