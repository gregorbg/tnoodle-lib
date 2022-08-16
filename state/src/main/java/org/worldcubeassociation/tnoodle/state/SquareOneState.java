package org.worldcubeassociation.tnoodle.state;

import cs.sq12phase.FullCube;
import org.worldcubeassociation.tnoodle.AbstractPuzzleState;
import org.worldcubeassociation.tnoodle.util.ArrayUtils;

import java.util.*;

public class SquareOneState extends AbstractPuzzleState<SquareOneState> {
    static HashMap<String, Integer> wcaCostsByMove = new HashMap<>();
    public static Map<String, Integer> slashabilityCostsByMove = new HashMap<>();
    static {
        for(int top = -5; top <= 6; top++) {
            for(int bottom = -5; bottom <= 6; bottom++) {
                if(top == 0 && bottom == 0) {
                    // No use doing nothing =)
                    continue;
                }
                String turn = "(" + top + "," + bottom + ")";

                int wcaCost = 1; // https://www.worldcubeassociation.org/regulations/#12c4
                wcaCostsByMove.put(turn, wcaCost);

                int topCost = Math.abs(top);
                int bottomCost = Math.abs(bottom);
                int topBottomCost = topCost + bottomCost;
                slashabilityCostsByMove.put(turn, topBottomCost);
            }
        }
        // https://www.worldcubeassociation.org/regulations/#12c4
        wcaCostsByMove.put("/", 1);
    }

    public boolean sliceSolved;
    public int[] pieces;

    public SquareOneState() {
        sliceSolved = true;
        pieces = new int[]{ 0, 0, 1, 2, 2, 3, 4, 4, 5, 6, 6, 7, 8, 9, 9, 10, 11, 11, 12, 13, 13, 14, 15, 15 }; //piece array
    }

    public SquareOneState(boolean sliceSolved, int[] pieces) {
        this.sliceSolved = sliceSolved;
        this.pieces = pieces;
    }

    public FullCube toFullCube() {
        int[] map1 = new int[]{3, 2, 1, 0, 7, 6, 5, 4, 0xa, 0xb, 8, 9, 0xe, 0xf, 0xc, 0xd};
        int[] map2 = new int[]{5,4,3,2,1,0,11,10,9,8,7,6,17,16,15,14,13,12,23,22,21,20,19,18};
        FullCube f = new FullCube();
        for (int i=0; i<24; i++) {
            f.setPiece(map2[i], map1[pieces[i]]);
        }
        f.setPiece(24, sliceSolved ? 0 : 1);
        return f;
    }

    private int[] doSlash() {
        int[] newPieces = ArrayUtils.cloneArr(pieces);
        for(int i = 0; i < 6; i++) {
            int c = newPieces[i+12];
            newPieces[i+12] = newPieces[i+6];
            newPieces[i+6] = c;
        }
        return newPieces;
    }

    public boolean canSlash() {
        if(pieces[0] == pieces[11]) {
            return false;
        }
        if(pieces[6] == pieces[5]) {
            return false;
        }
        if(pieces[12] == pieces[23]) {
            return false;
        }
        if(pieces[12+6] == pieces[(12+6)-1]) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param top Amount to rotate top
     * @param bottom Amount to rotate bottom
     * @return A copy of pieces with (top, bottom) applied to it
     */
    private int[] doRotateTopAndBottom(int top, int bottom) {
        top = ((-top % 12) + 12) % 12;
        int[] newPieces = ArrayUtils.cloneArr(pieces);
        int[] t = new int[12];
        System.arraycopy(newPieces, 0, t, 0, 12);
        for(int i = 0; i < 12; i++) {
            newPieces[i] = t[(top + i) % 12];
        }

        bottom = ((-bottom % 12) + 12) % 12;

        System.arraycopy(newPieces, 12, t, 0, 12);
        for(int i = 0; i < 12; i++) {
            newPieces[i+12] = t[(bottom + i) % 12];
        }

        return newPieces;
    }

    public int getMoveCost(String move) {
        // TODO - We do a lookup here rather than string parsing because
        // this is a very performance critical section of code.
        // I believe the best thing to do would be to change the puzzle
        // api to return move costs as part of the object returned by
        // getScrambleSuccessors(), then subclasses wouldn't have to do
        // weird stuff like this for speed.
        return wcaCostsByMove.get(move);
    }

    @Override
    public Map<String, SquareOneState> getScrambleSuccessors() {
        Map<String, SquareOneState> successors = getSuccessorsByName();
        Iterator<String> iter = successors.keySet().iterator();
        while(iter.hasNext()) {
            String key = iter.next();
            SquareOneState state = successors.get(key);
            if(!state.canSlash()) {
                iter.remove();
            }
        }
        return successors;
    }

    @Override
    public Map<String, SquareOneState> getSuccessorsByName() {
        LinkedHashMap<String, SquareOneState> successors = new LinkedHashMap<>();
        for(int top = -5; top <= 6; top++) {
            for(int bottom = -5; bottom <= 6; bottom++) {
                if(top == 0 && bottom == 0) {
                    // No use doing nothing =)
                    continue;
                }
                int[] newPieces = doRotateTopAndBottom(top, bottom);
                String turn = "(" + top + "," + bottom + ")";
                successors.put(turn, new SquareOneState(sliceSolved, newPieces));
            }
        }
        if(canSlash()) {
            successors.put("/", new SquareOneState(!sliceSolved, doSlash()));
        }
        return successors;
    }

    @Override
    public SquareOneState unpack() {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        SquareOneState o = ((SquareOneState) other);
        return Arrays.equals(pieces, o.pieces) && sliceSolved == o.sliceSolved;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pieces) ^ (sliceSolved ? 1 : 0);
    }

    public String toString() {
        return "sliceSolved: " + sliceSolved + " " + Arrays.toString(pieces);
    }

}
