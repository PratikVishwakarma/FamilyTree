# 🌳 Family Tree — Android App

A modern **Family Tree management app** built using **Kotlin**, **Jetpack Compose**, and **MVVM architecture**, designed to visually represent family connections and member ancestry with ease.

---

## 🧱 Tech Stack

| Layer | Libraries / Tools |
|-------|--------------------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Dependency Injection** | Hilt |
| **Database / Persistence** | Room |
| **Navigation** | Type-safe Compose Navigation with `@Serializable` routes |
| **Async / Coroutines** | Kotlin Coroutines + Flow |
| **Paging / Search** | Paging 3 + Flow |
| **Design** | Material 3 Components |

---

## 📱 App Features

### 👤 Member Management
- Add new family members with essential details:  
  *Name, Gender, Date of Birth, City, Mobile Number, Living Status, etc.*
- Edit existing member details.
- Intuitive **Date Picker** for date selection (`yyyy-MM-dd` format).

### 🔍 Member Search
- Fast, debounced search powered by **Paging 3** and **Flow**.
- Dynamic filtering (e.g., gender-based filters for relation selection).
- Optimized for large family data trees.

### 🧬 Member Details
- Detailed profile view with:
  - Personal Information
  - Relations (Parents, Spouse, Children, Siblings, Grandchildren)
- Color-coded sections and icons for easy visual parsing.
- Age calculated dynamically from date of birth.

### ❤️ Add Relations
- Add new relations between any two family members.
- Smart filtering to ensure valid relation types based on gender:
  - Example: Male → can be *Father, Husband, Son*
  - Female → can be *Mother, Wife, Daughter*
  - *Sibling* is common to all.
- Option to search and select a related person from an existing list.

### 🌿 Ancestry Map Screen
- Visual representation of family hierarchy.
- Displays relationships across generations.
- Supports navigating up and down ancestry lines.
