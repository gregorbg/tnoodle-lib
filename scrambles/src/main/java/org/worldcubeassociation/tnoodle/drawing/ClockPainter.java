package org.worldcubeassociation.tnoodle.drawing;

import org.worldcubeassociation.tnoodle.scrambles.PuzzleSvgPainter;
import org.worldcubeassociation.tnoodle.state.ClockState;
import org.worldcubeassociation.tnoodle.svglite.*;

import java.util.HashMap;
import java.util.Map;

public class ClockPainter extends PuzzleSvgPainter<ClockState> {
    private static final int STROKE_WIDTH = 2;
    private static final int radius = 70;
    private static final int clockRadius = 14;
    private static final int clockOuterRadius = 20;
    private static final int pointRadius = (clockRadius + clockOuterRadius) / 2;
    private static final int tickMarkRadius = 1;
    private static final int arrowHeight = 10;
    private static final int arrowRadius = 2;
    private static final int pinRadius = 4;
    private static final int pinUpOffset = 6;
    private static final double arrowAngle = Math.PI / 2 - Math.acos( (double)arrowRadius / (double)arrowHeight );

    private static final int gap = 5;

    @Override
    public void drawScramble(Svg svg, ClockState state, Map<String, Color> colorScheme) {
        svg.setStroke(STROKE_WIDTH, 10, "round");
        drawBackground(svg, state.rightSideUp, colorScheme);

        for(int i = 0; i < 18; i++) {
            drawClock(svg, i, state.posit[i], colorScheme);
        }

        drawPins(svg, state.pins, colorScheme);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(4*(radius+gap), 2*(radius+gap));
    }

    private static final Map<String, Color> DEFAULT_COLOR_SCHEME = new HashMap<>();

    static {
        DEFAULT_COLOR_SCHEME.put("Front", new Color(0x3375b2));
        DEFAULT_COLOR_SCHEME.put("Back", new Color(0x55ccff));
        DEFAULT_COLOR_SCHEME.put("FrontClock", new Color(0x55ccff));
        DEFAULT_COLOR_SCHEME.put("BackClock", new Color(0x3375b2));
        DEFAULT_COLOR_SCHEME.put("Hand", Color.YELLOW);
        DEFAULT_COLOR_SCHEME.put("HandBorder", Color.RED);
        DEFAULT_COLOR_SCHEME.put("PinUp", Color.YELLOW);
        DEFAULT_COLOR_SCHEME.put("PinDown", new Color(0x885500));
    }

    @Override
    public Map<String, Color> getDefaultColorScheme() {
        return new HashMap<>(DEFAULT_COLOR_SCHEME);
    }

    protected void drawBackground(Svg g, boolean rightSideUp, Map<String, Color> colorScheme) {
        String[] colorString;
        if(rightSideUp) {
            colorString = new String[]{"Front", "Back"};
        } else {
            colorString = new String[]{"Back", "Front"};
        }

        for(int s = 0; s < 2; s++) {
            Transform t = Transform.getTranslateInstance((s*2+1)*(radius + gap), radius + gap);

            // Draw puzzle
            for(int centerX : new int[] { -2*clockOuterRadius, 2*clockOuterRadius }) {
                for(int centerY : new int[] { -2*clockOuterRadius, 2*clockOuterRadius }) {
                    Circle c = new Circle(centerX, centerY, clockOuterRadius);
                    c.setTransform(t);
                    c.setStroke(Color.BLACK);
                    g.appendChild(c);
                }
            }

            Circle outerCircle = new Circle(0, 0, radius);
            outerCircle.setTransform(t);
            outerCircle.setStroke(Color.BLACK);
            outerCircle.setFill(colorScheme.get(colorString[s]));
            g.appendChild(outerCircle);

            for(int centerX : new int[] { -2*clockOuterRadius, 2*clockOuterRadius }) {
                for(int centerY : new int[] { -2*clockOuterRadius, 2*clockOuterRadius }) {
                    // We don't want to clobber part of our nice
                    // thick outer border.
                    int innerClockOuterRadius = clockOuterRadius - STROKE_WIDTH/2;
                    Circle c = new Circle(centerX, centerY, innerClockOuterRadius);
                    c.setTransform(t);
                    c.setFill(colorScheme.get(colorString[s]));
                    g.appendChild(c);
                }
            }

            // Draw clocks
            for(int i = -1; i <= 1; i++) {
                for(int j = -1; j <= 1; j++) {
                    Transform tCopy = new Transform(t);
                    tCopy.translate(2*i*clockOuterRadius, 2*j*clockOuterRadius);

                    Circle clockFace = new Circle(0, 0, clockRadius);
                    clockFace.setStroke(Color.BLACK);
                    clockFace.setFill(colorScheme.get(colorString[s]+ "Clock"));
                    clockFace.setTransform(tCopy);
                    g.appendChild(clockFace);

                    for(int k = 0; k < 12; k++) {
                        Circle tickMark = new Circle(0, -pointRadius, tickMarkRadius);
                        tickMark.setFill(colorScheme.get(colorString[s] + "Clock"));
                        tickMark.rotate(Math.toRadians(30*k));
                        tickMark.transform(tCopy);
                        g.appendChild(tickMark);
                    }

                }
            }
        }
    }

    protected void drawClock(Svg g, int clock, int position, Map<String, Color> colorScheme) {
        Transform t = new Transform();
        t.rotate(Math.toRadians(position*30));
        int deltaX, deltaY;
        if(clock < 9) {
            deltaX = radius + gap;
            deltaY = radius + gap;
            t.translate(deltaX, deltaY);
        } else {
            deltaX = 3*(radius + gap);
            deltaY = radius + gap;
            t.translate(deltaX, deltaY);
            clock -= 9;
        }

        deltaX = 2*((clock%3) - 1)*clockOuterRadius;
        deltaY = 2*((clock/3) - 1)*clockOuterRadius;
        t.translate(deltaX, deltaY);

        Path arrow = new Path();
        arrow.moveTo(0, 0);
        arrow.lineTo(arrowRadius*Math.cos(arrowAngle), -arrowRadius*Math.sin(arrowAngle));
        arrow.lineTo(0, -arrowHeight);
        arrow.lineTo(-arrowRadius*Math.cos( arrowAngle ), -arrowRadius*Math.sin(arrowAngle));
        arrow.closePath();
        arrow.setStroke(colorScheme.get("HandBorder"));
        arrow.setTransform(t);
        g.appendChild(arrow);

        Circle handBase = new Circle(0, 0, arrowRadius);
        handBase.setStroke(colorScheme.get("HandBorder"));
        handBase.setTransform(t);
        g.appendChild(handBase);

        arrow = new Path(arrow);
        arrow.setFill(colorScheme.get("Hand"));
        arrow.setStroke(null);
        arrow.setTransform(t);
        g.appendChild(arrow);

        handBase = new Circle(handBase);
        handBase.setFill(colorScheme.get("Hand"));
        handBase.setStroke(null);
        handBase.setTransform(t);
        g.appendChild(handBase);
    }

    protected void drawPins(Svg g, boolean[] pins, Map<String, Color> colorScheme) {
        Transform t = new Transform();
        t.translate(radius + gap, radius + gap);
        int k = 0;
        for(int i = -1; i <= 1; i += 2) {
            for(int j = -1; j <= 1; j += 2) {
                Transform tt = new Transform(t);
                tt.translate(j*clockOuterRadius, i*clockOuterRadius);
                drawPin(g, tt, pins[k++], colorScheme);
            }
        }

        t.translate(2*(radius + gap), 0);
        k = 1;
        for(int i = -1; i <= 1; i += 2) {
            for(int j = -1; j <= 1; j += 2) {
                Transform tt = new Transform(t);
                tt.translate(j*clockOuterRadius, i*clockOuterRadius);
                drawPin(g, tt, !pins[k--], colorScheme);
            }
            k = 3;
        }
    }

    protected void drawPin(Svg g, Transform t, boolean pinUp, Map<String, Color> colorScheme) {
        Circle pin = new Circle(0, 0, pinRadius);
        pin.setTransform(t);
        pin.setStroke(Color.BLACK);
        pin.setFill(colorScheme.get( pinUp ? "PinUp" : "PinDown" ));
        g.appendChild(pin);

        // there have been problems in the past with clock pin states being "inverted",
        // see https://github.com/thewca/tnoodle/issues/423 for details.
        if (pinUp) {
            Transform bodyTransform = new Transform(t);
            // pin circle transform relates to the circle *center*. Since it is two
            // radii wide, we only move *one* radius to the right.
            bodyTransform.translate(-pinRadius, -pinUpOffset);

            Rectangle cylinderBody = new Rectangle(0, 0, 2 * pinRadius, pinUpOffset);
            cylinderBody.setTransform(bodyTransform);
            cylinderBody.setStroke(null);
            cylinderBody.setFill(colorScheme.get( "PinUp" ));
            g.appendChild(cylinderBody);

            // We are NOT using the rectangle stroke, because those border strokes would cross through
            // the bottom circle (ie cylinder "foot"). Drawing paths left and right is less cumbersome
            // than drawing a stroked rectangle and overlaying it yet again with a stroke-less circle
            Path cylinderWalls = new Path();

            // left border
            cylinderWalls.moveTo(0, 0);
            cylinderWalls.lineTo(0, pinUpOffset);

            // right border
            cylinderWalls.moveTo(2 * pinRadius, 0);
            cylinderWalls.lineTo(2 * pinRadius, pinUpOffset);

            cylinderWalls.closePath();
            cylinderWalls.setStroke(Color.BLACK);
            cylinderWalls.setTransform(bodyTransform);
            g.appendChild(cylinderWalls);

            // Cylinder top "lid". Basically just a second pin circle
            // that is lifted `pinRadius` pixels high.
            Transform headTransform = new Transform(t);
            headTransform.translate(0, -pinUpOffset);

            Circle cylinderHead = new Circle(0, 0, pinRadius);
            cylinderHead.setTransform(headTransform);
            cylinderHead.setStroke(Color.BLACK);
            cylinderHead.setFill(colorScheme.get( "PinUp" ));
            g.appendChild(cylinderHead);
        }
    }
}
