package com.pmb.common.ui.scaffold.navigation.drawer

import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.offset
import com.pmb.common.ui.scaffold.model.NavigationContentPosition
import com.pmb.common.ui.scaffold.model.NavigationLayoutType

/**
 * Custom measure policy for drawer layouts that positions header and content
 * based on the specified navigation content position
 */
fun drawerMeasurePolicy(
    contentPosition: NavigationContentPosition
): MeasurePolicy {
    return MeasurePolicy { measurables, constraints ->
        lateinit var headerMeasurable: Measurable
        lateinit var contentMeasurable: Measurable
        
        measurables.forEach {
            when (it.layoutId) {
                NavigationLayoutType.HEADER -> headerMeasurable = it
                NavigationLayoutType.CONTENT -> contentMeasurable = it
                else -> error("Unknown layoutId encountered!")
            }
        }

        val headerPlaceable = headerMeasurable.measure(constraints)
        val contentPlaceable = contentMeasurable.measure(
            constraints.offset(vertical = -headerPlaceable.height)
        )
        
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Place the header at the top
            headerPlaceable.placeRelative(0, 0)

            // Calculate vertical space not used by content
            val nonContentVerticalSpace = constraints.maxHeight - contentPlaceable.height

            val contentPlaceableY = when (contentPosition) {
                NavigationContentPosition.TOP -> 0
                NavigationContentPosition.CENTER -> nonContentVerticalSpace / 2
            }
                // Ensure we don't overlap with the header
                .coerceAtLeast(headerPlaceable.height)

            contentPlaceable.placeRelative(0, contentPlaceableY)
        }
    }
}