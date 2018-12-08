package com.rain.example

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.rain.example.model.Todo

class TodoAdapter : ListAdapter<Todo, TodoAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<Todo>() {
            override fun areItemsTheSame(oldItem: Todo, newItem: Todo) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Todo, newItem: Todo) = oldItem == newItem
        }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false))
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        vh.tvTitle.text = getItem(position).title
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
    }
}
