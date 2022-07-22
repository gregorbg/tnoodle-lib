package org.worldcubeassociation.tnoodle.solver;

import cs.sq12phase.FullCube;
import cs.sq12phase.Search;
import org.worldcubeassociation.tnoodle.scrambles.AlgorithmBuilder;
import org.worldcubeassociation.tnoodle.scrambles.InvalidMoveException;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleSolutionEngine;
import org.worldcubeassociation.tnoodle.state.SquareOneState;

import java.util.Map;
import java.util.Random;

public class TwoPhaseSquareSolver extends PuzzleSolutionEngine<SquareOneState> {
    private final ThreadLocal<Search> twoPhaseSearcher;

    public TwoPhaseSquareSolver() {
        twoPhaseSearcher = new ThreadLocal<Search>() {
            protected Search initialValue() {
                return new Search();
            }
        };
    }

    @Override
    public String solveIn(SquareOneState puzzleState, int n) {
        // apparently sq12phase can neither represent nor solve "unslashable" squares
        if (!puzzleState.canSlash()) {
            // getScrambleSuccessors automatically filters for slashability
            Map<String, SquareOneState> slashableSuccessors = puzzleState.getScrambleSuccessors();

            Map.Entry<String, SquareOneState> bestSlashable = null;
            int currentMin = Integer.MAX_VALUE;

            for (Map.Entry<String, SquareOneState> possState : slashableSuccessors.entrySet()) {
                // we're not interested in the standard uniform WCA cost distribution here.
                // instead, we aim to find the "minimum invasive" move to achieve slashability.
                // in other words, for a sq1 with scramble (-1, 0) we want to prefer (1, 0) over (4, 0) or (5, 0)
                // despite all of those moves technically achieving a slashable state
                Integer moveCost = SquareOneState.slashabilityCostsByMove.get(possState.getKey());

                if (moveCost != null && moveCost < currentMin) {
                    currentMin = moveCost;
                    bestSlashable = possState;
                }
            }

            // absolutely no successor found to make it slashable
            if (bestSlashable == null) {
                return null;
            }

            try {
                String slashabilitySetupMove = bestSlashable.getKey();
                SquareOneState slashableState = bestSlashable.getValue();

                return solveWithSlashabilityIn(slashableState, n, slashabilitySetupMove, puzzleState, n - 1);
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }
        }

        FullCube f = puzzleState.toFullCube();
        String scramble = twoPhaseSearcher.get().solutionOpt(f, n);
        return scramble == null ? null : scramble.trim();
    }

    private String solveWithSlashabilityIn(SquareOneState puzzleState, int n, String slashabilityMove, SquareOneState preSlashabilityState, int lowerThreshold) throws InvalidMoveException {
        if (!puzzleState.canSlash()) {
            // nice try.
            return null;
        }

        if (n < lowerThreshold) {
            // we do not want to dig any deeper
            return null;
        }

        // despite already having "wasted" one move for slashability, we do NOT search for n-1 here
        // because doing so would remove one degree of freedom that could potentially cancel into our setup move
        String nextBestSolution = solveIn(puzzleState, n);

        if (nextBestSolution == null) {
            // we cannot even solve the slashable state in n moves, give up
            return null;
        }

        AlgorithmBuilder<SquareOneState> ab = new AlgorithmBuilder<>(AlgorithmBuilder.MergingMode.CANONICALIZE_MOVES, preSlashabilityState);

        // initially just hope that slashability cancels with the n-move optimal solution
        ab.appendMove(slashabilityMove);
        ab.appendAlgorithm(nextBestSolution);

        if (ab.getTotalCost() > n) {
            // slashability move did not cancel with the n-move solution, try shorter solution
            return solveWithSlashabilityIn(puzzleState, n - 1, slashabilityMove, preSlashabilityState, lowerThreshold);
        }

        // if we reach here, the total cost is implicitly <= n
        return ab.getStateAndGenerator().generator;
    }

    public String randomScramble(Random r) {
        FullCube randomState = FullCube.randomCube(r);
        return twoPhaseSearcher.get().solution(randomState, Search.INVERSE_SOLUTION).trim();
    }
}
