package com.subhamgupta.roomiessaver.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiessaver.adapters.IssueAdapter.IssueHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.subhamgupta.roomiessaver.R
import android.widget.TextView

class IssueAdapter(var data: List<Map<String, Any?>>, var context: Context) : RecyclerView.Adapter<IssueHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.issue_item, parent, false)
        return IssueHolder(view)
    }

    override fun onBindViewHolder(holder: IssueHolder, position: Int) {
        holder.issue.text = data[position]["ISSUE"].toString()
        holder.from.text = data[position]["PERSON_FROM"].toString()
        holder.to.text = data[position]["PERSON_TO"].toString()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class IssueHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var issue: TextView
        var from: TextView
        var to: TextView

        init {
            issue = itemView.findViewById(R.id.issue)
            from = itemView.findViewById(R.id.from)
            to = itemView.findViewById(R.id.to)
        }
    }
}