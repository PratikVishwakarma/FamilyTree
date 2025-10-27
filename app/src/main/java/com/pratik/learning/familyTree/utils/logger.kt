package com.pratik.learning.familyTree.utils

import android.util.Log


const val LOGGER_ON = true

/**
 * Logs provided string
 * @param tag Tag for identification
 * @param str message to log
 */
fun logger(tag: String, str: String) {
    if (LOGGER_ON)
        Log.d("FamilyTree", "$tag : $str")
}

/**
 * Logs provided string
 * @param str message to log
 */
fun Any.logger(str: String) {
    if (LOGGER_ON)
        Log.d("FamilyTree", "${this::class.simpleName} : $str")
}