package dk.rpix.wordle.game

enum class LetterStatus {
    CORRECT, PRESENT, ABSENT, EMPTY
}

data class EvaluatedLetter(
    val char: Char,
    val status: LetterStatus
)

class WordleGameEngine {

    fun evaluateGuess(guess: String, target: String): List<EvaluatedLetter> {
        if (guess.length != 5 || target.length != 5) {
            return guess.map { EvaluatedLetter(it, LetterStatus.EMPTY) }
        }

        val result = MutableList(5) { index -> EvaluatedLetter(guess[index], LetterStatus.ABSENT) }
        val targetCharCounts = mutableMapOf<Char, Int>()

        // Pass 1: Mark CORRECT letters and count remaining target letters
        for (i in 0 until 5) {
            if (guess[i] == target[i]) {
                result[i] = EvaluatedLetter(guess[i], LetterStatus.CORRECT)
            } else {
                targetCharCounts[target[i]] = targetCharCounts.getOrDefault(target[i], 0) + 1
            }
        }

        // Pass 2: Mark PRESENT letters
        for (i in 0 until 5) {
            if (result[i].status != LetterStatus.CORRECT) {
                val char = guess[i]
                if (targetCharCounts.getOrDefault(char, 0) > 0) {
                    result[i] = EvaluatedLetter(char, LetterStatus.PRESENT)
                    targetCharCounts[char] = targetCharCounts[char]!! - 1
                }
            }
        }

        return result
    }

    fun isPossibleWord(word: String, guesses: List<List<EvaluatedLetter>>): Boolean {
        if (word.length != 5) return false
        val wordLower = word.lowercase()

        for (guess in guesses) {
            // Count letter frequencies in the current candidate word
            val candidateCharCounts = wordLower.groupingBy { it }.eachCount().toMutableMap()
            
            // Check Correct positions
            for (i in 0 until 5) {
                if (guess[i].status == LetterStatus.CORRECT) {
                    if (wordLower[i] != guess[i].char.lowercaseChar()) return false
                    candidateCharCounts[wordLower[i]] = candidateCharCounts[wordLower[i]]!! - 1
                }
            }

            // Check Absent and Present
            for (i in 0 until 5) {
                val guessChar = guess[i].char.lowercaseChar()
                when (guess[i].status) {
                    LetterStatus.PRESENT -> {
                        // Present means it MUST be in the word but NOT at this position
                        if (wordLower[i] == guessChar) return false
                        if (candidateCharCounts.getOrDefault(guessChar, 0) <= 0) return false
                        candidateCharCounts[guessChar] = candidateCharCounts[guessChar]!! - 1
                    }
                    LetterStatus.ABSENT -> {
                        // Absent means this specific instance of the letter is not in the word.
                        // However, if the same letter was marked CORRECT or PRESENT elsewhere in the guess, 
                        // it just means there are no MORE instances of it.
                        if (candidateCharCounts.getOrDefault(guessChar, 0) > 0) {
                            // If it was already accounted for by CORRECT/PRESENT, it's okay.
                            // But we need to be careful: if a letter is ABSENT, it means there are 
                            // no more occurrences of that letter in the target word than what we already found.
                            
                            // Let's refine: A word is invalid if it has a letter at a position where it was marked ABSENT
                            // AND that letter is not present in the target word at all, OR we've already found all instances.
                            
                            // Simple rule for Wordle hint systems: 
                            // If a letter is marked ABSENT, the target word cannot contain it at all UNLESS
                            // that same letter is also CORRECT or PRESENT in the same guess.
                            val countInGuess = guess.count { it.char.lowercaseChar() == guessChar && it.status != LetterStatus.ABSENT }
                            val countInCandidate = wordLower.count { it == guessChar }
                            if (countInCandidate > countInGuess) return false
                        }
                    }
                    else -> {}
                }
            }
        }
        return true
    }
}
