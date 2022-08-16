package org.worldcubeassociation.tnoodle.state;

import org.worldcubeassociation.tnoodle.AbstractPuzzleState;
import org.worldcubeassociation.tnoodle.solver.TwoByTwoSolver;
import org.worldcubeassociation.tnoodle.util.ArrayUtils;

import java.util.*;

public class CubeState extends AbstractPuzzleState<CubeState> {
    public enum Face {
        R, U, F, L, D, B;

        public Face oppositeFace() {
            return values()[(ordinal() + 3) % 6];
        }
    }

    private static final String[] DIR_TO_STR = new String[] { null, "", "2", "'" };
    private static final HashMap<Face, String> faceRotationsByName = new HashMap<>();
    static {
        faceRotationsByName.put(Face.R, "x");
        faceRotationsByName.put(Face.U, "y");
        faceRotationsByName.put(Face.F, "z");
    }

    public final int[][][] image;
    public final int size;
    private CubeState normalizedState = null;

    public CubeState(int size) {
        this.image = new int[6][size][size];
        this.size = size;
        for(int face = 0; face < image.length; face++) {
            for(int j = 0; j < size; j++) {
                for(int k = 0; k < size; k++) {
                    image[face][j][k] = face;
                }
            }
        }
        normalizedState = this;
    }

    public CubeState(int[][][] image) {
        this.image = image;
        this.size = image[0].length;
    }

    public boolean isNormalized() {
        return isNormalized(this.image, this.size);
    }

    private static boolean isNormalized(int[][][] image, int size) {
        // A CubeState is normalized if the BLD piece is solved
        return image[Face.B.ordinal()][size-1][size-1] == Face.B.ordinal() &&
            image[Face.L.ordinal()][size-1][0] == Face.L.ordinal() &&
            image[Face.D.ordinal()][size-1][0] == Face.D.ordinal();
    }

    private int[][][] normalize(int[][][] image) {
        image = cloneImage(image);

        int spins = 0;
        while (!isNormalized(image, size)) {
            assert spins < 2;
            int[][] stickersByPiece = getStickersByPiece(image);

            int goal = 0;
            goal |= 1 << Face.B.ordinal();
            goal |= 1 << Face.L.ordinal();
            goal |= 1 << Face.D.ordinal();
            int idx = -1;
            for (int i = 0; i < stickersByPiece.length; i++) {
                int t = 0;
                for (int j = 0; j < stickersByPiece[i].length; j++) {
                    t |= 1 << stickersByPiece[i][j];
                }
                if (t == goal) {
                    idx = i;
                    break;
                }
            }
            assert idx >= 0;
            Face f = null;
            int dir = 1;
            if (stickersByPiece[idx][0] == Face.D.ordinal()) {
                if (idx < 4) {
                    // on U
                    f = Face.F;
                    dir = 2;
                } else {
                    // on D
                    f = Face.U;
                    switch(idx) {
                        case 4:
                            dir = 2; break;
                        case 5:
                            dir = 1; break;
                        case 6:
                            dir = 3; break;
                        default:
                            assert false;
                    }
                }
            } else if (stickersByPiece[idx][1] == Face.D.ordinal()) {
                switch (idx) {
                    case 0: case 6:
                        f = Face.F; break; // on R
                    case 1: case 4:
                        f = Face.L; break; // on F
                    case 2: case 7:
                        f = Face.R; break; // on B
                    case 3: case 5:
                        f = Face.B; break; // on L
                    default:
                        assert false;
                }
            } else {
                switch (idx) {
                    case 2: case 4:
                        f = Face.F; break; // on R
                    case 0: case 5:
                        f = Face.L; break; // on F
                    case 3: case 6:
                        f = Face.R; break; // on B
                    case 1: case 7:
                        f = Face.B; break; // on L
                    default:
                        assert false;
                }
            }
            spinCube(image, f, dir);
            spins++;
        }

        return image;
    }

    public CubeState getNormalized() {
        if(normalizedState == null) {
            int[][][] normalizedImage = normalize(image);
            normalizedState = new CubeState(normalizedImage);
        }
        return normalizedState;
    }

    public TwoByTwoSolver.TwoByTwoState toTwoByTwoState() {
        TwoByTwoSolver.TwoByTwoState state = new TwoByTwoSolver.TwoByTwoState();

        int[][] stickersByPiece = getStickersByPiece(image);

        // Here's a clever color value assigning system that gives each piece
        // a unique id just by summing up the values of its stickers.
        //
        //            +----------+
        //            |*3*    *2*|
        //            |   U (0)  |
        //            |*1*    *0*|
        // +----------+----------+----------+----------+
        // | 3      1 | 1      0 | 0      2 | 2      3 |
        // |   L (1)  |   F (0)  |   R (0)  |   B (2)  |
        // | 7      5 | 5      4 | 4      6 | 6      7 |
        // +----------+----------+----------+----------+
        //            |*5*    *4*|
        //            |   D (4)  |
        //            |*7*    *6*|
        //            +----------+
        //

        int dColor = stickersByPiece[7][0];
        int bColor = stickersByPiece[7][1];
        int lColor = stickersByPiece[7][2];

        int uColor = Face.values()[dColor].oppositeFace().ordinal();
        int fColor = Face.values()[bColor].oppositeFace().ordinal();
        int rColor = Face.values()[lColor].oppositeFace().ordinal();

        int[] colorToVal = new int[8];
        colorToVal[uColor] = 0;
        colorToVal[fColor] = 0;
        colorToVal[rColor] = 0;
        colorToVal[lColor] = 1;
        colorToVal[bColor] = 2;
        colorToVal[dColor] = 4;

        int[] pieces = new int[7];
        for(int i = 0; i < pieces.length; i++) {
            int[] stickers = stickersByPiece[i];
            int pieceVal = colorToVal[stickers[0]] + colorToVal[stickers[1]] + colorToVal[stickers[2]];

            int clockwiseTurnsToGetToPrimaryColor = 0;
            while(stickers[clockwiseTurnsToGetToPrimaryColor] != uColor && stickers[clockwiseTurnsToGetToPrimaryColor] != dColor) {
                clockwiseTurnsToGetToPrimaryColor++;
                assert clockwiseTurnsToGetToPrimaryColor < 3;
            }
            int piece = (clockwiseTurnsToGetToPrimaryColor << 3) + pieceVal;
            pieces[i] = piece;
        }

        state.permutation = TwoByTwoSolver.packPerm(pieces);
        state.orientation = TwoByTwoSolver.packOrient(pieces);
        return state;
    }

    /**
     * Returns a string representing the colors of the cube.
     * Looking at the face, each sticker is described in the reading order,
     * that is, from left to right, from up to bottom.
     * Faces order follow U, R, F, D, L, B.
     * @return A string, not spaced, containing the color of each sticker.
     * This color refers to the solved state. If white is the color of the U face when solved,
     * then every white sticker is described as U.
     */
    public String toFaceCube() {
        assert size == 3;
        String state = "";
        for(char f : "URFDLB".toCharArray()) {
            Face face = Face.valueOf("" + f);
            int[][] faceArr = image[face.ordinal()];
            for(int i = 0; i < faceArr.length; i++) {
                for(int j = 0; j < faceArr[i].length; j++) {
                    state += Face.values()[faceArr[i][j]].toString();
                }
            }
        }
        return state;
    }

    @Override
    public Map<String, CubeState> getSuccessorsByName() {
        return getSuccessorsWithinSlice(size - 1, true);
    }

    @Override
    public CubeState unpack() {
        return this;
    }

    @Override
    public Map<String, CubeState> getScrambleSuccessors() {
        return getSuccessorsWithinSlice((size / 2) - 1, false);
    }

    @Override
    public Map<CubeState, String> getCanonicalMovesByState() {
        Map<CubeState, String> reversed = new HashMap<>();

        for (Map.Entry<String, CubeState> entry : getScrambleSuccessors().entrySet()) {
            reversed.put(entry.getValue(), entry.getKey());
        }

        return reversed;
    }

    private Map<String, CubeState> getSuccessorsWithinSlice(int maxSlice, boolean includeRedundant) {
        Map<String, CubeState> successors = new LinkedHashMap<>();
        for(int innerSlice = 0; innerSlice <= maxSlice; innerSlice++) {
            for(Face face : Face.values()) {
                boolean halfOfEvenCube = size % 2 == 0 && (innerSlice == (size / 2) - 1);
                if(!includeRedundant && face.ordinal() >= 3 && halfOfEvenCube) {
                    // Skip turning the other halves of even sized cubes
                    continue;
                }
                int outerSlice = 0;
                for(int dir = 1; dir <= 3; dir++) {
                    CubeMove move = new CubeMove(face, dir, innerSlice, outerSlice);
                    String moveStr = move.toString();
                    if(moveStr == null) {
                        // Skip unnamed rotations.
                        continue;
                    }

                    int[][][] imageCopy = cloneImage(image);
                    for(int slice = outerSlice; slice <= innerSlice; slice++) {
                        slice(face, slice, dir, imageCopy);
                    }
                    successors.put(moveStr, new CubeState(imageCopy));
                }
            }
        }

        return successors;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CubeState && Arrays.deepEquals(image, ((CubeState) other).image);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(image);
    }

    private void spinCube(int[][][] image, Face face, int dir) {
        for(int slice = 0; slice < size; slice++) {
            slice(face, slice, dir, image);
        }
    }

    public class CubeMove {
        public final Face face;
        int dir;
        int innerSlice, outerSlice;
        public CubeMove(Face face, int dir) {
            this(face, dir, 0);
        }
        public CubeMove(Face face, int dir, int innerSlice) {
            this(face, dir, innerSlice, 0);
        }
        public CubeMove(Face face, int dir, int innerSlice, int outerSlice) {
            this.face = face;
            this.dir = dir;
            this.innerSlice = innerSlice;
            this.outerSlice = outerSlice;
            // We haven't come up with names for moves where outerSlice != 0
            assert outerSlice == 0;
        }

        public String toString() {
            String f = face.toString();
            String move;
            if(innerSlice == 0) {
                move = f;
            } else if (innerSlice == 1) {
                move = f + "w";
            } else if (innerSlice == size - 1) {
                // Turning all the slices is a rotation
                String rotationName = faceRotationsByName.get(face);
                if(rotationName == null) {
                    // Not all rotations are actually named.
                    return null;
                }
                move = rotationName;
            } else {
                move = (innerSlice+1) + f + "w";
            }
            move += DIR_TO_STR[dir];

            return move;
        }
    }

    private static int[][][] cloneImage(int[][][] image) {
        int[][][] imageCopy = new int[image.length][image[0].length][image[0][0].length];
        ArrayUtils.deepCopy(image, imageCopy);
        return imageCopy;
    }

    private static void swap(int[][][] image,
                             int f1, int x1, int y1,
                             int f2, int x2, int y2,
                             int f3, int x3, int y3,
                             int f4, int x4, int y4,
                             int dir) {
        if (dir == 1) {
            int temp = image[f1][x1][y1];
            image[f1][x1][y1] = image[f2][x2][y2];
            image[f2][x2][y2] = image[f3][x3][y3];
            image[f3][x3][y3] = image[f4][x4][y4];
            image[f4][x4][y4] = temp;
        } else if (dir == 2) {
            int temp = image[f1][x1][y1];
            image[f1][x1][y1] = image[f3][x3][y3];
            image[f3][x3][y3] = temp;
            temp = image[f2][x2][y2];
            image[f2][x2][y2] = image[f4][x4][y4];
            image[f4][x4][y4] = temp;
        } else if (dir == 3) {
            int temp = image[f4][x4][y4];
            image[f4][x4][y4] = image[f3][x3][y3];
            image[f3][x3][y3] = image[f2][x2][y2];
            image[f2][x2][y2] = image[f1][x1][y1];
            image[f1][x1][y1] = temp;
        } else {
            assert false;
        }
    }

    private static void slice(Face face, int slice, int dir, int[][][] image) {
        int size = image[0].length;
        assert slice >= 0 && slice < size;

        Face sface = face;
        int sslice = slice;
        int sdir = dir;

        if(face != Face.L && face != Face.D && face != Face.B) {
            sface = face.oppositeFace();
            sslice = size - 1 - slice;
            sdir = 4 - dir;
        }
        for(int j = 0; j < size; j++) {
            if(sface == Face.L) {
                swap(image,
                    Face.U.ordinal(), j, sslice,
                    Face.B.ordinal(), size-1-j, size-1-sslice,
                    Face.D.ordinal(), j, sslice,
                    Face.F.ordinal(), j, sslice,
                    sdir);
            } else if(sface == Face.D) {
                swap(image,
                    Face.L.ordinal(), size-1-sslice, j,
                    Face.B.ordinal(), size-1-sslice, j,
                    Face.R.ordinal(), size-1-sslice, j,
                    Face.F.ordinal(), size-1-sslice, j,
                    sdir);
            } else if(sface == Face.B) {
                swap(image,
                    Face.U.ordinal(), sslice, j,
                    Face.R.ordinal(), j, size-1-sslice,
                    Face.D.ordinal(), size-1-sslice, size-1-j,
                    Face.L.ordinal(), size-1-j, sslice,
                    sdir);
            } else {
                assert false;
            }
        }
        if(slice == 0 || slice == size - 1) {
            int f;
            if(slice == 0) {
                f = face.ordinal();
                sdir = 4 - dir;
            } else if(slice == size - 1) {
                f = face.oppositeFace().ordinal();
                sdir = dir;
            } else {
                assert false;
                return;
            }
            for(int j = 0; j < (size+1)/2; j++) {
                for(int k = 0; k < size/2; k++) {
                    swap(image,
                        f, j, k,
                        f, k, size-1-j,
                        f, size-1-j, size-1-k,
                        f, size-1-k, j,
                        sdir);
                }
            }
        }
    }

    protected static int[][] getStickersByPiece(int[][][] img) {
        int s = img[0].length - 1;
        return new int[][] {
            { img[Face.U.ordinal()][s][s], img[Face.R.ordinal()][0][0], img[Face.F.ordinal()][0][s] },
            { img[Face.U.ordinal()][s][0], img[Face.F.ordinal()][0][0], img[Face.L.ordinal()][0][s] },
            { img[Face.U.ordinal()][0][s], img[Face.B.ordinal()][0][0], img[Face.R.ordinal()][0][s] },
            { img[Face.U.ordinal()][0][0], img[Face.L.ordinal()][0][0], img[Face.B.ordinal()][0][s] },

            { img[Face.D.ordinal()][0][s], img[Face.F.ordinal()][s][s], img[Face.R.ordinal()][s][0] },
            { img[Face.D.ordinal()][0][0], img[Face.L.ordinal()][s][s], img[Face.F.ordinal()][s][0] },
            { img[Face.D.ordinal()][s][s], img[Face.R.ordinal()][s][s], img[Face.B.ordinal()][s][0] },
            { img[Face.D.ordinal()][s][0], img[Face.B.ordinal()][s][s], img[Face.L.ordinal()][s][0] }
        };
    }

    public CubeMove[][] getRandomOrientationMoves(int thickness) {
        CubeMove[] randomUFaceMoves = new CubeMove[] {
            null,
            new CubeMove(Face.R, 1, thickness),
            new CubeMove(Face.R, 2, thickness),
            new CubeMove(Face.R, 3, thickness),
            new CubeMove(Face.F, 1, thickness),
            new CubeMove(Face.F, 3, thickness)
        };
        CubeMove[] randomFFaceMoves = new CubeMove[] {
            null,
            new CubeMove(Face.U, 1, thickness),
            new CubeMove(Face.U, 2, thickness),
            new CubeMove(Face.U, 3, thickness)
        };
        CubeMove[][] randomOrientationMoves = new CubeMove[randomUFaceMoves.length * randomFFaceMoves.length][];
        int i = 0;
        for(CubeMove randomUFaceMove : randomUFaceMoves) {
            for(CubeMove randomFFaceMove : randomFFaceMoves) {
                List<CubeMove> moves = new ArrayList<>();
                if(randomUFaceMove != null) {
                    moves.add(randomUFaceMove);
                }
                if(randomFFaceMove != null) {
                    moves.add(randomFFaceMove);
                }
                CubeMove[] movesArr = moves.toArray(new CubeMove[moves.size()]);
                randomOrientationMoves[i++] = movesArr;
            }
        }
        return randomOrientationMoves;
    }
}
