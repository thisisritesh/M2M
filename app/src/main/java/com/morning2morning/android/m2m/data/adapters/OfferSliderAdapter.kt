package com.morning2morning.android.m2m.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.morning2morning.android.m2m.databinding.OfferSliderItemLayoutBinding

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class OfferSliderAdapter(var list: MutableList<Int>, val viewPager2: ViewPager2, val adapterCallback: AdapterCallback) :
    RecyclerView.Adapter<OfferSliderAdapter.OfferSliderViewHolder>() {

    interface AdapterCallback{
        fun onItemClicked(productId: String)
    }

    inner class OfferSliderViewHolder(binding: OfferSliderItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val bgImageView = binding.imageView
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferSliderViewHolder {
        return OfferSliderViewHolder(OfferSliderItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: OfferSliderViewHolder, position: Int) {
        holder.bgImageView.setImageResource(list[position])
        if (position == list.size - 2){
            viewPager2.post(runnable)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private val runnable = Runnable {
        list.addAll(list)
        notifyDataSetChanged()
    }


//    fun setData(list : MutableList<HomeBannerResponse.ResultHomeBanner>){
//        mList = list
//        notifyDataSetChanged()
//    }

}