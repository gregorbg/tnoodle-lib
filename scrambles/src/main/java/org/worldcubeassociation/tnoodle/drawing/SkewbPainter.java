package org.worldcubeassociation.tnoodle.drawing;

import org.worldcubeassociation.tnoodle.scrambles.PuzzleSvgPainter;
import org.worldcubeassociation.tnoodle.state.SkewbState;
import org.worldcubeassociation.tnoodle.svglite.*;

import java.util.HashMap;
import java.util.Map;

public class SkewbPainter extends PuzzleSvgPainter<SkewbState> {
    private static final int pieceSize = 30;
    private static final int gap = 3;

    private static final double sq3d2 = Math.sqrt(3) / 2;

    @Override
    protected void drawScramble(Svg canvas, SkewbState state, Map<String, Color> colorScheme) {
        Color[] scheme = new Color[6];
        for(int i = 0; i < scheme.length; i++) {
            scheme[i] = colorScheme.get("URFDLB".charAt(i)+"");
        }
        Transform[] position = getFaceTrans();
        for (int face=0; face<6; face++) {
            Path[] p = getFacePaths();
            for (int i=0; i<5; i++) {
                p[i].transform(position[face]);
                p[i].setFill(scheme[state.image[face][i]]);
                p[i].setStroke(Color.BLACK);
                canvas.appendChild(p[i]);
            }
        }
    }

    private Transform[] getFaceTrans() {
        return new Transform[]{
            new Transform(pieceSize*sq3d2, -pieceSize/2, pieceSize*sq3d2, pieceSize/2, (pieceSize*4+gap*1.5)*sq3d2, pieceSize),
            new Transform(pieceSize*sq3d2, -pieceSize/2, 0, pieceSize, (pieceSize*7+gap*3)*sq3d2, pieceSize * 1.5),
            new Transform(pieceSize*sq3d2, -pieceSize/2, 0, pieceSize, (pieceSize*5+gap*2)*sq3d2, pieceSize * 2.5 + 0.5 * gap),
            new Transform(0, pieceSize, -pieceSize*sq3d2, -pieceSize/2, (pieceSize*3+gap*1)*sq3d2, pieceSize * 4.5 + 1.5 * gap),
            new Transform(pieceSize*sq3d2, pieceSize/2, 0, pieceSize, (pieceSize*3+gap*1)*sq3d2, pieceSize * 2.5 + 0.5 * gap),
            new Transform(pieceSize*sq3d2, pieceSize/2, 0, pieceSize, pieceSize*sq3d2, pieceSize * 1.5),
        };
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

    private static final Map<String, Color> DEFAULT_COLOR_SCHEME = new HashMap<>();

    static {
        DEFAULT_COLOR_SCHEME.put("U", Color.WHITE);
        DEFAULT_COLOR_SCHEME.put("R", Color.BLUE);
        DEFAULT_COLOR_SCHEME.put("F", Color.RED);
        DEFAULT_COLOR_SCHEME.put("D", Color.YELLOW);
        DEFAULT_COLOR_SCHEME.put("L", Color.GREEN);
        DEFAULT_COLOR_SCHEME.put("B", new Color(0xFF8000));
    }

    @Override
    public Map<String, Color> getDefaultColorScheme() {
        return new HashMap<>(DEFAULT_COLOR_SCHEME);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
            (int) Math.ceil((3 * gap + 8 * pieceSize + 1) * sq3d2),
            (int) Math.ceil(2 * gap + 6 * pieceSize + 1));
    }
}
