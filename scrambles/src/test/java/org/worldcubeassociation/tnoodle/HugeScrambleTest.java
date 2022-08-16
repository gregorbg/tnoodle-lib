package org.worldcubeassociation.tnoodle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.worldcubeassociation.tnoodle.algorithm.AlgorithmBuilder;
import org.worldcubeassociation.tnoodle.cache.ScrambleCacher;
import org.worldcubeassociation.tnoodle.cache.ScrambleCacherListener;
import org.worldcubeassociation.tnoodle.cache.ScrambleCacherListenerImpl;
import org.worldcubeassociation.tnoodle.exceptions.InvalidMoveException;
import org.worldcubeassociation.tnoodle.exceptions.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.scrambles.*;
import org.worldcubeassociation.tnoodle.puzzle.ClockPuzzle;
import org.worldcubeassociation.tnoodle.puzzle.SquareOnePuzzle;
import org.worldcubeassociation.tnoodle.puzzle.CubePuzzle;
import org.worldcubeassociation.tnoodle.puzzle.ThreeByThreeCubePuzzle;
import org.worldcubeassociation.tnoodle.puzzle.PyraminxPuzzle;
import org.worldcubeassociation.tnoodle.solver.PyraminxSolver;
import org.worldcubeassociation.tnoodle.solver.PyraminxSolver.PyraminxSolverState;
import org.worldcubeassociation.tnoodle.puzzle.MegaminxPuzzle;
import org.worldcubeassociation.tnoodle.solver.TwoByTwoSolver;
import org.worldcubeassociation.tnoodle.solver.TwoByTwoSolver.TwoByTwoState;
import org.junit.jupiter.api.Test;
import org.worldcubeassociation.tnoodle.state.*;
import org.worldcubeassociation.tnoodle.svglite.Svg;
import org.worldcubeassociation.tnoodle.util.ArrayUtils;

public class HugeScrambleTest {
    private static final Logger l = Logger.getLogger(HugeScrambleTest.class.getName());

    private static final Random r = WcaScrambler.getSecureRandom();

    static class LockHolder extends Thread {
        public LockHolder() {
            setDaemon(true);
        }

        private Object o;
        public void setObjectToLock(Object o) {
            synchronized(this) {
                this.o = o;
                if(isAlive()) {
                    notify();
                } else {
                    start();
                }
            }
            try {
                Thread.sleep(100); // give the locker thread a chance to grab the lock
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        @Override
        public synchronized void run() {
            while(o != null) {
                synchronized(o) {
                    System.out.println("GOT LOCK " + o);
                    Object locked = o;
                    while(o == locked) {
                        try {
                            wait();
                        } catch (InterruptedException ignored) {}
                    }
                }
            }
        }
    }

    @Test
    public void testScrambleFiltering() throws InvalidScrambleException {
        System.out.println("Testing scramble filtering");

        int SCRAMBLE_COUNT = 10;

        for(WcaEvent event : WcaEvent.values()) {
            WcaScrambler<? extends PuzzleState> scrambler = WcaScrambler.getForEvent(event);

            System.out.println("Testing " + scrambler.getDescription());

            for(int count = 0; count < SCRAMBLE_COUNT; count++){
                String scramble = scrambler.generateScramble();
                System.out.println("Filtering for scramble " + scramble);

                assertSame(scrambler.solveIn(scramble, scrambler.getMinScrambleDistance() - 1), null);
            }
        }
    }

    @Test
    public void testSolveIn() throws InvalidScrambleException, InvalidMoveException {
        int SCRAMBLE_COUNT = 10;
        int SCRAMBLE_LENGTH = 4;

        for(WcaEvent event : WcaEvent.values()) {
            WcaScrambler<? extends PuzzleState> scrambler = WcaScrambler.getForEvent(event);

            System.out.println("Testing " + scrambler.getDescription());

            // Test solving the solved state
            String solution = scrambler.solveIn(null, 0);
            assertEquals("", solution);

            for(int count = 0; count < SCRAMBLE_COUNT; count++) {
                System.out.print("Scramble ["+(count+1)+"/"+SCRAMBLE_COUNT+"]: ");
                AlgorithmBuilder<? extends PuzzleState> ab = scrambler.startAlgorithmBuilder(AlgorithmBuilder.MergingMode.NO_MERGING);
                for(int i = 0; i < SCRAMBLE_LENGTH; i++){
                    Map<String, ? extends PuzzleState> successors = ab.getState().getSuccessorsByName();
                    String move = ArrayUtils.choose(r, successors.keySet());
                    System.out.print(" "+move);
                    ab.appendMove(move);
                }
                System.out.print("...");
                solution = scrambler.solveIn(ab.getStateAndGenerator().generator, SCRAMBLE_LENGTH);
                assertNotNull(solution, "Puzzle "+scrambler.getKey()+" solveIn method failed!");
                System.out.println("Found: "+solution);
                PuzzleState state = ab.getStateAndGenerator().state.applyAlgorithm(solution);
                assertTrue(scrambler.getSolvedState().equalsNormalized(state), "Solution was not correct");
            }
        }
    }

    @Test
    public void testThreads() throws InvalidScrambleException {
        LockHolder lh = new LockHolder();

        int SCRAMBLE_COUNT = 10;
        final boolean drawScramble = true;

        for(WcaEvent event : WcaEvent.values()) {
            final WcaScrambler<? extends PuzzleState> scrambler = WcaScrambler.getForEvent(event);

            System.out.println("Testing " + scrambler.getDescription());

            // It's easy to get this wrong (read about Arrays.hashCode vs Arrays.deepHashCode).
            // This is just a sanity check.
            assertEquals(scrambler.getSolvedState().hashCode(), scrambler.getSolvedState().hashCode());

            // Generating a scramble
            System.out.println("Generating a " + scrambler.getDescription() + " scramble");
            String scramble;
            lh.setObjectToLock(scrambler);
            scramble = scrambler.generateScramble();

            // Drawing that scramble
            System.out.println("Drawing " + scramble);
            Svg scrambledSvg = scrambler.drawScramble(scramble, null);
            assertNotNull(scrambledSvg);

            // Scramblers should support "null" as the empty scramble
            Svg solvedSvg = scrambler.drawScramble(null, null);
            assertNotNull(solvedSvg);

            System.out.println("Generating & drawing 2 sets of " + SCRAMBLE_COUNT + " scrambles simultaneously." +
                                " This is meant to shake out threading problems in scramblers.");
            final Object[] o = new Object[0];
            ScrambleCacherListener cacherStopper = new ScrambleCacherListenerImpl() {
                @Override
                public void scrambleCacheUpdated(ScrambleCacher src) {
                    System.out.println(Thread.currentThread() + " " + src.getAvailableCount() + " / " + src.getCacheSize());
                    if(src.getAvailableCount() == src.getCacheSize()) {
                        src.stop();
                        synchronized(o) {
                            o.notify();
                        }
                    }
                }
            };
            ScrambleCacherListener cachedScrambleDrawer = new ScrambleCacherListenerImpl() {
                @Override
                public void scrambleGenerated(String scramble) {
                    if(drawScramble) {
                        // The drawScramble option exists so we can test out generating and drawing
                        // a bunch of scrambles in 2 threads at the same time. See ScrambleTest.
                        try {
                            scrambler.drawScramble(scramble, null);
                        } catch (InvalidScrambleException e1) {
                            l.log(Level.SEVERE, "Error drawing scramble we just created. ", e1);
                        }
                    }
                }
            };
            ScrambleCacher c1 = scrambler.startCache(SCRAMBLE_COUNT);
            c1.addCacheListener(cacherStopper);
            c1.addCacheListener(cachedScrambleDrawer);
            ScrambleCacher c2 = scrambler.startCache(SCRAMBLE_COUNT);
            c2.addCacheListener(cacherStopper);
            c2.addCacheListener(cachedScrambleDrawer);
            while(c1.isRunning() || c2.isRunning()) {
                synchronized(o) {
                    try {
                        o.wait();
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        lh.setObjectToLock(null);
        System.out.println("\nTest passed!");
    }

    @Test
    public void testClockPuzzle() throws InvalidScrambleException {
        ClockPuzzle clock = new ClockPuzzle();
        PuzzleSolutionEngine<ClockState> engine = clock.getSolutionEngine();
        ClockState state = clock.getSolvedState();
        state = state.applyAlgorithm("ALL2+ y2 ALL1-"); // This scramble is breaking the solveIn method...
        String solution = engine.solveIn(state, 3);
        if(solution == null) {
            System.out.println("No solution");
        } else {
            System.out.println(solution);
        }
    }

    @Test
    public void testCubePuzzle() throws InvalidScrambleException, InvalidMoveException {
        testCubeNormalization();
        testTwosConverter();
        testTwosSolver();
    }

    @Test
    public void testCubeNormalization() throws InvalidScrambleException, InvalidMoveException {
        CubePuzzle fours = new CubePuzzle(4);
        CubeState solved = fours.getSolvedState();

        CubeState state = solved.applyAlgorithm("Rw Lw'");
        CubeState normalizedState = state.getNormalized();
        CubeState normalizedSolvedState = solved.getNormalized();
        assertEquals(normalizedState, normalizedSolvedState);
        assertEquals(normalizedState.hashCode(), normalizedSolvedState.hashCode());

        state = solved.applyAlgorithm("Uw Dw'");
        normalizedState = state.getNormalized();
        assertEquals(normalizedState, normalizedSolvedState);

        CubePuzzle threes = new ThreeByThreeCubePuzzle();

        solved = threes.getSolvedState();
        CubeState bDone = solved.apply("B");
        CubeState fwDone = solved.apply("Fw");
        assertTrue(bDone.equalsNormalized(fwDone));

        AlgorithmBuilder<CubeState> ab3 = new AlgorithmBuilder<>(AlgorithmBuilder.MergingMode.CANONICALIZE_MOVES, threes.getSolvedState());
        String alg = "D2 U' L2 B2 F2 D B2 U' B2 F D' F U' R F2 L2 D' B D F'";
        ab3.appendAlgorithm(alg);
        assertEquals(ab3.toString(), alg);

        for(int depth = 0; depth < 100; depth++) {
            state = ArrayUtils.choose(r, state.getSuccessorsByName().values());
            normalizedState = state.getNormalized();
            CubeState rotatedState = state.applyAlgorithm("Uw Dw'").getNormalized();
            assertEquals(normalizedState, rotatedState);
        }
    }

    @Test
    public void testAlgorithmBuilder() throws InvalidMoveException {
        System.out.println("Testing algorithm builder");

        CubePuzzle fours = new CubePuzzle(4);
        AlgorithmBuilder<CubeState> ab4 = new AlgorithmBuilder<>(AlgorithmBuilder.MergingMode.CANONICALIZE_MOVES, fours.getSolvedState());
        String ogAlg = "Rw Lw";
        ab4.appendAlgorithm(ogAlg);
        String shortenedAlg = ab4.toString();
        System.out.println(ogAlg + " -> " + shortenedAlg);
        String[] shortenedAlgSplit = AlgorithmBuilder.splitAlgorithm(shortenedAlg);
        assertEquals(shortenedAlgSplit.length, 1);

        Puzzle<SquareOneState> sq1 = new SquareOnePuzzle();
        AlgorithmBuilder<SquareOneState> abSq1;

        abSq1 = new AlgorithmBuilder<>(AlgorithmBuilder.MergingMode.CANONICALIZE_MOVES, sq1.getSolvedState());
        abSq1.appendAlgorithm("(1,0) (0,1)");
        assertEquals(abSq1.toString(), "(1,1)");

        abSq1 = new AlgorithmBuilder<>(AlgorithmBuilder.MergingMode.CANONICALIZE_MOVES, sq1.getSolvedState());
        abSq1.appendAlgorithm("(0,1) (1,1)");
        assertEquals(abSq1.toString(), "(1,2)");

        CubePuzzle fives = new CubePuzzle(5);
        AlgorithmBuilder<CubeState> ab5 = new AlgorithmBuilder<>(AlgorithmBuilder.MergingMode.NO_MERGING, fives.getSolvedState());
        String alg = "U R 4Rw'";
        ab5.appendAlgorithm(alg);
        assertEquals(alg, ab5.toString());
    }

    @Test
    public void testTwosConverter() throws InvalidMoveException {
        int orient = 0;
        int permute = 0;

        int MOVE_R = 3;
        orient = TwoByTwoSolver.moveOrient[orient][MOVE_R];
        permute = TwoByTwoSolver.movePerm[permute][MOVE_R];

        CubePuzzle twos = new CubePuzzle(2);
        CubeState state = twos.getSolvedState().apply("R");
        TwoByTwoState twoByTwoState = state.toTwoByTwoState();

        assertEquals(twoByTwoState.orientation, orient);
        assertEquals(twoByTwoState.permutation, permute);

        TwoByTwoSolver twoByTwoSolver = new TwoByTwoSolver();
        assertEquals(twoByTwoSolver.solveIn(twoByTwoState, 1), "R'");

        int MOVE_R_PRIME = 5;
        orient = TwoByTwoSolver.moveOrient[orient][MOVE_R_PRIME];
        permute = TwoByTwoSolver.movePerm[permute][MOVE_R_PRIME];
        assertEquals(orient, 0);
        assertEquals(permute, 0);
    }

    @Test
    public void testTwosSolver() throws InvalidScrambleException {
        CubePuzzle twos = new CubePuzzle(2);
        PuzzleSolutionEngine<CubeState> engine = twos.getSolutionEngine();
        CubeState state = twos.getSolvedState();
        String solution = engine.solveIn(state, 0);
        assertEquals(solution, "");

        state = state.applyAlgorithm("R2 B2 F2");
        solution = engine.solveIn(state, 1);
        assertNotEquals(solution, null);
        state = state.applyAlgorithm(solution);
        assertTrue(twos.getSolvedState().equalsNormalized(state));
    }

    @Test
    public void testPyraConverter() throws InvalidMoveException {
        int SCRAMBLE_COUNT = 1000;
        int SCRAMBLE_LENGTH = 20;

        int edgePerm = 0;
        int edgeOrient = 0;
        int cornerOrient = 0;
        int tips = 0;
        final String[] moveToString = {"U", "U'", "L", "L'", "R", "R'", "B", "B'"};

        PyraminxPuzzle pyra = new PyraminxPuzzle();
        PyraminxState state = pyra.getSolvedState();
        PyraminxSolverState sstate = state.toPyraminxSolverState();
        assertEquals(sstate.edgePerm, edgePerm);
        assertEquals(sstate.edgeOrient, edgeOrient);
        assertEquals(sstate.cornerOrient, cornerOrient);
        assertEquals(sstate.tips, tips);

        for (int i = 0; i < SCRAMBLE_COUNT; i++){
            System.out.println(" Scramble ["+i+"/"+SCRAMBLE_COUNT+"]");
            edgePerm = 0;
            edgeOrient = 0;
            cornerOrient = 0;
            state = pyra.getSolvedState();
            for (int j = 0; j < SCRAMBLE_LENGTH; j++){
                int move = r.nextInt(moveToString.length);
                edgePerm = PyraminxSolver.moveEdgePerm[edgePerm][move];
                edgeOrient = PyraminxSolver.moveEdgeOrient[edgeOrient][move];
                cornerOrient = PyraminxSolver.moveCornerOrient[cornerOrient][move];
                state = state.apply(moveToString[move]);
            }
            sstate = state.toPyraminxSolverState();

            assertEquals(sstate.edgePerm, edgePerm);
            assertEquals(sstate.edgeOrient, edgeOrient);
            assertEquals(sstate.cornerOrient, cornerOrient);
        }
        System.out.println();
    }

    @Test
    public void testMega() throws InvalidScrambleException {
        MegaminxPuzzle megaminx = new MegaminxPuzzle();
        MegaminxState solved = megaminx.getSolvedState();

        String spinL = "R++ L2'";
        String spinU = "D++ U2'";
        MegaminxState state = solved.applyAlgorithm(spinL).applyAlgorithm(spinU).applyAlgorithm(spinU).applyAlgorithm(spinL).applyAlgorithm(spinL).applyAlgorithm(spinL);
        state = state.applyAlgorithm(spinU);
        assertTrue(state.equalsNormalized(solved));
    }

    @Test
    public void benchmarking() throws InvalidScrambleException {

        // Analyze the 3x3x3 solver.
        int THREE_BY_THREE_SCRAMBLE_COUNT = 100;
        int THREE_BY_THREE_MAX_SCRAMBLE_LENGTH = 21;
        int THREE_BY_THREE_TIMEMIN = 0; //milliseconds
        int THREE_BY_THREE_TIMEOUT = 5*1000; //milliseconds

        cs.min2phase.Search threeSolver = new cs.min2phase.Search();
        cs.min2phase.Search.init();
        l.info("Searching for " + THREE_BY_THREE_SCRAMBLE_COUNT + " random 3x3x3 cubes in less that " + THREE_BY_THREE_MAX_SCRAMBLE_LENGTH + " moves");
        long startMillis = System.currentTimeMillis();

        for(int i = 0; i < THREE_BY_THREE_SCRAMBLE_COUNT; i++){
            threeSolver.solution(cs.min2phase.Tools.randomCube(r), THREE_BY_THREE_MAX_SCRAMBLE_LENGTH, THREE_BY_THREE_TIMEOUT, THREE_BY_THREE_TIMEMIN, cs.min2phase.Search.INVERSE_SOLUTION);
        }

        long endMillis = System.currentTimeMillis();
        l.info("Finished after " + (endMillis - startMillis) + "ms");

        // How long does it takes to test if a puzzle is solvable in <= 1 move?
        int SCRAMBLE_COUNT = 100;

        for(WcaEvent event : WcaEvent.values()) {
            final WcaScrambler<? extends PuzzleState> scrambler = WcaScrambler.getForEvent(event);

            l.info("Are " + THREE_BY_THREE_SCRAMBLE_COUNT + " " + scrambler.getDescription() + " more than one move away from solved?");
            startMillis = System.currentTimeMillis();
            for(int count = 0; count < SCRAMBLE_COUNT; count++){
                String scramble = scrambler.generateScramble();
                System.out.println("Searching for solution in <= 1 move to " + scramble);
                String solution = scrambler.solveIn(scramble, 1);
                assertNull(solution);
            }
            endMillis = System.currentTimeMillis();
            l.info("Finished after " + (endMillis - startMillis) + "ms");
        }
    }
}
