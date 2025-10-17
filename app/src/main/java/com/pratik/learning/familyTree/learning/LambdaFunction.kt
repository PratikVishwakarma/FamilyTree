package com.pratik.learning.familyTree.learning

// Lambda Basics
val add = { a: Int, b: Int -> a + b }

// Higher-order function using lambda
fun operate(a: Int, b: Int, op: (Int, Int) -> Int): Int {
    return op(a, b)
}

val multiply = operate(4, 5) { x, y -> x * y }


// Collection functions using lambda
val numbers = listOf(1, 2, 3, 4, 5)
val evens = numbers.filter { it % 2 == 0 }


// Lambda with receiver (DSL-style)
val greeting = buildString {
    append("Hello ")
    append("World")
}

// Named function vs Lambda
fun sum(a: Int, b: Int): Int = a + b
val sumLambda = { a: Int, b: Int -> a + b }


fun main() {
    println("Add: " + add(2, 3))

    println("Multiply: $multiply")

    println("Even numbers: $evens")

    println(greeting)

    println("Named function sum: ${sum(3, 4)}")
    println("Lambda function sum: ${sumLambda(3, 4)}")
}