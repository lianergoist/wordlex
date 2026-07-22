package dk.rpix.wordle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.rpix.wordle.R
import dk.rpix.wordle.game.LetterStatus
import dk.rpix.wordle.ui.theme.*

@Composable
fun WordleKeyboard(
    keyboardState: Map<Char, LetterStatus>,
    onLetterInput: (Char) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    keyWidth: Dp = 36.dp,
    keyHeight: Dp = 56.dp,
    spacing: Dp = 6.dp,
    rows: List<List<Char>> = listOf(
        "QWERTYUIOP".toList(),
        "ASDFGHJKL".toList(),
        "ZXCVBNM".toList()
    )
) {

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEachIndexed { rowIndex, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (rowIndex == 2) {
                    KeyItem(
                        text = stringResource(R.string.content_desc_enter),
                        modifier = Modifier.width(keyWidth * 2.2f),
                        keyHeight = keyHeight,
                        keyWidth = keyWidth,
                        onClick = onSubmit
                    )
                }

                row.forEach { char ->
                    KeyItem(
                        text = char.toString(),
                        status = keyboardState[char] ?: LetterStatus.EMPTY,
                        keyHeight = keyHeight,
                        keyWidth = keyWidth,
                        onClick = { onLetterInput(char) }
                    )
                }

                if (rowIndex == 2) {
                    KeyItem(
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                contentDescription = stringResource(R.string.content_desc_delete),
                                modifier = Modifier.size(keyWidth * 0.6f)
                            )
                        },
                        modifier = Modifier.width(keyWidth * 1.8f),
                        keyHeight = keyHeight,
                        keyWidth = keyWidth,
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun KeyItem(
    text: String? = null,
    icon: @Composable (() -> Unit)? = null,
    status: LetterStatus = LetterStatus.EMPTY,
    modifier: Modifier = Modifier,
    keyWidth: Dp = 36.dp,
    keyHeight: Dp = 56.dp,
    onClick: () -> Unit
) {
    val backgroundColor = when (status) {
        LetterStatus.CORRECT -> WordleCorrect
        LetterStatus.PRESENT -> WordlePresent
        LetterStatus.ABSENT -> WordleAbsent
        LetterStatus.EMPTY -> if (isSystemInDarkTheme()) WordleEmptyDark else WordleEmpty
    }

    val contentColor = if (status == LetterStatus.EMPTY) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.White
    }

    Box(
        modifier = modifier
            .height(keyHeight)
            .width(keyWidth)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                fontSize = if (text.length > 1) (keyWidth.value * 0.3).sp else (keyWidth.value * 0.5).sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        } else if (icon != null) {
            icon()
        }
    }
}
