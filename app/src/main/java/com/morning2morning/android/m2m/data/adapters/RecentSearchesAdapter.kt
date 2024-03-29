package com.morning2morning.android.m2m.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.morning2morning.android.m2m.data.dbs.cart.db.RecentSearchDB
import com.morning2morning.android.m2m.data.dbs.cart.entity.RecentSearch
import com.morning2morning.android.m2m.databinding.RecentSearchItemLayoutBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class RecentSearchesAdapter(var list: List<RecentSearch>) : RecyclerView.Adapter<RecentSearchesAdapter.RecentSearchesViewHolder>() {


    @DelicateCoroutinesApi
    inner class RecentSearchesViewHolder(binding: RecentSearchItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val trendingSearchTitle = binding.trendingSearchTextView
        init {
            binding.removeIcon.setOnClickListener {
                GlobalScope.launch(Dispatchers.IO){
                    RecentSearchDB.getInstance(binding.removeIcon.context)
                        .dao()
                        .deleteRecentSearch(list[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchesViewHolder {
        return RecentSearchesViewHolder(RecentSearchItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecentSearchesViewHolder, position: Int) {
        holder.trendingSearchTitle.text = list[position].title
    }

    override fun getItemCount(): Int = list.size

    fun setData(newList: List<RecentSearch>){
        list = newList
        notifyDataSetChanged()
    }

}