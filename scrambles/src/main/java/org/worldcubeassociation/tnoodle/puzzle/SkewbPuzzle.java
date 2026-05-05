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
        String scramble = skewbSolver.generateExactly(state, MIN_SCRAMBLE_LENGTH, r);
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
        private final int[] colorToSolver = new int[] {
            SOLVER_U, SOLVER_BR, SOLVER_FR, SOLVER_D, SOLVER_FL, SOLVER_BL
        };

        // Primary base faces for both the Fixed and Free corners
        private final int[] CORNER_PRIMARY = { SOLVER_U, SOLVER_U, SOLVER_D, SOLVER_D };

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
            for (int i=0; i<6; i++) {
                System.arraycopy(_image[i], 0, image[i], 0, 5);
            }
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
            SkewbSolverState state = new SkewbSolverState();

            int[] centers = new int[] {
                colorToSolver[image[0][0]],
                colorToSolver[image[2][0]],
                colorToSolver[image[4][0]],
                colorToSolver[image[1][0]],
                colorToSolver[image[5][0]],
                colorToSolver[image[3][0]]
            };

            int[][] fixedCorners = new int[][] {
                { colorToSolver[image[0][4]], colorToSolver[image[1][1]], colorToSolver[image[2][2]] },
                { colorToSolver[image[0][1]], colorToSolver[image[4][1]], colorToSolver[image[5][2]] },
                { colorToSolver[image[3][1]], colorToSolver[image[4][4]], colorToSolver[image[2][3]] },
                { colorToSolver[image[3][4]], colorToSolver[image[1][4]], colorToSolver[image[5][3]] }
            };

            int[][] freeCorners = new int[][] {
                { colorToSolver[image[0][3]], colorToSolver[image[2][1]], colorToSolver[image[4][2]] },
                { colorToSolver[image[0][2]], colorToSolver[image[5][1]], colorToSolver[image[1][2]] },
                { colorToSolver[image[3][2]], colorToSolver[image[2][4]], colorToSolver[image[1][3]] },
                { colorToSolver[image[3][3]], colorToSolver[image[5][4]], colorToSolver[image[4][3]] }
            };

            // To identify corner permutations, we essentially use the same "sum trick" as PyraminxState:
            //   Every corner on the Skewb folds to a unique sum because the stickers on that corner never change
            int[] currentFixedPerm = new int[4];
            for (int i = 0; i < 4; i++) {
                int sum = fixedCorners[i][0] + fixedCorners[i][1] + fixedCorners[i][2];
                switch (sum) {
                    case 4:  currentFixedPerm[i] = 0; break; // U-FR-BR (0+1+3)
                    case 6:  currentFixedPerm[i] = 1; break; // U-FL-BL (0+2+4)
                    case 8:  currentFixedPerm[i] = 2; break; // D-FL-FR (5+2+1)
                    case 12: currentFixedPerm[i] = 3; break; // D-BL-BR (5+4+3)
                    default: assert false;
                }
            }

            int[] currentFreePerm = new int[4];
            for (int i = 0; i < 4; i++) {
                int sum = freeCorners[i][0] + freeCorners[i][1] + freeCorners[i][2];
                switch (sum) {
                    case 3:  currentFreePerm[i] = 0; break;
                    case 7:  currentFreePerm[i] = 1; break;
                    case 9:  currentFreePerm[i] = 2; break;
                    case 11: currentFreePerm[i] = 3; break;
                    default: assert false;
                }
            }

            // To identify orientation, we turn the piece long enough until it matches
            //   the corresponding center base color (because that's how the solver defines "oriented")
            int[] fixedTwist = new int[4];
            for (int i = 0; i < 4; i++) {
                int primaryColor = CORNER_PRIMARY[currentFixedPerm[i]];
                while (fixedCorners[i][fixedTwist[i]] != primaryColor) {
                    fixedTwist[i]++;
                    assert fixedTwist[i] < 3;
                }
            }

            int[] freeTwist = new int[4];
            for (int i = 0; i < 4; i++) {
                int primaryColor = CORNER_PRIMARY[currentFreePerm[i]];
                while (freeCorners[i][freeTwist[i]] != primaryColor) {
                    freeTwist[i]++;
                    assert freeTwist[i] < 3;
                }
            }

            state.perm = SkewbSolver.packCenterPerm(centers) * SkewbSolver.FREE_CORNER_PERM + SkewbSolver.packCornerPerm(currentFreePerm);
            state.twst = SkewbSolver.packCornerOrient(freeTwist, fixedTwist);

            return state;
        }

        @Override
        public String solveIn(int n) {
            return skewbSolver.solveIn(toSkewbSolverState(), n, new Random());
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
