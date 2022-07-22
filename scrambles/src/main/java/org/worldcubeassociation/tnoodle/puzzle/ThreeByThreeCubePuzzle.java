package org.worldcubeassociation.tnoodle.puzzle;

import java.util.Random;

import org.worldcubeassociation.tnoodle.scrambles.*;
import org.worldcubeassociation.tnoodle.scrambles.AlgorithmBuilder.MergingMode;
import org.timepedia.exporter.client.Export;
import org.worldcubeassociation.tnoodle.solver.TwoPhaseCubeSolver;
import org.worldcubeassociation.tnoodle.state.CubeState;

@Export
public class ThreeByThreeCubePuzzle extends CubePuzzle {
    private final TwoPhaseCubeSolver twoPhaseSearcher;
    public ThreeByThreeCubePuzzle() {
        super(3);
        String newMinDistance = System.getenv("TNOODLE_333_MIN_DISTANCE");
        if(newMinDistance != null) {
            wcaMinScrambleDistance = Integer.parseInt(newMinDistance);
        }
        twoPhaseSearcher = new TwoPhaseCubeSolver();
    }

    @Override
    public PuzzleSolutionEngine<CubeState> getSolutionEngine() {
        return twoPhaseSearcher;
    }

    public PuzzleStateAndGenerator<CubeState> generateRandomMoves(Random r, String firstAxisRestriction, String lastAxisRestriction) {
        String scramble = twoPhaseSearcher.generateRandomScramble(r, firstAxisRestriction, lastAxisRestriction);
        AlgorithmBuilder<CubeState> ab = new AlgorithmBuilder<>(this, MergingMode.CANONICALIZE_MOVES);
        try {
            ab.appendAlgorithm(scramble);
        } catch (InvalidMoveException e) {
            throw new RuntimeException(new InvalidScrambleException(scramble, e));
        }
        return ab.getStateAndGenerator();
    }
    @Override
    public PuzzleStateAndGenerator<CubeState> generateRandomMoves(Random r) {
        return generateRandomMoves(r, null, null);
    }
}
