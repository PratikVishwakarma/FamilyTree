package com.pratik.learning.familyTree.learning

// --- let ---
fun exampleLet() {
    val name: String? = "Pratik"
    name?.let {
        println("Length of name is ${it.length}")
    }
}

// --- run ---
fun exampleRun() {
    val result = "Welcome".run {
        println("Inside run block: $this")
        length
    }
    println("Length is $result")
}

// --- apply ---
data class Person(var name: String = "", var age: Int = 0)

fun exampleApply() {
    val person = Person().apply {
        name = "John"
        age = 30
    }
    println(person)
}

// --- also ---
fun exampleAlso() {
    val numbers = mutableListOf(1, 2, 3).also {
        println("Original list: $it")
        it.add(4)
    }
    println("Modified list: $numbers")
}

// --- with ---
fun exampleWith() {
    val result = with(StringBuilder()) {
        append("Start")
        append(" - Middle")
        append(" - End")
        toString()
    }
    println(result)
}

fun main() {
    exampleLet()
    exampleRun()
    exampleApply()
    exampleAlso()
    exampleWith()
}