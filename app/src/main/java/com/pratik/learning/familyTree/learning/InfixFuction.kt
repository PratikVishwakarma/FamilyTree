package com.pratik.learning.familyTree.learning

infix fun Int.times(str: String): String {
    return str.repeat(this)
}


fun main() {
    // Usage:
    val result = 3 times "Hello "
    // Equivalent to: 3.times("Hello ")
    println(result) // Hello Hello Hello


    // pair - predefined function in kotlin

    val pair = "Pratik" to "Vishwakarma"
    println(pair)  // Pratik, Vishwakarma
}