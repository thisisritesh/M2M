package com.morning2morning.android.m2m.data.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.models.HomeCategoryResponse
import com.morning2morning.android.m2m.databinding.NestedHomeItemLayoutBinding
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class NestedCategoryAdapter(var list: MutableList<HomeCategoryResponse.Categories.Category>, val context: Context, val adapterCallbacks: AdapterCallbacks, val recyclerView: RecyclerView) : RecyclerView.Adapter<NestedCategoryAdapter.NestedHomeViewHolder>() {

    companion object{
        private const val TAG = "NestedHomeAdapter"
    }

    private var lastVisibleItem = 0
    private var totalItemCount:Int = 0
    private var isLoading = false


    init {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = recyclerView.adapter!!.itemCount
                try {
                    lastVisibleItem =
                        recyclerView.getChildAdapterPosition(recyclerView.getChildAt(recyclerView.childCount - 1))
                } catch (e: Exception) {
//                    Tracer.error("Error", "onScrolled: EXCEPTION " + e.message)
                    lastVisibleItem = 0
                }
                if (!isLoading && totalItemCount == lastVisibleItem + 1) {
                    adapterCallbacks.onLoadMore()
                    isLoading = true
                }
            }
        })

    }
    
    interface AdapterCallbacks{
        fun onViewAllClicked(cat_id: String, cat_title: String)
        fun onNestedRecyclerViewItemClicked(productId: String)
        fun onLoadMore()
    }

    inner class NestedHomeViewHolder(binding: NestedHomeItemLayoutBinding) : RecyclerView.ViewHolder(binding.root), NestedHomeItemAdapter.AdapterCallbacks {
        val categoryTitle = binding.categoryText
        val gradientImageView = binding.gradientBgImage
        val recyclerView = binding.recyclerView
        val viewAllText = binding.viewAllText

        fun bind(position: Int) {

            recyclerView.adapter = NestedHomeItemAdapter(list[position].products.product!!,context,recyclerView,this)

            viewAllText.setOnClickListener {
                adapterCallbacks.onViewAllClicked(list[position].id, list[position].name)
            }
        }

//        override fun onRequestLoadMore() {
//            Log.d(TAG, "onRequestLoadMore: ")
//            GlobalScope.launch {
//                offset++
//                val data = apiClient.getProductsListByCatId(list[adapterPosition].id,6,offset)
//                withContext(Dispatchers.Main){
//                    progressBar.visibility = View.VISIBLE
//                    mList.addAll(data.product.product)
//                    adapter.notifyDataSetChanged()
//                    progressBar.visibility = View.GONE
//                }
//            }
//        }

        override fun onNestedRecyclerViewItemClicked(productId: String) {
            adapterCallbacks.onNestedRecyclerViewItemClicked(productId)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedHomeViewHolder {
        return NestedHomeViewHolder(NestedHomeItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: NestedHomeViewHolder, position: Int) {
        val randomInt = (0..3).random()
        holder.categoryTitle.text = list[position].name

        when(randomInt){
            0->{
                holder.gradientImageView.setImageResource(R.drawable.gradient_bg_1)
            }
            1->{
                holder.gradientImageView.setImageResource(R.drawable.gradient_bg_2)
            }
            2->{
                holder.gradientImageView.setImageResource(R.drawable.gradient_bg_3)
            }
            3->{
                holder.gradientImageView.setImageResource(R.drawable.gradient_bg_4)
            }
        }

        holder.bind(position)




    }

    fun setData(data: List<HomeCategoryResponse.Categories.Category>){
        list.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

}