package com.pratik.learning.familyTree.data.network

import com.pratik.learning.familyTree.data.network.model.ProductResponse
import com.pratik.learning.familyTree.data.network.model.QuestionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("products")
    suspend fun getProducts(
        @Query("skip") skip: Int,
        @Query("limit") limit: Int
    ): ProductResponse

    @GET("mumbaitrip/KheloQuestionBank/refs/heads/main/tech/androidKheloQues.json")
    suspend fun getData(): QuestionResponse
}

