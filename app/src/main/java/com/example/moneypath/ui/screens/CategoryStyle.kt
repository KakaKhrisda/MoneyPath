package com.example.moneypath.ui.screens

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.moneypath.data.models.CategoryItem

// Icons the user can pick from when creating or editing a category
val categoryIconOptions: List<Pair<String, ImageVector>> = listOf(
    "Restaurant" to Icons.Default.Restaurant,
    "DirectionsBus" to Icons.Default.DirectionsBus,
    "Description" to Icons.Default.Description,
    "Favorite" to Icons.Default.Favorite,
    "ShoppingCart" to Icons.Default.ShoppingCart,
    "SportsEsports" to Icons.Default.SportsEsports,
    "School" to Icons.Default.School,
    "Home" to Icons.Default.Home,
    "FitnessCenter" to Icons.Default.FitnessCenter,
    "Movie" to Icons.Default.Movie,
    "Work" to Icons.Default.Work,
    "Payments" to Icons.Default.Payments
)

private val extraIcons: Map<String, ImageVector> = mapOf(
    "DirectionsCar" to Icons.Default.DirectionsCar,
    "ShoppingBag" to Icons.Default.ShoppingBag,
    "Receipt" to Icons.Default.Receipt,
    "TrendingUp" to Icons.Default.TrendingUp,
    "Savings" to Icons.Default.Savings,
    "Category" to Icons.Default.Category
)

fun categoryIconFor(iconName: String): ImageVector =
    categoryIconOptions.firstOrNull { it.first == iconName }?.second
        ?: extraIcons[iconName]
        ?: Icons.Default.Category

// Color <-> stored value. Always keep the ARGB value in the low 32 bits.
fun colorToHex(color: Color): Long = color.toArgb().toLong() and 0xFFFFFFFFL

// Live copy of the category list, kept up to date by the ViewModel.
// Because it is Compose state, every screen using it refreshes when a category changes.
object CategoryStyleRegistry {
    val items = mutableStateMapOf<String, CategoryItem>()

    fun sync(list: List<CategoryItem>) {
        Snapshot.withMutableSnapshot {
            items.clear()
            list.forEach { items[it.name] = it }
        }
    }
}