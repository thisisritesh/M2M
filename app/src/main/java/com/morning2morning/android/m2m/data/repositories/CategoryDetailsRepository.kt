package com.morning2morning.android.m2m.data.repositories

import com.morning2morning.android.m2m.data.models.ProductsListByCatIdResponse
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
class CategoryDetailsRepository(val apiClient: ApiClient, val cat_id: String) {

    public suspend fun getCategoriesData(offset: Int) = flow<NetworkState<List<ProductsListByCatIdResponse.Product.ProductX>>> {
        emit(NetworkState.loading())
        val data = apiClient.getProductsListByCatId(cat_id,20,offset)
        emit(NetworkState.success(data.product.product))
    }.catch {
        if (it is NoConnectivityException){
            emit(NetworkState.failed(it.localizedMessage!!))
        } else {
            emit(NetworkState.failed(it.message!!))
        }
    }.flowOn(Dispatchers.IO)

}