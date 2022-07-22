package org.worldcubeassociation.tnoodle.puzzle;

import java.util.Random;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleStateAndGenerator;
import org.worldcubeassociation.tnoodle.scrambles.InvalidMoveException;
import org.worldcubeassociation.tnoodle.scrambles.AlgorithmBuilder;
import org.timepedia.exporter.client.Export;
import org.worldcubeassociation.tnoodle.state.CubeState;

@Export
public class NoInspectionFourByFourCubePuzzle extends FourByFourCubePuzzle {
    public NoInspectionFourByFourCubePuzzle() {
        super();
    }

    @Override
    public PuzzleStateAndGenerator<CubeState> generateRandomMoves(Random r) {
        PuzzleStateAndGenerator<CubeState> psag = super.generateRandomMoves(r);

        CubeState.CubeMove[][] randomOrientationMoves = psag.state.getRandomOrientationMoves(size - 1);
        CubeState.CubeMove[] randomOrientation = randomOrientationMoves[r.nextInt(randomOrientationMoves.length)];

        return applyOrientation(this, randomOrientation, psag, true);
    }

    public static PuzzleStateAndGenerator<CubeState> applyOrientation(CubePuzzle puzzle, CubeState.CubeMove[] randomOrientation, PuzzleStateAndGenerator<CubeState> psag, boolean discardRedundantMoves) {
        if(randomOrientation.length == 0) {
            // No reorientation required
            return psag;
        }

        // Append reorientation to scramble.
        try {
            AlgorithmBuilder<CubeState> ab = new AlgorithmBuilder<CubeState>(puzzle, AlgorithmBuilder.MergingMode.NO_MERGING);
            ab.appendAlgorithm(psag.generator);
            for(CubeState.CubeMove cm : randomOrientation) {
                ab.appendMove(cm.toString());
            }

            psag = ab.getStateAndGenerator();
            return psag;
        } catch(InvalidMoveException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getShortName() {
        return "444ni";
    }

    @Override
    public String getLongName() {
        return "4x4x4 no inspection";
    }
}
