package com.pratik.learning.familyTree.learning

// Task 1: Extension Function
fun List<Int>.evenNumbers(): List<Int> = this.filter { it % 2 == 0 }

// Task 2: Sealed Class to represent screen states
sealed class ScreenState<out T> {
    object Loading : ScreenState<Nothing>()
    data class Error(val message: String) : ScreenState<Nothing>()
    data class DataAvailable<T>(val data: T) : ScreenState<T>()
}

// Task 3: Null-safe formatter using let + apply
fun formatUserName(input: String?): String {
    return input?.let {
        it.trim().replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
    }?.apply {
        println("Formatted name: $this")
    } ?: "Unknown"
}

// You can test the above with a main function (optional)
fun main() {
    val numbers = listOf(1, 2, 3, 4, 5, 6)
    println("Even numbers: ${numbers.evenNumbers()}")

    val userName: String? = "  pratik  "
    println("Formatted user: ${formatUserName(userName)}")

    val state: ScreenState<List<String>> = ScreenState.DataAvailable(listOf("Task1", "Task2"))
    when (state) {
        is ScreenState.Loading -> println("Loading...")
        is ScreenState.Error -> println("Error: ${state.message}")
        is ScreenState.DataAvailable -> println("Data: ${state.data}")
    }
}
