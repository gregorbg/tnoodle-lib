package org.worldcubeassociation.tnoodle.scrambleanalysis;

import org.worldcubeassociation.tnoodle.exceptions.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.scrambles.WcaScrambler;
import org.worldcubeassociation.tnoodle.state.CubeState;

import java.util.List;

public class App {

    public static void main(String[] args)
        throws InvalidScrambleException, RepresentationException {

        // to test your set of scrambles
        // ArrayList<String> scrambles = ScrambleProvider.getScrambles(fileName);
        // boolean passed = testScrambles(scrambles);

        // Main test
        int numberOfScrambles = 6500;

        List<String> scrambles = ScrambleProvider.generateWcaScrambles(WcaScrambler.THREE, numberOfScrambles);
        List<CubeState> representations = ScrambleProvider.convertToCubeStates(scrambles);

        boolean passed = CubeTest.testScrambles(representations);
        System.out.println("\nMain test passed? " + passed);

    }
}
