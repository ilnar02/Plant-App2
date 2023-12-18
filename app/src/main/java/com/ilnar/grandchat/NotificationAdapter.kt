package com.ilnar.grandchat

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ilnar.grandchat.databinding.NotificationItemBinding

class NotificationAdapter (val listener: Listener): RecyclerView.Adapter<NotificationAdapter.NotiHolder>() {

    val notiList = ArrayList<Notifications>()

    class NotiHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = NotificationItemBinding.bind(item)

        fun bind(notifications: Notifications, listener: Listener) = with(binding){

            when (notifications.notifications_type) {
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
                else -> println("x не равно 1, 2 или 3")
            }

            type.text = notifications.notifications_type

            when (notifications.day_of_week) {
                1 -> {
                    text.text = "Воскресенье в " + notifications.time
                }
                2 -> {
                    text.text = "Понедельник в " + notifications.time
                }
                3 -> {
                    text.text = "Вторник в " + notifications.time
                }
                4 -> {
                    text.text = "Среда в " + notifications.time
                }
                5 -> {
                    text.text = "Четверг в " + notifications.time
                }
                6 -> {
                    text.text = "Пятница в " + notifications.time
                }
                7 -> {
                    text.text = "Суббота в " + notifications.time
                }

            }

            del.setOnClickListener{
                listener.onClickConf(notifications)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotiHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        return NotiHolder(view)
    }

    override fun onBindViewHolder(holder: NotiHolder, position: Int) {
        holder.bind(notiList[position], listener)
    }

    override fun getItemCount(): Int {
        return notiList.size
    }

    interface Listener{
        fun onClickConf(notifications: Notifications)
    }

    fun addNoti(notifications: Notifications){
        notiList.add(0, notifications)
        notifyDataSetChanged()
    }

    fun clearNoti(){
        notiList.clear()
        notifyDataSetChanged()
    }
}