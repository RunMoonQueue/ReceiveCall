package com.moon.receivecall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log


class MessageReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent?.extras
        val messages = parseMessage(bundle!!)
        if (messages!!.isNotEmpty()) {
            val sender = messages[0]?.originatingAddress
            Log.d(TAG, "sender: $sender")

            val contents = messages[0]?.messageBody.toString()
            Log.d(TAG, "contents:$contents")
            context?.startActivity(Intent(context, MainActivity::class.java).apply {
                Log.d(TAG, "startActivity MainActivity")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("sender", sender)
                putExtra("contents", contents)
            })
        }
    }

    private fun parseMessage(bundle: Bundle): Array<SmsMessage?>? {
        val objs = bundle["pdus"] as Array<Any>?
        val messages = arrayOfNulls<SmsMessage>(
            objs!!.size
        )
        for (i in objs.indices) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val format = bundle.getString("format")
                messages[i] = SmsMessage.createFromPdu(objs[i] as ByteArray, format)
            } else {
                messages[i] = SmsMessage.createFromPdu(objs[i] as ByteArray)
            }
        }
        return messages
    }

    companion object {
        const val TAG = "MessageReceiver"
    }
}