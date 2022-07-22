package org.worldcubeassociation.tnoodle.solver;

import org.worldcubeassociation.tnoodle.scrambles.AlgorithmBuilder;
import org.worldcubeassociation.tnoodle.scrambles.InvalidMoveException;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleSolutionEngine;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleState;
import org.worldcubeassociation.tnoodle.util.SortedBuckets;

import java.util.HashMap;
import java.util.Map;

public class BidirectionalBFSSolver<PS extends PuzzleState<PS>> extends PuzzleSolutionEngine<PS> {
    private final PS solvedState;

    public BidirectionalBFSSolver(PS solvedState) {
        this.solvedState = solvedState;
    }

    public String solveIn(PS state, int n) {
        if(state.equalsNormalized(solvedState)) {
            return "";
        }

        Map<PS, Integer> seenSolved = new HashMap<>();
        SortedBuckets<PS> fringeSolved = new SortedBuckets<>();
        Map<PS, Integer> seenScrambled = new HashMap<>();
        SortedBuckets<PS> fringeScrambled = new SortedBuckets<>();

        // We're only interested in solutions of cost <= n
        int bestIntersectionCost = n + 1;
        PS bestIntersection = null;

        PS solvedNormalized = solvedState.getNormalized();
        fringeSolved.add(solvedNormalized, 0);
        seenSolved.put(solvedNormalized, 0);
        fringeScrambled.add(state.getNormalized(), 0);
        seenScrambled.put(state.getNormalized(), 0);

        int fringeTies = 0;

        // The task here is to do a breadth-first search starting from both the solved state and the scrambled state.
        // When we got an intersection from the two hash maps, we are done!
        int minFringeScrambled = -1, minFringeSolved = -1;
        while(!fringeSolved.isEmpty() || !fringeScrambled.isEmpty()) {
            // We have to choose on which side we are extending our search.
            // I'm choosing the non-empty fringe with the node nearest
            // its origin. In the event of a tie, we make sure to alternate.
            if(!fringeScrambled.isEmpty()) {
                minFringeScrambled = fringeScrambled.smallestValue();
            }
            if(!fringeSolved.isEmpty()) {
                minFringeSolved = fringeSolved.smallestValue();
            }
            boolean extendSolved;
            if(fringeSolved.isEmpty() || fringeScrambled.isEmpty()) {
                // If the solved fringe is not empty, we'll expand it.
                // Otherwise, we're expanding the scrambled fringe.
                extendSolved = !fringeSolved.isEmpty();
            } else {
                if(minFringeSolved < minFringeScrambled) {
                    extendSolved = true;
                } else if(minFringeSolved > minFringeScrambled) {
                    extendSolved = false;
                } else {
                    extendSolved = (fringeTies++) % 2 == 0;
                }
            }

            // We are using references for a more concise code.
            Map<PS, Integer> seenExtending;
            SortedBuckets<PS> fringeExtending;
            Map<PS, Integer> seenComparing;
            int minComparingFringe;
            if(extendSolved) {
                seenExtending = seenSolved;
                fringeExtending = fringeSolved;
                seenComparing = seenScrambled;
                minComparingFringe = minFringeScrambled;
            } else {
                seenExtending = seenScrambled;
                fringeExtending = fringeScrambled;
                seenComparing = seenSolved;
                minComparingFringe = minFringeSolved;
            }

            PS node = fringeExtending.pop();
            int distance = seenExtending.get(node);
            if(seenComparing.containsKey(node)) {
                // We found an intersection! Compute the total cost of the
                // path going through this node.
                int cost = seenComparing.get(node) + distance;
                if(cost < bestIntersectionCost) {
                    bestIntersection = node;
                    bestIntersectionCost = cost;
                }
                continue;
            }
            // The best possible solution involving this node would
            // be through a child of this node that gets us across to
            // the other fringe's smallest distance node.
            int bestPossibleSolution = distance + minComparingFringe;
            if(bestPossibleSolution >= bestIntersectionCost) {
                continue;
            }
            if(distance >= (n+1)/2) {
                // The +1 is because if n is odd, we would have to search
                // from one side with distance n/2 and from the other side
                // distance n/2 + 1. Because we don't know which is which,
                // let's take (n+1)/2 for both.
                continue;
            }

            Map<PS, String> movesByState = node.getCanonicalMovesByState();
            for(Map.Entry<PS, String> entry : movesByState.entrySet()) {
                PS next = entry.getKey();
                int moveCost = node.getMoveCost(entry.getValue());
                int nextDistance = distance + moveCost;
                next = next.getNormalized();
                if(seenExtending.containsKey(next)) {
                    if(nextDistance >= seenExtending.get(next)) {
                        // We already found a better path to next.
                        continue;
                    }
                    // Go on to clobber seenExtending with our updated
                    // distance. Unfortunately, we're going have 2 copies
                    // of next in our fringe. This doesn't change correctness,
                    // it just means a bit of wasted work when we get around
                    // to popping off the second one.
                }
                fringeExtending.add(next, nextDistance);
                seenExtending.put(next, nextDistance);
            }
        }

        if(bestIntersection == null) {
            return null;
        }

        // We have found a solution, but we still have to recover the move sequence.
        // the `bestIntersection` is the bound between the solved and the scrambled states.
        // We can travel from `bestIntersection` to either states, like that:
        // solved <----- bestIntersection -----> scrambled
        // However, to build a solution, we need to travel like that:
        // solved <----- bestIntersection <----- scrambled
        // So we have to travel backward for the scrambled side.

        // Step 1: bestIntersection -----> scrambled

        assert bestIntersection.isNormalized();
        PS lookupState = bestIntersection;
        int distanceFromScrambled = seenScrambled.get(lookupState);

        // We have to keep track of all states we have visited
        PuzzleState<PS>[] linkedStates = new PuzzleState[distanceFromScrambled + 1];
        linkedStates[distanceFromScrambled] = lookupState;

        outer:
        while(distanceFromScrambled > 0) {
            for(PS next : lookupState.getCanonicalMovesByState().keySet()) {
                next = next.getNormalized();
                if(seenScrambled.containsKey(next)) {
                    int newDistanceFromScrambled = seenScrambled.get(next);
                    if(newDistanceFromScrambled < distanceFromScrambled) {
                        lookupState = next;
                        distanceFromScrambled = newDistanceFromScrambled;
                        linkedStates[distanceFromScrambled] = lookupState;
                        continue outer;
                    }
                }
            }
            assert false;
        }

        // Step 2: bestIntersection <----- scrambled

        AlgorithmBuilder<PS> solution = new AlgorithmBuilder<>(AlgorithmBuilder.MergingMode.CANONICALIZE_MOVES, lookupState);
        distanceFromScrambled = 0;

        outer:
        while(!lookupState.equalsNormalized(bestIntersection)) {
            for(Map.Entry<PS, String> next : lookupState.getCanonicalMovesByState().entrySet()) {
                PS nextState = next.getKey();
                String moveName = next.getValue();
                if(nextState.equalsNormalized(linkedStates[distanceFromScrambled+1])) {
                    lookupState = nextState;
                    try {
                        solution.appendMove(moveName);
                    } catch(InvalidMoveException e) {
                        throw new RuntimeException(e);
                    }
                    distanceFromScrambled = seenScrambled.get(lookupState.getNormalized());
                    continue outer;
                }
            }
            assert false;
        }

        // Step 3: solved <----- bestIntersection

        int distanceFromSolved = seenSolved.get(lookupState.getNormalized());
        outer:
        while(distanceFromSolved > 0) {
            for(Map.Entry<PS, String> next : lookupState.getCanonicalMovesByState().entrySet()) {
                PS nextState = next.getKey();
                PS nextStateNormalized = nextState.getNormalized();
                String moveName = next.getValue();
                if(seenSolved.containsKey(nextStateNormalized)) {
                    int newDistanceFromSolved = seenSolved.get(nextStateNormalized);
                    if(newDistanceFromSolved < distanceFromSolved) {
                        lookupState = nextState;
                        distanceFromSolved = newDistanceFromSolved;
                        try {
                            solution.appendMove(moveName);
                        } catch(InvalidMoveException e) {
                            throw new RuntimeException(e);
                        }
                        continue outer;
                    }
                }
            }
            assert false;
        }

        return solution.toString();
    }
}
