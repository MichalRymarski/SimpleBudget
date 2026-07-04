package prayit.simplebudget.feature.home.state

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.composables.icons.lucide.Apple
import com.composables.icons.lucide.Ellipsis
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Monitor
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.UtensilsCrossed
import com.composables.icons.lucide.Zap

enum class FinancialTag(
    val color: Color,
    val icon: @Composable () -> Unit,
) {
    Groceries(
        color = Color(0xFF4CAF50),
        icon = { Icon(Lucide.Apple, contentDescription = null) },
    ),
    EatingOut(
        color = Color(0xFFFF9800),
        icon = { Icon(Lucide.UtensilsCrossed, contentDescription = null) },
    ),
    Health(
        color = Color(0xFFE91E63),
        icon = { Icon(Lucide.Heart, contentDescription = null) },
    ),
    Bills(
        color = Color(0xFF2196F3),
        icon = { Icon(Lucide.Zap, contentDescription = null) },
    ),
    Cosmetics(
        color = Color(0xFF9C27B0),
        icon = { Icon(Lucide.Sparkles, contentDescription = null) },
    ),
    Technology(
        color = Color(0xFF00BCD4),
        icon = { Icon(Lucide.Monitor, contentDescription = null) },
    ),
    Misc(
        color = Color(0xFF9E9E9E),
        icon = { Icon(Lucide.Ellipsis, contentDescription = null) },
    ),
}
