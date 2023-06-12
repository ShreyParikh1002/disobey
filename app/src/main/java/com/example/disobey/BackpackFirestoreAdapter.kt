package com.example.disobey

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso

public class BackpackFirestoreAdapter(private val fslist: ArrayList<SneakerDataStruc>,private val fsmap: HashMap<String, Int>, private val fstype:Int):RecyclerView.Adapter<BackpackFirestoreAdapter.ViewHolder>() {
    val sneakerCountMap = hashMapOf<String, Int>()
    lateinit var pref: SharedPreferences
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BackpackFirestoreAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.backpackcard,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: BackpackFirestoreAdapter.ViewHolder, position: Int) {
        if(fstype==1){
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
            holder.coin.text="x "+fsmap[fslist[position].name]
            holder.tryonButton.setOnClickListener{
                val intent = Intent(holder.itemView.context, snapCam::class.java)
                intent.putExtra("Type","1")
                holder.itemView.context.startActivity(intent)
            }


        }
        if(fstype==2){
            holder.coin.text="Try-On"
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
            holder.type.visibility=View.GONE
            holder.tryonButton.setOnClickListener{
                val intent = Intent(holder.itemView.context, snapCam::class.java)
                intent.putExtra("Type","1")
                intent.putExtra("lens",fslist[position].type)
                holder.itemView.context.startActivity(intent)
            }
        }
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
        val tryonButton: ImageView= itemView.findViewById(R.id.tryonButton)
    }
}