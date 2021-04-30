package com.plusProject.IQuiz.frontEnd;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static com.plusProject.IQuiz.frontEnd.Difficulty.*;
import static org.junit.jupiter.api.Assertions.*;

class DifficultyTest {

    @Test
    void calculateNextDifficulty_easy() {
        EnumSet<Difficulty> given = EnumSet.of(EASY);
        Difficulty result = calculateNextDifficulty(given);
        assertEquals(MEDIUM, result);
    }

    @Test
    void calculateNextDifficulty_null() {
        EnumSet<Difficulty> given = null;
        Difficulty result = calculateNextDifficulty(given);
        assertNull(result);
    }

    @Test
    void calculateNextDifficulty_none() {
        EnumSet<Difficulty> given = EnumSet.noneOf(Difficulty.class);
        Difficulty result = calculateNextDifficulty(given);
        assertNull(result);
    }

    @Test
    void calculateNextDifficulty_medium() {
        EnumSet<Difficulty> given = EnumSet.of(MEDIUM);
        Difficulty result = calculateNextDifficulty(given);
        assertEquals(HARD, result);
    }

    @Test
    void calculateNextDifficulty_hard() {
        EnumSet<Difficulty> given = EnumSet.of(HARD);
        Difficulty result = calculateNextDifficulty(given);
        assertEquals(MEDIUM, result);
    }

    @Test
    void calculateNextDifficulty_easy_medium() {
        EnumSet<Difficulty> given = EnumSet.of(EASY, MEDIUM);
        Difficulty result = calculateNextDifficulty(given);
        assertEquals(HARD, result);
    }

    @Test
    void calculateNextDifficulty_medium_hard() {
        EnumSet<Difficulty> given = EnumSet.of(MEDIUM, HARD);
        Difficulty result = calculateNextDifficulty(given);
        assertEquals(EASY, result);
    }

    @Test
    void calculateNextDifficulty_hard_easy() {
        EnumSet<Difficulty> given = EnumSet.of(HARD, EASY);
        Difficulty result = calculateNextDifficulty(given);
        assertEquals(MEDIUM, result);
    }

    @Test
    void calculateNextDifficulty_all() {
        EnumSet<Difficulty> given = EnumSet.of(EASY, MEDIUM, HARD);
        Difficulty result = calculateNextDifficulty(given);
        assertNull(result);
    }
}
