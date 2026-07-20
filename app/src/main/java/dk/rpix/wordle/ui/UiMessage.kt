package dk.rpix.wordle.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

data class UiMessage(
    @StringRes val resId: Int,
    val args: List<Any> = emptyList()
)

@Composable
fun UiMessage.asString(): String {
    return stringResource(resId, *args.toTypedArray())
}
