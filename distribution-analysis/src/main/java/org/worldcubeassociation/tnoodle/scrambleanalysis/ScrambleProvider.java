package org.worldcubeassociation.tnoodle.scrambleanalysis;

import org.worldcubeassociation.tnoodle.PuzzleState;
import org.worldcubeassociation.tnoodle.exceptions.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.scrambles.WcaScrambler;
import org.worldcubeassociation.tnoodle.state.CubeState;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ScrambleProvider {

    public static List<String> getScrambles(String fileName) throws IOException {
        List<String> scrambles = new ArrayList<>();

        // Read scrambles
        File file = new File(fileName);

        try (Scanner input = new Scanner(file)) {
            while (input.hasNextLine()) {
                String scramble = input.nextLine().trim();
                if (scramble.length() > 0) {
                    scrambles.add(scramble);
                }
            }
        } catch (Exception e) {
            throw new IOException("There was an error reading the file.");
        }

        return scrambles;
    }

    // This is the main test
    public static List<String> generateWcaScrambles(WcaScrambler<? extends PuzzleState> cube, int N) {
        List<String> scrambles = new ArrayList<>(N);

        for (int i = 0; i < N; i++) {
            // Give some status to the user
            if (i % 1000 == 0) {
                System.out.println("Generating scramble " + (i + 1) + "/" + N);
            }

            String scramble = cube.generateScramble();
            scrambles.add(scramble);
        }

        return scrambles;
    }

    static WcaScrambler<CubeState> defaultCube = WcaScrambler.THREE;

    public static List<String> generateWcaScrambles(int N) {
        return generateWcaScrambles(defaultCube, N);
    }

    public static List<CubeState> convertToCubeStates(List<String> scrambles) throws InvalidScrambleException {
        List<CubeState> cubeStates = new ArrayList<>(scrambles.size());

        for (String scramble : scrambles) {
            CubeState cubeState = defaultCube.getPuzzleState(scramble);

            cubeStates.add(cubeState);
        }

        return cubeStates;
    }
}
