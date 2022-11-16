package org.worldcubeassociation.tnoodle.puzzle;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.worldcubeassociation.tnoodle.Puzzle;
import org.worldcubeassociation.tnoodle.exceptions.InvalidMoveException;
import org.worldcubeassociation.tnoodle.exceptions.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.solver.TwoPhaseCubeSolver;
import org.worldcubeassociation.tnoodle.state.CubeState;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class NoInspectionThreeByThreeTest {
    protected static final Map<String, String> OPPOSITE_FACES = new HashMap<>();

    @BeforeAll
    public static void loadOppositeMoves() {
        String faces = "URFDLB";
        String oppFaces = "DLBURF";

        // this is the most hilarious alternative to dict(zip(faces, oppFaces)) I've ever seen…
        for (int i = 0; i < faces.length(); i++) {
            OPPOSITE_FACES.put(String.valueOf(faces.charAt(i)), String.valueOf(oppFaces.charAt(i)));
        }
    }

    @Test
    public void testSomething() throws InvalidMoveException, InvalidScrambleException {
        TwoByTwoCubePuzzle twos = new TwoByTwoCubePuzzle();
        Collection<String> canonicalMoves = twos.getSolvedState().getCanonicalMovesByState().values();

        List<String> desiredCanonicalMoves = new ArrayList<>();
        List<String> modifiers = Arrays.asList("", "'", "2");

        for (char face : "RUF".toCharArray()) {
            for (String d : modifiers) {
                desiredCanonicalMoves.add(face + d);
            }
        }

        assertEquals(new HashSet<>(canonicalMoves), new HashSet<>(desiredCanonicalMoves));

        NoInspectionThreeByThreeCubePuzzle threes = new NoInspectionThreeByThreeCubePuzzle();
        CubeState solved = threes.getSolvedState();

        String faces = "URFDLB";

        for (String p : modifiers) {
            for (char face : faces.toCharArray()) {
                String actFace = String.valueOf(face);
                String oppositeFace = OPPOSITE_FACES.get(actFace);

                List<String> restrictions = Arrays.asList(actFace, oppositeFace);

                for (String axisRestriction : restrictions) {
                    String scramble = axisRestriction + p;

                    testSolveIn(threes, scramble, axisRestriction);
                }
            }
        }

        TwoPhaseCubeSolver twoPhaseEngine = new TwoPhaseCubeSolver();

        CubeState scrambled = solved.applyAlgorithm("L' R2 U D2 L2");
        String solution = twoPhaseEngine.solveIn(scrambled, 20, "L", null);

        assertFalse(solution.startsWith("L"));
        assertTrue(solved.equalsNormalized(scrambled.applyAlgorithm(solution)));

        // min2phase can look at the inverse of a given cube and try to solve it.
        // This can screw up restricting the first turn, however. Check that
        // min2phase handles this correctly. This particular
        // scramble and restriction caused tickled this behavior originally.
        scrambled = solved.applyAlgorithm("F D B L' U L' F D' L2 D L' B2 D F2 U B2 R2 U D2 L2");
        solution = twoPhaseEngine.solveIn(scrambled, 20, "L", null);

        assertFalse(solution.startsWith("L"));
        assertFalse(solution.startsWith("R"));

        assertTrue(solved.equalsNormalized(scrambled.applyAlgorithm(solution)));

        Random r = Puzzle.getSecureRandom();

        for (int i = 0; i < 10; i++) {
            System.out.println(threes.generateWcaScramble(r));
        }
    }

    public void testSolveIn(NoInspectionThreeByThreeCubePuzzle threeNi, String scramble, String axisRestriction) throws InvalidScrambleException, InvalidMoveException {
        TwoPhaseCubeSolver twoPhaseEngine = new TwoPhaseCubeSolver();
        // Search for a solution to a cube scrambled with scramble,
        // but require that that solution not start with restriction
        CubeState solved = threeNi.getSolvedState();

        CubeState u = solved.apply(scramble);
        String solution = twoPhaseEngine.solveIn(u, 20, axisRestriction, null);

        System.out.printf("Solution to %s (restriction %s): %s%n", scramble, axisRestriction, solution);

        // restriction defines an axis of turns that may not start a solution,
        // so we assert the solution starts with neither restriction, nor
        // the face opposite restriction.
        assertFalse(solution.startsWith(axisRestriction));
        assertFalse(solution.startsWith(OPPOSITE_FACES.get(axisRestriction)));

        CubeState shouldBeSolved = u.applyAlgorithm(solution);
        assertTrue(threeNi.getSolvedState().equalsNormalized(shouldBeSolved));
    }
}
