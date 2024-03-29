package com.morning2morning.android.m2m.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.models.HomeCategoryResponse
import com.morning2morning.android.m2m.databinding.HomeCategoryItemLayoutBinding

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class HomeCategoryAdapter(private val list: List<HomeCategoryResponse.Categories.Category>, private val adapterCallbacks: AdapterCallbacks) : RecyclerView.Adapter<HomeCategoryAdapter.CategoryViewHolder>() {


    interface AdapterCallbacks{
        fun onItemClicked(cat_id: String, cat_title: String)
    }

    inner class CategoryViewHolder(binding: HomeCategoryItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.categoryImageView
        val title = binding.categoryNameTextView
        init {
            binding.root.setOnClickListener {
                title.performClick()
                adapterCallbacks.onItemClicked(list[adapterPosition].id, list[adapterPosition].name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(HomeCategoryItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        Glide.with(holder.imageView.context)
            .load(list[position].image)
            .placeholder(R.mipmap.placeholder_portrait)
            .into(holder.imageView)
        holder.title.text = list[position].name
    }

    override fun getItemCount() = list.size

}