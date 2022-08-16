package org.worldcubeassociation.tnoodle;

public abstract class PuzzleSolutionEngine<PS extends PuzzleState<PS>> {
    /**
     * Returns a number between 0 and 1 representing how "initialized" this
     * Scrambler is. 0 means nothing has been accomplished, and 1 means
     * we're done, and are generating scrambles.
     * @return A double between 0 and 1, inclusive.
     */
    public double getInitializationStatus() {
        return 1;
    }

    public abstract String solveIn(PS puzzleState, int n);
}
