package com.danilkinkin.buckwheat.spendsHistory

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorEditor
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.*
import java.lang.Float.max
import java.lang.Float.min
import java.math.BigDecimal
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

enum class DeleteState { IDLE, DELETE }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Spent(
    spent: Spent,
    currency: ExtendCurrency,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = {},
) {
    val swipeableState = rememberSwipeableState(
        DeleteState.IDLE,
        confirmStateChange = {
            if (it == DeleteState.DELETE) onDelete()

            true
        }
    )
    val width = remember { mutableStateOf(0F)}
    val height = remember { mutableStateOf(0F)}

    val localDensity = LocalDensity.current

    val anchors = if (width.value != 0F) {
        mapOf(0f to DeleteState.IDLE, -width.value to DeleteState.DELETE)
    } else {
        mapOf(0f to DeleteState.IDLE)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                orientation = Orientation.Horizontal,
                thresholds = { _, _ -> FractionalThreshold(0.4f) },
            )
            .onGloballyPositioned {
                width.value = it.size.width.toFloat()
                height.value = it.size.height.toFloat()
            }
            .zIndex(if (swipeableState.offset.value.roundToInt() < 0f) 1f else 0f),
    ) {
        val size = with(LocalDensity.current) {
            if (swipeableState.offset.value > 0f) 0f.toDp()
            else max(min(16.dp.toPx(), abs(swipeableState.offset.value)), 0f).toDp()
        }

        if (swipeableState.direction == -1f) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(with(localDensity) { height.value.toDp() })
                    .background(MaterialTheme.colorScheme.errorContainer),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_forever),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    contentDescription = null,
                    modifier = Modifier
                        .offset(with(LocalDensity.current) {
                            (swipeableState.offset.value / 2).toDp() + 12.dp
                        })
                )
            }
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .offset { IntOffset(min(swipeableState.offset.value, 0f).roundToInt(), 0) }
                .padding(vertical = min(size, 4.dp))
                .shadow(
                    min(size, 4.dp),
                    RoundedCornerShape(size),
                ),
            color = colorEditor,
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(-min(size - 4.dp, 0.dp))
            ) {
                Text(
                    text = prettyCandyCanes(spent.value, currency = currency),
                    style = MaterialTheme.typography.displaySmall,
                    color = colorOnEditor,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
                Spacer(Modifier.weight(1F))
                Box(Modifier) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        text = prettyDate(spent.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorOnEditor,
                    )
                }
            }
        }
    }

}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        Spent(
            Spent(value = BigDecimal(12340), date = Date()),
            ExtendCurrency(type = CurrencyType.NONE)
        )
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        Spent(
            Spent(value = BigDecimal(12340), date = Date()),
            ExtendCurrency(type = CurrencyType.NONE)
        )
    }
}