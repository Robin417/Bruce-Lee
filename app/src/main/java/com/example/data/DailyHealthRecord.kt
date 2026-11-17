package com.example.data

enum class MedicineSlot {
  MORNING,
  LUNCH,
  DINNER,
}

data class DailyHealthRecord(
  val dateKey: String,
  val morningTaken: Boolean = false,
  val lunchTaken: Boolean = false,
  val dinnerTaken: Boolean = false,
  val waterCount: Int = 0,
) {
  val takenMedicineCount: Int
    get() = listOf(morningTaken, lunchTaken, dinnerTaken).count { it }

  val allMedicinesTaken: Boolean
    get() = morningTaken && lunchTaken && dinnerTaken
}
