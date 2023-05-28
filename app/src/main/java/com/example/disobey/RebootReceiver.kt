package com.example.disobey

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            println("Hello, Msg from Disobey, Device Rebooted")
            var pref=context?.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val myEdit = pref?.edit()
            myEdit?.putInt("initialSteps",0)
            myEdit?.apply()
        }
    }
}