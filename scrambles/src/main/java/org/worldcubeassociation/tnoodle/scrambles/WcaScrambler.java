package org.worldcubeassociation.tnoodle.scrambles;

import org.worldcubeassociation.tnoodle.*;
import org.worldcubeassociation.tnoodle.algorithm.AlgorithmBuilder;
import org.worldcubeassociation.tnoodle.cache.ScrambleCacher;
import org.worldcubeassociation.tnoodle.exceptions.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.puzzle.*;
import org.worldcubeassociation.tnoodle.state.*;
import org.worldcubeassociation.tnoodle.svglite.Color;
import org.worldcubeassociation.tnoodle.svglite.Svg;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;

public final class WcaScrambler<PS extends AbstractPuzzleState<PS>> {
    public static final WcaScrambler<CubeState> TWO = new WcaScrambler<>(TwoByTwoCubePuzzle.class, WcaRenderingEngine.TWO);
    public static final WcaScrambler<CubeState> THREE = new WcaScrambler<>(ThreeByThreeCubePuzzle.class, WcaRenderingEngine.THREE);
    public static final WcaScrambler<CubeState> FOUR = new WcaScrambler<>(FourByFourCubePuzzle.class, WcaRenderingEngine.FOUR);
    public static final WcaScrambler<CubeState> FOUR_FAST = new WcaScrambler<>(FourByFourRandomTurnsCubePuzzle.class, WcaRenderingEngine.FOUR);
    public static final WcaScrambler<CubeState> FIVE = new WcaScrambler<>(CubePuzzle.class, WcaRenderingEngine.FIVE, 5);
    public static final WcaScrambler<CubeState> SIX = new WcaScrambler<>(CubePuzzle.class, WcaRenderingEngine.SIX, 6);
    public static final WcaScrambler<CubeState> SEVEN = new WcaScrambler<>(CubePuzzle.class, WcaRenderingEngine.SEVEN, 7);
    public static final WcaScrambler<CubeState> THREE_NI = new WcaScrambler<>(NoInspectionThreeByThreeCubePuzzle.class, WcaRenderingEngine.THREE);
    public static final WcaScrambler<CubeState> FOUR_NI = new WcaScrambler<>(NoInspectionFourByFourCubePuzzle.class, WcaRenderingEngine.FOUR);
    public static final WcaScrambler<CubeState> FIVE_NI = new WcaScrambler<>(NoInspectionFiveByFiveCubePuzzle.class, WcaRenderingEngine.FIVE);
    public static final WcaScrambler<CubeState> THREE_FM = new WcaScrambler<>(ThreeByThreeCubeFewestMovesPuzzle.class, WcaRenderingEngine.THREE);
    public static final WcaScrambler<PyraminxState> PYRA = new WcaScrambler<>(PyraminxPuzzle.class, WcaRenderingEngine.PYRA);
    public static final WcaScrambler<SquareOneState> SQ1 = new WcaScrambler<>(SquareOnePuzzle.class, WcaRenderingEngine.SQ1);
    public static final WcaScrambler<MegaminxState> MEGA = new WcaScrambler<>(MegaminxPuzzle.class, WcaRenderingEngine.MEGA);
    public static final WcaScrambler<ClockState> CLOCK = new WcaScrambler<>(ClockPuzzle.class, WcaRenderingEngine.CLOCK);
    public static final WcaScrambler<SkewbState> SKEWB = new WcaScrambler<>(SkewbPuzzle.class, WcaRenderingEngine.SKEWB);

    private final LazySupplier<? extends Puzzle<PS>> puzzleSupplier;
    private final WcaRenderingEngine<PS> renderingEngine;
    private final Random secureRandom = getSecureRandom();

    <T extends Puzzle<PS>> WcaScrambler(Class<T> suppliyingClass, WcaRenderingEngine<PS> renderingEngine, Object... ctorArgs) {
        this.puzzleSupplier = new LazySupplier<>(suppliyingClass, ctorArgs);
        this.renderingEngine = renderingEngine;
    }

    private Puzzle<PS> getScramblingPuzzle() {
        // WORD OF ADVICE: The puzzles that use local scrambling mechanisms
        // should not take long to boot anyways because their computation-heavy
        // code is wrapped in ThreadLocal objects that are only executed on-demand
        return this.puzzleSupplier.getInstance();
    }

    public int getMinScrambleDistance() {
        return getScramblingPuzzle().getWcaMinScrambleDistance();
    }

    public String generateScramble() {
        return generateScramble(secureRandom);
    }

    public String[] generateScrambles(int count) {
        return generateScrambles(secureRandom, count);
    }

    private String generateScramble(Random r) {
        return getScramblingPuzzle().generateScramble(r);
    }

    private String[] generateScrambles(Random r, int count) {
        String[] scrambles = new String[count];
        for (int i = 0; i < count; i++) {
            scrambles[i] = generateScramble(r);
        }
        return scrambles;
    }

    /**
     * seeded scrambles, these can't be cached, so they'll be a little slower
     *
     * @param seed The seed to be used for generating this scramble
     * @return A scramble similar to {@link #generateScramble}, except that it is guaranteed to be based on {@code seed}
     */
    public String generateSeededScramble(String seed) {
        return generateSeededScramble(seed.getBytes());
    }

    public String[] generateSeededScrambles(String seed, int count) {
        return generateSeededScrambles(seed.getBytes(), count);
    }

    private String generateSeededScramble(byte[] seed) {
        Random r = getSeededRandom(seed);
        return generateScramble(r);
    }

    private String[] generateSeededScrambles(byte[] seed, int count) {
        Random r = getSeededRandom(seed);
        return generateScrambles(r, count);
    }

    public PS getSolvedState() {
        return getScramblingPuzzle().getSolvedState();
    }

    public PS getPuzzleState(String scramble) throws InvalidScrambleException {
        if (scramble == null) {
            return getPuzzleState("");
        }

        return getScramblingPuzzle().getSolvedState().applyAlgorithm(scramble);
    }

    public Svg drawScramble(String scramble, Map<String, Color> colorScheme) throws InvalidScrambleException {
        PS state = getPuzzleState(scramble);
        return this.renderingEngine.getPuzzlePainter().drawScramble(state, colorScheme);
    }

    public String solveIn(String scramble, int n) throws InvalidScrambleException {
        PS scrambledState = getPuzzleState(scramble);
        return getScramblingPuzzle().getSolutionEngine().solveIn(scrambledState, n);
    }

    public AlgorithmBuilder<PS> startAlgorithmBuilder(AlgorithmBuilder.MergingMode mergingMode) {
        return new AlgorithmBuilder<>(mergingMode, getScramblingPuzzle().getSolvedState());
    }

    public ScrambleCacher startCache(int capacity) {
        return new ScrambleCacher(this.getScramblingPuzzle(), capacity);
    }

    public String getKey() {
        return this.getScramblingPuzzle().getShortName();
    }

    public String getDescription() {
        return this.getScramblingPuzzle().getLongName();
    }

    public static SecureRandom getSecureRandom() {
        try {
            try {
                return SecureRandom.getInstance("SHA1PRNG", "SUN");
            } catch (NoSuchProviderException e) {
                return SecureRandom.getInstance("SHA1PRNG");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static SecureRandom getSeededRandom(byte[] seed) {
        // We must create our own Random because
        // other threads can access the static one.
        // Also, setSeed supplements an existing seed,
        // rather than replacing it.
        // TODO - consider using something other than SecureRandom for seeded scrambles,
        // because we really, really want this to be portable across platforms (desktop java, gwt, and android)
        // https://github.com/thewca/tnoodle/issues/146
        SecureRandom r = getSecureRandom();
        r.setSeed(seed);
        return r;
    }

    public static WcaScrambler<? extends PuzzleState> getForEvent(WcaEvent event) {
        switch (event) {
            case TWO:
                return WcaScrambler.TWO;
            case THREE:
                return WcaScrambler.THREE;
            case FOUR:
                return WcaScrambler.FOUR;
            case FIVE:
                return WcaScrambler.FIVE;
            case SIX:
                return WcaScrambler.SIX;
            case SEVEN:
                return WcaScrambler.SEVEN;
            case THREE_BLD:
                return WcaScrambler.THREE_NI;
            case FOUR_BLD:
                return WcaScrambler.FOUR_NI;
            case FIVE_BLD:
                return WcaScrambler.FIVE_NI;
            case THREE_FM:
                return WcaScrambler.THREE_FM;
            case PYRA:
                return WcaScrambler.PYRA;
            case SQ1:
                return WcaScrambler.SQ1;
            case MEGA:
                return WcaScrambler.MEGA;
            case CLOCK:
                return WcaScrambler.CLOCK;
            case SKEWB:
                return WcaScrambler.SKEWB;
        }

        // somehow the Java compiler doesn't detect exhaustive switch :(
        return null;
    }
}
