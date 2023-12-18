package com.ilnar.grandchat

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ilnar.grandchat.databinding.NewsItemBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat

class NewsAdapter(val listener: Listener): RecyclerView.Adapter<NewsAdapter.NewsHolder>() {

    val newsList = ArrayList<News>()
    class NewsHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = NewsItemBinding.bind(item)

        fun bind(news: News, listener: Listener) = with(binding){

            val imageUrl = news.image
// Загрузите изображение с помощью Picasso и отобразите его в ImageView
            Picasso.get()
                .load(imageUrl)
                .into(im)

            author.text = news.author

            val formatter = SimpleDateFormat("MM/dd HH:mm")
            val formattedDate = formatter.format(news.date)
            date.text = formattedDate

            newsText.text = news.text

            val like = "❤ " + news.likes.toString()
            likes.text = like

            likes.setOnClickListener{
                listener.onClick(news)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_item, parent, false)
        return NewsHolder(view)
    }

    override fun onBindViewHolder(holder: NewsHolder, position: Int) {
        holder.bind(newsList[position], listener)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    interface Listener{
        fun onClick(news: News)
    }

    fun addNews(news: News){
        newsList.add(news)
        notifyDataSetChanged()
    }

    fun clearNews(){
        newsList.clear()
        notifyDataSetChanged()
    }

    fun likesUpdate(dockId: String, likes: Long?){
        for(i in newsList){
            if(dockId == i.dockId) i.likes = likes
        }
        notifyDataSetChanged()
    }

}