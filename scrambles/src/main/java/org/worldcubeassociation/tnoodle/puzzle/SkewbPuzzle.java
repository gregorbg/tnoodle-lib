package org.worldcubeassociation.tnoodle.puzzle;

import org.worldcubeassociation.tnoodle.svglite.Color;
import org.worldcubeassociation.tnoodle.svglite.Svg;
import org.worldcubeassociation.tnoodle.svglite.Dimension;
import org.worldcubeassociation.tnoodle.svglite.Path;
import org.worldcubeassociation.tnoodle.svglite.Transform;

import java.util.*;
import java.util.logging.Logger;

import org.worldcubeassociation.tnoodle.puzzle.SkewbSolver.SkewbSolverState;

import org.worldcubeassociation.tnoodle.scrambles.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.scrambles.Puzzle;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleStateAndGenerator;

import org.timepedia.exporter.client.Export;

@Export
public class SkewbPuzzle extends Puzzle {
    private static final int MIN_SCRAMBLE_LENGTH = 11;
    private static final Logger l = Logger.getLogger(SkewbPuzzle.class.getName());
    private final SkewbSolver skewbSolver;

    private static final int pieceSize = 30;
    private static final int gap = 3;

    private static final double sq3d2 = Math.sqrt(3) / 2;

    public SkewbPuzzle() {
        skewbSolver = new SkewbSolver();
        wcaMinScrambleDistance = 7;
    }

    @Override
    public PuzzleStateAndGenerator generateRandomMoves(Random r) {
        SkewbSolverState state = skewbSolver.randomState(r);
        String scramble = skewbSolver.generateExactly(state, MIN_SCRAMBLE_LENGTH);
        assert scramble.split(" ").length == MIN_SCRAMBLE_LENGTH;

        PuzzleState pState;
        try {
            pState = getSolvedState().applyAlgorithm(scramble);
        } catch (InvalidScrambleException e) {
            throw new RuntimeException(e);
        }
        return new PuzzleStateAndGenerator(pState, scramble);
    }

    /*************************************************************
     * Functions to display the puzzle
     */


    private static final Map<String, Color> defaultColorScheme = new HashMap<>();
    static {
        defaultColorScheme.put("U", Color.WHITE);
        defaultColorScheme.put("R", Color.BLUE);
        defaultColorScheme.put("F", Color.RED);
        defaultColorScheme.put("D", Color.YELLOW);
        defaultColorScheme.put("L", Color.GREEN);
        defaultColorScheme.put("B", new Color(0xFF8000));
    }

    @Override
    public Map<String, Color> getDefaultColorScheme() {
        return new HashMap<>(defaultColorScheme);
    }

    private Transform[] getFaceTrans() {
        Transform[] position = {
            new Transform(pieceSize*sq3d2, -pieceSize/2, pieceSize*sq3d2, pieceSize/2, (pieceSize*4+gap*1.5)*sq3d2, pieceSize),
            new Transform(pieceSize*sq3d2, -pieceSize/2, 0, pieceSize, (pieceSize*7+gap*3)*sq3d2, pieceSize * 1.5),
            new Transform(pieceSize*sq3d2, -pieceSize/2, 0, pieceSize, (pieceSize*5+gap*2)*sq3d2, pieceSize * 2.5 + 0.5 * gap),
            new Transform(0, pieceSize, -pieceSize*sq3d2, -pieceSize/2, (pieceSize*3+gap*1)*sq3d2, pieceSize * 4.5 + 1.5 * gap),
            new Transform(pieceSize*sq3d2, pieceSize/2, 0, pieceSize, (pieceSize*3+gap*1)*sq3d2, pieceSize * 2.5 + 0.5 * gap),
            new Transform(pieceSize*sq3d2, pieceSize/2, 0, pieceSize, pieceSize*sq3d2, pieceSize * 1.5),
        };
        return position;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                (int) Math.ceil((3 * gap + 8 * pieceSize + 1) * sq3d2),
                (int) Math.ceil(2 * gap + 6 * pieceSize + 1));
    }

    @Override
    public String getLongName() {
        return "Skewb";
    }

    @Override
    public String getShortName() {
        return "skewb";
    }

    @Override
    public PuzzleState getSolvedState() {
        return new SkewbState();
    }

    @Override
    protected int getRandomMoveCount() {
        return 15;
    }

    public class SkewbState extends PuzzleState {
        // Jaap's notation used in the solver
        private static final int SOLVER_U  = 0;
        private static final int SOLVER_FR = 1;
        private static final int SOLVER_FL = 2;
        private static final int SOLVER_BR = 3;
        private static final int SOLVER_BL = 4;
        private static final int SOLVER_D  = 5;

        // The order of faces in our internal `image` representation
        private final int[] COLOR_TO_SOLVER = new int[] {
            SOLVER_U, SOLVER_BR, SOLVER_FR, SOLVER_D, SOLVER_FL, SOLVER_BL
        };

        private final int[][] Z2_CORRECTIONS = new int[][]{
            { 3, 5, 4, 0, 2, 1 },
            { 2, 1, 3, 5, 4, 0 },
            { 4, 0, 2, 1, 3, 5 },
        };

        // Reusable coordinate maps for {Face, Sticker}
        private final int[][][] FIXED_CORNER_COORDS = {
            {{0, 4}, {1, 1}, {2, 2}}, // U-FR-BR
            {{0, 1}, {4, 1}, {5, 2}}, // U-FL-BL
            {{3, 1}, {4, 4}, {2, 3}}, // D-FL-FR
            {{3, 4}, {1, 4}, {5, 3}}  // D-BL-BR
        };

        private final int[][][] FREE_CORNER_COORDS = {
            {{0, 3}, {2, 1}, {4, 2}}, // U-FR-FL (Front)
            {{0, 2}, {5, 1}, {1, 2}}, // U-BL-BR (Back)
            {{3, 2}, {2, 4}, {1, 3}}, // D-FR-BR (Right-Down)
            {{3, 3}, {5, 4}, {4, 3}}  // D-FL-BL (Left-Down)
        };

        /**
         *           +---------+
         *           | 1     2 |
         *       U > |   0-0   |
         *           | 3     4 |
         * +---------+---------+---------+---------+
         * | 1     2 | 1     2 | 1     2 | 1     2 |
         * |   4-0   |   2-0   |   1-0   |   5-0   |
         * | 3     4 | 3     4 | 3     4 | 3     4 |
         * +---------+---------+---------+---------+
         *      ^    | 1     2 |
         *      FL   |   3-0   |
         *           | 3     4 |
         *           +---------+
         */
        private final int[][] image = new int[6][5];

        SkewbState() {
            for (int i=0; i<6; i++) {
                for (int j=0; j<5; j++) {
                    image[i][j] = i;
                }
            }
        }

        SkewbState(int[][] _image) {
            deepCopy(_image, image);
        }

        private void turn(int axis, int pow, int[][] image) {
            //axis:0-R 1-U 2-L 3-B
            for (int p=0; p<pow; p++) {
                switch (axis) {
                    case 0:
                        swap(2, 0, 3, 0, 1, 0, image);
                        swap(2, 4, 3, 2, 1, 3, image);
                        swap(2, 2, 3, 1, 1, 4, image);
                        swap(2, 3, 3, 4, 1, 1, image);
                        swap(4, 4, 5, 3, 0, 4, image);
                        break;
                    case 1:
                        swap(0, 0, 1, 0, 5, 0, image);
                        swap(0, 2, 1, 2, 5, 1, image);
                        swap(0, 4, 1, 4, 5, 2, image);
                        swap(0, 1, 1, 1, 5, 3, image);
                        swap(4, 1, 2, 2, 3, 4, image);
                        break;
                    case 2:
                        swap(4, 0, 5, 0, 3, 0, image);
                        swap(4, 3, 5, 4, 3, 3, image);
                        swap(4, 1, 5, 3, 3, 1, image);
                        swap(4, 4, 5, 2, 3, 4, image);
                        swap(2, 3, 0, 1, 1, 4, image);
                        break;
                    case 3:
                        swap(1, 0, 3, 0, 5, 0, image);
                        swap(1, 4, 3, 4, 5, 3, image);
                        swap(1, 3, 3, 3, 5, 1, image);
                        swap(1, 2, 3, 2, 5, 4, image);
                        swap(0, 2, 2, 4, 4, 3, image);
                        break;
                    default:
                        assert false;
                }
            }
        }

        private void swap(int f1, int s1, int f2, int s2, int f3, int s3, int[][] image) {
            int temp = image[f1][s1];
            image[f1][s1] = image[f2][s2];
            image[f2][s2] = image[f3][s3];
            image[f3][s3] = temp;
        }

        /**
         * return a square skewb face. whose 4 corners are (-1, -1), (1, -1), (1, 1), (-1, 1). It will be transformed later.
         */
        private Path[] getFacePaths() {
            Path[] p = new Path[5];
            for (int i=0; i<5; i++) {
                p[i] = new Path();
                // In svg, by default, borders are scaled along with shapes.
                // Setting vector-effect to non-scaling-stroke disables that.
                // Unfortunately, batik doesn't support it, so we have
                // to do something hacky by explicitly setting the
                // stroke-width to something teeny.
                // If Batik ever changes to support vector-effect, we
                // can clean this up.
                //p[i].setAttribute("vector-effect", "non-scaling-stroke");
                p[i].setAttribute("stroke-width", 1.0/pieceSize + "px");
            }
            p[0].moveTo(-1, 0); p[0].lineTo( 0, 1); p[0].lineTo( 1, 0); p[0].lineTo(0,-1); p[0].closePath();
            p[1].moveTo(-1, 0); p[1].lineTo(-1,-1); p[1].lineTo( 0,-1); p[1].closePath();
            p[2].moveTo( 0,-1); p[2].lineTo( 1,-1); p[2].lineTo( 1, 0); p[2].closePath();
            p[3].moveTo(-1, 0); p[3].lineTo(-1, 1); p[3].lineTo( 0, 1); p[3].closePath();
            p[4].moveTo( 0, 1); p[4].lineTo( 1, 1); p[4].lineTo( 1, 0); p[4].closePath();
            return p;
        }

        @Override
        protected Svg drawScramble(Map<String, Color> colorScheme) {
            Svg g = new Svg(getPreferredSize());
            Color[] scheme = new Color[6];
            for(int i = 0; i < scheme.length; i++) {
                scheme[i] = colorScheme.get("URFDLB".charAt(i)+"");
            }
            Transform[] position = getFaceTrans();
            for (int face=0; face<6; face++) {
                Path[] p = getFacePaths();
                for (int i=0; i<5; i++) {
                    p[i].transform(position[face]);
                    p[i].setFill(scheme[image[face][i]]);
                    p[i].setStroke(Color.BLACK);
                    g.appendChild(p[i]);
                }
            }
            return g;
        }

        public SkewbSolverState toSkewbSolverState() {
            // Look at the orientation of the four Jaap corners
            int[] fcnTwist = new int[4];
            for (int i = 0; i < 4; i++) {
                while (this.image[FIXED_CORNER_COORDS[i][fcnTwist[i]][0]][FIXED_CORNER_COORDS[i][fcnTwist[i]][1]] != 0 &&
                    this.image[FIXED_CORNER_COORDS[i][fcnTwist[i]][0]][FIXED_CORNER_COORDS[i][fcnTwist[i]][1]] != 3) {
                    fcnTwist[i]++;
                    assert fcnTwist[i] < 3;
                }
            }

            // FCN and Jaap notation have misaligned reference frames.
            // In FCN, the "Holy Corner" (UFR) is fixed. Jaap's solver relies on the
            // Opposite Orbit (the four corners it considers "fixed") to anchor orientation.
            // Because FCN 'B' moves act on this Opposite Orbit, the sum of their twists modulo 3
            // acts as a perfect mathematical ledger of the reference frame misalignment.
            // We calculate this offset and apply the corresponding Z2_CORRECTION palette shift.
            int orientSum = fcnTwist[0] + fcnTwist[1] + fcnTwist[2] + fcnTwist[3];
            int[] z2Correction = Z2_CORRECTIONS[orientSum % 3];

            // We must now physically rotate the puzzle by z2 (swapping U<->D, R<->B, F<->L).
            // Why? The Jaap notation to WCA-FCN string converter implicitly assumes the puzzle
            // is in this tilted reference frame. As a beautiful side effect, this rotation
            // physically maps all four Jaap fixed corners into their required permutation slots,
            // locking the absolute orientation for the solver.
            int[][] jaapImage = new int[][] {
                { z2Correction[image[3][0]], z2Correction[image[3][2]], z2Correction[image[3][4]], z2Correction[image[3][1]], z2Correction[image[3][3]] },
                { z2Correction[image[5][0]], z2Correction[image[5][4]], z2Correction[image[5][3]], z2Correction[image[5][2]], z2Correction[image[5][1]] },
                { z2Correction[image[4][0]], z2Correction[image[4][4]], z2Correction[image[4][3]], z2Correction[image[4][2]], z2Correction[image[4][1]] },
                { z2Correction[image[0][0]], z2Correction[image[0][3]], z2Correction[image[0][1]], z2Correction[image[0][4]], z2Correction[image[0][2]] },
                { z2Correction[image[2][0]], z2Correction[image[2][4]], z2Correction[image[2][3]], z2Correction[image[2][2]], z2Correction[image[2][1]] },
                { z2Correction[image[1][0]], z2Correction[image[1][4]], z2Correction[image[1][3]], z2Correction[image[1][2]], z2Correction[image[1][1]] }
            };

            int[] centers = new int[] {
                COLOR_TO_SOLVER[jaapImage[0][0]],
                COLOR_TO_SOLVER[jaapImage[2][0]],
                COLOR_TO_SOLVER[jaapImage[4][0]],
                COLOR_TO_SOLVER[jaapImage[1][0]],
                COLOR_TO_SOLVER[jaapImage[5][0]],
                COLOR_TO_SOLVER[jaapImage[3][0]]
            };

            int[] fixedTwist = new int[4];
            for (int i = 0; i < 4; i++) {
                while (COLOR_TO_SOLVER[jaapImage[FIXED_CORNER_COORDS[i][fixedTwist[i]][0]][FIXED_CORNER_COORDS[i][fixedTwist[i]][1]]] != SOLVER_U &&
                    COLOR_TO_SOLVER[jaapImage[FIXED_CORNER_COORDS[i][fixedTwist[i]][0]][FIXED_CORNER_COORDS[i][fixedTwist[i]][1]]] != SOLVER_D) {
                    fixedTwist[i]++;
                    assert fixedTwist[i] < 3;
                }
            }

            int[] currentFreePerm = new int[4];
            int[] freeTwist = new int[4];
            for (int i = 0; i < 4; i++) {
                int c0 = COLOR_TO_SOLVER[jaapImage[FREE_CORNER_COORDS[i][0][0]][FREE_CORNER_COORDS[i][0][1]]];
                int c1 = COLOR_TO_SOLVER[jaapImage[FREE_CORNER_COORDS[i][1][0]][FREE_CORNER_COORDS[i][1][1]]];
                int c2 = COLOR_TO_SOLVER[jaapImage[FREE_CORNER_COORDS[i][2][0]][FREE_CORNER_COORDS[i][2][1]]];

                int sum = c0 + c1 + c2;
                switch (sum) {
                    case 3:  currentFreePerm[i] = 0; break; // Front (0+1+2)
                    case 7:  currentFreePerm[i] = 1; break; // Back (0+4+3)
                    case 9:  currentFreePerm[i] = 2; break; // Right-Down (5+1+3)
                    case 11: currentFreePerm[i] = 3; break; // Left-Down (5+4+2)
                    default: assert false;
                }

                while (COLOR_TO_SOLVER[jaapImage[FREE_CORNER_COORDS[i][freeTwist[i]][0]][FREE_CORNER_COORDS[i][freeTwist[i]][1]]] != SOLVER_U &&
                    COLOR_TO_SOLVER[jaapImage[FREE_CORNER_COORDS[i][freeTwist[i]][0]][FREE_CORNER_COORDS[i][freeTwist[i]][1]]] != SOLVER_D) {
                    freeTwist[i]++;
                    assert freeTwist[i] < 3;
                }
            }

            SkewbSolverState state = new SkewbSolverState();
            state.perm = SkewbSolver.packCenterPerm(centers) * SkewbSolver.FREE_CORNER_PERM + SkewbSolver.packCornerPerm(currentFreePerm);
            state.twst = SkewbSolver.packCornerOrient(freeTwist, fixedTwist);

            return state;
        }

        @Override
        public String solveIn(int n) {
            return skewbSolver.solveIn(toSkewbSolverState(), n);
        }

        @Override
        public Map<String, PuzzleState> getSuccessorsByName() {
            Map<String, PuzzleState> successors = new LinkedHashMap<>();
            String axes = "RULB";
            for(int axis = 0; axis < axes.length(); axis++) {
                char face = axes.charAt(axis);
                for(int pow = 1; pow <= 2; pow++) {
                    String turn = "" + face;
                    if(pow == 2) {
                        turn += "'";
                    }
                    int[][] imageCopy = new int[image.length][image[0].length];
                    deepCopy(image, imageCopy);
                    turn(axis, pow, imageCopy);
                    successors.put(turn, new SkewbState(imageCopy));
                }
            }

            return successors;
        }

        @Override
        public boolean equals(Object other) {
            // Sure this could blow up with a cast exception, but shouldn't it? =)
            return Arrays.deepEquals(image, ((SkewbState) other).image);
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(image);
        }
    }

}
