package fr.estiam.traillog.model

enum class DistanceUnit { KM, MI }
enum class TrailSortOrder { NAME, DISTANCE }
data class UserSettings(
    val unit: DistanceUnit = DistanceUnit.KM,
    val sortOrder: TrailSortOrder = TrailSortOrder.NAME
)

object TrailFormatter {
    /** 1 km = 0.621371 mi (1 mi = 1.609344 km). */
    const val MILES_PER_KM = 0.621371

    fun distance(km: Double, unit: DistanceUnit): String =
        if (unit == DistanceUnit.KM) "%.1f km".format(java.util.Locale.getDefault(), km)
        else "%.1f mi".format(java.util.Locale.getDefault(), km * MILES_PER_KM)
}
