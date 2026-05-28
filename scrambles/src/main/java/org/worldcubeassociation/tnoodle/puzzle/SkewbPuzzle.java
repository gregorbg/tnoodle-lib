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

        // Primary base faces for both the Fixed and Free corners
        private final int[] CORNER_PRIMARY = { SOLVER_U, SOLVER_U, SOLVER_D, SOLVER_D };

        private final String[][] ROTATION_LOOKUP = new String[][] {
            { null, "", "R", "RR", null, null, null, null },
            { "RLR", null, "RL", "RRLL", null, null, null, null },
            { "LLRR", "LL", null, "LLR", null, null, null, null },
            { "LR", "L", "LRR", null, null, null, null, null },
            { null, null, null, null, null, "yyy", "Ry", "LLy" },
            { null, null, null, null, "y", null, "RRyyy", "Lyyy" },
            { null, null, null, null, "Ryyy", "RRy", null, "RLLy" },
            { null, null, null, null, "LLyyy", "Ly", "RRLy", null },
        };

        private final int[] Z2 = new int[]{ 3, 5, 4, 0, 2, 1 };

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

        // in order to rotate the whole puzzle, we must touch two opposite corners.
        // By definition, these two will not be in the same orbit, so only one of them
        //   is defined as FCN notation in `swap`, and the other one references Jaap notation.
        private void rotate(int axis, int pow, int[][] image) {
            //axis:0-JaapR 1-JaapL 2-yAxis
            for (int p=0; p<pow; p++) {
                switch (axis) {
                    case 0:
                        // FCN L' move
                        turn(2, 2, image);
                        // Jaap R move
                        swap(0, 0, 2, 0, 1, 0, image);
                        swap(0, 4, 2, 2, 1, 1, image);
                        swap(0, 3, 2, 4, 1, 2, image);
                        swap(0, 2, 2, 1, 1, 3, image);
                        swap(4, 2, 3, 2, 5, 1, image);
                        break;
                    case 1:
                        // FCN R' move
                        turn(0, 2, image);
                        // Jaap L move
                        swap(0, 0, 5, 0, 4, 0, image);
                        swap(0, 1, 5, 2, 4, 1, image);
                        swap(0, 3, 5, 1, 4, 3, image);
                        swap(0, 2, 5, 4, 4, 2, image);
                        swap(2, 1, 1, 2, 3, 3, image);
                        break;
                    case 2:
                        // U and D faces
                        swap(0, 1, 0, 3, 0, 4, 0, 2, image);
                        swap(3, 1, 3, 2, 3, 4, 3, 3, image);
                        // Four faces around the Skewb
                        // Luckily, the sticker numbering is invariant under y rotations :)
                        for (int s = 0; s < 5; s++) {
                            swap(2, s, 1, s, 5,  s, 4, s, image);
                        }
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

        private void swap(int f1, int s1, int f2, int s2, int f3, int s3, int f4, int s4, int[][] image) {
            int temp = image[f1][s1];
            image[f1][s1] = image[f2][s2];
            image[f2][s2] = image[f3][s3];
            image[f3][s3] = image[f4][s4];
            image[f4][s4] = temp;
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

        private int findCornerSlot(int c1, int c2, int c3, int[][] image) {
            int[][] slots = new int[][] {
                { image[0][4], image[1][1], image[2][2] },
                { image[0][1], image[4][1], image[5][2] },
                { image[3][1], image[4][4], image[2][3] },
                { image[3][4], image[1][4], image[5][3] },
                { image[0][3], image[2][1], image[4][2] },
                { image[0][2], image[5][1], image[1][2] },
                { image[3][2], image[2][4], image[1][3] },
                { image[3][3], image[5][4], image[4][3] }
            };

            for (int i = 0; i < 8; i++) {
                int s1 = slots[i][0], s2 = slots[i][1], s3 = slots[i][2];

                // Check for each possible rotation
                if ((s1 == c1 && s2 == c2 && s3 == c3) ||
                    (s2 == c1 && s3 == c2 && s1 == c3) ||
                    (s3 == c1 && s1 == c2 && s2 == c3)) {
                    return i;
                }
            }

            return -1;
        }

        public SkewbSolverState toSkewbSolverState() {
            int[][] jaapImage = new int[6][5];

            for(int f = 0; f < 6; f++) {
                for(int s = 0; s < 5; s++) {
                    // Initialize pretending that the scramble orientation is off by z2
                    //   (the axis that runs along the plane between FCN-R and FCN-L)
                    // This is required because the conversion from Jaap notation in the solver
                    //   to WCA-FCN implicitly assumes a z2 rotation. But because z2 has order=2,
                    //   it does not matter whether we do z2*scramble or scramble*z2.
                    jaapImage[f][s] = Z2[this.image[f][s]];
                }
            }

            // Jaap R corner piece is between faces U=0, BR=1, FR=2
            int jaapR = findCornerSlot(0, 1, 2, jaapImage);

            // Jaap L corner piece is between faces U=0, FL=4, BL=5
            int jaapL = findCornerSlot(0, 4, 5, jaapImage);

            String rotations = ROTATION_LOOKUP[jaapR][jaapL];

            for (char face : rotations.toCharArray()) {
                int axis = "RLy".indexOf(face);
                rotate(axis, 1, jaapImage);
            }

            SkewbSolverState state = new SkewbSolverState();

            int[] centers = new int[] {
                COLOR_TO_SOLVER[jaapImage[0][0]],
                COLOR_TO_SOLVER[jaapImage[2][0]],
                COLOR_TO_SOLVER[jaapImage[4][0]],
                COLOR_TO_SOLVER[jaapImage[1][0]],
                COLOR_TO_SOLVER[jaapImage[5][0]],
                COLOR_TO_SOLVER[jaapImage[3][0]]
            };

            int[][] fixedCorners = new int[][] {
                { COLOR_TO_SOLVER[jaapImage[0][4]], COLOR_TO_SOLVER[jaapImage[1][1]], COLOR_TO_SOLVER[jaapImage[2][2]] },
                { COLOR_TO_SOLVER[jaapImage[0][1]], COLOR_TO_SOLVER[jaapImage[4][1]], COLOR_TO_SOLVER[jaapImage[5][2]] },
                { COLOR_TO_SOLVER[jaapImage[3][1]], COLOR_TO_SOLVER[jaapImage[4][4]], COLOR_TO_SOLVER[jaapImage[2][3]] },
                { COLOR_TO_SOLVER[jaapImage[3][4]], COLOR_TO_SOLVER[jaapImage[1][4]], COLOR_TO_SOLVER[jaapImage[5][3]] }
            };

            // To identify orientation, we turn the piece long enough until it matches
            //   the corresponding center base color (because that's how the solver defines "oriented")
            int[] fixedTwist = new int[4];
            for (int i = 0; i < 4; i++) {
                // We have rotated the puzzle so that by definition, the four fixed corners are permuted
                int primaryColor = CORNER_PRIMARY[i];
                while (fixedCorners[i][fixedTwist[i]] != primaryColor) {
                    fixedTwist[i]++;
                    assert fixedTwist[i] < 3;
                }
            }

            int[][] freeCorners = new int[][] {
                { COLOR_TO_SOLVER[jaapImage[0][3]], COLOR_TO_SOLVER[jaapImage[2][1]], COLOR_TO_SOLVER[jaapImage[4][2]] },
                { COLOR_TO_SOLVER[jaapImage[0][2]], COLOR_TO_SOLVER[jaapImage[5][1]], COLOR_TO_SOLVER[jaapImage[1][2]] },
                { COLOR_TO_SOLVER[jaapImage[3][2]], COLOR_TO_SOLVER[jaapImage[2][4]], COLOR_TO_SOLVER[jaapImage[1][3]] },
                { COLOR_TO_SOLVER[jaapImage[3][3]], COLOR_TO_SOLVER[jaapImage[5][4]], COLOR_TO_SOLVER[jaapImage[4][3]] }
            };

            // To identify corner permutations, we essentially use the same "sum trick" as PyraminxState:
            //   Every corner on the Skewb folds to a unique sum because the stickers on that corner never change
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
