package dk.rpix.wordle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.rpix.wordle.game.EvaluatedLetter
import dk.rpix.wordle.game.LetterStatus
import dk.rpix.wordle.ui.theme.*

@Composable
fun WordleGrid(
    guesses: List<List<EvaluatedLetter>>,
    currentGuess: String,
    modifier: Modifier = Modifier,
    focusedIndex: Int = -1,
    onCellClick: (Int) -> Unit = {},
    cellSize: Dp = 48.dp,
    spacing: Dp = 8.dp
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display past guesses
        guesses.forEach { guess ->
            WordleRow(letters = guess, cellSize = cellSize, spacing = spacing)
        }

        // Display current guess
        if (guesses.size < 6) {
            val currentLetters = currentGuess.map { EvaluatedLetter(it, LetterStatus.EMPTY) }
            WordleRow(
                letters = currentLetters,
                isCurrent = true,
                focusedIndex = focusedIndex,
                onCellClick = onCellClick,
                cellSize = cellSize,
                spacing = spacing
            )
        }

        // Display empty rows
        repeat(5 - guesses.size) {
            WordleRow(letters = List(5) { EvaluatedLetter(' ', LetterStatus.EMPTY) }, cellSize = cellSize, spacing = spacing)
        }
    }
}

@Composable
fun WordleRow(
    letters: List<EvaluatedLetter>,
    isCurrent: Boolean = false,
    focusedIndex: Int = -1,
    onCellClick: (Int) -> Unit = {},
    cellSize: Dp = 48.dp,
    spacing: Dp = 8.dp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        letters.forEachIndexed { index, letter ->
            WordleCell(
                letter = letter,
                isCurrent = isCurrent,
                isFocused = isCurrent && index == focusedIndex,
                onClick = { if (isCurrent) onCellClick(index) },
                cellSize = cellSize
            )
        }
    }
}

@Composable
fun WordleCell(
    letter: EvaluatedLetter,
    isCurrent: Boolean = false,
    isFocused: Boolean = false,
    onClick: () -> Unit = {},
    cellSize: Dp = 48.dp
) {
    val backgroundColor = when (letter.status) {
        LetterStatus.CORRECT -> WordleCorrect
        LetterStatus.PRESENT -> WordlePresent
        LetterStatus.ABSENT -> WordleAbsent
        LetterStatus.EMPTY -> Color.Transparent
    }

    val borderColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else if (letter.status == LetterStatus.EMPTY) {
        if (isCurrent && letter.char != ' ') {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
            if (isSystemInDarkTheme()) WordleEmptyDark else WordleEmpty
        }
    } else {
        Color.Transparent
    }

    val borderWidth = if (isFocused) 3.dp else 2.dp

    val textColor = if (letter.status == LetterStatus.EMPTY) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
            .clickable(enabled = isCurrent) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.char.uppercase(),
            fontSize = (cellSize.value * 0.5).sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
