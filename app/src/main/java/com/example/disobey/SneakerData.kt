package com.example.disobey

data class SneakerDataStruc(val name:String, val coin:Int,val image:String, val type:String)

class SneakerData {
    var sneakerList = ArrayList<SneakerDataStruc>()
}
