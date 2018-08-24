package com.stebtech.zorktouch;

/**
 * Created by alexanderstebner on 24.02.18.
 */

public class buffer {

    static int[] buffer = new int[header.TEXT_BUFFER_SIZE];
    static int bufpos = 0;
    static int prev_c = 0;

    public static void init_buffer(){
        for (int x = 0; x < header.TEXT_BUFFER_SIZE; x++){
            buffer[x] = 0;
        }
    }

    /*
 * new_line
 *
 * High level newline function.
 *
 */
    public static void new_line ()
    {
        MainActivity.OutputText("\n");
    }


    /*
 * print_char
 *
 * High level output function.
 *
 */
    public static void print_char (char c)
    {
        boolean flag = false;

/*        if (message || ostream_memory || enable_buffering) {

            if (!flag) {

	    /* Characters 0 and ZC_RETURN are special cases * /

                if (c == header.ZC_RETURN)
                { new_line (); return; }
                if (c == 0)
                    return;

	    /* Flush the buffer before a whitespace or after a hyphen * /

                if (c == ' ' || c == header.ZC_INDENT || c == header.ZC_GAP || (prev_c == '-' && c != '-'))
                    flush_buffer ();

	    /* Set the flag if this is part one of a style or font change * /

                if (c == header.ZC_NEW_FONT || c == header.ZC_NEW_STYLE)
                    flag = true;

	    /* Remember the current character code * /

                prev_c = c;

            } else flag = false;

	/* Insert the character into the buffer * /

            buffer[bufpos++] = c;

            if (bufpos == header.TEXT_BUFFER_SIZE)
                err.runtime_error (header.ERR_TEXT_BUF_OVF);

        } else */

        MainActivity.OutputText(""+c);

    }
}
