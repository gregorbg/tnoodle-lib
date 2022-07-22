package org.worldcubeassociation.tnoodle.drawing;

import org.worldcubeassociation.tnoodle.scrambles.PuzzleSvgPainter;
import org.worldcubeassociation.tnoodle.state.PyraminxState;
import org.worldcubeassociation.tnoodle.svglite.*;

import java.util.HashMap;
import java.util.Map;

public class PyraminxPainter extends PuzzleSvgPainter<PyraminxState> {
    private static final int pieceSize = 30;
    private static final int gap = 5;

    @Override
    protected void drawScramble(Svg canvas, PyraminxState state, Map<String, Color> colorScheme) {
        canvas.setStroke(2, 10, "round");

        Color[] scheme = new Color[4];
        for(int i = 0; i < scheme.length; i++) {
            scheme[i] = colorScheme.get("FDLR".charAt(i)+"");
        }
        drawMinx(canvas, gap, pieceSize, scheme, state.image);
    }

    private void drawMinx(Svg g, int gap, int pieceSize, Color[] colorScheme, int[][] image) {
        drawTriangle(g, 2*gap+3*pieceSize, gap+Math.sqrt(3)*pieceSize, true, image[0], pieceSize, colorScheme);
        drawTriangle(g, 2*gap+3*pieceSize, 2*gap+2*Math.sqrt(3)*pieceSize, false, image[1], pieceSize, colorScheme);
        drawTriangle(g, gap+1.5*pieceSize, gap+Math.sqrt(3)/2*pieceSize, false, image[2], pieceSize, colorScheme);
        drawTriangle(g, 3*gap+4.5*pieceSize, gap+Math.sqrt(3)/2*pieceSize,  false, image[3], pieceSize, colorScheme);
    }

    private void drawTriangle(Svg g, double x, double y, boolean up, int[] state, int pieceSize, Color[] colorScheme) {
        Path p = triangle(up, pieceSize);
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

    private static Path triangle(boolean pointup, int pieceSize) {
        int rad = (int)(Math.sqrt(3) * pieceSize);
        double[] angs = { 7/6., 11/6., .5 };
        for(int i = 0; i < angs.length; i++) {
            if(pointup) {
                angs[i] += 1/3.;
            }
            angs[i] *= Math.PI;
        }
        double[] x = new double[angs.length];
        double[] y = new double[angs.length];
        for(int i = 0; i < x.length; i++) {
            x[i] = rad * Math.cos(angs[i]);
            y[i] = rad * Math.sin(angs[i]);
        }
        Path p = new Path();
        p.moveTo(x[0], y[0]);
        for(int ch = 1; ch < x.length; ch++) {
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

    private static int getPyraminxViewWidth(int gap, int pieceSize) {
        return (2 * 3 * pieceSize + 4 * gap);
    }
    private static int getPyraminxViewHeight(int gap, int pieceSize) {
        return (int)(2 * 1.5 * Math.sqrt(3) * pieceSize + 3 * gap);
    }

    private static int getNewUnitSize(int width, int height, int gap) {
        return (int) Math.round(Math.min((width - 4*gap) / (3 * 2f),
            (height - 3*gap) / (3 * Math.sqrt(3))));
    }

    private static Path getTriangle(double x, double y, int pieceSize, boolean up) {
        Path p = triangle(up, pieceSize);
        p.translate(x, y);
        return p;
    }

    private static final Map<String, Color> DEFAULT_COLOR_SCHEME = new HashMap<>();

    static {
        DEFAULT_COLOR_SCHEME.put("F", new Color(0x00FF00));
        DEFAULT_COLOR_SCHEME.put("D", new Color(0xFFFF00));
        DEFAULT_COLOR_SCHEME.put("L", new Color(0xFF0000));
        DEFAULT_COLOR_SCHEME.put("R", new Color(0x0000FF));
    }

    @Override
    public Map<String, Color> getDefaultColorScheme() {
        return new HashMap<>(DEFAULT_COLOR_SCHEME);
    }

    @Override
    public Dimension getPreferredSize() {
        return getImageSize(gap, pieceSize);
    }

    private static Dimension getImageSize(int gap, int pieceSize) {
        return new Dimension(getPyraminxViewWidth(gap, pieceSize), getPyraminxViewHeight(gap, pieceSize));
    }
}
