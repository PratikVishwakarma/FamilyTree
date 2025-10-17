package com.pratik.learning.familyTree.learning

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class WifiItem(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val capabilities: String,
    val frequency: Int,
    var lastSeen: Long = System.currentTimeMillis()
)

val finalShowingWifiList = HashMap<String, WifiItem>()

fun generateRandomWifiItems(count: Int): List<WifiItem> {
    val wifiList = mutableListOf<WifiItem>()

    (1..5).forEach {
        wifiList.add(
            WifiItem(
                ssid = "SSID_${Random.nextInt(0,10)}",
                bssid = "BSSID_$it",
                level = Random.nextInt(-10,100),
                capabilities = "Capabilities $it",
                frequency = listOf(2, 5).random(),
            )
        )
    }
    return wifiList
}

fun getFinalList(newWifiList: List<WifiItem>): MutableCollection<WifiItem> {
    val tempList = mutableListOf<WifiItem>()
    println("new list:")
    println(newWifiList.map { "${it.ssid}-${it.frequency}" })
    newWifiList.forEach {
        val tempKey = "${it.ssid}-${it.frequency}"
        if (tempKey in finalShowingWifiList) {
            // item already exists so updating the time stamp
            finalShowingWifiList[tempKey] = it
        } else {
            // this is new item so adding it to list
            finalShowingWifiList[tempKey] = it
        }
    }


    finalShowingWifiList.forEach { (key, value) ->
        if (key !in newWifiList.map { "${it.ssid}-${it.frequency}" }) {
            //item not present in new list so checking the last seen time
            if (System.currentTimeMillis() - value.lastSeen > (3 * 15 * 1000)) {
                println("not seen in last 3 scan so Removing item $key")
                tempList.add(value)
            }
        }
    }
    println("final list:")
    println(finalShowingWifiList.keys.map { it})

    println("==================================================")
    println()

    return finalShowingWifiList.values
}


fun main() {
    runBlocking {
        flow {
            (1..5).forEach {
                emit(generateRandomWifiItems(it))
                delay(4000)
            }
        }.collect{
            getFinalList(it)
        }
    }
}