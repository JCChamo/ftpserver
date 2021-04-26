package com.example.ftpserver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.apache.commons.net.ftp.FTPClient
import java.net.SocketException

class MainActivity : AppCompatActivity(), View.OnClickListener, TextWatcher {

    private lateinit var server : EditText
    private lateinit var port : EditText
    private lateinit var user : EditText
    private lateinit var psswd : EditText
    private lateinit var defaultValuesButton: Button
    private lateinit var connectButton: Button

    private lateinit var ftp : FTPClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        server = findViewById(R.id.server)
        port = findViewById(R.id.port)
        user = findViewById(R.id.user)
        psswd = findViewById(R.id.psswd)
        defaultValuesButton = findViewById(R.id.defaultValuesButton)
        connectButton = findViewById(R.id.connectButton)

        defaultValuesButton.setOnClickListener(this)
        connectButton.setOnClickListener(this)

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
                connectFtp()
                Toast.makeText(applicationContext, "CONECTANDO", Toast.LENGTH_SHORT).show()
                getFiles()
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

    private fun connectFtp(){
        ftp = FTPClient()
        Thread(Runnable {
            try {
                ftp.connect("ftp://$server:$port")
                ftp.enterLocalPassiveMode()
                ftp.login("$user", "$psswd")
                Log.d("f", ftp.login("$user", "$psswd").toString())
            } catch (e : SocketException){
                e.printStackTrace()
            }
        }).start()
    }

    private fun getFiles(){
        val files = ftp.listFiles()
        files.forEach {
            Log.d(":::NAME", it.name)
        }
    }
}