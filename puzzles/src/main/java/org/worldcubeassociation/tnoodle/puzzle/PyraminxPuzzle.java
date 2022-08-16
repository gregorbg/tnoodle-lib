package org.worldcubeassociation.tnoodle.puzzle;

import org.worldcubeassociation.tnoodle.Puzzle;
import org.worldcubeassociation.tnoodle.PuzzleSolutionEngine;
import org.worldcubeassociation.tnoodle.algorithm.PuzzleStateAndGenerator;
import org.worldcubeassociation.tnoodle.exceptions.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.solver.PyraminxSolver;
import org.worldcubeassociation.tnoodle.state.PyraminxState;
import java.util.Random;

import org.worldcubeassociation.tnoodle.solver.PyraminxSolver.PyraminxSolverState;

import org.timepedia.exporter.client.Export;

@Export
public class PyraminxPuzzle extends Puzzle<PyraminxState> {
    private static final int MIN_SCRAMBLE_LENGTH = 11;
    private final PyraminxSolver pyraminxSolver;

    public PyraminxPuzzle() {
        pyraminxSolver = new PyraminxSolver();
        wcaMinScrambleDistance = 6;
    }

    @Override
    public PuzzleStateAndGenerator<PyraminxState> generateRandomMoves(Random r) {
        PyraminxSolverState state = pyraminxSolver.randomState(r);
        String scramble = pyraminxSolver.generateExactly(state, MIN_SCRAMBLE_LENGTH, false);
        assert scramble.split(" ").length == MIN_SCRAMBLE_LENGTH + state.unsolvedTips();

        PyraminxState pState;
        try {
            pState = getSolvedState().applyAlgorithm(scramble);
        } catch (InvalidScrambleException e) {
            throw new RuntimeException(e);
        }

        return new PuzzleStateAndGenerator<>(pState, scramble);
    }

    @Override
    public PuzzleSolutionEngine<PyraminxState> getSolutionEngine() {
        return pyraminxSolver;
    }

    @Override
    public String getLongName() {
        return "Pyraminx";
    }

    @Override
    public String getShortName() {
        return "pyram";
    }

    @Override
    public PyraminxState getSolvedState() {
        return new PyraminxState();
    }

    @Override
    protected int getRandomMoveCount() {
        return 15;
    }
}
