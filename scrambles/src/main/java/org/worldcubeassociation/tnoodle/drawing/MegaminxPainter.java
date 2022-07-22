package org.worldcubeassociation.tnoodle.drawing;

import org.worldcubeassociation.tnoodle.scrambles.PuzzleSvgPainter;
import org.worldcubeassociation.tnoodle.state.MegaminxState;
import org.worldcubeassociation.tnoodle.svglite.*;

import java.util.HashMap;
import java.util.Map;

public class MegaminxPainter extends PuzzleSvgPainter<MegaminxState> {
    @Override
    protected void drawScramble(Svg canvas, MegaminxState state, Map<String, Color> colorScheme) {
        drawMinx(canvas, state.image, gap, minxRad, colorScheme);
    }

    private static final int gap = 2;
    private static final int minxRad = 30;

    private void drawMinx(Svg g, int[][] image, int gap, int minxRad, Map<String, Color> colorScheme) {
        Map<MegaminxState.Face, Path> pentagons = getFaceBoundaries(gap, minxRad);
        for(MegaminxState.Face face : pentagons.keySet()) {
            int f = face.ordinal();
            int rotateCounterClockwise;
            if(face == MegaminxState.Face.U) {
                rotateCounterClockwise = 0;
            } else if(f >= 1 && f <= 5) {
                rotateCounterClockwise = 1;
            } else if(f >= 6 && f <= 11) {
                rotateCounterClockwise = 2;
            } else {
                assert false;
                return;
            }
            String label = null;
            if(face == MegaminxState.Face.U || face == MegaminxState.Face.F) {
                label = face.toString();
            }
            drawPentagon(g, pentagons.get(face), image[f], rotateCounterClockwise, label, colorScheme);
        }
    }

    private void drawPentagon(Svg g, Path p, int[] state, int rotateCounterClockwise, String label, Map<String, Color> colorScheme) {
        double[] xpoints = new double[5];
        double[] ypoints = new double[5];
        PathIterator iter = p.getPathIterator();
        for(int ch = 0; ch < 5; ch++) {
            double[] coords = new double[6];
            int type = iter.currentSegment(coords);
            if(type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                xpoints[ch] = coords[0];
                ypoints[ch] = coords[1];
            }
            iter.next();
        }

        double[] xs = new double[10];
        double[] ys = new double[10];
        for(int i = 0; i < 5; i++) {
            xs[i]=.4*xpoints[(i+1)%5]+.6*xpoints[i];
            ys[i]=.4*ypoints[(i+1)%5]+.6*ypoints[i];
            xs[i+5]=.6*xpoints[(i+1)%5]+.4*xpoints[i];
            ys[i+5]=.6*ypoints[(i+1)%5]+.4*ypoints[i];
        }

        Path[] ps = new Path[11];
        for(int i = 0 ; i < ps.length; i++) {
            ps[i] = new Path();
        }
        Point2D.Double[] intpent = new Point2D.Double[5];
        for(int i = 0; i < intpent.length; i++) {
            intpent[i] = getLineIntersection(xs[i], ys[i], xs[5+(3+i)%5], ys[5+(3+i)%5], xs[(i+1)%5], ys[(i+1)%5], xs[5+(4+i)%5], ys[5+(4+i)%5]);
            if(i == 0) {
                ps[10].moveTo(intpent[i].x, intpent[i].y);
            } else {
                ps[10].lineTo(intpent[i].x, intpent[i].y);
            }
        }
        ps[10].closePath();

        for(int i = 0; i < 5; i++) {
            ps[2*i].moveTo(xpoints[i], ypoints[i]);
            ps[2*i].lineTo(xs[i], ys[i]);
            ps[2*i].lineTo(intpent[i].x, intpent[i].y);
            ps[2*i].lineTo(xs[5+(4+i)%5], ys[5+(4+i)%5]);
            ps[2*i].closePath();

            ps[2*i+1].moveTo(xs[i], ys[i]);
            ps[2*i+1].lineTo(xs[i+5], ys[i+5]);
            ps[2*i+1].lineTo(intpent[(i+1)%5].x, intpent[(i+1)%5].y);
            ps[2*i+1].lineTo(intpent[i].x, intpent[i].y);
            ps[2*i+1].closePath();
        }

        for(int i = 0; i < ps.length; i++) {
            int j = i;
            if(j < 10) {
                // This is a bit convoluted, but tries to keep the intuitive derivation clear.
                j = (j + 2*rotateCounterClockwise) % 10;
            }
            ps[i].setStroke(Color.BLACK);
            ps[i].setFill(colorScheme.get("" + MegaminxState.Face.values()[state[j]]));
            g.appendChild(ps[i]);
        }

        if(label != null) {
            double centerX = 0;
            double centerY = 0;
            for(Point2D.Double pt : intpent) {
                centerX += pt.x;
                centerY += pt.y;
            }
            centerX /= intpent.length;
            centerY /= intpent.length;
            Text labelText = new Text(label, centerX, centerY);
            // Vertically and horizontally center text
            labelText.setAttribute("text-anchor", "middle");
            // dominant-baseline works great on Chrome, but
            // unfortunately isn't supported by androidsvg.
            // See http://stackoverflow.com/q/56402 for workaround.
            //labelText.setStyle("dominant-baseline", "central");
            labelText.setAttribute("dy", "0.7ex");
            g.appendChild(labelText);
        }
    }

    private static final Map<String, Color> DEFAULT_COLOR_SCHEME = new HashMap<>();

    static {
        DEFAULT_COLOR_SCHEME.put("U", new Color(0xffffff));
        DEFAULT_COLOR_SCHEME.put("BL", new Color(0xffcc00));
        DEFAULT_COLOR_SCHEME.put("BR", new Color(0x0000b3));
        DEFAULT_COLOR_SCHEME.put("R", new Color(0xdd0000));
        DEFAULT_COLOR_SCHEME.put("F", new Color(0x006600));
        DEFAULT_COLOR_SCHEME.put("L", new Color(0x8a1aff));
        DEFAULT_COLOR_SCHEME.put("D", new Color(0x999999));
        DEFAULT_COLOR_SCHEME.put("DR", new Color(0xffffb3));
        DEFAULT_COLOR_SCHEME.put("DBR", new Color(0xff99ff));
        DEFAULT_COLOR_SCHEME.put("B", new Color(0x71e600));
        DEFAULT_COLOR_SCHEME.put("DBL", new Color(0xff8433));
        DEFAULT_COLOR_SCHEME.put("DL", new Color(0x88ddff));
    }

    @Override
    public Map<String, Color> getDefaultColorScheme() {
        return new HashMap<>(DEFAULT_COLOR_SCHEME);
    }

    @Override
    public Dimension getPreferredSize() {
        return getImageSize(gap, minxRad);
    }

    private static Dimension getImageSize(int gap, int minxRad) {
        return new Dimension(getMegaminxViewWidth(gap, minxRad), getMegaminxViewHeight(gap, minxRad));
    }

    private static final double UNFOLDHEIGHT = 2 + 3 * Math.sin(.3 * Math.PI) + Math.sin(.1 * Math.PI);
    private static final double UNFOLDWIDTH = 4 * Math.cos(.1 * Math.PI) + 2 * Math.cos(.3 * Math.PI);

    private static int getMegaminxViewWidth(int gap, int minxRad) {
        return (int)(UNFOLDWIDTH * 2 * minxRad + 3 * gap);
    }
    private static int getMegaminxViewHeight(int gap, int minxRad) {
        return (int)(UNFOLDHEIGHT * minxRad + 2 * gap);
    }

    private static Path pentagon(boolean pointup, int minxRad) {
        double[] angs = { 1.3, 1.7, .1, .5, .9 };
        for(int i = 0; i < angs.length; i++) {
            if(pointup) {
                angs[i] -= .2;
            }
            angs[i] *= Math.PI;
        }
        double[] x = new double[angs.length];
        double[] y = new double[angs.length];
        for(int i = 0; i < x.length; i++) {
            x[i] = minxRad * Math.cos(angs[i]);
            y[i] = minxRad * Math.sin(angs[i]);
        }
        Path p = new Path();
        p.moveTo(x[0], y[0]);
        for(int ch = 1; ch < x.length; ch++) {
            p.lineTo(x[ch], y[ch]);
        }
        p.lineTo(x[0], y[0]); // TODO - this is retarded, why do i need to do this? it would appear that closePath() isn't doing it's job
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

    private static Path getPentagon(double x, double y, boolean up, int minxRad) {
        Path p = pentagon(up, minxRad);
        p.translate(x, y);
        return p;
    }

    double x = minxRad*Math.sqrt(2*(1-Math.cos(.6*Math.PI)));
    double a = minxRad*Math.cos(.1*Math.PI);
    double b = x*Math.cos(.1*Math.PI);
    double c = x*Math.cos(.3*Math.PI);
    double d = x*Math.sin(.1*Math.PI);
    double e = x*Math.sin(.3*Math.PI);

    double leftCenterX = gap + a + b + d/2;
    double leftCenterY = gap + x + minxRad - d;

    double f = Math.cos(.1*Math.PI);
    double gg = Math.cos(.2*Math.PI);
    double magicShiftNumber = d*0.6+minxRad*(f+gg);
    double shift = leftCenterX+magicShiftNumber;

    public Map<MegaminxState.Face, Path> getFaceBoundaries(int gap, int minxRad) {
        Map<MegaminxState.Face, Path> faces = new HashMap<>();
        faces.put(MegaminxState.Face.U,   getPentagon(leftCenterX  , leftCenterY  , true , minxRad));
        faces.put(MegaminxState.Face.BL,  getPentagon(leftCenterX-c, leftCenterY-e, false, minxRad));
        faces.put(MegaminxState.Face.BR,  getPentagon(leftCenterX+c, leftCenterY-e, false, minxRad));
        faces.put(MegaminxState.Face.R,   getPentagon(leftCenterX+b, leftCenterY+d, false, minxRad));
        faces.put(MegaminxState.Face.F,   getPentagon(leftCenterX  , leftCenterY+x, false, minxRad));
        faces.put(MegaminxState.Face.L,   getPentagon(leftCenterX-b, leftCenterY+d, false, minxRad));

        faces.put(MegaminxState.Face.D,   getPentagon(shift+gap+a+b  , gap+x+minxRad  , false, minxRad));
        faces.put(MegaminxState.Face.DR,  getPentagon(shift+gap+a+b-c, gap+x+e+minxRad, true , minxRad));
        faces.put(MegaminxState.Face.DBR, getPentagon(shift+gap+a    , gap+x-d+minxRad, true , minxRad));
        faces.put(MegaminxState.Face.B,   getPentagon(shift+gap+a+b  , gap+minxRad    , true , minxRad));
        faces.put(MegaminxState.Face.DBL, getPentagon(shift+gap+a+2*b, gap+x-d+minxRad, true , minxRad));
        faces.put(MegaminxState.Face.DL,  getPentagon(shift+gap+a+b+c, gap+x+e+minxRad, true , minxRad));
        return faces;
    }
}
