package com.ilnar.grandchat

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ilnar.grandchat.databinding.FavoriteItemBinding
import com.ilnar.grandchat.databinding.NewsItemBinding
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.text.SimpleDateFormat

class FavoriteAdapter(val listener: Listener): RecyclerView.Adapter<FavoriteAdapter.FavoriteHolder>() {

    val newsList = ArrayList<FavoritePlants>()

    class FavoriteHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = FavoriteItemBinding.bind(item)

        fun bind(favorite: FavoritePlants, listener: Listener) = with(binding){

            val transformation = CropCircleTransformation()
            val imageUrl = favorite.image
            Picasso.get()
                .load(imageUrl)
                .transform(transformation)
                .into(binding.im)

            plantName.text = favorite.input_name

            itemView.setOnClickListener{
                listener.onClick(favorite)
            }

            del.setOnClickListener{
                listener.onClickConf(favorite)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorite_item, parent, false)
        return FavoriteHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteHolder, position: Int) {
        holder.bind(newsList[position], listener)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    interface Listener{
        fun onClick(favorite: FavoritePlants)
        fun onClickConf(favorite: FavoritePlants)
    }

    fun addFavorite(favorite: FavoritePlants){
        newsList.add(favorite)
        notifyDataSetChanged()
    }

    fun clearFavorite(){
        newsList.clear()
        notifyDataSetChanged()
    }


}