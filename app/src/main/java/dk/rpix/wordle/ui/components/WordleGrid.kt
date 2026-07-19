package dk.rpix.wordle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.rpix.wordle.game.EvaluatedLetter
import dk.rpix.wordle.game.LetterStatus
import dk.rpix.wordle.ui.theme.*

@Composable
fun WordleGrid(
    guesses: List<List<EvaluatedLetter>>,
    currentGuess: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display past guesses
        guesses.forEach { guess ->
            WordleRow(letters = guess)
        }

        // Display current guess
        if (guesses.size < 6) {
            val currentLetters = currentGuess.map { EvaluatedLetter(it, LetterStatus.EMPTY) }
            val paddedLetters = currentLetters + List(5 - currentLetters.size) { EvaluatedLetter(' ', LetterStatus.EMPTY) }
            WordleRow(letters = paddedLetters, isCurrent = true)
        }

        // Display empty rows
        repeat(5 - guesses.size) {
            WordleRow(letters = List(5) { EvaluatedLetter(' ', LetterStatus.EMPTY) })
        }
    }
}

@Composable
fun WordleRow(
    letters: List<EvaluatedLetter>,
    isCurrent: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        letters.forEach { letter ->
            WordleCell(letter = letter, isCurrent = isCurrent)
        }
    }
}

@Composable
fun WordleCell(
    letter: EvaluatedLetter,
    isCurrent: Boolean = false
) {
    val backgroundColor = when (letter.status) {
        LetterStatus.CORRECT -> WordleCorrect
        LetterStatus.PRESENT -> WordlePresent
        LetterStatus.ABSENT -> WordleAbsent
        LetterStatus.EMPTY -> Color.Transparent
    }

    val borderColor = if (letter.status == LetterStatus.EMPTY) {
        if (isCurrent && letter.char != ' ') {
            MaterialTheme.colorScheme.primary
        } else {
            if (isSystemInDarkTheme()) WordleEmptyDark else WordleEmpty
        }
    } else {
        Color.Transparent
    }

    val textColor = if (letter.status == LetterStatus.EMPTY) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .border(2.dp, borderColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.char.uppercase(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
