package com.pratik.learning.familyTree.learning

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking


fun numberFlow(): Flow<Int> = flow {
    for (i in 1..10) {
        delay(100)
        emit(i)
    }
}

val observeQuery = MutableStateFlow("")



@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
fun main() {

    runBlocking {
//        launch {
//            numberFlow().map { it * it }.collect {
//                println("Square: $it")
//            }
//        }
//
//
//        flow { while (true) emit(System.currentTimeMillis()) }.take(5).collect {
//            println(it)
//            delay(500)
//        }


        // Exception handling
        flow {
            (1..5).forEach {
                observeQuery.value = it.toString()
                if (it == 4) throw RuntimeException("Something went wrong at $it")
                else emit(it)
                delay(350)
            }
        }.onCompletion {
            if (it == null) {
                println("✅ Flow completed successfully")
            } else {
                println("⚠\uFE0F Flow completed with exception: $it")
            }
        }.catch { e ->
            println("🚨 Caught error: ${e.message}")
        }.collect { value ->
            println("Collected: $value")
        }


        val searchFlow: Flow<List<String>> = observeQuery
            .debounce(300)
            .filter { it.isNotBlank() }
            .mapLatest { query ->
                delay(500)
                listOf("Result for $query 1", "Result for $query 2")
            }

        searchFlow.collect {
            println(it)
        }

    }

}
