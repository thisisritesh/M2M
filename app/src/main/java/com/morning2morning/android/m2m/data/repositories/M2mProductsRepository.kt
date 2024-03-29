package com.morning2morning.android.m2m.data.repositories

import com.morning2morning.android.m2m.data.models.M2mProductsResponse
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
class M2mProductsRepository(val apiClient: ApiClient) {

    public suspend fun getM2mProductsData(offset: Int) = flow<NetworkState<List<M2mProductsResponse.Product.ProductX>>> {
        emit(NetworkState.loading())
        val data = apiClient.getM2mProducts(1,10,offset)
        emit(NetworkState.success(data.product.product))
    }.catch {
        if (it is NoConnectivityException){
            emit(NetworkState.failed(it.localizedMessage!!))
        } else {
            emit(NetworkState.failed(it.message!!))
        }
    }.flowOn(Dispatchers.IO)

}

