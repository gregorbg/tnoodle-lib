package org.worldcubeassociation.tnoodle.state;

import org.worldcubeassociation.tnoodle.AbstractPuzzleState;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClockState extends AbstractPuzzleState<ClockState> {
    public static final String[] TURNS = {"UR","DR","DL","UL","U","R","D","L","ALL"};
    private static final int[][] moves = {
        {0,1,1,0,1,1,0,0,0,  -1, 0, 0, 0, 0, 0, 0, 0, 0},// UR
        {0,0,0,0,1,1,0,1,1,   0, 0, 0, 0, 0, 0,-1, 0, 0},// DR
        {0,0,0,1,1,0,1,1,0,   0, 0, 0, 0, 0, 0, 0, 0,-1},// DL
        {1,1,0,1,1,0,0,0,0,   0, 0,-1, 0, 0, 0, 0, 0, 0},// UL
        {1,1,1,1,1,1,0,0,0,  -1, 0,-1, 0, 0, 0, 0, 0, 0},// U
        {0,1,1,0,1,1,0,1,1,  -1, 0, 0, 0, 0, 0,-1, 0, 0},// R
        {0,0,0,1,1,1,1,1,1,   0, 0, 0, 0, 0, 0,-1, 0,-1},// D
        {1,1,0,1,1,0,1,1,0,   0, 0,-1, 0, 0, 0, 0, 0,-1},// L
        {1,1,1,1,1,1,1,1,1,  -1, 0,-1, 0, 0, 0,-1, 0,-1},// A
    };

    public boolean[] pins;
    public int[] posit;
    public boolean rightSideUp;

    public ClockState() {
        pins = new boolean[] {false, false, false, false};
        posit = new int[] {0,0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0,0};
        rightSideUp = true;
    }

    public ClockState(boolean[] pins, int[] posit, boolean rightSideUp) {
        this.pins = pins;
        this.posit = posit;
        this.rightSideUp = rightSideUp;
    }

    @Override
    public Map<String, ClockState> getSuccessorsByName() {
        Map<String, ClockState> successors = new LinkedHashMap<>();

        for(int turn = 0; turn < TURNS.length; turn++) {
            for(int rot = 0; rot < 12; rot++) {
                // Apply the move
                int[] positCopy = new int[18];
                boolean[] pinsCopy = new boolean[4];
                for( int p=0; p<18; p++) {
                    positCopy[p] = (posit[p] + rot*moves[turn][p] + 12)%12;
                }
                System.arraycopy(pins, 0, pinsCopy, 0, 4);

                // Build the move string
                boolean clockwise = ( rot < 7 );
                String move = TURNS[turn] + (clockwise?(rot+"+"):((12-rot)+"-"));

                successors.put(move, new ClockState(pinsCopy, positCopy, rightSideUp));
            }
        }

        // Still y2 to implement
        int[] positCopy = new int[18];
        boolean[] pinsCopy = new boolean[4];
        System.arraycopy(posit, 0, positCopy, 9, 9);
        System.arraycopy(posit, 9, positCopy, 0, 9);
        System.arraycopy(pins, 0, pinsCopy, 0, 4);
        successors.put("y2", new ClockState(pinsCopy, positCopy, !rightSideUp));

        // Pins position moves
        for(int pin = 0; pin < 4; pin++) {
            int[] positC = new int[18];
            boolean[] pinsC = new boolean[4];
            System.arraycopy(posit, 0, positC, 0, 18);
            System.arraycopy(pins, 0, pinsC, 0, 4);
            int pinI = (pin==0?1:(pin==1?3:(pin==2?2:0)));
            pinsC[pinI] = true;

            successors.put(TURNS[pin], new ClockState(pinsC, positC, rightSideUp));
        }

        return successors;
    }

    @Override
    public ClockState unpack() {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        ClockState o = ((ClockState) other);
        return Arrays.equals(posit, o.posit);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(posit);
    }
}
