package org.worldcubeassociation.tnoodle.exceptions;

public class InvalidMoveException extends Exception {
    public InvalidMoveException(String move) {
        super("Invalid move: " + move);
    }
}
