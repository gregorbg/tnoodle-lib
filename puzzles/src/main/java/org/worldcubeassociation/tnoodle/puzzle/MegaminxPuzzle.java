package org.worldcubeassociation.tnoodle.puzzle;

import org.worldcubeassociation.tnoodle.Puzzle;
import org.worldcubeassociation.tnoodle.algorithm.PuzzleStateAndGenerator;
import org.worldcubeassociation.tnoodle.exceptions.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.state.MegaminxState;
import java.util.Random;

import org.timepedia.exporter.client.Export;

@Export
public class MegaminxPuzzle extends Puzzle<MegaminxState> {
    public MegaminxPuzzle() {}

    @Override
    public String getLongName() {
        return "Megaminx";
    }

    @Override
    public String getShortName() {
        return "minx";
    }

    @Override
    public MegaminxState getSolvedState() {
        return new MegaminxState();
    }

    @Override
    protected int getRandomMoveCount() {
        return 11*7;
    }

    @Override
    public PuzzleStateAndGenerator<MegaminxState> generateRandomMoves(Random r) {
        StringBuilder scramble = new StringBuilder();

        int width = 10, height = 7;
        for(int i = 0; i < height; i++) {
            if(i > 0) {
                scramble.append("\n");
            }
            int dir = 0;
            for(int j = 0; j < width; j++) {
                if(j > 0) {
                    scramble.append(" ");
                }
                char side = (j % 2 == 0) ? 'R' : 'D';
                dir = r.nextInt(2);
                scramble.append(side).append((dir == 0) ? "++" : "--");
            }
            scramble.append(" U");
            if(dir != 0) {
                scramble.append("'");
            }
        }

        String scrambleStr = scramble.toString();

        MegaminxState state = getSolvedState();
        try {
            state = state.applyAlgorithm(scrambleStr);
        } catch(InvalidScrambleException e) {
            throw new RuntimeException(e);
        }
        return new PuzzleStateAndGenerator<>(state, scrambleStr);
    }
}
