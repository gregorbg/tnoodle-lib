package org.worldcubeassociation.tnoodle.puzzle;

import org.timepedia.exporter.client.Export;
import org.worldcubeassociation.tnoodle.scrambles.Puzzle;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleStateAndGenerator;
import org.worldcubeassociation.tnoodle.svglite.*;

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

    private static final int pieceSize = 30;
    private static final int gap = 3;

    private static HashMap<String, Color> defaultColorScheme = new HashMap<>();
    static {
        defaultColorScheme.put("U", Color.WHITE);
        defaultColorScheme.put("R", Color.RED);
        defaultColorScheme.put("F", Color.GREEN);
        defaultColorScheme.put("L", Color.PURPLE);
        defaultColorScheme.put("B", Color.BLUE);
        defaultColorScheme.put("BL", Color.ORANGE);
        defaultColorScheme.put("D", Color.YELLOW);
        defaultColorScheme.put("BR", Color.GRAY);
    }
    @Override
    public HashMap<String, Color> getDefaultColorScheme() {
        return new HashMap<>(defaultColorScheme);
    }

    @Override
    public Dimension getPreferredSize() {
        return getImageSize(gap, pieceSize);
    }

    private static Dimension getImageSize(int gap, int pieceSize) {
        return new Dimension(getFTOViewWidth(gap, pieceSize), getFTOViewHeight(gap, pieceSize));
    }

    // Draws two squares side-by-side.
    // Square 1: Faces 0-3. Square 2: Faces 4-7.
    private void drawFTO(Svg g, int gap, int pieceSize, Color[] colorScheme, int[][] image) {
        // Push the triangles outward from the center by gap / sqrt(2)
        double offset = gap / Math.sqrt(2);
        double sqSize = 2 * pieceSize + 2 * offset;

        // A wider gap between the left and right halves of the puzzle
        int macroGap = 2 * gap;

        // Center coordinates for the first square (Left)
        // Left margin (gap) + half of the expanded square
        double x1 = gap + sqSize / 2.0;
        double y1 = gap + sqSize / 2.0;

        // Center coordinates for the second square (Right)
        // Left margin + full left square + macro gap + half of right square
        double x2 = gap + sqSize + macroGap + sqSize / 2.0;
        double y2 = gap + sqSize / 2.0;

        // Draw Left Square
        for (int i = 0; i < 4; i++) {
            double angle = (-0.5 + i * 0.5) * Math.PI;
            double dx = offset * Math.cos(angle);
            double dy = offset * Math.sin(angle);
            drawTriangle(g, x1 + dx, y1 + dy, i, image[i], pieceSize, colorScheme);
        }

        // Draw Right Square
        for (int i = 0; i < 4; i++) {
            double angle = (-0.5 + i * 0.5) * Math.PI;
            double dx = offset * Math.cos(angle);
            double dy = offset * Math.sin(angle);
            drawTriangle(g, x2 + dx, y2 + dy, i, image[i + 4], pieceSize, colorScheme);
        }
    }

    private void drawTriangle(Svg g, double x, double y, int rot, int[] state, int pieceSize, Color[] colorScheme) {
        Path p = triangle(rot, pieceSize);
        p.translate(x, y);

        double[] xpoints = new double[3];
        double[] ypoints = new double[3];
        PathIterator iter = p.getPathIterator();
        for(int ch = 0; ch < 3; ch++) {
            double[] coords = new double[6];
            int type = iter.currentSegment(coords);
            if(type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                xpoints[ch] = coords[0];
                ypoints[ch] = coords[1];
            }
            iter.next();
        }

        double[] xs = new double[6];
        double[] ys = new double[6];
        for(int i = 0; i < 3; i++) {
            xs[i]=1/3.*xpoints[(i+1)%3]+2/3.*xpoints[i];
            ys[i]=1/3.*ypoints[(i+1)%3]+2/3.*ypoints[i];
            xs[i+3]=2/3.*xpoints[(i+1)%3]+1/3.*xpoints[i];
            ys[i+3]=2/3.*ypoints[(i+1)%3]+1/3.*ypoints[i];
        }

        Path[] ps = new Path[9];
        for(int i = 0; i < ps.length; i++) {
            ps[i] = new Path();
        }

        Point2D.Double center = getLineIntersection(xs[0], ys[0], xs[4], ys[4], xs[2], ys[2], xs[3], ys[3]);

        for(int i = 0; i < 3; i++) {
            ps[3*i].moveTo(xpoints[i], ypoints[i]);
            ps[3*i].lineTo(xs[i], ys[i]);
            ps[3*i].lineTo(xs[3+(2+i)%3], ys[3+(2+i)%3]);
            ps[3*i].closePath();

            ps[3*i+1].moveTo(xs[i], ys[i]);
            ps[3*i+1].lineTo(xs[3+(i+2)%3], ys[3+(i+2)%3]);
            ps[3*i+1].lineTo(center.x, center.y);
            ps[3*i+1].closePath();

            ps[3*i+2].moveTo(xs[i], ys[i]);
            ps[3*i+2].lineTo(xs[i+3], ys[i+3]);
            ps[3*i+2].lineTo(center.x, center.y);
            ps[3*i+2].closePath();
        }

        for(int i = 0; i < ps.length; i++) {
            Path sticker = ps[i];
            sticker.setFill(colorScheme[state[i]]);
            sticker.setStroke(Color.BLACK);
            g.appendChild(sticker);
        }
    }

    private static Path triangle(int rot, int pieceSize) {
        // Distance from center to the square's corners
        double rad = Math.sqrt(2) * pieceSize;
        double[] x = new double[3];
        double[] y = new double[3];

        // The tip of the macro triangle is always at the center of the square
        x[0] = 0;
        y[0] = 0;

        // Base angles for rot = 0: 225 degrees (-0.75 Pi) and 315 degrees (-0.25 Pi)
        double baseAngle = -0.75;
        double[] angs = { baseAngle, baseAngle + 0.5 };

        for(int i = 0; i < angs.length; i++) {
            // Shift by 90 degrees (0.5 Pi) per rotation step
            angs[i] += rot * 0.5;
            angs[i] *= Math.PI;
            x[i+1] = rad * Math.cos(angs[i]);
            y[i+1] = rad * Math.sin(angs[i]);
        }

        Path p = new Path();
        p.moveTo(x[0], y[0]);
        for(int ch = 1; ch < 3; ch++) {
            p.lineTo(x[ch], y[ch]);
        }
        p.closePath();
        return p;
    }

    private static Point2D.Double getLineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        return new Point2D.Double(
            det(det(x1, y1, x2, y2), x1 - x2,
                det(x3, y3, x4, y4), x3 - x4)/
                det(x1 - x2, y1 - y2, x3 - x4, y3 - y4),
            det(det(x1, y1, x2, y2), y1 - y2,
                det(x3, y3, x4, y4), y3 - y4)/
                det(x1 - x2, y1 - y2, x3 - x4, y3 - y4));
    }

    private static double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }

    private static int getFTOViewWidth(int gap, int pieceSize) {
        double offset = gap / Math.sqrt(2);
        int macroGap = 2 * gap;

        // 2 outer margins (left/right) + middle macro gap + 2 expanded squares
        return (int) Math.ceil(2 * gap + macroGap + 4 * pieceSize + 4 * offset);
    }

    private static int getFTOViewHeight(int gap, int pieceSize) {
        double offset = gap / Math.sqrt(2);
        // 2 margins (top, bottom) + 1 expanded square
        return (int) Math.ceil(2 * gap + 2 * pieceSize + 2 * offset);
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
            /*   State index of FTO internal state
                          U                     B
                       L     R               BR   BL
                          F                     D
                    3  4  5  7  6        39 40 41 43 42
                33     2  1  8    12  69    38 37 44    48
                34 35     0    11 13  70 71    36    47 49
                32 28 27     9 10 14  68 64 63    45 46 50
                31 29    18    17 16  67 65    54    53 52
                30    26 19 20    15  66    62 55 56    51
                   24 25 23 22 21        60 61 59 58 57
             */

            int[][] state_corner = {
                { 0, 9,18,27}, { 6,39,69,12}, { 3,33,48,42},
                {45,54,63,36}, {24,57,51,30}, {66,60,21,15}
            };
            int[][] state_edge = {
                { 8,11}, { 2,35}, { 5,41}, {53,56}, {65,62}, {23,59},
                {20,17}, {26,29}, {50,32}, {47,44}, {71,38}, {68,14}
            };
            int[] state_upfront =   { 1, 7, 4,19,25,22,64,70,67,46,52,49};
            int[] state_rightleft = {55,61,58,37,43,40,28,34,31,10,16,13};

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

            int[][] image = new int[8][9];

            for (int side = 0; side < 8; side++) {
                for (int sticker = 0; sticker < 9; sticker++) {
                    image[side][sticker] = state[side * 9 + sticker];
                }
            }

            Dimension preferredSize = getPreferredSize();
            Svg svg = new Svg(preferredSize);
            svg.setStroke(2, 10, "round");

            Color[] scheme = new Color[8];
            for(int i = 0; i < scheme.length; i++) {
                scheme[i] = colorScheme.get("U|L|F|R|BR|B|BL|D".split("\\|")[i]);
            }
            drawFTO(svg, gap, pieceSize, scheme, image);

            return svg;
        }
    }
}
