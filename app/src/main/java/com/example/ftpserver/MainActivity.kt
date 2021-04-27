package com.example.ftpserver

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ftpserver.adapters.FileAdapter
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.net.SocketException

class MainActivity : AppCompatActivity(), View.OnClickListener, TextWatcher, FileAdapter.OnItemClickListener {

    private lateinit var server : EditText
    private lateinit var port : EditText
    private lateinit var user : EditText
    private lateinit var psswd : EditText
    private lateinit var defaultValuesButton: Button
    private lateinit var connectButton: Button
    private lateinit var displayFilesButton: Button
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    companion object {
        var filesList = arrayOf<FTPFile>()
        var filesMap = hashMapOf<String, String>()
        lateinit var thread : Thread
        lateinit var myThread : MyThread
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        server = findViewById(R.id.server)
        port = findViewById(R.id.port)
        user = findViewById(R.id.user)
        psswd = findViewById(R.id.psswd)
        defaultValuesButton = findViewById(R.id.defaultValuesButton)
        connectButton = findViewById(R.id.connectButton)
        displayFilesButton = findViewById(R.id.displayFilesButton)

        verifyStoragePermissions(this)


        defaultValuesButton.setOnClickListener(this)
        connectButton.setOnClickListener(this)
        displayFilesButton.setOnClickListener(this)

        server.addTextChangedListener(this)
        port.addTextChangedListener(this)
        user.addTextChangedListener(this)
        psswd.addTextChangedListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.defaultValuesButton -> {
                server.setText("172.20.255.12")
                port.setText("21")
                user.setText("paco")
                psswd.setText("jones")
            }
            R.id.connectButton -> {
                connectFtpAndListFiles()
                Toast.makeText(applicationContext, "CONECTADO", Toast.LENGTH_SHORT).show()
                Handler().postDelayed({
                    displayFilesButton.visibility = View.VISIBLE
                }, 1500)
            }

            R.id.displayFilesButton -> {
                val intent = Intent(this, Files::class.java)
                startActivity(intent)
            }
        }
    }

    override fun afterTextChanged(p0: Editable?) {
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (server.text.isNotEmpty() && port.text.isNotEmpty() && user.text.isNotEmpty() && psswd.text.isNotEmpty())
            connectButton.visibility = View.VISIBLE
    }

    private fun connectFtpAndListFiles(){
        myThread = MyThread()
        thread = Thread(myThread)
        thread.start()
    }

    inner class MyThread : Runnable {
        private val ftp = FTPClient()

        override fun run() {
            try {
                ftp.connect(server.text.toString(), Integer.parseInt(port.text.toString()))
                ftp.login("${user.text}", "${psswd.text}")
                filesList = ftp.listFiles()
                for (i in filesList.indices)
                    filesMap[filesList[i].name.split(".")[1]] = filesList[i].name.split(".")[0]
            } catch (e : SocketException){
                e.printStackTrace()
            }
        }

        fun getFtpClient() : FTPClient {
            return ftp
        }
    }

//    private fun connectFtpAndListFiles(){
//        val ftp = FTPClient()
//        thread = Thread(Runnable {
//            try {
//                ftp.connect(server.text.toString(), Integer.parseInt(port.text.toString()))
//                    ftp.login("${user.text}", "${psswd.text}")
//                    filesList = ftp.listFiles()
//                    for (i in filesList.indices)
//                        filesMap[filesList[i].name.split(".")[1]] = filesList[i].name.split(".")[0]
//                } catch (e : SocketException){
//                    e.printStackTrace()
//                }
//        })
//        thread.start()
//    }

    override fun onItemClick(position: Int) {
        TODO("Not yet implemented")
    }

    fun verifyStoragePermissions(activity: Activity?) {
        val permission = ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }
    }
}