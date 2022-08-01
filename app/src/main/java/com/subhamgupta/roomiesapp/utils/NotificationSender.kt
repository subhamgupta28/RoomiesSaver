package com.subhamgupta.roomiesapp.utils

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.subhamgupta.roomiesapp.utils.Constant.Companion.SERVER_KEY
import org.json.JSONObject

class NotificationSender {
    fun sendNotification(notification: JSONObject, context: Context) {
        val requestQueue = Volley.newRequestQueue(context)
        val request = object : JsonObjectRequest(
            Method.POST, Constant.BASE_URL, notification,
            { _ ->
            },
            Response.ErrorListener { error ->
                Log.e("Notification Error", error.message.toString())

            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "key=${SERVER_KEY}"
                headers["Content-Type"] = Constant.CONTENT_TYPE
                return headers
            }
        }
        requestQueue.add(request)
    }

}