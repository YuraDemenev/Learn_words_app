package com.example.learn_words_app.data.fragments.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.learn_words_app.R

class CardAdapter(private val data: Array<String>) :
    RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (Class Adapter)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTest: TextView

        init {
            // Define click listener for the ViewHolder's View
//            textView = view.findViewById(R.id.textView)
            textTest = view.findViewById(R.id.levelsName)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_for_levels, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.textTest.text = data[position]


    }
}