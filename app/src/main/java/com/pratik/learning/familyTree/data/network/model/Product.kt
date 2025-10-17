package com.pratik.learning.familyTree.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val thumbnail: String
)

@Serializable
data class ProductResponse(
    val products: List<Product>,
    val total: Int,
    val skip: Int,
    val limit: Int
)



// Model for each item in "allQuestions"
data class AllQuestion(
    val id: Int,
    val question: String,
    val category: String,
    val answer: List<String>,
    val correctAnswer: String
)

// Model for each item in "interviewQuestions"
data class InterviewQuestion(
    val id: Int,
    val category: String,
    val subType: String,
    val question: String,
    val referenceLink: String,
    val answer: String
)

// Main response model
data class QuestionResponse(
    val allQuestions: List<AllQuestion>,
    val interviewQuestions: List<InterviewQuestion>
)