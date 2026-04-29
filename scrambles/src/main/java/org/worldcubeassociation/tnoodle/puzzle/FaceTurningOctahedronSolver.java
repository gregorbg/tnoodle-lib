// FTO Solver is originally ported from csTimer that licensed under GPL-3.0
// https://github.com/cs0x7f/cstimer
// The Java version was originally created by hato-ya for CubicTimer, also licensed under GPL-3.0
// https://github.com/hato-ya/CubicTimer/blob/90ba77795ebba4f65fe3cfe293ba491d814962a2/app/src/main/java/com/hatopigeon/cubictimer/puzzle/FtoPuzzle.java

package org.worldcubeassociation.tnoodle.puzzle;

import java.util.*;
import java.util.logging.Logger;

public class FaceTurningOctahedronSolver {
    private static final Logger l = Logger.getLogger(FaceTurningOctahedronSolver.class.getName());

    //
    // definition and initialization of move operations and rotate operations
    //
    public static final String[] moveName = new String[] {
        "U", "U'", "F", "F'", "BR", "BR'", "BL", "BL'",
        "D", "D'", "B", "B'", "R", "R'", "L", "L'"
    };

    public static final FtoSolverState[] moveOp = new FtoSolverState[20];
    public static final FtoSolverState[] rotOp = new FtoSolverState[12];
    private static final int[][] rotMul = new int[12][12];
    private static final int[][] rotDiv = new int[12][12];
    private static final int[][] rotMov = new int[12][8];

    private static void initOp() {
        // U
        moveOp[0] = new FtoSolverState(
            new int[] {1, 2, 0, 3, 4, 5},
            new int[] {0, 0, 0, 0, 0, 0},
            new int[] {2, 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11},
            new int[] {1, 2, 0, 3, 4, 5, 6, 7, 8, 9, 10, 11},
            new int[] {0, 1, 2, 3, 6, 7, 11, 9, 8, 5, 10, 4});
        // F
        moveOp[2] = new FtoSolverState(
            new int[] {4, 1, 2, 3, 5, 0},
            new int[] {1, 0, 0, 0, 1, 0},
            new int[] {0, 1, 2, 3, 4, 6, 7, 5, 8, 9, 10, 11},
            new int[] {0, 1, 2, 4, 5, 3, 6, 7, 8, 9, 10, 11},
            new int[] {0, 9, 10, 3, 4, 5, 2, 7, 1, 8, 6, 11});
        // BR
        moveOp[4] = new FtoSolverState(
            new int[] {0, 5, 2, 1, 4, 3},
            new int[] {0, 1, 0, 0, 0, 1},
            new int[] {0, 1, 2, 3, 10, 5, 6, 7, 8, 9, 11, 4},
            new int[] {0, 1, 2, 3, 4, 5, 7, 8, 6, 9, 10, 11},
            new int[] {5, 3, 2, 11, 4, 10, 6, 7, 8, 9, 0, 1});
        // BL
        moveOp[6] = new FtoSolverState(
            new int[] {0, 1, 3, 4, 2, 5},
            new int[] {0, 0, 1, 1, 0, 0},
            new int[] {0, 1, 2, 8, 4, 5, 6, 7, 9, 3, 10, 11},
            new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 9},
            new int[] {8, 1, 7, 2, 0, 5, 6, 3, 4, 9, 10, 11});
        // D
        moveOp[8] = new FtoSolverState(
            new int[] {0, 1, 2, 5, 3, 4},
            new int[] {0, 0, 0, 0, 0, 0},
            new int[] {0, 1, 2, 4, 5, 3, 6, 7, 8, 9, 10, 11},
            new int[] {0, 1, 2, 3, 9, 10, 5, 7, 4, 8, 6, 11},
            new int[] {1, 2, 0, 3, 4, 5, 6, 7, 8, 9, 10, 11});
        // B
        moveOp[10] = new FtoSolverState(
            new int[] {0, 3, 1, 2, 4, 5},
            new int[] {0, 1, 1, 0, 0, 0},
            new int[] {0, 1, 10, 3, 4, 5, 6, 7, 8, 2, 9, 11},
            new int[] {0, 6, 7, 3, 4, 5, 11, 9, 8, 2, 10, 1},
            new int[] {0, 1, 2, 4, 5, 3, 6, 7, 8, 9, 10, 11});
        // R
        moveOp[12] = new FtoSolverState(
            new int[] {5, 0, 2, 3, 4, 1},
            new int[] {1, 1, 0, 0, 0, 0},
            new int[] {6, 1, 2, 3, 4, 5, 11, 7, 8, 9, 10, 0},
            new int[] {5, 3, 2, 8, 4, 7, 6, 0, 1, 9, 10, 11},
            new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 9});
        // L
        moveOp[14] = new FtoSolverState(
            new int[] {2, 1, 4, 3, 0, 5},
            new int[] {1, 0, 1, 0, 0, 0},
            new int[] {0, 8, 2, 3, 4, 5, 6, 1, 7, 9, 10, 11},
            new int[] {11, 1, 10, 2, 0, 5, 6, 7, 8, 9, 3, 4},
            new int[] {0, 1, 2, 3, 4, 5, 7, 8, 6, 9, 10, 11});
        // Uw
        moveOp[16] = new FtoSolverState(
            new int[] {1, 2, 0, 3, 4, 5},
            new int[] {0, 0, 0, 0, 0, 0},
            new int[] {2, 0, 1, 3, 4, 5, 10, 11, 6, 7, 8, 9},
            new int[] {1, 2, 0, 7, 4, 5, 6, 11, 8, 9, 10, 3},
            new int[] {0, 1, 2, 8, 6, 7, 11, 9, 10, 5, 3, 4});
        // Lw
        moveOp[18] = new FtoSolverState(
            new int[] {2, 1, 4, 3, 0, 5},
            new int[] {1, 0, 1, 0, 0, 0},
            new int[] {9, 8, 3, 6, 4, 0, 2, 1, 7, 5, 10, 11},
            new int[] {11, 9, 10, 2, 0, 1, 6, 7, 8, 5, 3, 4},
            new int[] {0, 1, 9, 3, 2, 5, 7, 8, 6, 4, 10, 11});

        // prime
        for (int i = 0; i < 20; i += 2) {
            moveOp[i+1] = moveOp[i].applyNew(moveOp[i]);
        }

        // rotation
        rotOp[0] = new FtoSolverState(
            new int[] {0, 1, 2, 3, 4, 5},
            new int[] {0, 0, 0, 0, 0, 0},
            new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},
            new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},
            new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
        rotOp[1] = new FtoSolverState(
            new int[] {1, 2, 0, 4, 5, 3},
            new int[] {0, 0, 0, 0, 0, 0},
            new int[] {2, 0, 1, 5, 3, 4, 10, 11, 6, 7, 8, 9},
            new int[] {1, 2, 0, 7, 8, 6, 10, 11, 9, 4, 5, 3},
            new int[] {2, 0, 1, 8, 6, 7, 11, 9, 10, 5, 3, 4});
        rotOp[2] = new FtoSolverState(
            new int[] {2, 0, 1, 5, 3, 4},
            new int[] {0, 0, 0, 0, 0, 0},
            new int[] {1, 2, 0, 4, 5, 3, 8, 9, 10, 11, 6, 7},
            new int[] {2, 0, 1, 11, 9, 10, 5, 3, 4, 8, 6, 7},
            new int[] {1, 2, 0, 10, 11, 9, 4, 5, 3, 7, 8, 6});
        rotOp[3] = new FtoSolverState(
            new int[] {0, 4, 5, 3, 1, 2},
            new int[] {1, 0, 1, 1, 0, 1},
            new int[] {7, 6, 5, 10, 9, 2, 1, 0, 11, 4, 3, 8},
            new int[] {3, 4, 5, 0, 1, 2, 9, 10, 11, 6, 7, 8},
            new int[] {3, 4, 5, 0, 1, 2, 9, 10, 11, 6, 7, 8});
        rotOp[4] = new FtoSolverState(
            new int[] {4, 5, 0, 1, 2, 3},
            new int[] {0, 1, 1, 0, 1, 1},
            new int[] {5, 7, 6, 2, 10, 9, 3, 8, 1, 0, 11, 4},
            new int[] {4, 5, 3, 10, 11, 9, 7, 8, 6, 1, 2, 0},
            new int[] {5, 3, 4, 11, 9, 10, 8, 6, 7, 2, 0, 1});
        rotOp[5] = new FtoSolverState(
            new int[] {5, 0, 4, 2, 3, 1},
            new int[] {1, 1, 0, 1, 1, 0},
            new int[] {6, 5, 7, 9, 2, 10, 11, 4, 3, 8, 1, 0},
            new int[] {5, 3, 4, 8, 6, 7, 2, 0, 1, 11, 9, 10},
            new int[] {4, 5, 3, 7, 8, 6, 1, 2, 0, 10, 11, 9});
        rotOp[6] = new FtoSolverState(
            new int[] {3, 1, 5, 0, 4, 2},
            new int[] {1, 1, 0, 1, 1, 0},
            new int[] {10, 4, 11, 7, 1, 8, 9, 3, 5, 6, 0, 2},
            new int[] {6, 7, 8, 9, 10, 11, 0, 1, 2, 3, 4, 5},
            new int[] {6, 7, 8, 9, 10, 11, 0, 1, 2, 3, 4, 5});
        rotOp[7] = new FtoSolverState(
            new int[] {1, 5, 3, 4, 2, 0},
            new int[] {1, 0, 1, 1, 0, 1},
            new int[] {11, 10, 4, 8, 7, 1, 0, 2, 9, 3, 5, 6},
            new int[] {7, 8, 6, 1, 2, 0, 4, 5, 3, 10, 11, 9},
            new int[] {8, 6, 7, 2, 0, 1, 5, 3, 4, 11, 9, 10});
        rotOp[8] = new FtoSolverState(
            new int[] {5, 3, 1, 2, 0, 4},
            new int[] {0, 1, 1, 0, 1, 1},
            new int[] {4, 11, 10, 1, 8, 7, 5, 6, 0, 2, 9, 3},
            new int[] {8, 6, 7, 5, 3, 4, 11, 9, 10, 2, 0, 1},
            new int[] {7, 8, 6, 4, 5, 3, 10, 11, 9, 1, 2, 0});
        rotOp[9] = new FtoSolverState(
            new int[] {3, 4, 2, 0, 1, 5},
            new int[] {0, 1, 1, 0, 1, 1},
            new int[] {3, 9, 8, 0, 6, 11, 4, 10, 2, 1, 7, 5},
            new int[] {9, 10, 11, 6, 7, 8, 3, 4, 5, 0, 1, 2},
            new int[] {9, 10, 11, 6, 7, 8, 3, 4, 5, 0, 1, 2});
        rotOp[10] = new FtoSolverState(
            new int[] {4, 2, 3, 1, 5, 0},
            new int[] {1, 1, 0, 1, 1, 0},
            new int[] {8, 3, 9, 11, 0, 6, 7, 5, 4, 10, 2, 1},
            new int[] {10, 11, 9, 4, 5, 3, 1, 2, 0, 7, 8, 6},
            new int[] {11, 9, 10, 5, 3, 4, 2, 0, 1, 8, 6, 7});
        rotOp[11] = new FtoSolverState(
            new int[] {2, 3, 4, 5, 0, 1},
            new int[] {1, 0, 1, 1, 0, 1},
            new int[] {9, 8, 3, 6, 11, 0, 2, 1, 7, 5, 4, 10},
            new int[] {11, 9, 10, 2, 0, 1, 8, 6, 7, 5, 3, 4},
            new int[] {10, 11, 9, 1, 2, 0, 7, 8, 6, 4, 5, 3});

        HashMap<Long, Integer> moveHash = new HashMap<>();
        for (int i = 0; i < moveOp.length; i++) {
            moveHash.put(moveOp[i].epHash(), i);
        }
        HashMap<Long, Integer> rotHash = new HashMap<>();
        for (int i = 0; i < rotOp.length; i++) {
            rotHash.put(rotOp[i].epHash(), i);
        }

        for (int i = 0; i < rotOp.length; i++) {
            for (int j = 0; j < rotOp.length; j++) {
                int k = rotHash.get(rotOp[i].applyNew(rotOp[j]).epHash());
                rotMul[i][j] = k;
                rotDiv[k][j] = i;
            }
        }
        for (int i = 0; i < rotOp.length; i++) {
            for (int j = 0; j < 8; j++) {
                int k = moveHash.get(rotOp[rotDiv[0][i]].applyNew(moveOp[j*2]).applyNew(rotOp[i]).epHash());
                rotMov[i][j] = k >> 1;
            }
        }
    }

    static {
        initOp();
    }

    private static Object[] createMoveTable(int[] moves, HashFunction hash) {
        List<FtoSolverState> states = new ArrayList<>();
        states.add(new FtoSolverState());

        HashMap<Long, Integer> idx = new HashMap<>();
        idx.put(hash.get(states.get(0)), 0);

        List<List<Integer>> moveTable = new ArrayList<>();
        for (int m = 0; m < moves.length; m++) {
            moveTable.add(new ArrayList<>());
        }

        for (int i = 0; i < states.size(); i++) {
            FtoSolverState state = states.get(i);

            for (int m = 0; m < moves.length; m++) {
                FtoSolverState newState = state.applyNew(moveOp[moves[m]]);
                long newHash = hash.get(newState);

                if (!idx.containsKey(newHash)) {
                    idx.put(newHash, states.size());
                    states.add(newState);
                }

                moveTable.get(m).add(idx.get(newHash));
            }
        }

        return new Object[]{moveTable, idx};
    }

    private static byte[] createPruningTable(int size, int moves, int maxDepth, MoveFunction move) {
        byte[] prun = new byte[size];
        LinkedList<Integer> nextDepth = new LinkedList<>();
        int count = 0;

        Arrays.fill(prun, (byte)-1);

        prun[0] = 0;
        nextDepth.push(0);
        count++;

        for (int l = 0; l <= maxDepth; l++) {
            boolean done = true;
            LinkedList<Integer> currentDepth = nextDepth;
            nextDepth = new LinkedList<>();
            for (int p : currentDepth) {
                for (int m = 0; m < moves; m++) {
                    int q = p;
                    for (int c = 0; c < 2; c++) {
                        q = move.get(q, m);
                        if (prun[q] != -1) continue;
                        prun[q] = (byte)(l + 1);
                        nextDepth.push(q);
                        count++;
                        done = false;
                    }
                }
            }
//                Log.d("FtoPuzzle", "    createPruningTable depth " + l + " count " + count);
            if (done) break;
        }

        return prun;
    }

    private static int[] createInvalidTable(int[] moves) {
        int[] invalidTable = new int[moves.length];
        for (int m1 = 0; m1 < moves.length; m1++) {
            invalidTable[m1] = 1 << m1;
            for (int m2 = 0; m2 < m1; m2++) {
                FtoSolverState m1m2 = moveOp[moves[m1]].applyNew(moveOp[moves[m2]]);
                FtoSolverState m2m1 = moveOp[moves[m2]].applyNew(moveOp[moves[m1]]);
                if (m1m2.equals(m2m1)) {
                    invalidTable[m1] += 1 << m2;
                }
            }
        }
        return invalidTable;
    }

    private static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    private static void reverse(int[] nums, int start, int end) {
        while (start < end) {
            swap(nums, start, end);
            start++;
            end--;
        }
    }

    private static boolean nextPermutation(int[] nums) {
        int i = nums.length - 2;
        while (i >= 0 && nums[i] >= nums[i+1]) {
            i--;
        }

        if (i < 0) {
            return false;
        }

        int j = nums.length - 1;
        while (nums[j] <= nums[i]) {
            j--;
        }

        swap(nums, i, j);
        reverse(nums, i + 1, nums.length - 1);

        return true;
    }

    //
    // definition and initialization of phase1 tables
    //
    private static int[] p1Moves = new int[] {0, 2, 18, 6, 16, 10, 12, 14};

    private static int p1epSize;
    private static List<List<Integer>> p1epMoveTable;
    private static HashMap<Long, Integer> p1epIdx;

    private static int p1rlSize;
    private static List<List<Integer>> p1rlMoveTable;
    private static HashMap<Long, Integer> p1rlIdx;

    private static byte[] p1Pruning;

    private static int[] p1InvalidTable;

    static class p1epHash implements HashFunction {
        @Override
        public long get(FtoSolverState state) {
            long hash = 0;
            int first = -1;

            // phase1 ep
            // only check D face edges
            //  1) whether D face edges are on the D face or not
            //  2) order of D face edges
            for (int i = 0; i < state.ep.length; i++) {
                if ( !((3 <= state.ep[i]) && (state.ep[i] <= 5)) ) {
                    continue;
                }
                if (first == -1) {
                    first = state.ep[i];
                }
                hash += ((state.ep[i] - first + 3) % 3 + 1) << (i * 2);
            }
            return hash;
        }
    }

    static class p1rlHash implements HashFunction {
        @Override
        public long get(FtoSolverState state) {
            long hash = 0;

            // phase1 rl
            // whether D face triangles are on the D face or not
            for (int i = 0; i < state.rl.length; i++) {
                if (state.rl[i] < 3) {
                    hash += 1 << i;
                }
            }
            return hash;
        }
    }

    static class p1MoveIdx implements MoveFunction {
        @Override
        public int get(int idx, int move) {
            int ep = idx % p1epSize;
            int rl = idx / p1epSize;
            return p1rlMoveTable.get(move).get(rl) * p1epSize + p1epMoveTable.get(move).get(ep);
        }
    }

    private static void initPhase1() {
        l.info("  p1epHash");
        Object[] p1ep = createMoveTable(p1Moves, new p1epHash());
        p1epMoveTable = (List<List<Integer>>) p1ep[0];
        p1epIdx = (HashMap<Long, Integer>) p1ep[1];
        p1epSize = p1epIdx.size();
        l.info("  p1rlHash");
        Object[] p1rl = createMoveTable(p1Moves, new p1rlHash());
        p1rlMoveTable = (List<List<Integer>>) p1rl[0];
        p1rlIdx = (HashMap<Long, Integer>) p1rl[1];
        p1rlSize = p1rlIdx.size();
        l.info("  p1Pruning");
        p1Pruning = createPruningTable(p1epSize * p1rlSize, p1Moves.length, 14, new p1MoveIdx());
        l.info("  p1InvalidTable");
        p1InvalidTable = createInvalidTable(p1Moves);
    }

    //
    // definition and initialization of phase2 tables
    //
    private static int[] p2Moves = new int[] {0, 12, 14, 8, 10};

    private static int p2epSize;
    private static List<List<Integer>> p2epMoveTable;
    private static HashMap<Long, Integer> p2epIdx;

    private static int p2rlSize;
    private static List<List<Integer>> p2rlMoveTable;
    private static HashMap<Long, Integer> p2rlIdx;

    private static int p2ccSize;
    private static List<List<Integer>> p2ccMoveTable;
    private static HashMap<Long, Integer> p2ccIdx;

    private static int p2ufSize = 30800;
    private static int[] p2ufRotMap = new int[16];
    private static HashMap<Long, Integer> p2ufIdx = new HashMap<>();
    private static long[] p2ufIdx2ufHash = new long[p2ufSize];
    private static int[][] p2ufMoveTable = new int[p2Moves.length][p2ufSize];

    private static long[] p2ccIdx2ufHash;
    private static int[][] p2ccRotTable;
    private static int[] p2trplPruning = new int[] {
        0,99, 3, 4, 5, 6, 8,
        99, 2, 3, 4, 5, 6, 8,
        1, 3, 4, 5, 6, 7, 8,
        1, 3, 4, 5, 6, 7, 9,
        99, 2, 3, 4, 5, 6, 8,
        2, 2, 4, 4, 5, 6, 8,
        3, 3, 4, 5, 6, 7, 8,
        3, 3, 4, 5, 6, 7, 9,
        3, 3, 4, 5, 6, 7, 8,
        4, 4, 4, 5, 6, 7, 8,
        4, 4, 5, 6, 7, 8, 9,
        4, 4, 5, 6, 7, 8, 9,
        4, 4, 5, 6, 7, 8, 9,
        4, 4, 5, 6, 7, 8, 9,
        5, 5, 6, 7, 8, 9,10,
        5, 5, 6, 7, 8, 9,10
    };

    private static byte[] p2eprlPruning;
    private static int p2eprlPruningMax = 11;

    private static int[] p2InvalidTable;

    static class p2epHash implements HashFunction {
        @Override
        public long get(FtoSolverState state) {
            int[] ep2group = new int[] {0, 1, 2, 3, 3, 3, 0, 1, 1, 2, 2, 0};
            int[] ep2index = new int[] {0, 0, 0, 0, 1, 2, 1, 1, 2, 1, 2, 2};
            long hash = 0;
            int[] first = new int[] {-1, -1, -1, -1};

            // phase2 ep
            //  1) whether rl edges are on the correct face or not
            //  2) order of each rl face edges
            for (int i = 0; i < state.ep.length; i++) {
                int group = ep2group[state.ep[i]];
                int index = ep2index[state.ep[i]];

                if (first[group] == -1) {
                    first[group] = index;
                }

                hash += ((long)(group * 4 + (index - first[group] + 3) % 3)) << (i * 4);
            }
            return hash;
        }
    }

    static class p2rlHash implements HashFunction {
        @Override
        public long get(FtoSolverState state) {
            long hash = 0;

            // phase2 rl
            // whether rl triangles are on the correct face or not
            for (int i = 0; i < state.rl.length; i++) {
                hash += (state.rl[i] / 3) << (i * 2);
            }
            return hash;
        }
    }

    static class p2ccHash implements HashFunction {
        @Override
        public long get(FtoSolverState state) {
            long hash = state.ccHash();
            return hash;
        }
    }

    private static long p2ufThirdHash(int[] ufThird) {
        long hash = 0;
        for (int i = 0; i < ufThird.length; i++) {
            hash += ufThird[i] << (i * 2);
        }
        return hash;
    }

    private static int p2ufThirdStandardize(int[] ufThird) {
        int col1 = ufThird[0], col2 = -1;
        for (int i = 1; i < ufThird.length; i++) {
            if (ufThird[i] != col1) {
                col2 = ufThird[i];
                break;
            }
        }
        int rot = p2ufRotMap[col1 * 4 + col2];
        for (int i = 0; i < ufThird.length; i++) {
            ufThird[i] = rotOp[rot].uf[ufThird[i] * 3] / 3;
        }
        return rot;
    }

    static class p2MoveIdx implements MoveFunction {
        @Override
        public int get(int idx, int move) {
            int ep = idx % p2epSize;
            int rl = idx / p2epSize;
            return p2rlMoveTable.get(move).get(rl) * p2epSize + p2epMoveTable.get(move).get(ep);
        }
    }

    private static void initPhase2() {
        l.info("  p2epHash");
        Object[] p2ep = createMoveTable(p2Moves, new p2epHash());
        p2epMoveTable = (List<List<Integer>>) p2ep[0];
        p2epIdx = (HashMap<Long, Integer>) p2ep[1];
        p2epSize = p2epIdx.size();
        l.info("  p2rlHash");
        Object[] p2rl = createMoveTable(p2Moves, new p2rlHash());
        p2rlMoveTable = (List<List<Integer>>) p2rl[0];
        p2rlIdx = (HashMap<Long, Integer>) p2rl[1];
        p2rlSize = p2rlIdx.size();
        l.info("  p2ccHash");
        Object[] p2cc = createMoveTable(p2Moves, new p2ccHash());
        p2ccMoveTable = (List<List<Integer>>) p2cc[0];
        p2ccIdx = (HashMap<Long, Integer>) p2cc[1];
        p2ccSize = p2ccIdx.size();

        l.info("  p2ufRotMap");
        for (int r = 0; r < rotOp.length; r++) {
            int[] uf = rotOp[r].uf.clone();
            int col1 = 0, col2 = 0;
            for (int i = 0; i < uf.length; i++) {
                if (uf[i] == 0) col1 = i / 3;
                if (uf[i] == 3) col2 = i / 3;
            }
            p2ufRotMap[col1 * 4 + col2] = r;
        }

        l.info("  p2ufIdx / p2ufIdx2ufHash");
        {
            int indexStd = 0;
            int[] ufThird = new int[]{0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3};
            for (int i = 0; i < 42000; i++) {
                for (int j = 1; j < ufThird.length; j++) {
                    if (ufThird[j] > 1) {
                        break;
                    } else if (ufThird[j] == 1) {
                        long hash = p2ufThirdHash(ufThird);
                        p2ufIdx.put(hash, indexStd);
                        p2ufIdx2ufHash[indexStd] = hash;

                        indexStd++;
                        break;
                    }
                }
                nextPermutation(ufThird);
            }
        }

        l.info("  p2ufMoveTable");
        for (int i = 0; i < p2ufSize; i++) {
            long hash = p2ufIdx2ufHash[i];

            // reconstruct ufThird from ufHash
            int[] ufThird = new int[12];
            for (int j = 0; j < ufThird.length; j++) {
                ufThird[j] = (int)(hash & 3);
                hash = hash >> 2;
            }

            for (int m = 0; m < p2Moves.length; m++) {
                int[] ufThirdNew = new int[12];
                for (int j = 0; j < ufThirdNew.length; j++) {
                    ufThirdNew[j] = ufThird[moveOp[p2Moves[m]].uf[j]];
                }

                int rot = p2ufThirdStandardize(ufThirdNew);
                p2ufMoveTable[m][i] = p2ufIdx.get(p2ufThirdHash(ufThirdNew)) << 4 | rot;
            }
        }

        l.info("  p2ccIdx2ufHash / p2ccRotTable");
        p2ccIdx2ufHash = new long[p2ccSize];
        p2ccRotTable = new int[rotOp.length][p2ccSize];
        for (Map.Entry<Long, Integer> entry : p2ccIdx.entrySet()) {
            long key = entry.getKey();
            int idx = entry.getValue();

            // reconstruct cp/co from ccHash
            FtoSolverState state = new FtoSolverState();
            for (int i = 0; i < state.cp.length; i++) {
                state.cp[i] = ((int) (key >> (i * 4))) & 15;
                state.co[i] = ((int) (key >> ((i + state.cp.length) * 4))) & 15;
            }

            // calculate pseudo ufHash from cp/co state
            int[][] cornerColor = new int[][] {{0,1}, {0,2}, {0,3}, {3,2}, {1,3}, {2,1}};
            int[] cpidx = new int[] {0, 1, 2, 0, 4, 5, 3, 1, 5, 3, 4, 2};
            int[] coidx = new int[] {0, 0, 0, 1, 0, 1, 1, 1, 0, 0, 1, 1};

            long ufHash = 0;
            for (int i = 0; i < 12; i++) {
                int ufThird = cornerColor[state.cp[cpidx[i]]][state.co[cpidx[i]]^coidx[i]];
                ufHash += ufThird << (i * 2);
            }

            p2ccIdx2ufHash[idx] = ufHash;

            // make rotation table for cp/co index
            for (int r = 0; r < rotOp.length; r++) {
                p2ccRotTable[r][idx] = p2ccIdx.get((rotOp[r].applyNew(state)).ccHash());
            }
        }

        l.info("  p2eprlPruning : " + p2epSize * p2rlSize);
        p2eprlPruning = createPruningTable(p2epSize * p2rlSize, p2Moves.length, p2eprlPruningMax - 2, new p2MoveIdx());
        l.info("  p2InvalidTable");
        p2InvalidTable = createInvalidTable(p2Moves);
    }

    //
    // definition and initialization of phase3 tables
    //
    private static int[] p3Moves = new int[] {8, 10, 12, 14};

    private static int p3epSize;
    private static List<List<Integer>> p3epMoveTable;
    private static HashMap<Long, Integer> p3epIdx;

    private static int p3ccSize;
    private static List<List<Integer>> p3ccMoveTable;
    private static HashMap<Long, Integer> p3ccIdx;

    private static byte[] p3epPruning;
    private static byte[] p3ccPruning;

    private static int[] p3InvalidTable;

    static class p3epHash implements HashFunction {
        @Override
        public long get(FtoSolverState state) {
            return state.epHash();
        }
    }

    static class p3ccHash implements HashFunction {
        @Override
        public long get(FtoSolverState state) {
            return state.ccHash();
        }
    }

    static class p3epMoveIdx implements MoveFunction {
        @Override
        public int get(int idx, int move) {
            return p3epMoveTable.get(move).get(idx);
        }
    }

    static class p3ccMoveIdx implements MoveFunction {
        @Override
        public int get(int idx, int move) {
            return p3ccMoveTable.get(move).get(idx);
        }
    }

    private static void initPhase3() {
        l.info("  p3epHash");
        Object[] p3ep = createMoveTable(p3Moves, new p3epHash());
        p3epMoveTable = (List<List<Integer>>) p3ep[0];
        p3epIdx = (HashMap<Long, Integer>) p3ep[1];
        p3epSize = p3epIdx.size();
        l.info("  p3ccHash");
        Object[] p3cc = createMoveTable(p3Moves, new p3ccHash());
        p3ccMoveTable = (List<List<Integer>>) p3cc[0];
        p3ccIdx = (HashMap<Long, Integer>) p3cc[1];
        p3ccSize = p3ccIdx.size();

        l.info("  p3epPruning : " + p3epSize);
        p3epPruning = createPruningTable(p3epSize, p3Moves.length, 14, new p3epMoveIdx());
        l.info("  p3ccPruning : " + p3ccSize);
        p3ccPruning = createPruningTable(p3ccSize, p3Moves.length, 14, new p3ccMoveIdx());
        l.info("  p3InvalidTable");
        p3InvalidTable = createInvalidTable(p3Moves);
    }

    static boolean inited = false;

    static void fullInit() {
        if (!inited) {
            initPhase1();
            initPhase2();
            initPhase3();

            inited = true;
        }
    }

    //
    // common functions for table initialization
    //
    private interface HashFunction {
        long get(FtoSolverState state);
    }

    private interface MoveFunction {
        int get(int idx, int move);
    }

    public static class FtoSolverState {
        int[] cp;
        int[] co;
        int[] ep;
        int[] uf;
        int[] rl;

        FtoSolverState() {
            cp = new int[] {0,1,2,3,4,5};
            co = new int[] {0,0,0,0,0,0};
            ep = new int[] {0,1,2,3,4,5,6,7,8,9,10,11};
            uf = new int[] {0,1,2,3,4,5,6,7,8,9,10,11};
            rl = new int[] {0,1,2,3,4,5,6,7,8,9,10,11};
        }

        FtoSolverState(int[] cp, int[] co, int[] ep, int[] uf, int[] rl) {
            this.cp = cp;
            this.co = co;
            this.ep = ep;
            this.uf = uf;
            this.rl = rl;
        }

        FtoSolverState(FtoSolverState src) {
            this.cp = src.cp.clone();
            this.co = src.co.clone();
            this.ep = src.ep.clone();
            this.uf = src.uf.clone();
            this.rl = src.rl.clone();
        }

        public FtoSolverState apply(FtoSolverState op) {
            FtoSolverState org = new FtoSolverState(this);
            for (int i = 0; i < this.cp.length; i++) {
                this.cp[i] = org.cp[op.cp[i]];
                this.co[i] = org.co[op.cp[i]] ^ op.co[i];
            }
            for (int i = 0; i < this.ep.length; i++) {
                this.ep[i] = org.ep[op.ep[i]];
                this.uf[i] = org.uf[op.uf[i]];
                this.rl[i] = org.rl[op.rl[i]];
            }
            return this;
        }

        public FtoSolverState applyNew(FtoSolverState op) {
            FtoSolverState ret = new FtoSolverState(this);
            return ret.apply(op);
        }

        public boolean equals(FtoSolverState other) {
            return Arrays.equals(this.cp, other.cp) &&
                Arrays.equals(this.co, other.co) &&
                Arrays.equals(this.ep, other.ep) &&
                Arrays.equals(this.uf, other.uf) &&
                Arrays.equals(this.rl, other.rl);
        }

        public long epHash() {
            long hash = 0;
            for (int i = 0; i < ep.length; i++) {
                hash += ((long)ep[i]) << (i * 4);
            }
            return hash;
        }

        public long ccHash() {
            long hash = 0;
            for (int i = 0; i < cp.length; i++) {
                hash += ((long)cp[i]) << (i * 4);
            }
            for (int i = 0; i < co.length; i++) {
                hash += ((long)co[i]) << ((i + cp.length) * 4);
            }
            return hash;
        }

        private int[] _normalizeTriangle(int[] tr) {
            int[] norm = new int[tr.length];
            int[] ap = new int[tr.length/3];
            for (int i = 0; i < tr.length; i++) {
                int color = tr[i] / 3;
                norm[i] = color * 3 + ap[color];
                ap[color]++;
            }
            return norm;
        }

        public FtoSolverState normalizeTriangle() {
            FtoSolverState normalized = new FtoSolverState(this);
            normalized.uf = _normalizeTriangle(normalized.uf);
            normalized.rl = _normalizeTriangle(normalized.rl);
            return normalized;
        }

        public int hashCode() {
            return Arrays.hashCode(cp) ^
                Arrays.hashCode(co) ^
                Arrays.hashCode(ep) ^
                Arrays.hashCode(uf) ^
                Arrays.hashCode(rl);
        }

        public String toString() {
            String str;
            str = "cp: " + Arrays.toString(cp) + "\nco: " + Arrays.toString(co)
                + "\nep: " + Arrays.toString(ep) + "\nuf: " + Arrays.toString(uf)
                + "\nrl: " + Arrays.toString(rl);
            return str;
        }
    }

    private static int[] generateRandomPermutation(int num, Random r) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            list.add(i);
        }
        Collections.shuffle(list, r);

        // guarantee even permutation
        int inversions = 0;
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i) > list.get(j)) {
                    inversions++;
                }
            }
        }
        if (inversions % 2 == 1) {
            Collections.swap(list, 0, 1);
        }

        int[] ret = new int[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = list.get(i);
        }

        return ret;
    }

    private static int[] generateRandomOrientation(Random r) {
        int num = 6;
        int[] ret = new int[num];
        int sum = 0;
        for (int i = 0; i < ret.length; i++) {
            ret[i] = r.nextInt(2);
            sum += ret[i];
        }

        if (sum % 2 == 1) {
            int inv = r.nextInt(ret.length);
            ret[inv] = ret[inv] ^ 1;
        }

        return ret;
    }

    public FtoSolverState generateRandomState(Random r) {
        int[] cp = generateRandomPermutation(6, r);
        int[] co = generateRandomOrientation(r);
        int[] ep = generateRandomPermutation(12, r);
        int[] uf = generateRandomPermutation(12, r);
        int[] rl = generateRandomPermutation(12, r);

        return new FtoSolverState(cp, co, ep, uf, rl);
    }

    private static class SolveParam {
        int[] idxs;
        int sym;
        int rot;
        FtoSolverState state;
        LinkedList<Integer> solve;
    }

    private static class SolveResult {
        LinkedList<Integer> solve;
        int num;
        FtoSolverState state;
        int sym;
        int rot;
    }

    //
    // FTO Solver common definition
    //
    private interface IndexMoveFunction {
        int[] get(int[] idx, int move);
    }

    private interface PruningFunction {
        int get(int[] idx);
    }

    //
    // FTO Solver phase1
    //
    private SolveParam[] phase1PreProcess(FtoSolverState fto) {
        // make four symmetric FTOs
        SolveParam[] p1Params = new SolveParam[4];
        for (int sym = 0; sym < rotOp.length; sym += 3) {
            FtoSolverState state = rotOp[sym].applyNew(fto);
            FtoSolverState state2 = new FtoSolverState();
            int rot;
            // find a rotation where the D-BR edge is in the correct position
            for (rot = 0; rot < rotOp.length; rot++) {
                state2 = state.applyNew(rotOp[rot]);
                if (state2.ep[4] == 4) break;
            }

            SolveParam solveParam = new SolveParam();
            solveParam.idxs = new int[2];
            solveParam.idxs[0] = p1epIdx.get((new p1epHash()).get(state2));
            solveParam.idxs[1] = p1rlIdx.get((new p1rlHash()).get(state2));
            solveParam.sym = sym;
            solveParam.rot = rot;
            p1Params[sym/3] = solveParam;
        }
        return p1Params;
    }

    static class p1IndexMove implements IndexMoveFunction {
        @Override
        public int[] get(int[] idxs, int move) {
            return new int[] {p1epMoveTable.get(move).get(idxs[0]), p1rlMoveTable.get(move).get(idxs[1])};
        }
    }

    static class p1Pruning implements PruningFunction {
        @Override
        public int get(int[] idxs) {
            return p1Pruning[idxs[0] + idxs[1]*p1epSize];
        }
    }

    private int removeWideMove(SolveResult solveResult) {
        int accRot = 0;
        // convert Uw to Uv D, Lw to Lv BR
        int[] w2axis = new int[] {8 >> 1, 4 >> 1};
        int[] w2rot = new int[] {1, 11};
        for (int i = 0; i < solveResult.solve.size(); i++) {
            int rot = 0;
            int axis = solveResult.solve.get(i) >> 1;
            int pow = solveResult.solve.get(i) & 1;
            if (axis >= 8) {
                rot = w2rot[axis - 8];
                axis = w2axis[axis - 8];
            }
            if (pow == 0) {
                rot = rotMul[rot][rot];
            }
            solveResult.solve.set(i, rotMov[accRot][axis] * 2 + pow);
            accRot = rotMul[rot][accRot];
        }
        return accRot;
    }

    private void phase1PostProcess(FtoSolverState fto, SolveParam[] p1Params, List<SolveResult> p1SolveResultList) {
        for (SolveResult solveResult : p1SolveResultList) {
            for (int i = 0; i < solveResult.solve.size(); i++) {
                int move = p1Moves[solveResult.solve.get(i) % p1Moves.length] + solveResult.solve.get(i) / p1Moves.length;
                solveResult.solve.set(i, move);
            }
            solveResult.sym = p1Params[solveResult.num].sym;
            solveResult.rot = p1Params[solveResult.num].rot;

            int accRot = removeWideMove(solveResult);

            FtoSolverState tmpState = new FtoSolverState(fto);
            for (int i = 0; i < solveResult.solve.size(); i++) {
                int move = solveResult.solve.get(i);
                int newMove = rotMov[rotDiv[0][solveResult.rot]][move >> 1] * 2 + (move & 1);
                solveResult.solve.set(i, newMove);
                tmpState.apply(moveOp[newMove]);
            }
            solveResult.rot = rotDiv[solveResult.rot][accRot];
            solveResult.state = new FtoSolverState(rotOp[solveResult.sym]);
            solveResult.state.apply(tmpState);
            solveResult.state.apply(rotOp[solveResult.rot]);
        }
    }

    //
    // FTO Solver phase2
    //
    private static int phase2ufIdx(FtoSolverState state) {
        int[] ufThird = new int[state.uf.length];
        for (int i = 0; i < ufThird.length; i++) {
            ufThird[i] = state.uf[i] / 3;
        }
        int rot = p2ufThirdStandardize(ufThird);
        return p2ufIdx.get(p2ufThirdHash(ufThird)) << 4 | rot;
    }

    private SolveParam[] phase2PreProcess(List<SolveResult> p1SolveResultList) {
        SolveParam[] p2Params = new SolveParam[p1SolveResultList.size()];
        int idx = 0;
        for (SolveResult solveResult : p1SolveResultList) {
            p2Params[idx] = new SolveParam();
            p2Params[idx].idxs = new int[4];
            p2Params[idx].idxs[0] = p2epIdx.get((new p2epHash()).get(solveResult.state));
            p2Params[idx].idxs[1] = p2rlIdx.get((new p2rlHash()).get(solveResult.state));
            p2Params[idx].idxs[2] = p2ccIdx.get((new p2ccHash()).get(solveResult.state));
            p2Params[idx].idxs[3] = phase2ufIdx(solveResult.state);
            p2Params[idx].sym = solveResult.sym;
            p2Params[idx].rot = solveResult.rot;
            p2Params[idx].state = solveResult.state;
            p2Params[idx].solve = solveResult.solve;
            idx++;
        }
        return p2Params;
    }

    static class p2IndexMove implements IndexMoveFunction {
        @Override
        public int[] get(int[] idxs, int move) {
            // idxs[3] = ufIdx << 4 | ufRot
            int ufIdxRot = p2ufMoveTable[move][idxs[3] >> 4];
            int ufRot = rotMul[ufIdxRot & 0xf][idxs[3] & 0xf];

            return new int[] {
                p2epMoveTable.get(move).get(idxs[0]),
                p2rlMoveTable.get(move).get(idxs[1]),
                p2ccMoveTable.get(move).get(idxs[2]),
                ufIdxRot & (~0xf) | ufRot
            };
        }
    }

    static class p2Pruning implements PruningFunction {
        @Override
        public int get(int[] idxs) {
            // Rotate cp/co to match the standardized uf and extract the difference by XORing the hash.
            long xors = p2ufIdx2ufHash[idxs[3] >> 4] ^ p2ccIdx2ufHash[p2ccRotTable[idxs[3] & 15][idxs[2]]];
            xors = (xors | xors >> 1) & 0x555555;

            // Count the number of color mismatches between uf triangle and corner pieces
            int mismatchedCountW = Long.bitCount(xors & 0x3f);
            int mismatchedCountWAdj = Long.bitCount(xors & 0xc0c0c0);
            int mismatchedCountOther = Long.bitCount(xors & 0x3f3f00);
            int trplIdx = (mismatchedCountW * 4 + mismatchedCountWAdj) * 7 + mismatchedCountOther;

            // ep/rl pruning
            int eprlPrun = p2eprlPruning[idxs[0] + idxs[1] * p2epSize];
            if (eprlPrun == -1) {
                eprlPrun = 15;
            }

            return Math.max(
                Math.min(p2eprlPruningMax, eprlPrun),
                p2trplPruning[trplIdx]
            );
        }
    }

    //
    // FTO Solver phase3
    //
    private SolveParam[] phase3PreProcess(List<SolveResult> p2SolveResultList) {
        SolveParam[] p3States = new SolveParam[p2SolveResultList.size()];
        int idx = 0;
        for (SolveResult solveResult : p2SolveResultList) {
            p3States[idx] = new SolveParam();
            p3States[idx].idxs = new int[2];
            p3States[idx].idxs[0] = p3epIdx.get((new p3epHash()).get(solveResult.state));
            p3States[idx].idxs[1] = p3ccIdx.get((new p3ccHash()).get(solveResult.state));
            p3States[idx].sym = solveResult.sym;
            p3States[idx].rot = solveResult.rot;
            p3States[idx].state = solveResult.state;
            p3States[idx].solve = solveResult.solve;
            idx++;
        }
        return p3States;
    }

    static class p3IndexMove implements IndexMoveFunction {
        @Override
        public int[] get(int[] idxs, int move) {
            return new int[] {
                p3epMoveTable.get(move).get(idxs[0]),
                p3ccMoveTable.get(move).get(idxs[1])
            };
        }
    }

    static class p3Pruning implements PruningFunction {
        @Override
        public int get(int[] idxs) {
            return Math.max(p3epPruning[idxs[0]], p3ccPruning[idxs[1]]);
        }
    }

    //
    // FTO Solver
    //
    private enum SearchStatus {
        COMPLETE_SEARCH,
        ABORT_SEARCH,
        ABORT_AXIS_SEARCH
    }

    private SearchStatus idaSearch(int[] idxs, int num, int moves, int maxSolves, int remainingDepth, LinkedList<Integer> solve, List<SolveResult> solveResultList, int[] invalidTable, IndexMoveFunction move, PruningFunction pruning) {
        int prun = pruning.get(idxs);

        if (prun > remainingDepth) {
            return prun > remainingDepth + 1 ? SearchStatus.ABORT_AXIS_SEARCH : SearchStatus.ABORT_SEARCH;
        } else if (remainingDepth == 0) {
            // This means prun <= remainingDepth && remeiningDepth == 0 -> prun == 0
            // Since prun == 0, it indicates that it was solved exactly at the depth
            SolveResult solveResult = new SolveResult();
            solveResult.solve = new LinkedList<>(solve);
            solveResult.num = num;
            solveResultList.add(solveResult);

            if (solveResultList.size() >= maxSolves) {
                return SearchStatus.COMPLETE_SEARCH;
            } else {
                return SearchStatus.ABORT_SEARCH;
            }
        } else if (prun == 0 && remainingDepth == 1) {
            return SearchStatus.ABORT_SEARCH;
        }

        for (int m = 0; m < moves; m++) {
            if (!solve.isEmpty() && ((invalidTable[solve.getLast() % moves] >> m) & 1) == 1) continue;
            int[] idxs1 = idxs.clone();
            for (int c = 0; c < 2; c++) {
                idxs1 = move.get(idxs1, m);
                solve.add(m + c * moves);
                SearchStatus status = idaSearch(idxs1, num, moves, maxSolves, remainingDepth - 1, solve, solveResultList, invalidTable, move, pruning);
                if (status == SearchStatus.COMPLETE_SEARCH) {
                    return SearchStatus.COMPLETE_SEARCH;
                }
                solve.removeLast();
                if (status == SearchStatus.ABORT_AXIS_SEARCH) {
                    break;
                }
            }
        }

        return SearchStatus.ABORT_SEARCH;
    }

    private List<SolveResult> solveMulti(SolveParam[] params, int moves, int maxSolves, int maxDepth, int[] invalidTable, IndexMoveFunction move, PruningFunction pruning) {
        List<SolveResult> solveResultList = new ArrayList<>();
        for (int depth = 0; depth <= maxDepth; depth++) {
            for (int i = 0; i < params.length; i++) {
                LinkedList<Integer> solve = new LinkedList<>();
                if (idaSearch(params[i].idxs, i, moves, maxSolves, depth, solve, solveResultList, invalidTable, move, pruning) == SearchStatus.COMPLETE_SEARCH) {
                    return solveResultList;
                }
            }
        }
        return solveResultList;
    }

    private void postProcess(SolveParam[] params, List<SolveResult> solveResultList, int[] moves) {
        for (SolveResult solveResult : solveResultList) {
            for (int i = 0; i < solveResult.solve.size(); i++) {
                int move = moves[solveResult.solve.get(i) % moves.length] + solveResult.solve.get(i) / moves.length;
                solveResult.solve.set(i, move);
            }
            solveResult.sym = params[solveResult.num].sym;
            solveResult.rot = params[solveResult.num].rot;

            solveResult.state = new FtoSolverState(params[solveResult.num].state);
            for (int i = 0; i < solveResult.solve.size(); i++) {
                int move = solveResult.solve.get(i);
                int newMove = rotMov[rotDiv[0][solveResult.rot]][move >> 1] * 2 + (move & 1);
                solveResult.solve.set(i, newMove);
                solveResult.state.apply(moveOp[move]);
            }

            LinkedList<Integer> combined = new LinkedList<>(params[solveResult.num].solve);
            combined.addAll(solveResult.solve);
            solveResult.solve = combined;
        }
    }

    public String solveIn(FtoSolverState state, int length) {
        fullInit();

        long startTime = System.currentTimeMillis();

        // phase1
        l.info("Solve phase1");
        SolveParam[] p1Params = phase1PreProcess(state);
        List<SolveResult> p1SolveResultList = solveMulti(p1Params, p1Moves.length, 2000, 20, p1InvalidTable, new p1IndexMove(), new p1Pruning());
        phase1PostProcess(state, p1Params, p1SolveResultList);

        // phase2
        l.info("Solve phase2");
        SolveParam[] p2Params = phase2PreProcess(p1SolveResultList);
        List<SolveResult> p2SolveResultList = solveMulti(p2Params, p2Moves.length, 1, length, p2InvalidTable, new p2IndexMove(), new p2Pruning());
        postProcess(p2Params, p2SolveResultList, p2Moves);

        // phase3
        l.info("Solve phase3");
        SolveParam[] p3States = phase3PreProcess(p2SolveResultList);
        List<SolveResult> p3SolveResultList = solveMulti(p3States, p3Moves.length, 1, length, p3InvalidTable, new p3IndexMove(), new p3Pruning());
        postProcess(p3States, p3SolveResultList, p3Moves);

        if (p3SolveResultList.isEmpty()) {
            return null;
        }

        SolveResult solveResult = p3SolveResultList.get(0);

        // construct scramble string
        StringBuilder strScrambleBuilder = new StringBuilder();
        for (int i = solveResult.solve.size() - 1; i >= 0; i--) {
            strScrambleBuilder.append(moveName[solveResult.solve.get(i) ^ 1]).append(" ");
        }
        String strScramble = strScrambleBuilder.toString();

        l.info("Scramble complete : " + (System.currentTimeMillis() - startTime) + "ms");

        return strScramble;
    }
}
