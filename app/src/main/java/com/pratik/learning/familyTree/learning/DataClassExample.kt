package com.pratik.learning.familyTree.learning

// 🔹 Basic Data Class
data class User(val name: String, val age: Int)

val user1 = User("Alice", 25)
val user2 = user1.copy(age = 30)
//println(user1)             // toString()
//println(user1 == user2)    // equals()

// 🔹 Destructuring
//describe(user1)
fun describe(user: User) {
    val (name, age) = user
    println("User name: $name, age: $age")
}

// 🔹 Data class with default values
data class Product(val id: Int, val name: String = "Unknown", val price: Double = 0.0)

val defaultProduct = Product(1)
val customProduct = Product(2, "Laptop", 1200.0)

// 🔹 Immutability and Copy
val original = Product(10, "Phone", 999.0)
val discounted = original.copy(price = 699.0)

// 🔹 Nested Data Classes
data class Order(val orderId: Int, val user: User, val product: Product)

val order = Order(101, user1, customProduct)
//println(order)

// 🔹 Data Class vs Regular Class
class RegularUser(val name: String, val age: Int)
val reg1 = RegularUser("Bob", 30)
val reg2 = RegularUser("Bob", 30)
//println(reg1 == reg2) // false, no structural equality

// Data classes automatically implement:
// - equals()
// - hashCode()
// - toString()
// - componentN()
// - copy()
