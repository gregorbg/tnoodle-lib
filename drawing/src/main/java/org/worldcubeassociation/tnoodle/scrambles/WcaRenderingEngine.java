package org.worldcubeassociation.tnoodle.scrambles;

import org.worldcubeassociation.tnoodle.*;
import org.worldcubeassociation.tnoodle.drawing.*;
import org.worldcubeassociation.tnoodle.state.*;

public final class WcaRenderingEngine<PS extends PuzzleState> {
    public static final WcaRenderingEngine<CubeState> TWO = new WcaRenderingEngine<>(CubePainter.class, 2);
    public static final WcaRenderingEngine<CubeState> THREE = new WcaRenderingEngine<>(CubePainter.class, 3);
    public static final WcaRenderingEngine<CubeState> FOUR = new WcaRenderingEngine<>(CubePainter.class, 4);
    public static final WcaRenderingEngine<CubeState> FIVE = new WcaRenderingEngine<>(CubePainter.class, 5);
    public static final WcaRenderingEngine<CubeState> SIX = new WcaRenderingEngine<>(CubePainter.class, 6);
    public static final WcaRenderingEngine<CubeState> SEVEN = new WcaRenderingEngine<>(CubePainter.class, 7);
    public static final WcaRenderingEngine<PyraminxState> PYRA = new WcaRenderingEngine<>(PyraminxPainter.class);
    public static final WcaRenderingEngine<SquareOneState> SQ1 = new WcaRenderingEngine<>(SquareOnePainter.class);
    public static final WcaRenderingEngine<MegaminxState> MEGA = new WcaRenderingEngine<>(MegaminxPainter.class);
    public static final WcaRenderingEngine<ClockState> CLOCK = new WcaRenderingEngine<>(ClockPainter.class);
    public static final WcaRenderingEngine<SkewbState> SKEWB = new WcaRenderingEngine<>(SkewbPainter.class);

    private final LazySupplier<? extends PuzzleSvgPainter<PS>> puzzleSupplier;

    <T extends PuzzleSvgPainter<PS>> WcaRenderingEngine(Class<T> suppliyingClass, Object... ctorArgs) {
        this.puzzleSupplier = new LazySupplier<>(suppliyingClass, ctorArgs);
    }

    public PuzzleSvgPainter<PS> getPuzzlePainter() {
        return this.puzzleSupplier.getInstance();
    }

    public static WcaRenderingEngine<? extends PuzzleState> getForEvent(WcaEvent event) {
        switch (event) {
            case TWO:
                return WcaRenderingEngine.TWO;
            case THREE:
            case THREE_FM:
            case THREE_BLD:
                return WcaRenderingEngine.THREE;
            case FOUR:
            case FOUR_BLD:
                return WcaRenderingEngine.FOUR;
            case FIVE:
            case FIVE_BLD:
                return WcaRenderingEngine.FIVE;
            case SIX:
                return WcaRenderingEngine.SIX;
            case SEVEN:
                return WcaRenderingEngine.SEVEN;
            case PYRA:
                return WcaRenderingEngine.PYRA;
            case SQ1:
                return WcaRenderingEngine.SQ1;
            case MEGA:
                return WcaRenderingEngine.MEGA;
            case CLOCK:
                return WcaRenderingEngine.CLOCK;
            case SKEWB:
                return WcaRenderingEngine.SKEWB;
        }

        // somehow the Java compiler doesn't detect exhaustive switch :(
        return null;
    }
}
