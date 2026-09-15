package prayit.simplebudget.feature.budgetitem.state

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
import org.jetbrains.compose.resources.stringResource
import simplebudget.core.resources.generated.resources.Res
import simplebudget.core.resources.generated.resources.missing_accessibility

enum class ExpenseTag(
    val color: Color,
    val icon: @Composable () -> Unit,
) {
    Groceries(
        color = Color(0xFF4CAF50),
        icon = {
            Icon(
                Lucide.Apple,
                contentDescription = stringResource(Res.string.missing_accessibility)
            )
        },
    ),
    EatingOut(
        color = Color(0xFFFF9800),
        icon = {
            Icon(
                Lucide.UtensilsCrossed,
                contentDescription = stringResource(Res.string.missing_accessibility)
            )
        },
    ),
    Health(
        color = Color(0xFFE91E63),
        icon = {
            Icon(
                Lucide.Heart,
                contentDescription = stringResource(Res.string.missing_accessibility)
            )
        },
    ),
    Bills(
        color = Color(0xFF2196F3),
        icon = {
            Icon(
                Lucide.Zap,
                contentDescription = stringResource(Res.string.missing_accessibility)
            )
        },
    ),
    Cosmetics(
        color = Color(0xFF9C27B0),
        icon = {
            Icon(
                Lucide.Sparkles,
                contentDescription = stringResource(Res.string.missing_accessibility)
            )
        },
    ),
    Technology(
        color = Color(0xFF00BCD4),
        icon = {
            Icon(
                Lucide.Monitor,
                contentDescription = stringResource(Res.string.missing_accessibility)
            )
        },
    ),
    Misc(
        color = Color(0xFF9E9E9E),
        icon = {
            Icon(
                Lucide.Ellipsis,
                contentDescription = stringResource(Res.string.missing_accessibility)
            )
        },
    ),
}
