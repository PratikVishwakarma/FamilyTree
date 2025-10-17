package com.pratik.learning.familyTree.data.network

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pratik.learning.familyTree.data.network.model.InterviewQuestion

class ProductPagingSource (
    private val apiService: ApiService
) : PagingSource<Int, InterviewQuestion>() {
    override fun getRefreshKey(state: PagingState<Int, InterviewQuestion>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, InterviewQuestion> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            Log.d("ProductPagingSource", "Loaded page: ")
//            val response = apiService.getProducts(limit = pageSize, skip = page)
            val response = apiService.getData()
            Log.d("ProductPagingSource", "Response $response")
            Log.d("ProductPagingSource", "Loaded page: $page, items: ${response.interviewQuestions.size}")
            LoadResult.Page(
                data = response.interviewQuestions,
                prevKey = if (page == 0) null else page - pageSize,
                nextKey = if (response.interviewQuestions.isEmpty()) null else page + pageSize
            )
        } catch (e: Exception) {
            Log.d("ProductPagingSource exception", "${e.printStackTrace()}")
            LoadResult.Error(e)
        }
    }
}