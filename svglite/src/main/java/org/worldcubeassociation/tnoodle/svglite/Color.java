package org.worldcubeassociation.tnoodle.svglite;

public class Color {
    // NxN
    public static final Color WHITE = new Color(255, 255, 255);
    public final static Color YELLOW = new Color(255, 234, 0);
    public static final Color GREEN = new Color(41, 224,  22);
    public static final Color BLUE = new Color(0, 61, 255);
    public static final Color RED = new Color(255, 0, 51);
    public static final Color ORANGE = new Color(255, 126, 0);
    // Megaminx and others
    public static final Color PURPLE = new Color(135, 17, 220);
    public static final Color MIDDLE_GRAY = new Color(134, 134, 134);
    // Megaminx-only
    public static final Color DARK_GREEN = new Color(0, 118, 0);
    public static final Color PINK = new Color(255, 0, 184);
    public static final Color SKY_BLUE = new Color(0, 206, 255);
    public static final Color BEIGE = new Color(232, 206, 125);
    // Square-1
    public static final Color DARK_GRAY = new Color(75, 75, 75);

    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color GRAY = new Color(128, 128, 128);

    private int r, g, b, a;
    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int rgba) {
        this((rgba >>> 8*2) & 0xff,
             (rgba >>> 8) & 0xff,
             rgba & 0xff,
             (rgba >>> 8*3) & 0xff);
    }

    private static int hexToRGB(String htmlHex) throws InvalidHexColorException {
        if(htmlHex.startsWith("#")) {
            htmlHex = htmlHex.substring(1);
        }

        switch(htmlHex.length()) {
            case 3:
                char c0 = htmlHex.charAt(0);
                char c1 = htmlHex.charAt(1);
                char c2 = htmlHex.charAt(2);
                htmlHex = "" + c0 + c0 + c1 + c1 + c2 + c2;
            case 6:
                return Integer.parseInt(htmlHex, 16);
            default:
                throw new InvalidHexColorException(htmlHex);
        }
    }

    public Color(String htmlHex) throws InvalidHexColorException {
        this(hexToRGB(htmlHex));
    }

    public Color invertColor() {
        return new Color(255 - r, 255 - g, 255 - b);
    }

    public String toHex() {
        return Integer.toHexString(0x1000000 | (getRGB() & 0xffffff)).substring(1);
    }

    public int getRed() {
        return r;
    }

    public int getGreen() {
        return g;
    }

    public int getBlue() {
        return b;
    }

    public int getRGB() {
        return (a << 8*3) | (r << 8*2) | (g << 8) | b;
    }

    public String toString() {
        return "<color #" + toHex() + ">";
    }

}
