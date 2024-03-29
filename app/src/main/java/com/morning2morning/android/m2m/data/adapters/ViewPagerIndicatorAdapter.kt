package com.morning2morning.android.m2m.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.databinding.IndicatorItemLayoutBinding

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class ViewPagerIndicatorAdapter(var numberOfItems: Int) : RecyclerView.Adapter<ViewPagerIndicatorAdapter.ViewPagerIndicatorViewHolder>() {

    private var index: Int = 0;

    inner class ViewPagerIndicatorViewHolder(private val binding: IndicatorItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.indicatorImageView
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerIndicatorViewHolder {
        return ViewPagerIndicatorViewHolder(IndicatorItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewPagerIndicatorViewHolder, position: Int) {

        val rightIndex = index % numberOfItems

        if (position == rightIndex){
            holder.imageView.setImageResource(R.drawable.selected_pager_dot)
        } else {
            holder.imageView.setImageResource(R.drawable.unselected_pager_dot)
        }
    }

    override fun getItemCount(): Int {
        return numberOfItems
    }

    public fun selectItem(position: Int){
        this.index = position
        notifyDataSetChanged()
    }

    fun setData(data: Int){
        numberOfItems = data
        notifyDataSetChanged()
    }


}