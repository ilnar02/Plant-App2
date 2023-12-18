package com.ilnar.grandchat

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ilnar.grandchat.databinding.PlantItemBinding
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class PlantAdapter(val listener: Listener): RecyclerView.Adapter<PlantAdapter.PlantHolder>() {
    val plantList = ArrayList<Plant>()
    class PlantHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = PlantItemBinding.bind(item)

        fun bind(plant: Plant, listener: Listener) = with(binding){
            val transformation = RoundedCornersTransformation(26, 0, RoundedCornersTransformation.CornerType.ALL)
            val imageUrl = plant.image
            Picasso.get()
                .load(imageUrl)
                .transform(transformation)
                .into(im)

            tvTitle.text = plant.ruName
            itemView.setOnClickListener{
                listener.onClick(plant)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plant_item, parent, false)
        return PlantHolder(view)
    }

    override fun onBindViewHolder(holder: PlantHolder, position: Int) {
        holder.bind(plantList[position], listener)

    }

    override fun getItemCount(): Int {
        return plantList.size
    }

    fun addPlant(plant: Plant){
        plantList.add(plant)
        notifyDataSetChanged()
    }

    fun clearPlant(){
        plantList.clear()
        notifyDataSetChanged()
    }

    interface Listener{
        fun onClick(plant: Plant)
    }

}