package org.worldcubeassociation.tnoodle.puzzle;

import org.worldcubeassociation.tnoodle.scrambles.*;
import org.worldcubeassociation.tnoodle.solver.TwoPhaseSquareSolver;
import org.worldcubeassociation.tnoodle.state.SquareOneState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SquareOnePuzzleTest {
    @Test
    public void testMergingMode() throws InvalidMoveException {
        Puzzle<SquareOneState> sq1 = new SquareOnePuzzle();
        PuzzleSolutionEngine<SquareOneState> engine = new TwoPhaseSquareSolver();

        AlgorithmBuilder<SquareOneState> ab = new AlgorithmBuilder<SquareOneState>(sq1, AlgorithmBuilder.MergingMode.CANONICALIZE_MOVES);

        assertEquals(ab.getTotalCost(), 0);

        ab.appendMove("(1,0)");
        assertEquals(ab.getTotalCost(), 1);

        ab.appendMove("(2,0)");
        assertEquals(ab.getTotalCost(), 1);

        ab.appendMove("(0,-1)");
        assertEquals(ab.getTotalCost(), 1);

        ab.appendMove("/");
        assertEquals(ab.getTotalCost(), 2);

        ab.appendMove("/");
        assertEquals(ab.getTotalCost(), 1);

        SquareOneState state = ab.getState();

        String solution = engine.solveIn(state, 1);
        assertEquals(solution, "(-3,1)");

        solution = engine.solveIn(state, 2);
        assertEquals(solution, "(-3,1)");
    }

    @Test
    public void testSlashabilitySolutions() throws InvalidMoveException {
        Puzzle<SquareOneState> sq1 = new SquareOnePuzzle();

        // slashability is (-1,0) which then cancels into (-3,0)
        String cancelsWithSlashability = "(3,0) / (4,0)";

        String solution = solveScrambleStringIn(sq1, cancelsWithSlashability, 3);
        assertNotNull(solution);

        // slashability is (-1, 0) which trivially doesn't cancel the / move
        String doesntCancelSlashability = "(3,0) / (1,0)";

        solution = solveScrambleStringIn(sq1, doesntCancelSlashability, 3);
        assertNotNull(solution);
    }

    private String solveScrambleStringIn(Puzzle<SquareOneState> puzzle, String scramble, int n) throws InvalidMoveException {
        PuzzleSolutionEngine<SquareOneState> engine = new TwoPhaseSquareSolver();

        AlgorithmBuilder<SquareOneState> ab = new AlgorithmBuilder<SquareOneState>(puzzle, AlgorithmBuilder.MergingMode.CANONICALIZE_MOVES);
        ab.appendAlgorithm(scramble);

        return engine.solveIn(ab.getState(), n);
    }
}
