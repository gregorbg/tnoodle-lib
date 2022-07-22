package org.worldcubeassociation.tnoodle.puzzle;

import org.worldcubeassociation.tnoodle.scrambles.AlgorithmBuilder;
import org.worldcubeassociation.tnoodle.scrambles.InvalidMoveException;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleStateAndGenerator;
import org.junit.jupiter.api.Test;
import org.worldcubeassociation.tnoodle.state.CubeState;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoInspectionFiveByFiveTest {
    @Test
    public void testSomething() throws InvalidMoveException {
        CubePuzzle fives = new NoInspectionFiveByFiveCubePuzzle();
        CubeState solvedState = fives.getSolvedState();

        CubeState.CubeMove dummyMove = solvedState.new CubeMove(CubeState.Face.U, 1, 3);
        CubeState.CubeMove[] reorient = new CubeState.CubeMove[]{ dummyMove };

        assertEquals(reorient[0].toString(), "4Uw");

        AlgorithmBuilder<CubeState> ab = new AlgorithmBuilder<CubeState>(fives, AlgorithmBuilder.MergingMode.NO_MERGING);
        ab.appendAlgorithm("F R");

        PuzzleStateAndGenerator<CubeState> psag1 = ab.getStateAndGenerator();
        PuzzleStateAndGenerator<CubeState> psag2 = NoInspectionFiveByFiveCubePuzzle.applyOrientation(fives, reorient, psag1, true);
        //The scramble (F R) and the reorient (4Uw) don't conflict,
        //so the resulting scramble should be "F R 4Uw"
        assertEquals(psag2.generator, "F R 4Uw");

        ab = new AlgorithmBuilder<CubeState>(fives, AlgorithmBuilder.MergingMode.NO_MERGING);
        ab.appendAlgorithm("F D");

        psag1 = ab.getStateAndGenerator();
        psag2 = NoInspectionFiveByFiveCubePuzzle.applyOrientation(fives, reorient, psag1, true);
        //The scramble (F D) and the reorient (4Uw) are redundant.
        //The problematic D turn should be removed, and the resulting
        //scramble should be "F 4Uw"
        assertEquals(psag2.generator, "F 4Uw");

        ab = new AlgorithmBuilder<CubeState>(fives, AlgorithmBuilder.MergingMode.NO_MERGING);
        ab.appendAlgorithm("D U D U");

        psag1 = ab.getStateAndGenerator();
        psag2 = NoInspectionFiveByFiveCubePuzzle.applyOrientation(fives, reorient, psag1, true);
        //The scramble (D U D U) and the reorient (4Uw) are redundant.
        //The problematic D turns should be removed, and the resulting
        //scramble should be "U U 4Uw"
        assertEquals(psag2.generator, "U U 4Uw");
    }
}
