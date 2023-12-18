package com.ilnar.grandchat

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ilnar.grandchat.databinding.LogItemBinding
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.text.SimpleDateFormat

class LogAdapter (val listener: Listener): RecyclerView.Adapter<LogAdapter.LogHolder>() {

    val newsList = ArrayList<Logs>()

    class LogHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = LogItemBinding.bind(item)

        fun bind(logs: Logs, listener: Listener) = with(binding){


            when (logs.record_type) {
                "Добавлено" -> {
                    im.setImageResource(R.drawable.ic_desc)
                    im.setBackgroundResource(R.drawable.create_button)
                }
                "Полив" -> {
                    im.setImageResource(R.drawable.ic_watering)
                    im.setBackgroundResource(R.drawable.water_button)
                }
                "Удобрение" -> {
                    im.setImageResource(R.drawable.ic_top_dressing)
                    im.setBackgroundResource(R.drawable.top_dressing_button)
                }
                "Опрыскивание" -> {
                    im.setImageResource(R.drawable.ic_characteristics)
                    im.setBackgroundResource(R.drawable.oprisk_button)
                }
                "Фото" -> {
                    val transformation = CropCircleTransformation()
                    val imageUrl = logs.image
                    Picasso.get()
                        .load(imageUrl)
                        .transform(transformation)
                        .into(binding.im)
                }
                else -> println("x не равно 1, 2 или 3")
            }

            val formatter = SimpleDateFormat("dd/MM/yyyy")
            val formattedDate = formatter.format(logs.date)
            date.text = formattedDate
            text.text = logs.text


            del.setOnClickListener{
                listener.onClickConf(logs)
            }

            im.setOnClickListener{
                listener.onClickImage(logs)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false)
        return LogHolder(view)
    }

    override fun onBindViewHolder(holder: LogHolder, position: Int) {
        holder.bind(newsList[position], listener)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    interface Listener{
        fun onClickConf(logs: Logs)
        fun onClickImage(logs: Logs)
    }

    fun addLog(logs: Logs){
        newsList.add(logs)
        notifyDataSetChanged()
    }

    fun clearLog(){
        newsList.clear()
        notifyDataSetChanged()
    }
}