package com.example.ftpserver.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ftpserver.R
import org.apache.commons.net.ftp.FTPFile

class FileAdapter (var listener: OnItemClickListener, private var filesList: Array<FTPFile>, var filesMap: HashMap<String, String>) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    inner class ViewHolder (view : View) : RecyclerView.ViewHolder(view), View.OnClickListener{

        val fileName : TextView = view.findViewById(R.id.name)
        val fileSize : TextView = view.findViewById(R.id.size)
        val fileIcon : ImageView = view.findViewById(R.id.icon)


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemClick(position)
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.file, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filesList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val size : String = if (filesList[position].size / 1000 < 1)
            "1"
        else
            (filesList[position].size / 1000).toString()

        holder.fileSize.text = "$size KB"
        //size.substringBefore(size.length - 3) + .substring(size.length - 3)
        filesMap.forEach {
            if (filesList[position].name.contains(it.key)){
                Log.d(":::", it.value)
                holder.fileName.text = codifySpanish(it.value)
                when(it.key){
                    "txt" -> holder.fileIcon.setImageResource(R.drawable.txt)
                    "mp4" -> holder.fileIcon.setImageResource(R.drawable.mp4)
                    "apk" -> holder.fileIcon.setImageResource(R.drawable.apk)
                    "sql" -> holder.fileIcon.setImageResource(R.drawable.sql)
                    "png" -> holder.fileIcon.setImageResource(R.drawable.png)
                    "jpg" -> holder.fileIcon.setImageResource(R.drawable.jpg)
                    "pdf" -> holder.fileIcon.setImageResource(R.drawable.pdf)
                    "csv" -> holder.fileIcon.setImageResource(R.drawable.csv)
                }
            }
        }
    }

    fun setData() {
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun codifySpanish(string: String): String {
        val text = string.toByteArray(Charsets.ISO_8859_1)
        return String(text, Charsets.UTF_8)
    }

}