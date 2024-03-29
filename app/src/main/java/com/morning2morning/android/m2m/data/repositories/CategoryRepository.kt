package com.morning2morning.android.m2m.data.repositories

import com.morning2morning.android.m2m.data.models.CategoriesResponse
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
class CategoryRepository(val apiClient: ApiClient) {

    public suspend fun getCategoriesData() = flow<NetworkState<List<CategoriesResponse.Result>>> {
        emit(NetworkState.loading())
        val data = apiClient.getCategories()
        emit(NetworkState.success(data.result))
    }.catch {
        if (it is NoConnectivityException){
            emit(NetworkState.failed(it.localizedMessage!!))
        } else {
            emit(NetworkState.failed(it.message!!))
        }
    }.flowOn(Dispatchers.IO)
}
