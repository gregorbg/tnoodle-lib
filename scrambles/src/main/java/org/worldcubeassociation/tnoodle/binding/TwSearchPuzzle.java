package org.worldcubeassociation.tnoodle.binding;

import org.worldcubeassociation.tnoodle.puzzle.PyraminxPuzzle;
import org.worldcubeassociation.tnoodle.puzzle.SkewbPuzzle;
import org.worldcubeassociation.tnoodle.puzzle.TwoByTwoCubePuzzle;
import org.worldcubeassociation.tnoodle.scrambles.Puzzle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class TwSearchPuzzle {
    static {
        System.loadLibrary("twsearch");
    }

    private final Puzzle puzzle;
    private final String kSolveDefinition;

    public TwSearchPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.kSolveDefinition = readKSolveDefinition();
    }

    private String readKSolveDefinition() {
        InputStream defStream = this.getClass().getResourceAsStream("/kpuzzle/" + puzzle.getShortName() + ".tws");
        BufferedReader defReader = new BufferedReader(new InputStreamReader(defStream));

        return defReader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    public String generateRandomStateScramble() {
        //String kSolveDefinition = puzzle.getKSolveDefinition();

        String randomScrambleDef = generateRandomState(kSolveDefinition).lines()
            .map(String::trim)
            .collect(Collectors.joining("\n"));

        List<String> solutionLines = solveState(kSolveDefinition, randomScrambleDef)
            .lines()
            .collect(Collectors.toList());

        String solution = solutionLines.get(solutionLines.size() - 2);
        String scramble = invertAlgorithm(kSolveDefinition, solution);

        return scramble.trim();
    }

    private native String generateRandomState(String kSolveDefinition);

    public String solveState(Puzzle.PuzzleState puzzleState) {
        //String kSolveState = puzzleState.getKSolveState();
        String kSolveState = "FooBarTODO";
        return solveState(kSolveDefinition, kSolveState);
    }

    private native String solveState(String kSolveDefinition, String kSolveState);
    private native String invertAlgorithm(String kSolveDefinition, String scramble);

    public static void main(String[] args) {
        Puzzle tNoodlePuzzle = new TwoByTwoCubePuzzle();
        TwSearchPuzzle twSearchPuzzle = new TwSearchPuzzle(tNoodlePuzzle);

        System.out.println(twSearchPuzzle.generateRandomStateScramble());
    }
}
