package org.worldcubeassociation.tnoodle.algorithm;

import org.worldcubeassociation.tnoodle.PuzzleState;

public class PuzzleStateAndGenerator<PS extends PuzzleState> {
    public PS state;
    public String generator;
    public PuzzleStateAndGenerator(PS state, String generator) {
        this.state = state;
        this.generator = generator;
    }
}
