package org.worldcubeassociation.tnoodle.state;

import org.worldcubeassociation.tnoodle.PuzzleState;
import org.worldcubeassociation.tnoodle.util.ArrayUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MegaminxState extends PuzzleState<MegaminxState> {
    public enum Face {
        U, BL, BR, R, F, L, D, DR, DBR, B, DBL, DL;

        // TODO We could rename faces so we can just do +6 mod 12 here instead.
        public Face oppositeFace() {
            switch(this) {
                case U:
                    return D;
                case BL:
                    return DR;
                case BR:
                    return DL;
                case R:
                    return DBL;
                case F:
                    return B;
                case L:
                    return DBR;
                case D:
                    return U;
                case DR:
                    return BL;
                case DBR:
                    return L;
                case B:
                    return F;
                case DBL:
                    return R;
                case DL:
                    return BR;
                default:
                    assert false;
                    return null;
            }
        }
    }

    public final int[][] image;
    private MegaminxState normalizedState;
    public MegaminxState() {
        image = new int[12][11];
        for(int i = 0; i < image.length; i++) {
            for(int j = 0; j < image[0].length; j++) {
                image[i][j] = i;
            }
        }
        normalizedState = this;
    }

    public MegaminxState(int[][] image) {
        this.image = image;
    }

    public MegaminxState getNormalized() {
        if(normalizedState == null) {
            int[][] normalizedImage = normalize(image);
            normalizedState = new MegaminxState(normalizedImage);
        }
        return normalizedState;
    }

    public boolean isNormalized() {
        return isNormalized(image);
    }

    @Override
    public Map<String, MegaminxState> getSuccessorsByName() {
        Map<String, MegaminxState> successors = new LinkedHashMap<>();

        String[] prettyDir = new String[] { null, "", "2", "2'", "'" };
        for(Face face : Face.values()) {
            for(int dir = 1; dir <= 4; dir++) {
                String move = face.toString();
                move += prettyDir[dir];

                int[][] imageCopy = cloneImage(image);
                turn(imageCopy, face, dir);

                successors.put(move, new MegaminxState(imageCopy));
            }
        }

        Map<String, Face> pochmannFaceNames = new HashMap<>();
        pochmannFaceNames.put("R", Face.DBR);
        pochmannFaceNames.put("D", Face.D);
        String[] prettyPochmannDir = new String[] { null, "+", "++", "--" , "-"};
        for(String pochmannFaceName : pochmannFaceNames.keySet()) {
            for(int dir = 1; dir < 5; dir++) {
                String move = pochmannFaceName + prettyPochmannDir[dir];

                int[][] imageCopy = cloneImage(image);
                bigTurn(imageCopy, pochmannFaceNames.get(pochmannFaceName), dir);

                successors.put(move, new MegaminxState(imageCopy));
            }
        }
        return successors;
    }

    private static int centerIndex = 10;

    private static boolean isNormalized(int[][] image) {
        return image[Face.U.ordinal()][centerIndex] == Face.U.ordinal() && image[Face.F.ordinal()][centerIndex] == Face.F.ordinal();
    }

    private static int[][] normalize(int[][] image) {
        if(isNormalized(image)) {
            return image;
        }

        image = cloneImage(image);
        for(Face face : Face.values()) {
            if(image[face.ordinal()][centerIndex] == Face.U.ordinal()) {
                spinToTop(image, face);
                assert image[Face.U.ordinal()][centerIndex] == Face.U.ordinal();
                for(int chooseF = 0; chooseF < 5; chooseF++) {
                    spinMinx(image, Face.U, 1);
                    if(isNormalized(image)) {
                        return image;
                    }
                }
                assert false;
            }
        }
        assert false;
        return null;
    }

    @Override
    public MegaminxState unpack() {
        return this;
    }

    @Override
    public Map<String, MegaminxState> getScrambleSuccessors() {
        Map<String, MegaminxState> successors = getSuccessorsByName();
        Map<String, MegaminxState> scrambleSuccessors = new HashMap<>();
        for(String turn : new String[] { "R++", "R--", "D++", "D--", "U", "U2", "U2'", "U'" }) {
            scrambleSuccessors.put(turn, successors.get(turn));
        }
        return scrambleSuccessors;
    }

    @Override
    public boolean equals(Object other) {
        MegaminxState o = ((MegaminxState) other);
        return Arrays.deepEquals(image, o.image);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(image);
    }

    private static void turn(int[][] image, Face side, int dir) {
        dir = ((dir % 5) + 5) % 5;

        for(int i = 0; i < dir; i++) {
            turn(image, side);
        }
    }

    private static void turn(int[][] image, Face face) {
        int s = face.ordinal();
        int b = (s >= 6 ? 6 : 0);
        switch(s % 6) {
            case 0:
                swapOnSide(image, b, 1, 6, 5, 4, 4, 2, 3, 0, 2, 8); break;
            case 1:
                swapOnSide(image, b, 0, 0, 2, 0, 9, 6, 10, 6, 5, 2); break;
            case 2:
                swapOnSide(image, b, 0, 2, 3, 2, 8, 4, 9, 4, 1, 4); break;
            case 3:
                swapOnSide(image, b, 0, 4, 4, 4, 7, 2, 8, 2, 2, 6); break;
            case 4:
                swapOnSide(image, b, 0, 6, 5, 6, 11, 0, 7, 0, 3, 8); break;
            case 5:
                swapOnSide(image, b, 0, 8, 1, 8, 10, 8, 11, 8, 4, 0); break;
            default:
                assert false;
        }

        rotateFace(image, face);
    }

    private static void swapOnSide(int[][] image, int b, int f1, int s1, int f2, int s2, int f3, int s3, int f4, int s4, int f5, int s5) {
        for(int i = 0; i < 3; i++) {
            int temp = image[(f1+b)%12][(s1+i)%10];
            image[(f1+b)%12][(s1+i)%10] = image[(f2+b)%12][(s2+i)%10];
            image[(f2+b)%12][(s2+i)%10] = image[(f3+b)%12][(s3+i)%10];
            image[(f3+b)%12][(s3+i)%10] = image[(f4+b)%12][(s4+i)%10];
            image[(f4+b)%12][(s4+i)%10] = image[(f5+b)%12][(s5+i)%10];
            image[(f5+b)%12][(s5+i)%10] = temp;
        }
    }

    private static void swapOnFace(int[][] image, Face face, int s1, int s2, int s3, int s4, int s5) {
        int f = face.ordinal();
        int temp = image[f][s1];
        image[f][s1] = image[f][s2];
        image[f][s2] = image[f][s3];
        image[f][s3] = image[f][s4];
        image[f][s4] = image[f][s5];
        image[f][s5] = temp;
    }

    private static void rotateFace(int[][] image, Face f) {
        swapOnFace(image, f, 0, 8, 6, 4, 2);
        swapOnFace(image, f, 1, 9, 7, 5, 3);
    }

    private static void bigTurn(int[][] image, Face side, int dir) {
        dir = ((dir % 5) + 5) % 5;

        for(int i = 0; i < dir; i++) {
            bigTurn(image, side);
        }
    }

    private static void bigTurn(int[][] image, Face f) {
        if(f == Face.DBR) {
            for(int i = 0; i < 7; i++) {
                swap(image, 0, (1+i)%10, 4, (3+i)%10, 11, (1+i)%10, 10, (1+i)%10, 1, (1+i)%10);
            }
            swapCenters(image, 0, 4, 11, 10, 1);

            swapWholeFace(image, 2, 0, 3, 0, 7, 0, 6, 8, 9, 8);

            rotateFace(image, Face.DBR);
        } else {
            assert f == Face.D;
            for(int i = 0; i < 7; i++) {
                swap(image, 1, (9+i)%10, 2, (1+i)%10, 3, (3+i)%10, 4, (5+i)%10, 5, (7+i)%10);
            }
            swapCenters(image, 1, 2, 3, 4, 5);

            swapWholeFace(image, 11, 0, 10, 8, 9, 6, 8, 4, 7, 2);

            rotateFace(image, Face.D);
        }
    }

    private static void swap(int[][] image, int f1, int s1, int f2, int s2, int f3, int s3, int f4, int s4, int f5, int s5) {
        int temp = image[f1][s1];
        image[f1][s1] = image[f2][s2];
        image[f2][s2] = image[f3][s3];
        image[f3][s3] = image[f4][s4];
        image[f4][s4] = image[f5][s5];
        image[f5][s5] = temp;
    }

    private static void swapCenters(int[][] image, int f1, int f2, int f3, int f4, int f5) {
        swap(image, f1, 10, f2, 10, f3, 10, f4, 10, f5, 10);
    }

    private static void swapWholeFace(int[][] image, int f1, int s1, int f2, int s2, int f3, int s3, int f4, int s4, int f5, int s5) {
        for(int i = 0; i < 10; i++) {
            int temp = image[(f1)%12][(s1+i)%10];
            image[(f1)%12][(s1+i)%10] = image[(f2)%12][(s2+i)%10];
            image[(f2)%12][(s2+i)%10] = image[(f3)%12][(s3+i)%10];
            image[(f3)%12][(s3+i)%10] = image[(f4)%12][(s4+i)%10];
            image[(f4)%12][(s4+i)%10] = image[(f5)%12][(s5+i)%10];
            image[(f5)%12][(s5+i)%10] = temp;
        }
        swapCenters(image, f1, f2, f3, f4, f5);
    }

    private static void spinMinx(int[][] image, Face face, int dir) {
        turn(image, face, dir);
        bigTurn(image, face.oppositeFace(), 5 - dir);
    }

    private static void spinToTop(int[][] image, Face face) {
        switch(face) {
            case U:
                break;
            case BL:
                spinMinx(image, Face.L, 1);
                break;
            case BR:
                spinMinx(image, Face.U, 1);
                spinToTop(image, Face.R);
                break;
            case R:
                spinMinx(image, Face.U, 1);
                spinToTop(image, Face.F);
                break;
            case F:
                spinMinx(image, Face.L, -1);
                break;
            case L:
                spinMinx(image, Face.U, 1);
                spinToTop(image, Face.BL);
                break;
            case D:
                spinMinx(image, Face.L, -2);
                spinToTop(image, Face.R);
                break;
            case DR:
                spinMinx(image, Face.L, -1);
                spinToTop(image, Face.R);
                break;
            case DBR:
                spinMinx(image, Face.U, 1);
                spinMinx(image, Face.L, -1);
                spinToTop(image, Face.R);
                break;
            case B:
                spinMinx(image, Face.L, -3);
                spinToTop(image, Face.R);
                break;
            case DBL:
                spinMinx(image, Face.L, 2);
                break;
            case DL:
                spinMinx(image, Face.L, -2);
                break;
            default:
                assert false;
        }
    }

    private static int[][] cloneImage(int[][] image) {
        int[][] imageCopy = new int[image.length][image[0].length];
        ArrayUtils.deepCopy(image, imageCopy);
        return imageCopy;
    }
}
