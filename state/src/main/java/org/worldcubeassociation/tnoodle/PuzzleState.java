package org.worldcubeassociation.tnoodle;

import org.worldcubeassociation.tnoodle.algorithm.AlgorithmBuilder;
import org.worldcubeassociation.tnoodle.exceptions.InvalidMoveException;
import org.worldcubeassociation.tnoodle.exceptions.InvalidScrambleException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class PuzzleState<PS extends PuzzleState<PS>> {
    public PuzzleState() {}

    /**
     * @return A LinkedHashMap mapping move Strings to resulting PuzzleStates.
     *         The move Strings may not contain spaces.
     *         Multiple keys (moves) in the returned LinkedHashMap may
     *         map to the same state, or states that are .equal().
     *         Preferred notations should appear earlier in the
     *         LinkedHashMap.
     */
    public abstract Map<String, PS> getSuccessorsByName();

    public abstract PS unpack();

    /**
     * @param algorithm A space separated String of moves to apply to state
     * @return The resulting PuzzleState
     * @throws InvalidScrambleException If the scramble is invalid, for example if it uses invalid notation.
     */
    public PS applyAlgorithm(String algorithm) throws InvalidScrambleException {
        PS state = this.unpack();
        for(String move : AlgorithmBuilder.splitAlgorithm(algorithm)) {
            try {
                state = state.apply(move);
            } catch(InvalidMoveException e) {
                throw new InvalidScrambleException(algorithm, e);
            }
        }
        return state;
    }

    /**
     * Applies the given move to this PuzzleState. This method is non-destructive,
     * that is, it does not mutate the current state, instead it returns a new state.
     * @param move The move to apply
     * @return The PuzzleState achieved after applying move
     * @throws InvalidMoveException if the move is unrecognized.
     */
    public PS apply(String move) throws InvalidMoveException {
        Map<String, PS> successors = getSuccessorsByName();

        if(!successors.containsKey(move)) {
            throw new InvalidMoveException("Unrecognized turn " + move);
        }

        return successors.get(move);
    }

    /**
     * Canonical successors are all the successor states that
     * are "normalized" unique.
     * @return A mapping of canonical PuzzleState's to the name of
     *         the move that gets you to them.
     */
    public Map<PS, String> getCanonicalMovesByState() {
        Map<String, PS> successorsByName = getSuccessorsByName();
        Map<PS, String> uniqueSuccessors = new HashMap<PS, String>();
        Set<PS> statesSeenNormalized = new HashSet<PS>();

        // We're not interested in any successor states are just a
        // rotation away.
        statesSeenNormalized.add(this.getNormalized());

        for(Map.Entry<String, PS> next : successorsByName.entrySet()) {
            PS nextState = next.getValue();
            PS nextStateNormalized = nextState.getNormalized();
            String moveName = next.getKey();
            // Only add nextState if it's "unique"
            if(!statesSeenNormalized.contains(nextStateNormalized)) {
                uniqueSuccessors.put(nextState, moveName);
                statesSeenNormalized.add(nextStateNormalized);
            }
        }

        return uniqueSuccessors;
    }

    /**
     * By default, this method returns getSuccessorsByName(). Some
     * puzzles may wish to override this method to provide a reduced set
     * of moves to be used for scrambling.
     * <br><br>
     * One example of where this is useful is a puzzle like the square
     * one. Someone extending Puzzle to implement SquareOnePuzzle is left
     * with the question of whether to allow turns that leave the puzzle
     * incapable of doing a /.
     * <br><br>
     * If getSuccessorsByName() returns states that cannot do a /, then
     * generateRandomMoves() will hang because any move that can be
     * applied to one of those states is redundant.
     * <br><br>
     * Alternatively, if getSuccessorsByName() only returns states that
     * can do a /, AlgorithmBuilder's isRedundant() breaks.
     * Here's why:<br>
     * Imagine a solved square one. Lets say we pick the turn (1,0) to
     * apply to it, and now we're considering applying (2,0) to it.
     * Obviously this is the exact same state you would have achieved by
     * just applying (3,0) to the solved puzzle, but isRedundant()
     * only checks for this against the previous moves that commute with
     * (2,0). movesCommute("(1,0)", "(2,0)") will only return
     * true if (2,0) can be applied to a solved square one, even though
     * it results in a state that cannot
     * be slashed.

     * @return A HashMap mapping move Strings to resulting PuzzleStates.
     *         The move Strings may not contain spaces.
     */
    public Map<String, PS> getScrambleSuccessors() {
        Map<String, PS> reversed = new HashMap<String, PS>();

        for (Map.Entry<PS, String> entry : getCanonicalMovesByState().entrySet()) {
            reversed.put(entry.getValue(), entry.getKey());
        }

        return reversed;
    }

    /**
     * There exist PuzzleState's that are 0 moves apart, but are
     * not .equal(). This is because we consider the visibly different
     * PuzzleState's to be not equals (consider the state achieved by
     * applying L to a solved 3x3x3, and the state after applying Rw.
     * These puzzles "look" different, but they are 0 moves apart.
     * @return A PuzzleState that all rotations of state will all
     *         return when normalized. This makes it possible to check
     *         if 2 puzzle states are 0 moves apart, even if they
     *         "look" different.
     * TODO - This method could be implemented in this superclass by
     *        defining a "cost" for moves (which we will have to do for
     *        sq1 anyways), and walking the complete
     *        0 cost state tree for this state. Then we'd return one
     *        element from that state tree in a deterministic way.
     *        We could do something simple like returning the state
     *        that has the smallest hash, but that wouldn't work if
     *        we have hash collisions. I think the best thing to do
     *        would be to require all PuzzleStates to implement
     *        a marshall() function that returns a unique string. Then
     *        we can just do an alphabetical sort of these and return the
     *        min or max.
     */
    public PS getNormalized() {
        return this.unpack();
    }

    public boolean isNormalized() {
        return this.equals(getNormalized());
    }

    /**
     * Most puzzles are happy to split an algorithm by turns, and declare
     * each turn a move. However, this simple model doesn't work for all
     * puzzles. For example, square one may wish to declare (3,3) as 1
     * move. Another possible use for this would be rotations, which
     * count as 0 moves.
     * @param move The move for which to compute costs
     * @return The cost of doing this move.
     */
    public int getMoveCost(String move) {
        return 1;
    }

    /**
     * Returns true if this state is equal to other.
     * Note that a puzzle like 4x4 must compare all orientations of the puzzle, otherwise
     * generateRandomMoves() will allow for trivial sequences of turns like Lw Rw'.
     * @param other The other object to check for equality
     * @return true if this is equal to other
     */
    public abstract boolean equals(Object other);

    public abstract int hashCode();

    public boolean equalsNormalized(PuzzleState<PS> other) {
        return getNormalized().equals(other.getNormalized());
    }

    /**
     * Two moves A and B commute on a puzzle if regardless of
     * the order you apply A and B, you end up in the same state.
     * Interestingly enough, the set of moves that commute can change
     * with the state a puzzle is in. That's why this is a method of
     * PuzzleState instead of Puzzle.
     * @param move1 The first move
     * @param move2 The second move
     * @return True iff move1 and move2 commute.
     */
    public boolean movesCommute(String move1, String move2) {
        try {
            PS state1 = apply(move1).apply(move2);
            PS state2 = apply(move2).apply(move1);
            return state1.equals(state2);
        } catch (InvalidMoveException e) {
            return false;
        }
    }
}
