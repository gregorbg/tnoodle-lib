package org.worldcubeassociation.tnoodle.scrambles;

public class PuzzleStateAndGenerator<PS extends PuzzleState<PS>> {
    public PS state;
    public String generator;
    public PuzzleStateAndGenerator(PS state, String generator) {
        this.state = state;
        this.generator = generator;
    }
}
