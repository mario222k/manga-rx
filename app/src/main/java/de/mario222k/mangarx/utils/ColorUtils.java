package de.mario222k.mangarx.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

import de.mario222k.mangarx.R;

/**
 * Sum of color helping methods.
 * Created by Mario.Sorge on 17/12/15.
 */
public class ColorUtils {

    private static final int[] COLOR_PALETTE = {
            R.color.primary,
            R.color.cobalt,
            R.color.navy,
            R.color.plum,
            R.color.wine_red,
            R.color.fuchsia,
            R.color.charcoal
    };

    @ColorInt
    private static int getColor ( Context context, @ColorRes int coloRes) {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.getColor(coloRes);
        } else {
            return context.getResources().getColor(coloRes);
        }
    }

    @ColorInt
    public static int getColorFromChar ( @NonNull Context context, int character ) {
        if (character <= 57) {
            character -= 48;
        } else {
            character -= 55;
        }

        if (character < 0 || character > 35) {
            return getColor(context, COLOR_PALETTE[6]);
        }

        if (character < 10) {
            return interpolateColor(context, character / 10f, 0, 5);
        }

        return interpolateColor(context, (character - 10) / 25f, 0, 5);
    }

    private static int interpolateColor ( @NonNull Context context, float percent, int paletteStartIndex, int paletteEndIndex ) {
        int paletteItemsCount = paletteEndIndex - paletteStartIndex + 1;
        float palettePosition = Math.min(percent * paletteItemsCount, paletteItemsCount - 1);

        int paletteIndex = (int) palettePosition;
        if (palettePosition - paletteIndex == 0f) {
            paletteIndex += paletteStartIndex;
            return getColor(context, COLOR_PALETTE[(paletteIndex)]);
        }

        paletteIndex += paletteStartIndex;
        float startPercent = paletteIndex / (float) paletteItemsCount;
        float endPercent = (paletteIndex + 1) / (float) paletteItemsCount;

        float p = (percent - startPercent) / (endPercent - startPercent);
        float inv_p = 1f - p;

        int[] startColor = getARGBValues(getColor(context, COLOR_PALETTE[(paletteIndex)]));
        int[] endColor = getARGBValues(getColor(context, COLOR_PALETTE[(paletteIndex + 1)]));

        return getColor(addColor(multiplyValue(startColor, inv_p), multiplyValue(endColor, p)));

    }

    /**
     * Convert a [ALPHA, RED, GREEN, BLUE] channel to integer.
     *
     * @param argb array in order: [ALPHA, RED, GREEN, BLUE]
     * @return color
     */
    @ColorInt
    public static int getColor ( int[] argb ) {
        int c = Color.TRANSPARENT;
        for (int i = argb.length - 1; i >= 0; i--) {
            c += (argb[i] & 0x0ff) << (i * 8);
        }
        return c;
    }

    /**
     * Covert int to [ALPHA, RED, GREEN, BLUE] channels.
     *
     * @param color target Color
     * @return int array in order: [ALPHA, RED, GREEN, BLUE]
     */
    public static int[] getARGBValues ( @ColorInt int color ) {
        int argb[] = {0, 0, 0, 0};
        for (int i = argb.length - 1; i >= 0; i--) {
            argb[i] = (color >> (i * 8)) & 0x0ff;
        }
        return argb;
    }


    /**
     * Will multiply all color channels with a value, example: {@code (255, 100, 0) * 0.5f --> (127, 50, 0)}
     *
     * @param color source color
     * @param value multiply value
     * @return new color
     */
    public static int[] multiplyValue ( int[] color, float value ) {
        if (value == 1f) {
            return color;
        }

        int argb[] = new int[4];
        for (int i = 0; i < 4; i++) {
            argb[i] = Math.max(0, Math.min(255, (int) (color[i] * value)));
        }
        return argb;
    }

    /**
     * Will add value to all color channels, example: {@code (255, 100, 0) + (3, 170, 100) --> (255, 255, 100)}.
     *
     * @param color1 source color
     * @param color2 add color
     * @return new color with max 255 for each channel
     */
    public static int[] addColor ( int[] color1, int[] color2 ) {
        int argb[] = new int[4];
        for (int i = 0; i < 4; i++) {
            argb[i] = Math.max(0, Math.min(255, color1[i] + color2[i]));
        }
        return argb;
    }

}
