package org.worldcubeassociation.tnoodle.scrambles;

import org.timepedia.exporter.client.Export;
import org.worldcubeassociation.tnoodle.svglite.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.Math.ceil;

public abstract class PuzzleSvgPainter<PS extends PuzzleState<PS>> {
    public Puzzle<PS> puzzle; // TODO GB

    /**
     * Draws the state of the puzzle.
     * NOTE: It is assumed that this method is thread safe! That means unless you know what you're doing,
     * use the synchronized keyword when implementing this method:<br>
     * <code>protected synchronized void drawScramble();</code>
     *
     * @param colorScheme The color scheme to use while drawing
     * @return An Svg instance representing this scramble.
     */
    protected abstract void drawScramble(Svg canvas, PS state, Map<String, Color> colorScheme);

    /**
     * Draws scramble as an Svg.
     * @param scramble The scramble to validate and apply to the puzzle. NOTE: May be null.
     * @param colorScheme A HashMap mapping face names to Colors.
     *          Any missing entries will be merged with the defaults from getDefaultColorScheme().
     *          If null, just the defaults are used.
     * @return An SVG object representing the drawn scramble.
     * @throws InvalidScrambleException If scramble is invalid.
     */
    public Svg drawScramble(String scramble, Map<String, Color> colorScheme) throws InvalidScrambleException {
        if(scramble == null) {
            scramble = "";
        }

        PS state = puzzle.getSolvedState();
        state = state.applyAlgorithm(scramble);
        Svg svg = new Svg(getPreferredSize());

        drawScramble(svg, state, colorScheme);

        // This is a hack I don't fully understand that prevents aliasing of
        // vertical and horizontal lines.
        // See http://stackoverflow.com/questions/7589650/drawing-grid-with-jquery-svg-produces-2px-lines-instead-of-1px
        Group g = new Group();
        List<Element> children = svg.getChildren();
        while(!children.isEmpty()) {
            g.appendChild(children.remove(0));
        }
        g.translate(0.5, 0.5);
        svg.appendChild(g);
        return svg;
    }

    /**
     * @return A *new* HashMap mapping face names to Colors.
     */
    public abstract Map<String, Color> getDefaultColorScheme();

    public abstract Dimension getPreferredSize();

    /**
     * Computes the best size to draw the scramble image.
     * @param maxWidth The maximum allowed width of the resulting image, 0 if it doesn't matter.
     * @param maxHeight The maximum allowed height of the resulting image, 0 if it doesn't matter.
     * @return The best size of the resulting image, constrained to maxWidth and maxHeight.
     */
    @Export
    public Dimension getPreferredSize(int maxWidth, int maxHeight) {
        if(maxWidth == 0 && maxHeight == 0) {
            return getPreferredSize();
        }
        if(maxWidth == 0) {
            maxWidth = Integer.MAX_VALUE;
        } else if(maxHeight == 0) {
            maxHeight = Integer.MAX_VALUE;
        }
        double ratio = 1.0 * getPreferredSize().width / getPreferredSize().height;
        int resultWidth = (int) Math.min(maxWidth, ceil(maxHeight*ratio));
        int resultHeight = (int) Math.min(maxHeight, ceil(maxWidth/ratio));
        return new Dimension(resultWidth, resultHeight);
    }
}
