package com.example.disobey

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class Leaderboard : AppCompatActivity() {
    //    lateinit var name:TextView
    //    lateinit var coin:TextView
    //    lateinit var step:TextView
    lateinit var fsrecyclerview:RecyclerView
    lateinit var pref: SharedPreferences
    private var disobeySteps = 0
    var userList = ArrayList<LeaderboardsUserData>()
    var fsadapter=LeaderboardsFirestoreAdapter(userList)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        fsrecyclerview=findViewById<RecyclerView>(R.id.leaderboardsRecycler)
        fsrecyclerview.layoutManager = LinearLayoutManager(this)
        val user = FirebaseAuth.getInstance().currentUser?.displayName
        pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        disobeySteps= pref.getInt("disobeySteps",0)
        findViewById<TextView>(R.id.name).text= user
        findViewById<TextView>(R.id.coin).text= (disobeySteps/100).toString()
        findViewById<TextView?>(R.id.steps).text=disobeySteps.toString()
//        step.text= disobeySteps.toString()
//        coin.text= (disobeySteps/100).toString()
//        name.text= user

        val db = FirebaseFirestore.getInstance()
        var leaderBoardsList=db.collection("leaderboards").orderBy("disobeySteps", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                if (!queryDocumentSnapshots.isEmpty) {
                    val list = queryDocumentSnapshots.documents
                    for (d in list) {
                        var v = d.toObject(LeaderboardsUserData::class.java)
                        if (v != null) {
                            userList.add(v)
                            fsadapter.notifyDataSetChanged()
                        }

                        Log.i("TAGG",""+v?.name+" "+v?.disobeySteps+" "+d.id)
                    }
//                    courseRVAdapter.notifyDataSetChanged()
                } else {
                    // if the snapshot is empty we are displaying a toast message.
                    Toast.makeText(
                        this@Leaderboard,
                        "No data found in Database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { // if we do not get any data or any error we are displaying
                // a toast message that we do not get any data
                Toast.makeText(this@Leaderboard, "Fail to get the data.", Toast.LENGTH_SHORT)
                    .show()
            }

        fsrecyclerview.adapter=fsadapter
        Log.i("TAGG",""+leaderBoardsList)
        println(leaderBoardsList)
    }
}