package org.worldcubeassociation.tnoodle.state;

import org.worldcubeassociation.tnoodle.AbstractPuzzleState;
import org.worldcubeassociation.tnoodle.solver.PyraminxSolver;
import org.worldcubeassociation.tnoodle.util.ArrayUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class PyraminxState extends AbstractPuzzleState<PyraminxState> {
    public int[][] image;
    /** Trying to make an ascii art of the pyraminx stickers position...
     *
     *                                    U
     *              ____  ____  ____              ____  ____  ____
     *             \    /\    /\    /     /\     \    /\    /\    /
     *              \0 /1 \2 /4 \3 /     /0 \     \0 /1 \2 /4 \3 /
     *               \/____\/____\/     /____\     \/____\/____\/
     *                \    /\    /     /\    /\     \    /\    /
     *        face 2   \8 /7 \5 /     /8 \1 /2 \     \8 /7 \5 / face 3
     *                  \/____\/     /____\/____\     \/____\/
     *                   \    /     /\    /\    /\     \    /
     *                    \6 /     /6 \7 /5 \4 /3 \     \6 /
     *                     \/     /____\/____\/____\     \/
     *                                  face 0
     *                        L    ____  ____  ____    R
     *                            \    /\    /\    /
     *                             \0 /1 \2 /4 \3 /
     *                              \/____\/____\/
     *                               \    /\    /
     *                                \8 /7 \5 /
     *                         face 1  \/____\/
     *                                  \    /
     *                                   \6 /
     *                                    \/
     *
     *                                    B
     */

    public PyraminxState() {
        image = new int[4][9];
        for(int i = 0; i < image.length; i++) {
            for(int j = 0; j < image[0].length; j++) {
                image[i][j] = i;
            }
        }
    }

    public PyraminxState(int[][] image) {
        this.image = image;
    }

    private void turn(int side, int dir, int[][] image) {
        for(int i = 0; i < dir; i++) {
            turn(side, image);
        }
    }

    private void turnTip(int side, int dir, int[][] image) {
        for(int i = 0; i < dir; i++) {
            turnTip(side, image);
        }
    }

    private void turn(int s, int[][] image) {
        switch(s) {
            case 0:
                swap(0, 8, 3, 8, 2, 2, image);
                swap(0, 1, 3, 1, 2, 4, image);
                swap(0, 2, 3, 2, 2, 5, image);
                break;
            case 1:
                swap(2, 8, 1, 2, 0, 8, image);
                swap(2, 7, 1, 1, 0, 7, image);
                swap(2, 5, 1, 8, 0, 5, image);
                break;
            case 2:
                swap(3, 8, 0, 5, 1, 5, image);
                swap(3, 7, 0, 4, 1, 4, image);
                swap(3, 5, 0, 2, 1, 2, image);
                break;
            case 3:
                swap(1, 8, 2, 2, 3, 5, image);
                swap(1, 7, 2, 1, 3, 4, image);
                swap(1, 5, 2, 8, 3, 2, image);
                break;
            default:
                assert false;
        }
        turnTip(s, image);
    }

    private void turnTip(int s, int[][] image) {
        switch(s) {
            case 0:
                swap(0, 0, 3, 0, 2, 3, image);
                break;
            case 1:
                swap(0, 6, 2, 6, 1, 0, image);
                break;
            case 2:
                swap(0, 3, 1, 3, 3, 6, image);
                break;
            case 3:
                swap(1, 6, 2, 0, 3, 3, image);
                break;
            default:
                assert false;
        }
    }

    private void swap(int f1, int s1, int f2, int s2, int f3, int s3, int[][] image) {
        int temp = image[f1][s1];
        image[f1][s1] = image[f2][s2];
        image[f2][s2] = image[f3][s3];
        image[f3][s3] = temp;
    }

    public PyraminxSolver.PyraminxSolverState toPyraminxSolverState() {
        PyraminxSolver.PyraminxSolverState state = new PyraminxSolver.PyraminxSolverState();

        /** Each face color is assigned a value so that the sum of the color (minus 1) of each edge gives a unique integer.
         * These edge values match the edge numbering in the PyraminxSolver class, making the following code simpler.
         *                                    U
         *              ____  ____  ____              ____  ____  ____
         *             \    /\    /\    /     /\     \    /\    /\    /
         *              \  /  \5 /  \  /     /  \     \  /  \5 /  \  /
         *               \/____\/____\/     /____\     \/____\/____\/
         *                \    /\    /     /\    /\     \    /\    /
         *        face +2  \2 /  \1 /     /1 \  /3 \     \3 /  \4 / face +4
         *                  \/____\/     /____\/____\     \/____\/
         *                   \    /     /\    /\    /\     \    /
         *                    \  /     /  \  /0 \  /  \     \  /
         *                     \/     /____\/____\/____\     \/
         *                                  face +0
         *                        L    ____  ____  ____    R
         *                            \    /\    /\    /
         *                             \  /  \0 /  \  /
         *                              \/____\/____\/
         *                               \    /\    /
         *                                \2 /  \4 /
         *                         face +1 \/____\/
         *                                  \    /
         *                                   \  /
         *                                    \/
         *
         *                                    B
         */
        int[][] stickersToEdges = new int[][] {
            { image[0][5], image[1][2] },
            { image[0][8], image[2][5] },
            { image[1][8], image[2][8] },
            { image[0][2], image[3][8] },
            { image[1][5], image[3][5] },
            { image[2][2], image[3][2] }
        };

        int[] colorToValue = new int[] {0, 1, 2, 4};

        int[] edges = new int[6];
        for (int i = 0; i < edges.length; i++){
            edges[i] = colorToValue[stickersToEdges[i][0]] + colorToValue[stickersToEdges[i][1]] - 1;
            // In the PyraminxSolver class, the primary facelet of each edge correspond to the lowest face number.
            if( stickersToEdges[i][0] > stickersToEdges[i][1] ) {
                edges[i] += 8;
            }
        }

        state.edgePerm = PyraminxSolver.packEdgePerm(edges);
        state.edgeOrient = PyraminxSolver.packEdgeOrient(edges);

        int[][] stickersToCorners = new int[][] {
            { image[0][1], image[2][4], image[3][1] },
            { image[0][7], image[1][1], image[2][7] },
            { image[0][4], image[3][7], image[1][4] },
            { image[1][7], image[3][4], image[2][1] }
        };

        /* The corners are supposed to be fixed, so we are also checking if they are in the right place.
         * We can use the sum trick, but here, no need for transition table :) */
        int[] correctSum = new int[] {5, 3, 4, 6};

        int[] corners = new int[4];
        for (int i = 0; i < corners.length; i++){
            assert  stickersToCorners[i][0] + stickersToCorners[i][1] + stickersToCorners[i][2] == correctSum[i];
            // The following code is not pretty, sorry...
            if(( stickersToCorners[i][0] < stickersToCorners[i][1] ) && ( stickersToCorners[i][0] < stickersToCorners[i][2] )) {
                corners[i] = 0;
            }
            if(( stickersToCorners[i][1] < stickersToCorners[i][0] ) && ( stickersToCorners[i][1] < stickersToCorners[i][2] )) {
                corners[i] = 1;
            }
            if(( stickersToCorners[i][2] < stickersToCorners[i][1] ) && ( stickersToCorners[i][2] < stickersToCorners[i][0] )) {
                corners[i] = 2;
            }
        }

        state.cornerOrient = PyraminxSolver.packCornerOrient(corners);

        /* For the tips, we use the same numbering */
        int[][] stickersToTips = new int[][] {
            { image[0][0], image[2][3], image[3][0] },
            { image[0][6], image[1][0], image[2][6] },
            { image[0][3], image[3][6], image[1][3] },
            { image[1][6], image[3][3], image[2][0] }
        };

        int[] tips = new int[4];
        for (int i = 0; i < tips.length; i++){
            int[] stickers = stickersToTips[i];
            // We can use the same color check as for the corners.
            assert stickers[0] + stickers[1] + stickers[2] == correctSum[i];

            // For the tips, we don't have to check colors against face, but against the attached corner.
            int cornerPrimaryColor = stickersToCorners[i][0];
            int clockwiseTurnsToMatchCorner = 0;
            while(stickers[clockwiseTurnsToMatchCorner] != cornerPrimaryColor) {
                clockwiseTurnsToMatchCorner++;
                assert clockwiseTurnsToMatchCorner < 3;
            }
            tips[i] = clockwiseTurnsToMatchCorner;
        }

        state.tips = PyraminxSolver.packCornerOrient(tips); // Same function as for corners.

        return state;
    }

    @Override
    public Map<String, PyraminxState> getSuccessorsByName() {
        Map<String, PyraminxState> successors = new LinkedHashMap<>();

        String axes = "ulrb";
        for(int axis = 0; axis < axes.length(); axis++) {
            for(boolean tip : new boolean[] { true, false }) {
                char face = axes.charAt(axis);
                face = tip ? Character.toLowerCase(face) : Character.toUpperCase(face);
                for(int dir = 1; dir <= 2; dir++) {
                    String turn = "" + face;
                    if(dir == 2) {
                        turn += "'";
                    }

                    int[][] imageCopy = new int[image.length][image[0].length];
                    ArrayUtils.deepCopy(image, imageCopy);

                    if(tip) {
                        turnTip(axis, dir, imageCopy);
                    } else {
                        turn(axis, dir, imageCopy);
                    }

                    successors.put(turn, new PyraminxState(imageCopy));
                }
            }
        }

        return successors;
    }

    @Override
    public PyraminxState unpack() {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        // Sure this could blow up with a cast exception, but shouldn't it? =)
        return Arrays.deepEquals(image, ((PyraminxState) other).image);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(image);
    }
}
