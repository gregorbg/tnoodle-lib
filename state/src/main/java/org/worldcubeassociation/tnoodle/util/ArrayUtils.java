package org.worldcubeassociation.tnoodle.util;

import java.util.Random;

public class ArrayUtils {
    public static int[] cloneArr(int[] src) {
        int[] dest = new int[src.length];
        System.arraycopy(src, 0, dest, 0, src.length);
        return dest;
    }

    public static void deepCopy(int[][] src, int[][] dest) {
        for(int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
    }

    public static void deepCopy(int[][][] src, int[][][] dest) {
        for(int i = 0; i < src.length; i++) {
            deepCopy(src[i], dest[i]);
        }
    }

    public static <H> H choose(Random r, Iterable<H> keySet) {
        H chosen = null;
        int count = 0;
        for(H element : keySet) {
            if(r.nextInt(++count) == 0) {
                chosen = element;
            }
        }
        assert count > 0;
        return chosen;
    }

    public static int[] copyOfRange(int[] src, int from, int to) {
        int[] dest = new int[to - from];
        System.arraycopy(src, from, dest, 0, dest.length);
        return dest;
    }
}
