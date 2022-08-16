package org.worldcubeassociation.tnoodle.solver;

import cs.threephase.Search;

import java.util.Random;

public class ThreePhaseCubeSolver { // TODO GB  extends PuzzleSolutionEngine<CubeState>
    private final ThreadLocal<Search> threePhaseSearcher;

    public ThreePhaseCubeSolver() {
        threePhaseSearcher = new ThreadLocal<Search>() {
            protected Search initialValue() {
                return new Search();
            }
        };
    }

    public String generateRandomScramble(Random r) {
        return threePhaseSearcher.get().randomState(r);
    }
}
