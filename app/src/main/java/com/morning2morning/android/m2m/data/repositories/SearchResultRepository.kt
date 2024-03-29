package com.morning2morning.android.m2m.data.repositories

import com.morning2morning.android.m2m.data.models.SearchResponse
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
class SearchResultRepository(val apiClient: ApiClient, val searchTag: String)  {

    public suspend fun getSearchResultData(offset: Int) = flow<NetworkState<SearchResponse.Product>> {
        emit(NetworkState.loading())
        val data = apiClient.getSearchResult(searchTag, 10,offset)
        emit(NetworkState.success(data.product))
    }.catch {
        if (it is NoConnectivityException){
            emit(NetworkState.failed(it.localizedMessage!!))
        } else {
            emit(NetworkState.failed(it.message!!))
        }
    }.flowOn(Dispatchers.IO)

}