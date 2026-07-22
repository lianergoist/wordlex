package dk.rpix.wordle.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

data class UiMessage(
    @StringRes val resId: Int,
    val args: List<Any> = emptyList(),
    @StringRes val suffixId: Int? = null,
    val suffixArgs: List<Any> = emptyList()
)

@Composable
fun UiMessage.asString(): String {
    val base = stringResource(resId, *args.toTypedArray())
    val suffix = suffixId?.let { "\n" + stringResource(it, *suffixArgs.toTypedArray()) } ?: ""
    return base + suffix
}
