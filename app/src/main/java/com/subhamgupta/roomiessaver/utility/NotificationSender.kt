package com.subhamgupta.roomiessaver.utility

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.subhamgupta.roomiessaver.Contenst
import org.json.JSONObject

class NotificationSender {
    fun sendNotification(notification: JSONObject, context: Context) {
        val requestQueue = Volley.newRequestQueue(context)
        val request = object : JsonObjectRequest(
            Method.POST, Contenst.BASE_URL, notification,
            { _ ->
            },
            Response.ErrorListener { error ->
                Log.e("Error", error.message.toString())

            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "key=${Contenst.SERVER_KEY}"
                headers["Content-Type"] = Contenst.CONTENT_TYPE
                return headers
            }
        }
        requestQueue.add(request)
    }

}