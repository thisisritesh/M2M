import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.models.CategoriesResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.databinding.CategoryItemLayoutBinding
import com.morning2morning.android.m2m.utils.ui.animations.Animations
/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class CategoryAdapter(var data: List<CategoriesResponse.Result>, private val adapterCallbacks: AdapterCallbacks, val context: Context, val apiClient: ApiClient, val recyclerView: RecyclerView) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    companion object{
        private const val TAG = "CategoryAdapter"
    }

//    private var lastVisibleItem = 0
//    private var totalItemCount = 0
//    private var isLoading = false

//    init {
//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                totalItemCount = recyclerView.adapter!!.itemCount
//                try {
//                    lastVisibleItem =
//                        recyclerView.getChildAdapterPosition(recyclerView.getChildAt(recyclerView.childCount - 1))
//                } catch (e: Exception) {
//                    Log.e(TAG, "onScrolled: exception", e)
//                    lastVisibleItem = 0
//                }
//                if (!isLoading && totalItemCount == lastVisibleItem + 1) {
//                    Log.d(TAG, "onRequestLoadMore: ")
//                    if (data.size > 12){
//                        adapterCallbacks.onRequestLoadMore()
//                    }
//                    isLoading = true
//                }
//            }
//        })
//
//    }

    interface AdapterCallbacks {
        fun onItemClicked(cat_id: String, cat_title: String)
        fun onRequestLoadMore()
    }



    inner class CategoryViewHolder(binding: CategoryItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val bannerImageView = binding.bannerImage
        val titleTextView = binding.categoryTitleTextView
        val headerLayout = binding.headerLayout
        val rootCardView = binding.rootCard
        init {
            binding.root.setOnClickListener {
                adapterCallbacks.onItemClicked(data[adapterPosition].id, data[adapterPosition].name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(CategoryItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.titleTextView.text = data[position].name
        Glide.with(holder.bannerImageView.context)
            .load(data[position].image)
            .placeholder(R.mipmap.placeholder_portrait)
            .into(holder.bannerImageView)

    }



    override fun getItemCount(): Int {
        return data.size
    }

    private fun toggleLayout(isExpanded: Boolean, v: View, layoutExpand: RecyclerView): Boolean {
        Animations.toggleArrow(v, isExpanded)
        if (isExpanded) {
            layoutExpand.visibility = View.VISIBLE
        } else {
            layoutExpand.visibility = View.GONE
        }
        return isExpanded
    }

    fun setDataList(list: List<CategoriesResponse.Result>){
        data = list
        notifyDataSetChanged()
    }

}