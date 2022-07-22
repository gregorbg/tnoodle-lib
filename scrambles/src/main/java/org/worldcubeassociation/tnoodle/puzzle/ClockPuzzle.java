package org.worldcubeassociation.tnoodle.puzzle;

import org.worldcubeassociation.tnoodle.drawing.ClockPainter;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleSvgPainter;
import org.worldcubeassociation.tnoodle.state.ClockState;

import java.util.*;

import org.worldcubeassociation.tnoodle.scrambles.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.scrambles.Puzzle;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleStateAndGenerator;

import org.timepedia.exporter.client.Export;

@Export
public class ClockPuzzle extends Puzzle<ClockState> {
    @Override
    public String getLongName() {
        return "Clock";
    }

    @Override
    public String getShortName() {
        return "clock";
    }

    @Override
    public ClockState getSolvedState() {
        return new ClockState();
    }

    @Override
    public PuzzleSvgPainter<ClockState> getPainter() {
        return new ClockPainter();
    }

    @Override
    protected int getRandomMoveCount() {
        return 19;
    }

    @Override
    public PuzzleStateAndGenerator<ClockState> generateRandomMoves(Random r) {
        StringBuilder scramble = new StringBuilder();

        for(int x=0; x<9; x++) {
            int turn = r.nextInt(12)-5;
            boolean clockwise = ( turn >= 0 );
            turn = Math.abs(turn);
            scramble.append(ClockState.TURNS[x]).append(turn).append(clockwise ? "+" : "-").append(" ");
        }
        scramble.append( "y2 ");
        for(int x=4; x<9; x++) {
            int turn = r.nextInt(12)-5;
            boolean clockwise = ( turn >= 0 );
            turn = Math.abs(turn);
            scramble.append(ClockState.TURNS[x]).append(turn).append(clockwise ? "+" : "-").append(" ");
        }

        boolean isFirst = true;
        for(int x=0;x<4;x++) {
            if (r.nextInt(2) == 1) {
                scramble.append(isFirst ? "" : " ").append(ClockState.TURNS[x]);
                isFirst = false;
            }
        }

        String scrambleStr = scramble.toString().trim();

        ClockState state = getSolvedState();
        try {
            state = state.applyAlgorithm(scrambleStr);
        } catch(InvalidScrambleException e) {
            throw new RuntimeException(e);
        }
        return new PuzzleStateAndGenerator<>(state, scrambleStr);
    }
}
