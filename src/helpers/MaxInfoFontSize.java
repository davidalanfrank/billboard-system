package helpers;

import java.awt.*;

/**
 * This class adapts stuffFromTheInternet.GraphicUtilities.getMaxFittingFontSize()
 * from: http://java-sl.com/tip_center_vertically.html
 * to set a font size that will fill a string across lines
 * in a space
 */
public class MaxInfoFontSize {
    public static int getMaxFittingFontSize(Graphics g, Font font, String string, int width, int height, int msgSize) {
        int minSize = 0;
        //max size is provided as the info needs to be smaller than what the message is
        int maxSize = msgSize;

        int curSize = font.getSize();
        //Area of the space to be filled is needed to compare against the space the string will fill
        int areaOfSpace = (width) * (height);

        while (maxSize - minSize > 2) {
            FontMetrics fm = g.getFontMetrics(new Font(font.getName(), font.getStyle(), curSize));
            int stringWidth = fm.stringWidth(string);
            int stringHeight = fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent();
            int areaOfString = stringWidth*stringHeight;
            /* In addition to comparing the height and width of the string against the height and width of the space
             * to fill, the area of the string is being compared to the area of the space to fill. When the area of
             * the string is just less than the area of the space, the point value returned should be roughly enough
             * for the string to fit the bounds of the space with line wrapping. */
            if ((areaOfString > areaOfSpace)) {
                if ((stringWidth > width) || (stringHeight > height)){
                    maxSize = curSize;
                }
                else{
                    minSize = curSize;
                }
            } else {
                minSize = curSize;
            }
            curSize = (maxSize + minSize) / 2;
        }
        return curSize;
    }
}

