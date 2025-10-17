package com.pratik.learning.familyTree.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pratik.learning.familyTree.data.network.ApiService
import com.pratik.learning.familyTree.data.network.ProductPagingSource
import com.pratik.learning.familyTree.data.network.model.InterviewQuestion
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService
): ProductRepository {

    override fun getPagedProducts(): Flow<PagingData<InterviewQuestion>> {
        Log.d("Pratik getPagedProducts", "Loaded page: ")
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ProductPagingSource(apiService) }
        ).flow
    }
}