package com.example.disobey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

public class LeaderboardsFirestoreAdapter(private val fslist: ArrayList<LeaderboardsUserData>):RecyclerView.Adapter<LeaderboardsFirestoreAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.leaderboardcard,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.rank.text= (position+1).toString()
        val userdata = fslist[position]
        holder.disobeySteps.text= userdata.disobeySteps.toString()
        holder.name.text=userdata.name
    }

    override fun getItemCount(): Int {
        return fslist.size
    }

    public class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val disobeySteps: TextView = itemView.findViewById(R.id.steps)
        val name: TextView = itemView.findViewById(R.id.user)
        val rank:TextView=itemView.findViewById(R.id.rank)
    }
}