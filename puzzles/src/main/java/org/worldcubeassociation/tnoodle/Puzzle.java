package org.worldcubeassociation.tnoodle;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.worldcubeassociation.tnoodle.algorithm.AlgorithmBuilder;
import org.worldcubeassociation.tnoodle.algorithm.PuzzleStateAndGenerator;
import org.worldcubeassociation.tnoodle.exceptions.InvalidMoveException;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;
import org.worldcubeassociation.tnoodle.solver.BidirectionalBFSSolver;
import org.worldcubeassociation.tnoodle.util.ArrayUtils;

/**
 * Puzzle and TwistyPuzzle encapsulate all the information to filter out
 * scrambles &lt;= wcaMinScrambleDistance (defaults to 1)
 * move away from solved (see generateWcaScramble),
 * and to generate random turn scrambles generically (see generateRandomMoves).
 *
 * The original proposal for these classes is accessible here:
 * https://docs.google.com/document/d/11ZfQPxAw0EhNNwE1yn5lZUO383qvAH6kJa2s3O9_6Zg/edit
 *
 * @author jeremy
 *
 */
@ExportClosure
public abstract class Puzzle<PS extends AbstractPuzzleState<PS>> implements Exportable {
    private static final Logger l = Logger.getLogger(Puzzle.class.getName());
    protected int wcaMinScrambleDistance = 2;

    /**
     * Returns a String describing this Scrambler
     * appropriate for use in a url. This shouldn't contain any periods.
     * @return a url appropriate String unique to this Scrambler
     */
    @Export
    public abstract String getShortName();

    /**
     * Returns a String fully describing this Scrambler.
     * Unlike shortName(), may contain spaces and other url-inappropriate characters.
     * This will also be used for the toString method of this Scrambler.
     * @return a String
     */
    @Export
    public abstract String getLongName();

    /**
     * Returns the minimum distance from solved that any scramble this Puzzle
     * generates will be.
     *
     * @return The integer representing the exact minimum scramble distance
     */
    public int getWcaMinScrambleDistance() {
        return wcaMinScrambleDistance;
    }

    /**
     * Generates a scramble appropriate for this Scrambler. It's important to note that
     * it's ok if this method takes some time to run, as it's going to be called many times and get queued up
     * by ScrambleCacher.
     * NOTE:  If a puzzle wants to provide custom scrambles
     * (for example: Pochmann style megaminx or MRSS), it should override generateRandomMoves.
     * @param r The instance of Random you must use as your source of randomness when generating scrambles.
     * @return A String containing the scramble, where turns are assumed to be separated by whitespace.
     */
    public final String generateScramble(Random r) {
        PuzzleSolutionEngine<PS> engine = getSolutionEngine();

        PuzzleStateAndGenerator<PS> psag;
        do {
            psag = generateRandomMoves(r);
        } while(engine.solveIn(psag.state, wcaMinScrambleDistance - 1) != null);
        return psag.generator;
    }

    /**
     * @return Simply returns getLongName()
     */
    @Export
    public String toString() {
        return getLongName();
    }

    /**
     * @return A PuzzleState representing the solved state of our puzzle
     * from where we will begin scrambling.
     */
    public abstract PS getSolvedState();

    public PuzzleSolutionEngine<PS> getSolutionEngine() {
        return new BidirectionalBFSSolver<>(getSolvedState());
    }

    /**
     * @return The number of random moves we must apply to call a puzzle
     * sufficiently scrambled.
     */
    protected abstract int getRandomMoveCount();

    /**
     * This function will generate getRandomTurnCount() number of non cancelling,
     * random turns. If a puzzle wants to provide custom scrambles
     * (for example: Pochmann style megaminx or MRSS), it should override this method.
     *
     * NOTE: It is assumed that this method is thread safe! That means that if you're
     * overriding this method and you don't know what you're doing,
     * use the synchronized keyword when implementing this method:<br>
     * <code>protected synchronized String generateScramble(Random r);</code>
     * @param r An instance of Random
     * @return A PuzzleStateAndGenerator that contains a scramble string, and the
     *         state achieved by applying that scramble.
     */
    @NoExport
    protected PuzzleStateAndGenerator<PS> generateRandomMoves(Random r) {
        AlgorithmBuilder<PS> ab = new AlgorithmBuilder<>(AlgorithmBuilder.MergingMode.NO_MERGING, getSolvedState());

        while(ab.getTotalCost() < getRandomMoveCount()) {
            Set<String> successors = ab.getState().getScrambleSuccessors().keySet();
            String move;

            try {
                do {
                    move = ArrayUtils.choose(r, successors);
                    // If this move happens to be redundant, there is no
                    // reason to select this move again in vain.
                    successors.remove(move);
                } while(ab.isRedundant(move));
                ab.appendMove(move);
            } catch(InvalidMoveException e) {
                l.log(Level.SEVERE, "", e);
                throw new RuntimeException(e);
            }
        }
        return ab.getStateAndGenerator();
    }
}
