package org.worldcubeassociation.tnoodle.exceptions;

public class InvalidScrambleException extends Exception {
    public InvalidScrambleException(String scramble) {
        super(scramble, null);
    }
    public InvalidScrambleException(String scramble, Throwable t) {
        super("Invalid scramble: " + scramble, t);
    }
}
