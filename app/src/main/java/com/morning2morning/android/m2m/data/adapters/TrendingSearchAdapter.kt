package com.morning2morning.android.m2m.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.morning2morning.android.m2m.databinding.TrendingSearchItemLayoutBinding

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class TrendingSearchAdapter(val list: List<String>) : RecyclerView.Adapter<TrendingSearchAdapter.TrendingViewHolder>() {


    inner class TrendingViewHolder(binding: TrendingSearchItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val trendingSearchTitle = binding.trendingSearchTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        return TrendingViewHolder(TrendingSearchItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        holder.trendingSearchTitle.text = list[position]
    }

    override fun getItemCount(): Int = list.size

}