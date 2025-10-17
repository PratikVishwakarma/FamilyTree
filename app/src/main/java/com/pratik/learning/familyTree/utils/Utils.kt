package com.pratik.learning.familyTree.utils

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavController
): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()

    // Remember parent entry to avoid recomposition issues
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }

    return hiltViewModel(parentEntry)
}



val genders = listOf(GENDER_TYPE_MALE, GENDER_TYPE_FEMALE)
val relations = listOf(
    RELATION_TYPE_FATHER,
    RELATION_TYPE_MOTHER,
    RELATION_TYPE_HUSBAND,
    RELATION_TYPE_WIFE,
    RELATION_TYPE_SIBLING,
    RELATION_TYPE_SON,
    RELATION_TYPE_DAUGHTER)

fun getAvailableRelationsForGender(gender: String): List<String> {
    Log.d("Utils", "getAvailableRelationsForGender for gender: $gender")
    val maleRelations = listOf(
        RELATION_TYPE_HUSBAND,
    )

    val femaleRelations = listOf(
        RELATION_TYPE_WIFE,
    )

    val commonRelations = listOf(
        RELATION_TYPE_MOTHER,
        RELATION_TYPE_FATHER,
        RELATION_TYPE_SON,
        RELATION_TYPE_DAUGHTER,
//        RELATION_TYPE_SIBLING,
    )

    return when (gender) {
        GENDER_TYPE_MALE -> femaleRelations + commonRelations
        GENDER_TYPE_FEMALE -> maleRelations + commonRelations
        else -> commonRelations
    }
}

fun getIcon(relation: String): String {
    return when (relation) {
        RELATION_TYPE_FATHER, RELATION_TYPE_FATHER_IN_LAW -> "🧔‍♂️"
        RELATION_TYPE_MOTHER, RELATION_TYPE_MOTHER_IN_LAW -> "🧑️"
        RELATION_TYPE_BROTHER -> "👦️"
        RELATION_TYPE_SISTER -> "️️👧️"
        RELATION_TYPE_HUSBAND -> "🤵🏻‍♂️"
        RELATION_TYPE_WIFE -> "️👰🏻‍♀️"
        RELATION_TYPE_SON, RELATION_TYPE_DAUGHTER -> "👶"
        RELATION_TYPE_GRANDFATHER_F, RELATION_TYPE_GRANDFATHER_M -> "👴"
        RELATION_TYPE_GRANDMOTHER_F, RELATION_TYPE_GRANDMOTHER_M -> "👵"
        else -> ""
    }
}

fun getRelationInHindi(relation: String): String {
    return when (relation) {
        RELATION_TYPE_FATHER -> "पिता"
        RELATION_TYPE_MOTHER -> "माता"
        RELATION_TYPE_FATHER_IN_LAW -> "ससुर"
        RELATION_TYPE_MOTHER_IN_LAW -> "सास"
        RELATION_TYPE_BROTHER -> "भाई"
        RELATION_TYPE_SISTER -> "बहन"
        RELATION_TYPE_HUSBAND -> "पति"
        RELATION_TYPE_WIFE -> "पत्नी"
        RELATION_TYPE_SON -> "पुत्र"
        RELATION_TYPE_DAUGHTER -> "पुत्री"
        RELATION_TYPE_GRANDFATHER_F -> "दादा जी"
        RELATION_TYPE_GRANDMOTHER_F -> "दादी जी"
        RELATION_TYPE_GRANDFATHER_M -> "नाना जी"
        RELATION_TYPE_GRANDMOTHER_M -> "नानी जी"
        else -> "संबंध"
    }
}


// Helper function to extract the last word (surname) from a full name string
fun String.getSurname(): String {
    return this.trim().substringAfterLast(' ', "")
}

// Helper function to extract all words except the last one (first name/given names)
fun String.getFirstName(): String {
    val surname = this.getSurname()
    return if (surname.isEmpty()) this else this.substringBeforeLast(' ', this)
}

fun formatIsoDate(dateString: String): String {
    Log.d("Utils", "formatIsoDate for date: $dateString")
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val outputFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH)
        val date = LocalDate.parse(dateString, inputFormatter)
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateString // return original if parsing fails
    }
}

fun calculateAgeFromDob(dobString: String): Int {
    Log.d("Utils", "calculateAgeFromDob for dob: $dobString")
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val dob = LocalDate.parse(dobString, formatter)
        val today = LocalDate.now()
        Period.between(dob, today).years
    } catch (e: Exception) {
        0 // Return 0 if parsing fails
    }
}




// Data class to hold the screen state (kept the same)
data class MemberFormState(
    val fullName: String = "",
    val dob: String = "",
    val gender: String = "Male",
    val isLiving: Boolean = true,
    val dod: String = "", // Date of Death
    val city: String = "",
    val mobile: String = ""
)



// Data class to hold the screen state (kept the same)
data class RelationFormState(
    val relatesToMemberId: Int = -1,
    val relatesToFullName: String = "",
    val relation: String = "",
    val relatedToMemberId: Int = -1,
    val relatedToFullName: String = "",
    val error: String = "",
)


/**
 * A utility function to launch the native Android DatePickerDialog.
 */
fun showDatePicker(
    context: Context,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val todayCalendar = Calendar.getInstance()

    val currentYear = todayCalendar.get(Calendar.YEAR)
    val currentMonth = todayCalendar.get(Calendar.MONTH)
    val currentDay = todayCalendar.get(Calendar.DAY_OF_MONTH)

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val formattedDate = dateFormat.format(calendar.time)
            onDateSelected(formattedDate)
        },
        currentYear,
        currentMonth,
        currentDay
    )

    // Optional: Prevent selecting a future date
    datePickerDialog.datePicker.maxDate = todayCalendar.timeInMillis

    datePickerDialog.show()
}