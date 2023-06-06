package com.example.disobey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

public class BackpackFirestoreAdapter(private val fslist: ArrayList<SneakerDataStruc>):RecyclerView.Adapter<BackpackFirestoreAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BackpackFirestoreAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.backpackcard,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: BackpackFirestoreAdapter.ViewHolder, position: Int) {
        val str=fslist[position].name
        val nameList=str.split(" ")
        holder.name1.text=nameList[0]
        if(nameList.size>1){
            holder.name2.text=nameList[1]
        }
        holder.type.text=fslist[position].type
        val picasso = Picasso.get()
        picasso.load(fslist[position].image)
            .into(holder.image)
        holder.coin.text="x "+fslist[position].coin.toString()
    }

    override fun getItemCount(): Int {
        return fslist.size
    }

    public class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val name1: TextView = itemView.findViewById(R.id.sneakerName1)
        val name2: TextView = itemView.findViewById(R.id.sneakerName2)
        val type: TextView = itemView.findViewById(R.id.sneakerType)
        val image: ImageView= itemView.findViewById(R.id.sneakerImage)
        val coin: TextView = itemView.findViewById(R.id.coin)
        val card: CardView= itemView.findViewById(R.id.card)
    }
}