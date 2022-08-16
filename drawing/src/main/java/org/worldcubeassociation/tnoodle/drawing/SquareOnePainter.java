package org.worldcubeassociation.tnoodle.drawing;

import org.worldcubeassociation.tnoodle.PuzzleSvgPainter;
import org.worldcubeassociation.tnoodle.state.SquareOneState;
import org.worldcubeassociation.tnoodle.svglite.*;
import org.worldcubeassociation.tnoodle.util.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

public class SquareOnePainter extends PuzzleSvgPainter<SquareOneState> {
    private static final int radius = 32;

    @Override
    protected void drawScramble(Svg canvas, SquareOneState state, Map<String, Color> colorScheme) {
        canvas.setStroke(2, 10, "round");

        String faces = "LBRFUD";
        Color[] colorSchemeArray = new Color[faces.length()];
        for(int i = 0; i < colorSchemeArray.length; i++) {
            colorSchemeArray[i] = colorScheme.get(faces.charAt(i)+"");
        }
        Dimension dim = getImageSize(radius);
        int width = dim.width;
        int height = dim.height;

        double half_square_width = (radius * RADIUS_MULTIPLIER * multiplier) / Math.sqrt(2);
        double edge_width = 2 * radius * multiplier * Math.sin(Math.toRadians(15));
        double corner_width = half_square_width - edge_width / 2.;
        Rectangle left_mid = new Rectangle(width / 2. - half_square_width, height / 2. - radius * (multiplier - 1) / 2., corner_width, radius * (multiplier - 1));
        left_mid.setFill(colorSchemeArray[3]); //front
        Rectangle right_mid;
        if(state.sliceSolved) {
            right_mid = new Rectangle(width / 2. - half_square_width, height / 2. - radius * (multiplier - 1) / 2., 2*corner_width + edge_width, radius * (multiplier - 1));
            right_mid.setFill(colorSchemeArray[3]); //front
        } else {
            right_mid = new Rectangle(width / 2. - half_square_width, height / 2. - radius * (multiplier - 1) / 2., corner_width + edge_width, radius * (multiplier - 1));
            right_mid.setFill(colorSchemeArray[1]); //back
        }
        canvas.appendChild(right_mid);
        canvas.appendChild(left_mid); //this will clobber part of the other guy

        right_mid = new Rectangle(right_mid);
        right_mid.setStroke(Color.BLACK);
        right_mid.setFill(null);
        left_mid = new Rectangle(left_mid);
        left_mid.setStroke(Color.BLACK);
        left_mid.setFill(null);

        canvas.appendChild(right_mid);
        canvas.appendChild(left_mid);

        Transform transform;
        double x = width / 2.0;
        double y = height / 4.0;
        transform = Transform.getRotateInstance(
            Math.toRadians(90 + 15), x, y);
        drawFace(canvas, transform, state.pieces, x, y, radius, colorSchemeArray);

        y *= 3.0;
        transform = Transform.getRotateInstance(
            Math.toRadians(-90 - 15), x, y);
        drawFace(canvas, transform, ArrayUtils.copyOfRange(state.pieces, 12, state.pieces.length), x, y, radius, colorSchemeArray);
    }

    private void drawFace(Svg g, Transform transform, int[] face, double x, double y, int radius, Color[] colorScheme) {
        for(int ch = 0; ch < 12; ch++) {
            if(ch < 11 && face[ch] == face[ch+1]) {
                ch++;
            }
            drawPiece(g, transform, face[ch], x, y, radius, colorScheme);
        }
    }

    private int drawPiece(Svg g, Transform transform, int piece, double x, double y, int radius, Color[] colorScheme) {
        boolean corner = isCornerPiece(piece);
        int degree = 30 * (corner ? 2 : 1);
        Path[] p = corner ? getCornerPoly(x, y, radius) : getWedgePoly(x, y, radius);

        Color[] cls = getPieceColors(piece, colorScheme);
        for(int ch = cls.length - 1; ch >= 0; ch--) {
            p[ch].setFill(cls[ch]);
            p[ch].setStroke(Color.BLACK);
            p[ch].setTransform(transform);
            g.appendChild(p[ch]);
        }
        transform.rotate(Math.toRadians(degree), x, y);
        return degree;
    }

    private boolean isCornerPiece(int piece) {
        return ((piece + (piece <= 7 ? 0 : 1)) % 2) == 0;
    }

    private Color[] getPieceColors(int piece, Color[] colorScheme) {
        boolean up = piece <= 7;
        Color top = up ? colorScheme[4] : colorScheme[5];
        if(isCornerPiece(piece)) { //corner piece
            if(!up) {
                piece = 15 - piece;
            }
            Color a = colorScheme[(piece/2+3) % 4];
            Color b = colorScheme[piece/2];
            if(!up) { //mirror for bottom
                Color t = a;
                a = b;
                b = t;
            }
            return new Color[] { top, a, b }; //ordered counter-clockwise
        } else { //wedge piece
            if(!up) {
                piece = 14 - piece;
            }
            return new Color[] { top, colorScheme[piece/2] };
        }
    }

    private Path[] getWedgePoly(double x, double y, int radius) {
        Path p = new Path();
        p.moveTo(0, 0);
        p.lineTo(radius, 0);
        double tempx = Math.sqrt(3) * radius / 2.0;
        double tempy = radius / 2.0;
        p.lineTo(tempx, tempy);
        p.closePath();
        p.translate(x, y);

        Path side = new Path();
        side.moveTo(radius, 0);
        side.lineTo(multiplier * radius, 0);
        side.lineTo(multiplier * tempx, multiplier * tempy);
        side.lineTo(tempx, tempy);
        side.closePath();
        side.translate(x, y);
        return new Path[]{ p, side };
    }
    private Path[] getCornerPoly(double x, double y, int radius) {
        Path p = new Path();
        p.moveTo(0, 0);
        p.lineTo(radius, 0);
        double tempx = radius*(1 + Math.cos(Math.toRadians(75))/Math.sqrt(2));
        double tempy = radius*Math.sin(Math.toRadians(75))/Math.sqrt(2);
        p.lineTo(tempx, tempy);
        double tempX = radius / 2.0;
        double tempY = Math.sqrt(3) * radius / 2.0;
        p.lineTo(tempX, tempY);
        p.closePath();
        p.translate(x, y);

        Path side1 = new Path();
        side1.moveTo(radius, 0);
        side1.lineTo(multiplier * radius, 0);
        side1.lineTo(multiplier * tempx, multiplier * tempy);
        side1.lineTo(tempx, tempy);
        side1.closePath();
        side1.translate(x, y);

        Path side2 = new Path();
        side2.moveTo(multiplier * tempx, multiplier * tempy);
        side2.lineTo(tempx, tempy);
        side2.lineTo(tempX, tempY);
        side2.lineTo(multiplier * tempX, multiplier * tempY);
        side2.closePath();
        side2.translate(x, y);
        return new Path[]{ p, side1, side2 };
    }

    private static final Map<String, Color> DEFAULT_COLOR_SCHEME = new HashMap<>();

    static {
        DEFAULT_COLOR_SCHEME.put("B", new Color(255, 128, 0)); //orange heraldic tincture
        DEFAULT_COLOR_SCHEME.put("D", Color.WHITE);
        DEFAULT_COLOR_SCHEME.put("F", Color.RED);
        DEFAULT_COLOR_SCHEME.put("L", Color.BLUE);
        DEFAULT_COLOR_SCHEME.put("R", Color.GREEN);
        DEFAULT_COLOR_SCHEME.put("U", Color.YELLOW);
    }

    @Override
    public Map<String, Color> getDefaultColorScheme() {
        return new HashMap<>(DEFAULT_COLOR_SCHEME);
    }

    @Override
    public Dimension getPreferredSize() {
        return getImageSize(radius);
    }

    private static Dimension getImageSize(int radius) {
        return new Dimension(getWidth(radius), getHeight(radius));
    }
    private static final double RADIUS_MULTIPLIER = Math.sqrt(2) * Math.cos(Math.toRadians(15));
    private static final double multiplier = 1.4;
    private static int getWidth(int radius) {
        return (int) (2 * RADIUS_MULTIPLIER * multiplier * radius);
    }
    private static int getHeight(int radius) {
        return (int) (4 * RADIUS_MULTIPLIER * multiplier * radius);
    }
}
