package org.worldcubeassociation.tnoodle.puzzle;

import org.worldcubeassociation.tnoodle.drawing.SquareOnePainter;
import org.worldcubeassociation.tnoodle.scrambles.*;
import org.worldcubeassociation.tnoodle.solver.TwoPhaseSquareSolver;
import org.worldcubeassociation.tnoodle.state.SquareOneState;

import java.util.*;

import org.timepedia.exporter.client.Export;

@Export
public class SquareOnePuzzle extends Puzzle<SquareOneState> {
    private final TwoPhaseSquareSolver twoPhaseSearcher;

    public SquareOnePuzzle() {
        wcaMinScrambleDistance = 11;
        twoPhaseSearcher = new TwoPhaseSquareSolver();
    }

    @Override
    public PuzzleStateAndGenerator<SquareOneState> generateRandomMoves(Random r) {
        String scramble = twoPhaseSearcher.randomScramble(r);
        SquareOneState state;
        try {
            state = getSolvedState().applyAlgorithm(scramble);
        } catch (InvalidScrambleException e) {
            throw new RuntimeException(e);
        }
        return new PuzzleStateAndGenerator<>(state, scramble);
    }

    @Override
    public PuzzleSolutionEngine<SquareOneState> getSolutionEngine() {
        return twoPhaseSearcher;
    }

    @Override
    public String getLongName() {
        return "Square-1";
    }

    @Override
    public String getShortName() {
        return "sq1";
    }

    @Override
    public SquareOneState getSolvedState() {
        return new SquareOneState();
    }

    @Override
    public PuzzleSvgPainter<SquareOneState> getPainter() {
        return new SquareOnePainter();
    }

    @Override
    protected int getRandomMoveCount() {
        return 40;
    }
}
