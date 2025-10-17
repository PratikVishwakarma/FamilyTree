package com.pratik.learning.familyTree.data.repository

import androidx.paging.PagingData
import com.pratik.learning.familyTree.data.network.model.InterviewQuestion
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    fun getPagedProducts(): Flow<PagingData<InterviewQuestion>>
}