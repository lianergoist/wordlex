package dk.rpix.wordle.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordleGameEngineTest {

    private val engine = WordleGameEngine()

    @Test
    fun testEvaluateGuess_AllCorrect() {
        val target = "apple"
        val guess = "apple"
        val result = engine.evaluateGuess(guess, target)
        
        assertTrue(result.all { it.status == LetterStatus.CORRECT })
    }

    @Test
    fun testEvaluateGuess_Mixed() {
        val target = "apple"
        val guess = "apply"
        val result = engine.evaluateGuess(guess, target)
        
        assertEquals(LetterStatus.CORRECT, result[0].status) // a
        assertEquals(LetterStatus.CORRECT, result[1].status) // p
        assertEquals(LetterStatus.CORRECT, result[2].status) // p
        assertEquals(LetterStatus.CORRECT, result[3].status) // l
        assertEquals(LetterStatus.ABSENT, result[4].status)  // y vs e
    }

    @Test
    fun testEvaluateGuess_Present() {
        val target = "apple"
        val guess = "plead"
        val result = engine.evaluateGuess(guess, target)
        
        assertEquals(LetterStatus.PRESENT, result[0].status) // p
        assertEquals(LetterStatus.PRESENT, result[1].status) // l
        assertEquals(LetterStatus.PRESENT, result[2].status) // e
        assertEquals(LetterStatus.PRESENT, result[3].status) // a
        assertEquals(LetterStatus.ABSENT, result[4].status)  // d
    }

    @Test
    fun testIsPossibleWord_Simple() {
        val target = "apple"
        val guess = engine.evaluateGuess("apply", target)
        val guesses = listOf(guess)
        
        assertTrue(engine.isPossibleWord("apple", guesses))
        assertFalse(engine.isPossibleWord("apply", guesses))
        assertFalse(engine.isPossibleWord("berry", guesses))
    }

    @Test
    fun testIsPossibleWord_DoubleLetters() {
        val target = "abbey"
        val guess = engine.evaluateGuess("babes", target)
        val guesses = listOf(guess)
        
        // Target: abbey, Guess: babes
        // b (pos 0) -> present
        // a (pos 1) -> present
        // b (pos 2) -> correct
        // e (pos 3) -> correct
        // s (pos 4) -> absent
        
        assertTrue(engine.isPossibleWord("abbey", guesses))
    }
}
