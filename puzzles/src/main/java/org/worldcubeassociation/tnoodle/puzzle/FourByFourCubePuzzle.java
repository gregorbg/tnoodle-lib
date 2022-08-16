package org.worldcubeassociation.tnoodle.puzzle;

import java.util.Random;

import org.worldcubeassociation.tnoodle.algorithm.AlgorithmBuilder;
import org.worldcubeassociation.tnoodle.algorithm.PuzzleStateAndGenerator;
import org.worldcubeassociation.tnoodle.exceptions.InvalidMoveException;
import org.worldcubeassociation.tnoodle.exceptions.InvalidScrambleException;
import org.timepedia.exporter.client.Export;
import org.worldcubeassociation.tnoodle.solver.ThreePhaseCubeSolver;
import org.worldcubeassociation.tnoodle.state.CubeState;

@Export
public class FourByFourCubePuzzle extends CubePuzzle {
    private final ThreePhaseCubeSolver threePhaseEngine;

    public FourByFourCubePuzzle() {
        super(4);
        this.threePhaseEngine = new ThreePhaseCubeSolver();
    }

    @Override
    public PuzzleStateAndGenerator<CubeState> generateRandomMoves(Random r) {
        String scramble = threePhaseEngine.generateRandomScramble(r);
        AlgorithmBuilder<CubeState> ab = new AlgorithmBuilder<>(AlgorithmBuilder.MergingMode.CANONICALIZE_MOVES, getSolvedState());
        try {
            ab.appendAlgorithm(scramble);
        } catch (InvalidMoveException e) {
            throw new RuntimeException(new InvalidScrambleException(scramble, e));
        }
        return ab.getStateAndGenerator();
    }
}
