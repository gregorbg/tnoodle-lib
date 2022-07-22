package org.worldcubeassociation.tnoodle.solver;

import cs.min2phase.SearchWCA;
import cs.min2phase.Tools;
import org.worldcubeassociation.tnoodle.puzzle.ThreeByThreeCubePuzzle;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleSolutionEngine;
import org.worldcubeassociation.tnoodle.state.CubeState;

import java.util.Random;
import java.util.logging.Logger;

public class TwoPhaseCubeSolver extends PuzzleSolutionEngine<CubeState> {
    private static final Logger l = Logger.getLogger(ThreeByThreeCubePuzzle.class.getName());
    private static final CubeState SOLVED_STATE = new ThreeByThreeCubePuzzle().getSolvedState();

    private static final int THREE_BY_THREE_MAX_SCRAMBLE_LENGTH = 21;
    private static final int THREE_BY_THREE_TIMEMIN = 200; //milliseconds
    private static final int THREE_BY_THREE_TIMEOUT = 60*1000; //milliseconds

    private final ThreadLocal<SearchWCA> twoPhaseSearcher;

    public TwoPhaseCubeSolver() {
        twoPhaseSearcher = new ThreadLocal<SearchWCA>() {
            protected SearchWCA initialValue() {
                return new SearchWCA();
            }
        };
    }

    @Override
    public String solveIn(CubeState puzzleState, int n) {
        return solveIn(puzzleState, n, null, null);
    }

    public String solveIn(CubeState ps, int n, String firstAxisRestriction, String lastAxisRestriction) {
        if(ps.equalsNormalized(SOLVED_STATE)) {
            // TODO - apparently min2phase can't solve the solved cube
            return "";
        }
        String solution = twoPhaseSearcher.get().solution(ps.toFaceCube(), n, THREE_BY_THREE_TIMEOUT, 0, 0, firstAxisRestriction, lastAxisRestriction).trim();
        if("Error 7".equals(solution)) {
            // No solution exists for given depth
            return null;
        } else if(solution.startsWith("Error")) {
            // TODO - Not really sure what to do here.
            l.severe(solution + " while searching for solution to " + ps.toFaceCube());
            assert false;
            return null;
        }
        return solution;
    }

    public String generateRandomScramble(Random r, String firstAxisRestriction, String lastAxisRestriction) {
        String randomState = Tools.randomCube(r);
        return twoPhaseSearcher.get().solution(randomState, THREE_BY_THREE_MAX_SCRAMBLE_LENGTH, THREE_BY_THREE_TIMEOUT, THREE_BY_THREE_TIMEMIN, SearchWCA.INVERSE_SOLUTION, firstAxisRestriction, lastAxisRestriction).trim();
    }
}
