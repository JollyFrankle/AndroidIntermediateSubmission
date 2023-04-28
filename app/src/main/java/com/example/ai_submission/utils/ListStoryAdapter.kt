package com.example.ai_submission.utils

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ai_submission.databinding.ItemRvStoryBinding
import com.example.ai_submission.retrofit.Story
import com.example.ai_submission.ui.detail_story.DetailStoryActivity
import androidx.core.util.Pair

class ListStoryAdapter(private val list: List<Story>): RecyclerView.Adapter<ListStoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRvStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = list[position]
        val context = holder.itemView.context

        holder.binding.apply {
            tvItemName.text = story.name
            tvItemDescription.text = story.description

            Glide.with(context)
                .load(story.photoUrl)
                .placeholder(android.R.color.darker_gray)
                .into(ivItemPhoto)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailStoryActivity::class.java)
            intent.putExtra(DetailStoryActivity.EXTRA_ID, story.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemRvStoryBinding): RecyclerView.ViewHolder(binding.root)
}