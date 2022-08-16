package org.worldcubeassociation.tnoodle.scrambles;

import org.worldcubeassociation.tnoodle.PuzzleState;
import org.worldcubeassociation.tnoodle.WcaEvent;
import org.worldcubeassociation.tnoodle.algorithm.AlgorithmBuilder;
import org.worldcubeassociation.tnoodle.exceptions.InvalidMoveException;
import org.worldcubeassociation.tnoodle.puzzle.CubePuzzle;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlgorithmBuilderTest {
    @Test
    void testRedundantMoves() throws InvalidMoveException {
        // This test doesn't really belong here, but I don't have a better
        // place for it right now.
        CubePuzzle sixes = new CubePuzzle(6);
        Set<String> moves = sixes.getSolvedState().getScrambleSuccessors().keySet();

        assertFalse(moves.contains("3Bw"));
        assertFalse(moves.contains("3Lw"));
        assertFalse(moves.contains("3Dw"));

        for (WcaEvent event : WcaEvent.values()) {
            WcaScrambler<? extends PuzzleState> scrambler = WcaScrambler.getForEvent(event);

            System.out.println("Testing redundant moves on " + scrambler.getKey());

            for (String move : scrambler.getSolvedState().getSuccessorsByName().keySet()) {
                AlgorithmBuilder<? extends PuzzleState> ab = scrambler.startAlgorithmBuilder(AlgorithmBuilder.MergingMode.NO_MERGING);
                ab.appendAlgorithm(move);

                // Right now, it is true to say that for every single WCA puzzle,
                // applying the same move twice is redundant.
                assertTrue(ab.isRedundant(move));
            }
        }
    }
}
