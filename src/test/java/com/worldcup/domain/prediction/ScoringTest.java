package com.worldcup.domain.prediction;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test — no Spring context.
 * Mirrors the scoring formula in PredictionController.recalculate() exactly.
 */
class ScoringTest {

    private static int score(int p1, int p2, int a1, int a2) {
        if (p1 == a1 && p2 == a2) return 3;
        if (Integer.signum(p1 - p2) == Integer.signum(a1 - a2)) return 1;
        return 0;
    }

    @ParameterizedTest(name = "predicted {0}-{1}, actual {2}-{3} → {4} pts")
    @CsvSource({
        // ---- exact score → 3 pts ----
        "2, 1, 2, 1, 3",
        "0, 0, 0, 0, 3",
        "3, 0, 3, 0, 3",
        "0, 1, 0, 1, 3",

        // ---- correct result direction → 1 pt ----
        "3, 1, 2, 0, 1",   // team1 win, different margin
        "0, 2, 1, 3, 1",   // team2 win, different margin
        "1, 1, 2, 2, 1",   // draw predicted, draw actual
        "1, 0, 4, 0, 1",   // team1 big win vs team1 narrow win

        // ---- wrong result → 0 pts ----
        "2, 1, 0, 2, 0",   // predicted team1 win, actual team2 win
        "1, 1, 2, 1, 0",   // predicted draw, actual team1 win
        "2, 1, 1, 1, 0",   // predicted team1 win, actual draw
        "0, 1, 2, 0, 0",   // predicted team2 win, actual team1 win
        "0, 0, 1, 0, 0",   // predicted draw, actual team1 win
    })
    void scoring(int p1, int p2, int a1, int a2, int expected) {
        assertThat(score(p1, p2, a1, a2)).isEqualTo(expected);
    }
}
