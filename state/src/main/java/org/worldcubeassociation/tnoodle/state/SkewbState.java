package org.worldcubeassociation.tnoodle.state;

import org.worldcubeassociation.tnoodle.AbstractPuzzleState;
import org.worldcubeassociation.tnoodle.util.ArrayUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class SkewbState extends AbstractPuzzleState<SkewbState> {

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
    public int[][] image = new int[6][5];

    public SkewbState() {
        for (int i=0; i<6; i++) {
            for (int j=0; j<5; j++) {
                image[i][j] = i;
            }
        }
    }

    public SkewbState(int[][] _image) {
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

    public Map<String, SkewbState> getSuccessorsByName() {
        Map<String, SkewbState> successors = new LinkedHashMap<>();
        String axes = "RULB";
        for(int axis = 0; axis < axes.length(); axis++) {
            char face = axes.charAt(axis);
            for(int pow = 1; pow <= 2; pow++) {
                String turn = "" + face;
                if(pow == 2) {
                    turn += "'";
                }
                int[][] imageCopy = new int[image.length][image[0].length];
                ArrayUtils.deepCopy(image, imageCopy);
                turn(axis, pow, imageCopy);
                successors.put(turn, new SkewbState(imageCopy));
            }
        }

        return successors;
    }

    @Override
    public SkewbState unpack() {
        return this;
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
