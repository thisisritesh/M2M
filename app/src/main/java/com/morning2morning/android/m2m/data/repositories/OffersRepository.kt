package com.morning2morning.android.m2m.data.repositories

import com.morning2morning.android.m2m.data.models.HomeBannerResponse
import com.morning2morning.android.m2m.data.models.HomeCategoryResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.network.NoConnectivityException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class OffersRepository(val apiClient: ApiClient) {

    public suspend fun getHomeBannerData() = flow<NetworkState<List<HomeBannerResponse.Result>>> {
        emit(NetworkState.loading())
        val data = apiClient.getHomeBannerData()
        emit(NetworkState.success(data.result))
    }.catch {
        if (it is NoConnectivityException){
            emit(NetworkState.failed(it.localizedMessage!!))
        } else {
            emit(NetworkState.failed(it.message!!))
        }
    }.flowOn(Dispatchers.IO)



    public suspend fun getCategoryList(catOffset: Int) = flow<NetworkState<List<HomeCategoryResponse.Categories.Category>>> {
        emit(NetworkState.loading())
        val data = apiClient.getHomeCategoryData(6,catOffset,8,0)
        emit(NetworkState.success(data.categories.category))
    }.catch {
        if (it is NoConnectivityException){
            emit(NetworkState.failed(it.localizedMessage!!))
        } else {
            emit(NetworkState.failed(it.message!!))
        }
    }.flowOn(Dispatchers.IO)

}