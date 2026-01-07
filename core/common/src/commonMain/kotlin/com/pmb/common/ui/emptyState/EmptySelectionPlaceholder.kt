
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme

@Composable
fun EmptySelectionPlaceholder(
    modifier: Modifier = Modifier.fillMaxSize(),
    title: String = "Nothing Selected",
    subtitle: String = "Choose an item or create a new one to continue"
) {
    val colorScheme = AppTheme.colors
    val shapes = MaterialTheme.shapes
    val typography = AppTheme.typography

    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        val infinite = rememberInfiniteTransition(label = "empty_transition")

        val floatOffset by infinite.animateFloat(
            initialValue = 0f,
            targetValue = -10f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float"
        )

        val pulse by infinite.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .padding(24.dp)
        ) {

            Surface(
                modifier = Modifier.size(width = 240.dp, height = 150.dp),
                color = colorScheme.surfaceVariant,
                shape = shapes.medium,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PlaceholderBar(
                                width = 80.dp,
                                color = colorScheme.surface,
                                shape = shapes.small
                            )
                            PlaceholderBar(
                                width = 80.dp,
                                color = colorScheme.surface,
                                shape = shapes.small
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        PlaceholderBar(
                            width = 150.dp,
                            color = colorScheme.surface,
                            shape = shapes.small
                        )
                    }

                    // pulsating dot (accent based on theme)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-14).dp, y = 14.dp)
                            .size((12 * pulse).dp)
                            .background(colorScheme.primary, CircleShape)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = title,
                style = typography.titleMedium,
                color = colorScheme.onBackground
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
private fun PlaceholderBar(
    width: Dp,
    color: Color,
    shape: Shape,
) {
    Box(
        modifier = Modifier
            .size(width, 16.dp)
            .background(color, shape)
    )
}
