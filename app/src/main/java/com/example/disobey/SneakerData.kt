package com.example.disobey

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Collections
import java.util.Random

data class SneakerDataStruc(val name:String="", val type:String="", val image:String="",  val coin:Int=0)

class SneakerData {
    var sneakerData = ArrayList<SneakerDataStruc>()
    var commonList = ArrayList<SneakerDataStruc>()
    var rareList = ArrayList<SneakerDataStruc>()
    var epicList = ArrayList<SneakerDataStruc>()
    var legendaryList = ArrayList<SneakerDataStruc>()
    var sneakerList = ArrayList<SneakerDataStruc>()
    val db = FirebaseFirestore.getInstance()
    fun firestoreRetrieve(){
        db.collection("sneakers").document("common").get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val document = task.getResult()
                    if (document.exists()) {
                        commonList.addAll(document.toObject(SneakerData::class.java)!!.sneakerData)
                        println(""+commonList)
                    }
                }
            }
        db.collection("sneakers").document("rare").get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val document = task.getResult()
                    if (document.exists()) {
                        rareList.addAll(document.toObject(SneakerData::class.java)!!.sneakerData)
                        println(""+rareList)
                    }
                }
            }
        db.collection("sneakers").document("epic").get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val document = task.getResult()
                    if (document.exists()) {
                        epicList.addAll(document.toObject(SneakerData::class.java)!!.sneakerData)
                        println(""+epicList)
                    }
                }
            }
        db.collection("sneakers").document("legendary").get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val document = task.getResult()
                    if (document.exists()) {
                        legendaryList.addAll(document.toObject(SneakerData::class.java)!!.sneakerData)
                        println(""+legendaryList)
                    }
                }
            }
    }
    fun populateMarkers():ArrayList<SneakerDataStruc>{
        firestoreRetrieve()
        createSneakerList(commonList,37)
        createSneakerList(rareList,8)
        createSneakerList(epicList,4)
        createSneakerList(legendaryList,1)
        return sneakerList
    }
    fun createSneakerList(inputList:ArrayList<SneakerDataStruc>,count:Int){
        sneakerList.addAll(List(count){inputList[Random().nextInt(inputList.size)]})
    }
}
