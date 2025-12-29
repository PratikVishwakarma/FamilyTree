package com.pratik.learning.familyTree.utils

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import java.io.File
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


val isAdmin = true

val genders = listOf(GENDER_TYPE_MALE, GENDER_TYPE_FEMALE)
val states = listOf(
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chhattisgarh",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal",
    "Andaman and Nicobar Islands",
    "Chandigarh",
    "Dadra and Nagar Haveli and Daman and Diu",
    "Delhi",
    "Jammu and Kashmir",
    "Ladakh",
    "Lakshadweep",
    "Puducherry"
)


fun getAvailableRelationsForGender(gender: String): List<String> {
    logger("Utils", "getAvailableRelationsForGender for gender: $gender")
    val maleRelations = listOf(
        RELATION_TYPE_HUSBAND,
    )

    val femaleRelations = listOf(
        RELATION_TYPE_WIFE,
    )

    val commonRelations = listOf(
        RELATION_TYPE_MOTHER,
        RELATION_TYPE_FATHER,
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
        RELATION_TYPE_BROTHER, RELATION_TYPE_SON -> "👦️"
        RELATION_TYPE_SISTER -> "️️👧️"
        RELATION_TYPE_HUSBAND, RELATION_TYPE_SON_IN_LAW -> "🤵🏻‍♂️"
        RELATION_TYPE_WIFE, RELATION_TYPE_DAUGHTER_IN_LAW,RELATION_TYPE_DAUGHTER -> "👩🏻‍"
        RELATION_TYPE_GRANDFATHER_F, RELATION_TYPE_GRANDFATHER_M -> "👴"
        RELATION_TYPE_GRANDMOTHER_F, RELATION_TYPE_GRANDMOTHER_M -> "👵"
        else -> ""
    }
}

fun String.inHindi(): String {
    return when (this.lowercase()) {
        RELATION_TYPE_FATHER.lowercase() -> "पिता"
        RELATION_TYPE_MOTHER.lowercase() -> "माता"
        RELATION_TYPE_SON_IN_LAW.lowercase() -> "दामाद"
        RELATION_TYPE_DAUGHTER_IN_LAW.lowercase() -> "बहू"
        RELATION_TYPE_FATHER_IN_LAW.lowercase() -> "ससुर"
        RELATION_TYPE_MOTHER_IN_LAW.lowercase() -> "सास"
        RELATION_TYPE_BROTHER.lowercase() -> "भाई"
        RELATION_TYPE_SISTER.lowercase() -> "बहन"
        RELATION_TYPE_HUSBAND.lowercase() -> "पति"
        RELATION_TYPE_WIFE.lowercase() -> "पत्नी"
        RELATION_TYPE_SON.lowercase() -> "पुत्र"
        RELATION_TYPE_DAUGHTER.lowercase() -> "पुत्री"
        RELATION_TYPE_GRANDFATHER_F.lowercase() -> "दादा जी"
        RELATION_TYPE_GRANDMOTHER_F.lowercase()-> "दादी जी"
        RELATION_TYPE_GRANDFATHER_M.lowercase() -> "नाना जी"
        RELATION_TYPE_GRANDMOTHER_M.lowercase() -> "नानी जी"
        RELATION_TYPE_GRANDCHILD.lowercase() -> "पोता"
        RELATION_TYPE_GRANDCHILD_F.lowercase() -> "पोती"
        RELATION_TYPE_GREAT_GRANDCHILD.lowercase() -> "परपोता"
        RELATION_TYPE_GREAT_GREAT_GRANDCHILD.lowercase() -> "पर परपोता"
        RELATION_TYPE_GREAT____GRANDCHILD.lowercase() -> "पर...परपोता"
        GENDER_TYPE_MALE.lowercase() -> "पुरुष"
        GENDER_TYPE_FEMALE.lowercase() -> "महिला"
        "full name" -> "नाम"
        "deceased" -> "स्वर्गवासी"
        "gotra" -> "गोत्र"
        "dob", "date of birth" -> "जन्मदिन"
        "dod", "date of death" -> "मृत्यु दिवस"
        "Mobile Number" -> "मोबाइल नंबर"
        "search" -> "खोजेंं"
        "unmarried" -> "अविवाहित"
        "add member" -> "सदस्य जोड़ें"
        "add relation" -> "संबंध जोड़ें"
        "see ancestry" -> "वंशावली देखें"
        "delete member" -> "सदस्य हटाएं"
        "member details" -> "सदस्य विवरण"
        "select member" -> "सदस्य का चयन करें"
        "relation" -> "संबंध"
        "please select a relation" -> "कृपया एक संबंध चुनें"
        "please select a related person" -> "कृपया संबंधित व्यक्ति का चयन करें"
        "person cannot be related to themselves" -> "व्यक्ति स्वयं से संबंधित नहीं हो सकता"
        "edit" -> "परिवर्तन"
        "are you sure you want to delete this member?" -> "क्या आप वाकई इस सदस्य को हटाना चाहते हैं?"
        "yes" -> "हाँ"
        "no", "cancel" -> "नहीं"
        "delete all relations" -> "सभी संबंध हटाएँ"
        "are you sure you want to delete all the relations for member?" -> "क्या आप वाकई सदस्य के सभी संबंध हटाना चाहते हैं?                "
        "please scroll left or down to see whole family tree" -> "कृपया संपूर्ण परिवार वृक्ष देखने के लिए बाईं ओर या नीचे स्क्रॉल करें"
        else -> this
    }
}

fun String.getSpouseRelation(): String {
    return when(this) {
        RELATION_TYPE_SON -> RELATION_TYPE_DAUGHTER_IN_LAW
        RELATION_TYPE_DAUGHTER -> RELATION_TYPE_SON_IN_LAW
        else -> ""
    }
}


fun String.relationTextInHindi(): String {
    return when (this) {
        RELATION_TYPE_FATHER -> "के पिता"
        RELATION_TYPE_MOTHER -> "की माता"
        RELATION_TYPE_FATHER_IN_LAW -> "ससुर"
        RELATION_TYPE_MOTHER_IN_LAW -> "सास"
        RELATION_TYPE_BROTHER -> "भाई"
        RELATION_TYPE_SISTER -> "बहन"
        RELATION_TYPE_HUSBAND -> "के पति"
        RELATION_TYPE_WIFE -> "की पत्नी"
        RELATION_TYPE_SON -> "का पुत्र"
        RELATION_TYPE_DAUGHTER -> "की पुत्री"
        RELATION_TYPE_GRANDFATHER_F -> "दादा जी"
        RELATION_TYPE_GRANDMOTHER_F -> "दादी जी"
        RELATION_TYPE_GRANDFATHER_M -> "नाना जी"
        RELATION_TYPE_GRANDMOTHER_M -> "नानी जी"
        RELATION_TYPE_DAUGHTER_IN_LAW -> "बहू"
        RELATION_TYPE_SON_IN_LAW -> "दामाद"
        GENDER_TYPE_MALE -> "पुरुष"
        GENDER_TYPE_FEMALE -> "महिला"
        "Full Name" -> "नाम"
        "Deceased" -> "स्वर्गवासी"
        "Gotra" -> "गोत्र"
        "DOB", "Date of Birth" -> "जन्मदिन"
        "DOD", "Date of Death" -> "मृत्यु दिवस"
        else -> this
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

fun String.getCombinedName(spouseName: String = ""): String {
    val memberSN = this.getSurname()
    val spouseSN = spouseName.getSurname()
    if (spouseName.isEmpty()) return this
    return  if (memberSN == spouseSN && memberSN.isNotEmpty()) {
       this.getFirstName() + " ❤️" + spouseName.getFirstName() + " \n" + memberSN
    } else {
        "$this ❤️$spouseName $memberSN"
    }
}

fun FamilyMember.getCombineNameWithLivingStatus(spouse: FamilyMember?): String {
    val memberSN = this.fullName.getSurname()
    val spouseSN = spouse?.fullName?.getSurname()?: ""
    if (spouse == null) return "${this.fullName} ${ if(this.isLiving) "" else  "🕊"}"
    return  if (memberSN == spouseSN && memberSN.isNotEmpty()) {
        "${this.fullName.getFirstName()} ${ if(this.isLiving) "" else  "🕊"} ❤️ ${spouse.fullName.getFirstName()} ${if (spouse.isLiving) "" else "🕊"} \n$memberSN"
    } else {
        "${this.fullName} ${ if(this.isLiving) "" else  "🕊"}❤️${spouse.fullName} ${if (spouse.isLiving) "" else "🕊"} $memberSN"
    }
}

fun formatIsoDate(dateString: String): String {
    logger("Utils", "formatIsoDate for date: $dateString")
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
    logger("Utils", "calculateAgeFromDob for dob: $dobString")
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
    val gender: String = GENDER_TYPE_MALE,
    val isLiving: Boolean = true,
    val dod: String = "", // Date of Death
    val city: String = "",
    val state: String = "Madhya Pradesh",
    val gotra: String = "",
    val mobile: String = ""
)



// Data class to hold the screen state (kept the same)
data class RelationFormState(
    val relatesToMemberId: Int = -1,
    val relatesToFullName: String = "",
    val relation: String = "",
    val relatedToMemberId: Int = -1,
    val relatedToFullName: String = "",
)


/**
 * A utility function to launch the native Android DatePickerDialog.
 */
fun showDatePicker(
    context: Context,
    date: String = "",
    minDate: String = "",
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val todayCalendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Try parsing the provided date
    if (date.isNotBlank()) {
        try {
            val parsedDate = dateFormat.parse(date)
            parsedDate?.let {
                calendar.time = it
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else {
        calendar.time = todayCalendar.time
    }


    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

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

    // Optional: Prevent selecting less date for dod then dob
    datePickerDialog.datePicker.maxDate = todayCalendar.timeInMillis
    if (minDate.isNotBlank()) {
        try {
            val parsedDate = dateFormat.parse(minDate)
            parsedDate?.let {
               datePickerDialog.datePicker.minDate = it.time
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    datePickerDialog.show()
}


fun validateMemberData(member: MemberFormState): String {
    logger("MembersViewModel", "validateData: $member")
    val error = ""
    if (member.fullName.isBlank()) {
        return "Please enter a name"
    }
    if (member.fullName.length < 3) {
        return "Please enter a valid name"
    }
    if (member.gotra.isBlank()) {
        return "Please enter a Gotra"
    }
    if (member.gotra.length < 3) {
        return "Please enter a valid Gotra"
    }
    if (member.dob.isBlank()) {
        return "Please enter a date of birth"
    }

    if (member.gender.isBlank()) {
        return "Please select a gender"
    }

    if (!member.isLiving && member.dod.isBlank()) {
        return "Please enter a date of death"
    }

    if (member.city.isBlank()) {
        return "Please enter a city"
    }

    if (member.city.length < 3) {
        return "Please enter a valid city"
    }

    if (member.state.isBlank()) {
        return "Please enter a state"
    }

    if (member.state.length < 3) {
        return "Please enter a valid state"
    }

    return error
}



fun saveBitmapAsImage(context: Context, bitmap: Bitmap): Uri {
    val filename = "FamilyTree_${System.currentTimeMillis()}.png"
    val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    fos.close()

    val file = File(context.filesDir, filename)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

fun saveBitmapAsPdf(context: Context, bitmap: Bitmap): Uri {
    val filename = "FamilyTree_${System.currentTimeMillis()}.pdf"
    val file = File(context.filesDir, filename)

    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    canvas.drawBitmap(bitmap, 0f, 0f, null)
    document.finishPage(page)

    file.outputStream().use { document.writeTo(it) }
    document.close()

    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

fun shareFile(context: Context, uri: Uri, mimeType: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Family Tree"))
}


