package org.worldcubeassociation.tnoodle.puzzle;

import org.worldcubeassociation.tnoodle.Puzzle;
import org.worldcubeassociation.tnoodle.state.CubeState;

import org.timepedia.exporter.client.Export;

@Export
public class CubePuzzle extends Puzzle<CubeState> {
    private static final int[] DEFAULT_LENGTHS = { 0, 0, 25, 25, 40, 60, 80, 100, 120, 140, 160, 180 };

    public final int size;

    public CubePuzzle(int size) {
        assert size >= 0 && size < DEFAULT_LENGTHS.length : "Invalid cube size";
        this.size = size;
    }

    @Override
    public String getLongName() {
        return size + "x" + size + "x" + size;
    }

    @Override
    public String getShortName() {
        return size + "" + size + "" + size;
    }

    @Override
    public CubeState getSolvedState() {
        return new CubeState(size);
    }

    @Override
    protected int getRandomMoveCount() {
        return DEFAULT_LENGTHS[size];
    }
}
