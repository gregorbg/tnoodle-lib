// FTO Solver is originally ported from csTimer that licensed under GPL-3.0
// https://github.com/cs0x7f/cstimer
// The Java version was originally created by hato-ya for CubicTimer, also licensed under GPL-3.0
// https://github.com/hato-ya/CubicTimer/blob/90ba77795ebba4f65fe3cfe293ba491d814962a2/app/src/main/java/com/hatopigeon/cubictimer/puzzle/FtoPuzzle.java

package org.worldcubeassociation.tnoodle.puzzle;

import org.timepedia.exporter.client.Export;
import org.worldcubeassociation.tnoodle.scrambles.Puzzle;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleStateAndGenerator;
import org.worldcubeassociation.tnoodle.svglite.Color;
import org.worldcubeassociation.tnoodle.svglite.Dimension;
import org.worldcubeassociation.tnoodle.svglite.Svg;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import static org.worldcubeassociation.tnoodle.puzzle.FaceTurningOctahedronSolver.*;

@Export
public class FaceTurningOctahedronPuzzle extends Puzzle {
    private static final int MIN_SCRAMBLE_LENGTH = 25;
    private static final Logger l = Logger.getLogger(FaceTurningOctahedronPuzzle.class.getName());

    private final FaceTurningOctahedronSolver ftoSolver;

    public FaceTurningOctahedronPuzzle() {
        ftoSolver = new FaceTurningOctahedronSolver();
    }

    @Override
    public String getShortName() {
        return "fto";
    }

    @Override
    public String getLongName() {
        return "Face-Turning Octahedron";
    }

    private static HashMap<String, Color> defaultColorScheme = new HashMap<>();
    static {
        defaultColorScheme.put("B",  new Color(0x304FFE));
        defaultColorScheme.put("B",  new Color(0x0000FF)); // csTimer
        defaultColorScheme.put("B",  new Color(0x2266FF)); // cubing.js
        defaultColorScheme.put("BL", new Color(0xFF8B24));
        defaultColorScheme.put("BL", new Color(0xFFAA00)); // csTimer
        defaultColorScheme.put("BL", new Color(0xFF8000)); // cubing.js
        defaultColorScheme.put("BR", new Color(0x999999));
        defaultColorScheme.put("BR", new Color(0xBBBBBB)); // csTimer
        defaultColorScheme.put("BR", new Color(0xAAAAAA)); // cubing.js
        defaultColorScheme.put("D",  new Color(0xFDD835));
        defaultColorScheme.put("D",  new Color(0xFFFF00)); // csTimer
        defaultColorScheme.put("D",  new Color(0xF4F400)); // cubing.js
        defaultColorScheme.put("F",  new Color(0x02D040));
        defaultColorScheme.put("F",  new Color(0x00DD00)); // csTimer
        defaultColorScheme.put("F",  new Color(0x44EE00)); // cubing.js
        defaultColorScheme.put("L",  new Color(0x8A1AFF));
        defaultColorScheme.put("L",  new Color(0x880088)); // csTimer
        defaultColorScheme.put("L",  new Color(0x8800DD)); // cubing.js
        defaultColorScheme.put("R",  new Color(0xEC0000));
        defaultColorScheme.put("R",  new Color(0xFF0000)); // csTimer
        defaultColorScheme.put("R",  new Color(0xFF0000)); // cubing.js
        defaultColorScheme.put("U",  new Color(0xffffff));
    }
    @Override
    public HashMap<String, Color> getDefaultColorScheme() {
        return new HashMap<>(defaultColorScheme);
    }

    @Override
    public Dimension getPreferredSize() {
        // roughly 2:1, but not exactly
        return new Dimension(2368, 1216);
    }

    @Override
    public PuzzleState getSolvedState() {
        return new FtoState();
    }

    @Override
    protected int getRandomMoveCount() {
        // csTimer does 30 moves if you set it to randomMoves
        return 30;
    }

    @Override
    public PuzzleStateAndGenerator generateRandomMoves(Random r) {
        FtoSolverState randomState = ftoSolver.generateRandomState(r);
        String scrambleStr = ftoSolver.solveIn(randomState, MIN_SCRAMBLE_LENGTH);

        FtoState pState = new FtoState(randomState);
        return new PuzzleStateAndGenerator(pState, scrambleStr);
    }

    public class FtoState extends PuzzleState {
        private final FtoSolverState fto;

        public FtoState() {
            fto = new FtoSolverState();
        }

        public FtoState(FtoSolverState src) {
            this.fto = new FtoSolverState(src);
        }

        @Override
        public LinkedHashMap<String, ? extends PuzzleState> getSuccessorsByName() {
            LinkedHashMap<String, PuzzleState> successors = new LinkedHashMap<>();

            for (int i = 0; i < moveName.length; i++) {
                FtoState state = new FtoState(this.fto.applyNew(moveOp[i]));
                successors.put(moveName[i], state);
            }

            return successors;
        }

        @Override
        public String solveIn(int n) {
            return ftoSolver.solveIn(this.fto, n);
        }

        @Override
        public boolean equals(Object other) {
            return fto.equals(((FtoState)other).fto);
        }

        @Override
        public int hashCode() {
            return fto.hashCode();
        }

        @Override
        public PuzzleState getNormalized() {
            FtoSolverState state2 = new FtoSolverState();
            int rot;
            // find a rotation where the D-BR edge is in the correct position
            for (rot = 0; rot < rotOp.length; rot++) {
                state2 = fto.applyNew(rotOp[rot]);
                if (state2.ep[4] == 4) break;
            }
            return new FtoState(state2.normalizeTriangle());
        }

        @Override
        protected Svg drawScramble(Map<String, Color> colorScheme) {
            /*   State index of FTODrawer
                         U                  B
                       L   R             BR   BL
                         F                  D
                   0  1  2  3  4      45 46 47 48 49
                 9    5  6  7   31  36   50 51 52   54
                10 11    8   29 30  38 39   53   55 56
                14 12 13  27 28 34  37 43 44  58 59 57
                15 16   18   32 33  41 42   71   60 61
                17   19 20 21   35  40   70 69 68   62
                  22 23 24 25 26      67 66 65 64 63
             */

            int[][] state_corner = {
                {8, 27,18,13}, {4, 45,36,31}, {0, 9, 54,49},
                {58,71,44,53}, {22,63,62,17}, {40,67,26,35}
            };
            int[][] state_edge = {
                {7, 29}, {5, 11}, {2, 47}, {60,68}, {42,70}, {24,65},
                {21,32}, {19,16}, {57,14}, {55,52}, {39,50}, {37,34}
            };
            int[] state_upfront = {6,3,1, 20,23,25, 43,38,41, 59,61,56};
            int[] state_rightleft = {69,66,64, 51,48,46, 12,10,15, 28,33,30};

            int[] state = new int[72];

            for (int i = 0; i < state_corner.length; i++) {
                for (int j = 0; j < state_corner[i].length; j++) {
                    int[][] corner = {
                        {0,3,2,1}, {0,5,4,3}, {0,1,6,5},
                        {6,7,4,5}, {2,7,6,1}, {4,7,2,3}
                    };
                    int[][] orientation = {{0,1,2,3}, {2,3,0,1}};
                    state[state_corner[i][j]] = corner[fto.cp[i]][orientation[fto.co[i]][j]];
                }
            }
            for (int i = 0; i < state_edge.length; i++) {
                int[][] edge = {
                    {0,3}, {0,1}, {0,5}, {6,7}, {4,7}, {2,7},
                    {2,3}, {2,1}, {6,1}, {6,5}, {4,5}, {4,3}
                };
                for (int j = 0; j < state_edge[i].length; j++) {
                    state[state_edge[i][j]] = edge[fto.ep[i]][j];
                }
            }
            for (int i = 0; i < state_upfront.length; i++) {
                int[] upfront = {0,0,0, 2,2,2, 4,4,4, 6,6,6};
                state[state_upfront[i]] = upfront[fto.uf[i]];
            }
            for (int i = 0; i < state_rightleft.length; i++) {
                int[] rightleft = {7,7,7, 5,5,5, 1,1,1, 3,3,3};
                state[state_rightleft[i]] = rightleft[fto.rl[i]];
            }

            String[] keys = {"U", "L", "F", "R", "BR", "B", "BL", "D"};
            String[] colorHexCodes = new String[keys.length];

            for (int i = 0; i < keys.length; i++) {
                Color color = colorScheme.get(keys[i]);
                colorHexCodes[i] = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
            }
            // TODO
            return new Svg(getPreferredSize());
        }
    }
}
