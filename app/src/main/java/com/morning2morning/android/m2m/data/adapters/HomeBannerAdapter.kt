package com.morning2morning.android.m2m.data.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.models.HomeBannerResponse

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class HomeBannerAdapter(var list: MutableList<HomeBannerResponse.Result>, val viewPager2: ViewPager2, val adapterCallback: AdapterCallback) :
    RecyclerView.Adapter<HomeBannerAdapter.HomeBannerViewHolder>() {

    interface AdapterCallback{
        fun onItemClicked(productId: String)
    }

    inner class HomeBannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val bgImageView: ImageView = itemView.findViewById(R.id.imageView)

        init {
            itemView.setOnClickListener {
                adapterCallback.onItemClicked(list[adapterPosition].id)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeBannerViewHolder {
        return HomeBannerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.home_banner_item,parent,false))
    }

    override fun onBindViewHolder(holder: HomeBannerViewHolder, position: Int) {
        Glide.with(holder.bgImageView.context)
            .load(list[position].banner)
            .placeholder(R.mipmap.m2m_placeholder)
            .into(holder.bgImageView)
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