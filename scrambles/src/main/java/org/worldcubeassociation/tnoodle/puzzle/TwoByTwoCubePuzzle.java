package org.worldcubeassociation.tnoodle.puzzle;

import java.util.Random;

import org.worldcubeassociation.tnoodle.scrambles.*;
import org.worldcubeassociation.tnoodle.scrambles.AlgorithmBuilder.MergingMode;
import org.worldcubeassociation.tnoodle.solver.TwoByTwoSolver;
import org.worldcubeassociation.tnoodle.solver.TwoByTwoSolver.TwoByTwoState;
import org.timepedia.exporter.client.Export;
import org.worldcubeassociation.tnoodle.state.CubeState;

@Export
public class TwoByTwoCubePuzzle extends CubePuzzle {
    private static final int TWO_BY_TWO_MIN_SCRAMBLE_LENGTH = 11;

    private final TwoByTwoSolver twoSolver;
    public TwoByTwoCubePuzzle() {
        super(2);
        wcaMinScrambleDistance = 4;
        twoSolver = new TwoByTwoSolver();
    }

    @Override
    public PuzzleStateAndGenerator<CubeState> generateRandomMoves(Random r) {
        TwoByTwoState state = twoSolver.randomState(r);
        String scramble = twoSolver.generateExactly(state, TWO_BY_TWO_MIN_SCRAMBLE_LENGTH);
        assert scramble.split(" ").length == TWO_BY_TWO_MIN_SCRAMBLE_LENGTH;

        AlgorithmBuilder<CubeState> ab = new AlgorithmBuilder<>(this, MergingMode.CANONICALIZE_MOVES);
        try {
            ab.appendAlgorithm(scramble);
        } catch (InvalidMoveException e) {
            throw new RuntimeException(new InvalidScrambleException(scramble, e));
        }
        return ab.getStateAndGenerator();
    }

    @Override
    public PuzzleSolutionEngine<CubeState> getSolutionEngine() {
        return twoSolver;
    }
}
