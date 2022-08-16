package org.worldcubeassociation.tnoodle.puzzle;

import org.worldcubeassociation.tnoodle.Puzzle;
import org.worldcubeassociation.tnoodle.PuzzleSvgPainter;
import org.worldcubeassociation.tnoodle.algorithm.PuzzleStateAndGenerator;
import org.worldcubeassociation.tnoodle.drawing.SkewbPainter;
import org.worldcubeassociation.tnoodle.exceptions.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.solver.SkewbSolver;
import org.worldcubeassociation.tnoodle.state.SkewbState;
import java.util.Random;

import org.worldcubeassociation.tnoodle.solver.SkewbSolver.SkewbSolverState;

import org.timepedia.exporter.client.Export;

@Export
public class SkewbPuzzle extends Puzzle<SkewbState> {
    private static final int MIN_SCRAMBLE_LENGTH = 11;
    private final SkewbSolver skewbSolver;

    public SkewbPuzzle() {
        skewbSolver = new SkewbSolver();
        wcaMinScrambleDistance = 7;
    }

    @Override
    public PuzzleStateAndGenerator<SkewbState> generateRandomMoves(Random r) {
        SkewbSolverState state = skewbSolver.randomState(r);
        String scramble = skewbSolver.generateExactly(state, MIN_SCRAMBLE_LENGTH, r);
        assert scramble.split(" ").length == MIN_SCRAMBLE_LENGTH;

        SkewbState pState;
        try {
            pState = getSolvedState().applyAlgorithm(scramble);
        } catch (InvalidScrambleException e) {
            throw new RuntimeException(e);
        }
        return new PuzzleStateAndGenerator<>(pState, scramble);
    }

    @Override
    public String getLongName() {
        return "Skewb";
    }

    @Override
    public String getShortName() {
        return "skewb";
    }

    @Override
    public SkewbState getSolvedState() {
        return new SkewbState();
    }

    @Override
    public PuzzleSvgPainter<SkewbState> getPainter() {
        return new SkewbPainter();
    }

    @Override
    protected int getRandomMoveCount() {
        return 15;
    }
}
