package helpers;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * This GUI class is used for the  transformation of the fonts,
 * colour and ratio so that they are formatted correctly in the Viewer
 */
public class gui {

    /**
     * Calculates the ratio to scale by in an Affine Transformation to maintain aspect ratio
     * @param initialH initial height of image (pixels)
     * @param initialW initial width of image (pixels)
     * @param maxH height of area image must fill (pixels)
     * @param maxW width or area image must fill (pixels)
     * @return ratio, a double that is the ratio to use for sx, sy in an affine transformation
     */
    public static double calculateTransformationRatio(double initialH, double initialW, double maxH, double maxW){
        double ratio;

        ratio = Math.min(maxW/initialW, maxH/initialH);

        return ratio;
    }

    /**
     * Class that formats the colour into a hex
     * @param c colour that is formatted into a hex
     * @return string of the hex
     */
    public static String formatColorAsHex(Color c){
        StringBuilder hexString = new StringBuilder(Integer.toHexString(c.getRGB() & 0xffffff));
        while(hexString.length() < 6){
            hexString.insert(0, "0");
        }
        hexString.insert(0, "#");
        return hexString.toString();
    }
    /**
     * Method takes text and dimensions of an area to fit it in, returns a font to use that will make the text fit the
     * area.
     * !!Can fit 302 characters on one line before being cut off on a 16:9, 1920*1080 screen!!
     * @param text text to scale
     * @param availableWidth pixels of width
     * @param availableHeight pixels of height
     * @return a Font object to apply to a JLabel.
     */
    @Deprecated
    public static Font deriveScaledFont(String text, double availableWidth, double availableHeight, Font font){
        Font scaledFont;
        BufferedImage image = new BufferedImage((int) availableWidth,(int) availableHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

        Rectangle2D rectangle = graphics.getFontMetrics(font).getStringBounds(text, graphics);
        //calculating the ratio to scale by to fit available area
        double ratio = calculateTransformationRatio(rectangle.getHeight(), rectangle.getWidth(), availableHeight, availableWidth * 0.93);
        //deriving scaled font based on ratio via an affine transformation
        scaledFont = font.deriveFont(AffineTransform.getScaleInstance(ratio, ratio));
        graphics.setFont(scaledFont);

        return scaledFont;
    }

    /**
     * * A convenience method to add a component to given grid bag
     * * layout locations. Code due to Cay Horstmann
     *
     * @param c the    component to   add* @param constraints the    grid bag    constraints to   use
     * @param x the    x grid position * @param y the    y grid position
     * @param w the    grid width of   the    component
     * @param h the    grid height   of   the    component
     */
    public static void addToPanel(JPanel jp, Component c, GridBagConstraints constraints, int x, int y,
                            int w, int h) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        jp.add(c, constraints);
    }

}
