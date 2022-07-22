package org.worldcubeassociation.tnoodle.puzzle;

import java.util.Random;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleStateAndGenerator;
import org.timepedia.exporter.client.Export;
import org.worldcubeassociation.tnoodle.state.CubeState;

@Export
public class NoInspectionThreeByThreeCubePuzzle extends ThreeByThreeCubePuzzle {
    public NoInspectionThreeByThreeCubePuzzle() {
        super();
    }

    @Override
    public PuzzleStateAndGenerator<CubeState> generateRandomMoves(Random r) {
        CubeState.CubeMove[][] randomOrientationMoves = getSolvedState().getRandomOrientationMoves(size / 2);
        CubeState.CubeMove[] randomOrientation = randomOrientationMoves[r.nextInt(randomOrientationMoves.length)];

        String firstAxisRestriction;
        if(randomOrientation.length > 0) {
            CubeState.Face restrictedFace = randomOrientation[0].face;
            // Restrictions are for an entire axis, so this will also
            // prevent the opposite of restrictedFace from being the first
            // move of our solution. This ensures that randomOrientation will
            // never be redundant with our scramble.
            firstAxisRestriction = restrictedFace.toString();
        } else {
            firstAxisRestriction = null;
        }

        PuzzleStateAndGenerator<CubeState> psag = super.generateRandomMoves(r, firstAxisRestriction, null);
        return NoInspectionFiveByFiveCubePuzzle.applyOrientation(this, randomOrientation, psag, false);
    }

    @Override
    public String getShortName() {
        return "333ni";
    }

    @Override
    public String getLongName() {
        return "3x3x3 no inspection";
    }
}
