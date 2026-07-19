package dk.rpix.wordle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.rpix.wordle.game.LetterStatus
import dk.rpix.wordle.ui.theme.*

@Composable
fun WordleKeyboard(
    keyboardState: Map<Char, LetterStatus>,
    onLetterInput: (Char) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        "QWERTYUIOP".toList(),
        "ASDFGHJKL".toList(),
        "ZXCVBNM".toList()
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEachIndexed { rowIndex, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (rowIndex == 2) {
                    KeyItem(
                        text = "ENTER",
                        modifier = Modifier.width(64.dp),
                        onClick = onSubmit
                    )
                }

                row.forEach { char ->
                    KeyItem(
                        text = char.toString(),
                        status = keyboardState[char] ?: LetterStatus.EMPTY,
                        onClick = { onLetterInput(char) }
                    )
                }

                if (rowIndex == 2) {
                    KeyItem(
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                contentDescription = "Delete"
                            )
                        },
                        modifier = Modifier.width(64.dp),
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
            .height(56.dp)
            .width(36.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                fontSize = if (text.length > 1) 12.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        } else if (icon != null) {
            icon()
        }
    }
}
