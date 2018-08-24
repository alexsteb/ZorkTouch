package com.stebtech.zorktouch;

/**
 * Created by alexanderstebner on 26.02.18.
 */

public class screen {

    public static Zwindow[] wp = new Zwindow[8];
    static int cwp = 0;


    /*
 * z_set_text_style, set the style for text output.
 *
 * 	zargs[0] = style flags to set or 0 to reset text style
 *
 */
    public static void z_set_text_style ()
    {
        int win = (header.h_version == 6) ? header.cwin : 0;
        int style = header.zargs[0];

        wp[win].style |= style;

        if (style == 0)
            wp[win].style = 0;

        refresh_text_style ();

    }

    /*
 * refresh_text_style
 *
 * Set the right text style. This can be necessary when the fixed font
 * flag is changed, or when a new window is selected, or when the game
 * uses the set_text_style opcode.
 *
 */
    public static void refresh_text_style ()
    {
        int style;

        if (header.h_version != 6) {

            style = wp[0].style;

            //if (header.cwin != 0 || (header.h_flags & header.FIXED_FONT_FLAG) != 0)
            //    style |= FIXED_WIDTH_STYLE;

        } else style = wp[cwp].style;

         //os_set_text_style (style);

    }


    /*
 * split_window
 *
 * Divide the screen into upper (1) and lower (0) windows. In V3 the upper
 * window appears below the status line.
 *
 */
    public static void split_window (int height)
    {
        int stat_height = 0;

        //flush_buffer ();

    /* Calculate height of status line and upper window */

        if (header.h_version != 6)
            height *= header.hi (wp[1].font_size);

        if (header.h_version <= 3)
            stat_height = header.hi (wp[7].font_size);

    /* Cursor of upper window mustn't be swallowed by the lower window */

        wp[1].y_cursor += wp[1].y_pos - 1 - stat_height;

        wp[1].y_pos = 1 + stat_height;
        wp[1].y_size = height;

        //if ((short) wp[1].y_cursor > (short) wp[1].y_size)
         //   reset_cursor (1);

    /* Cursor of lower window mustn't be swallowed by the upper window */

        wp[0].y_cursor += wp[0].y_pos - 1 - stat_height - height;

        wp[0].y_pos = 1 + stat_height + height;
        wp[0].y_size = header.h_screen_height - stat_height - height;

       // if ((short) wp[0].y_cursor < 1)
        //    reset_cursor (0);

    /* Erase the upper window in V3 only */

        if (header.h_version == 3 && height != 0)
            erase_window (1);

    }

    /*
 * z_split_window, split the screen into an upper (1) and lower (0) window.
 *
 *	zargs[0] = height of upper window in screen units (V6) or #lines
 *
 */
    public static void z_split_window ()
    {
        split_window (header.zargs[0]);

    }

    /*
     * erase_screen
     *
     * Erase the entire screen to background colour.
     *
     */
    public static void erase_screen (int win)
    {
        int i;

        //os_erase_area (1, 1, h_screen_height, h_screen_width, -2);

        if ((short) win == -1) {
            split_window (0);
            set_window (0);
        }

        for (i = 0; i < 8; i++)
            wp[i].line_count = 0;

    }

    /*
 * erase_window
 *
 * Erase a window to background colour.
 *
 */
    public static void erase_window (int win)
    {
        int y = wp[win].y_pos;
        int x = wp[win].x_pos;

        //if (header.h_version == 6 && win != cwin)
            //os_set_colour (lo (wp[win].colour), hi (wp[win].colour));

        /*os_erase_area (y,
                x,
                y + wp[win].y_size - 1,
                x + wp[win].x_size - 1,
                win);*/

        //if (header.h_version == 6 && win != cwin)
        //    os_set_colour (lo (cwp->colour), hi (cwp->colour));

        //reset_cursor (win);

        wp[win].line_count = 0;

    }


    /*
 * z_set_window, select the current window.
 *
 *	zargs[0] = window to be selected (-3 is the current one)
 *
 */
    public static void z_set_window ()
    {
        set_window (header.zargs[0]);

    }

    /*
 * set_window
 *
 * Set the current window. In V6 every window has its own set of window
 * properties such as colours, text style, cursor position and size.
 *
 */
    static void set_window (int win)
    {
        //flush_buffer ();

        //cwin = win;
        cwp = win;

        //update_attributes ();

        if (header.h_version == 6) {

            //os_set_colour (header.lo (wp[cwp].colour), header.hi (wp[cwp].colour));

            //if (os_font_data (cwp->font, &font_height, &font_width))
            //os_set_font (cwp->font);

            //os_set_text_style (cwp->style);

        } else refresh_text_style ();

        if (header.h_version != 6 && win != 0) {
            wp[win].y_cursor = 1;
            wp[win].x_cursor = 1;
        }

        //update_cursor ();

    }

    /*
 * z_set_cursor, set the cursor position or turn the cursor on/off.
 *
 *	zargs[0] = y-coordinate or -2/-1 for cursor on/off
 *	zargs[1] = x-coordinate
 *	zargs[2] = window (-3 is the current one, optional)
 *
 */
    public static void z_set_cursor ()
    {
        /*
        int win = (header.h_version == 6) ? winarg2 () : 1;

        zword y = zargs[0];
        zword x = zargs[1];

        flush_buffer ();

    /* Supply default arguments * /

        if (zargc < 3)
            zargs[2] = -3;

    /* Handle cursor on/off * /

        if ((short) y < 0) {

            if ((short) y == -2)
                cursor = TRUE;
            if ((short) y == -1)
                cursor = FALSE;

            return;

        }

    /* Convert grid positions to screen units if this is not V6 * /

        if (h_version != V6) {

            if (cwin == 0)
                return;

            y = (y - 1) * h_font_height + 1;
            x = (x - 1) * h_font_width + 1;

        }

    /* Protect the margins * /

        if (y == 0)			/* use cursor line if y-coordinate is 0 * /
            y = wp[win].y_cursor;
        if (x == 0)			/* use cursor column if x-coordinate is 0 * /
            x = wp[win].x_cursor;
        if (x <= wp[win].left || x > wp[win].x_size - wp[win].right)
            x = wp[win].left + 1;

    /* Move the cursor * /

        wp[win].y_cursor = y;
        wp[win].x_cursor = x;

        if (win == cwin)
            update_cursor ();
*/
    }

}
