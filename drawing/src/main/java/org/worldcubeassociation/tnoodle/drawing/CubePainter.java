package org.worldcubeassociation.tnoodle.drawing;

import org.worldcubeassociation.tnoodle.PuzzleSvgPainter;
import org.worldcubeassociation.tnoodle.state.CubeState;
import org.worldcubeassociation.tnoodle.svglite.Color;
import org.worldcubeassociation.tnoodle.svglite.Dimension;
import org.worldcubeassociation.tnoodle.svglite.Rectangle;
import org.worldcubeassociation.tnoodle.svglite.Svg;

import java.util.HashMap;
import java.util.Map;

public class CubePainter extends PuzzleSvgPainter<CubeState> {
    public final int puzzleSize;

    public CubePainter(int puzzleSize) {
        this.puzzleSize = puzzleSize;
    }

    private static final int gap = 2;
    private static final int cubieSize = 10;

    @Override
    protected void drawScramble(Svg canvas, CubeState state, Map<String, Color> colorScheme) {
        drawCube(canvas, state.image, gap, cubieSize, colorScheme);
    }

    private void drawCube(Svg g, int[][][] state, int gap, int cubieSize, Map<String, Color> colorScheme) {
        paintCubeFace(g, gap, 2 * gap + puzzleSize * cubieSize, puzzleSize, cubieSize, state[CubeState.Face.L.ordinal()], colorScheme);
        paintCubeFace(g, 2 * gap + puzzleSize * cubieSize, 3 * gap + 2 * puzzleSize * cubieSize, puzzleSize, cubieSize, state[CubeState.Face.D.ordinal()], colorScheme);
        paintCubeFace(g, 4 * gap + 3 * puzzleSize * cubieSize, 2 * gap + puzzleSize * cubieSize, puzzleSize, cubieSize, state[CubeState.Face.B.ordinal()], colorScheme);
        paintCubeFace(g, 3 * gap + 2 * puzzleSize * cubieSize, 2 * gap + puzzleSize * cubieSize, puzzleSize, cubieSize, state[CubeState.Face.R.ordinal()], colorScheme);
        paintCubeFace(g, 2 * gap + puzzleSize * cubieSize, gap, puzzleSize, cubieSize, state[CubeState.Face.U.ordinal()], colorScheme);
        paintCubeFace(g, 2 * gap + puzzleSize * cubieSize, 2 * gap + puzzleSize * cubieSize, puzzleSize, cubieSize, state[CubeState.Face.F.ordinal()], colorScheme);
    }

    private void paintCubeFace(Svg g, int x, int y, int size, int cubieSize, int[][] faceColors, Map<String, Color> colorScheme) {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int tempx = x + col * cubieSize;
                int tempy = y + row * cubieSize;
                Rectangle rect = new Rectangle(tempx, tempy, cubieSize, cubieSize);
                rect.setFill(colorScheme.get(CubeState.Face.values()[faceColors[row][col]].toString()));
                rect.setStroke(Color.BLACK);
                g.appendChild(rect);
            }
        }
    }

    private static final Map<String, Color> DEFAULT_COLOR_SCHEME = new HashMap<>();

    static {
        DEFAULT_COLOR_SCHEME.put("B", Color.BLUE);
        DEFAULT_COLOR_SCHEME.put("D", Color.YELLOW);
        DEFAULT_COLOR_SCHEME.put("F", Color.GREEN);
        DEFAULT_COLOR_SCHEME.put("L", new Color(255, 128, 0)); //orange heraldic tincture
        DEFAULT_COLOR_SCHEME.put("R", Color.RED);
        DEFAULT_COLOR_SCHEME.put("U", Color.WHITE);
    }

    @Override
    public Map<String, Color> getDefaultColorScheme() {
        return new HashMap<>(DEFAULT_COLOR_SCHEME);
    }

    @Override
    public Dimension getPreferredSize() {
        return getImageSize(gap, cubieSize, puzzleSize);
    }

    private static int getCubeViewWidth(int cubie, int gap, int size) {
        return (size * cubie + gap) * 4 + gap;
    }

    private static int getCubeViewHeight(int cubie, int gap, int size) {
        return (size * cubie + gap) * 3 + gap;
    }

    private static Dimension getImageSize(int gap, int unitSize, int size) {
        return new Dimension(getCubeViewWidth(unitSize, gap, size), getCubeViewHeight(unitSize, gap, size));
    }
}
