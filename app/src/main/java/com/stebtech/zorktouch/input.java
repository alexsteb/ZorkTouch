package com.stebtech.zorktouch;

import static com.stebtech.zorktouch.header.ZC_MENU_CLICK;

/**
 * Created by alexanderstebner on 26.02.18.
 */

public class input {
    /*
 * is_terminator
 *
 * Check if the given key is an input terminator.
 *
 */

    public static boolean is_terminator (int key)
    {

        if (key == header.ZC_TIME_OUT)
            return true;
        if (key == header.ZC_RETURN)
            return true;
        if (key >= header.ZC_HKEY_MIN && key <= header.ZC_HKEY_MAX)
            return true;

        if (header.h_terminating_keys != 0)

            if (key >= header.ZC_ARROW_MIN && key <= header.ZC_MENU_CLICK) {

                int addr = header.h_terminating_keys;
                int c;

                do {
                    c = header.LOW_BYTE (addr);
                    if (c == 255 || key == text.translate_from_zscii (c))
                        return true;
                    addr++;
                } while (c != 0);

            }

        return false;

    }/* is_terminator */

/*
 * z_make_menu, add or remove a menu and branch if successful.
 *
 * 	zargs[0] = number of menu
 *	zargs[1] = table of menu entries or 0 to remove menu
 *
 */

    public static void z_make_menu ()
    {

    /* This opcode was only used for the Macintosh version of Journey.
       It controls menus with numbers greater than 2 (menus 0, 1 and 2
       are system menus). Frotz doesn't implement menus yet. */

        process.branch (false);

    }/* z_make_menu */

/*
 * read_yes_or_no
 *
 * Ask the user a question; return true if the answer is yes.
 *
 */

    public static boolean read_yes_or_no (String s)
    {
        char key;

        text.print_string (s);
        text.print_string ("? (y/n) >");

        key = MainActivity.get_input_text().toCharArray()[0];//stream_read_key (0, 0, false);

        if (key == 'y' || key == 'Y') {
            text.print_string ("y\n");
            return true;
        } else {
            text.print_string ("n\n");
            return false;
        }

    }/* read_yes_or_no */

/*
 * read_string
 *
 * Read a string from the current input stream.
 *
 */

    public static void read_string (int max, char[] buffer)
    {

        buffer[0] = 0;
        char[] charArr = MainActivity.get_input_text().toCharArray();

        for (int x = 0; x < buffer.length; x++){
            if (x == charArr.length) break;
            buffer[x] = charArr[x];
        }



    }/* read_string */

/*
 * read_number
 *
 * Ask the user to type in a number and return it.
 *
 */

    public static int read_number ()
    {
        char[] buffer = new char[6];
        int value = 0;
        int i;

        read_string (5, buffer);

        for (i = 0; buffer[i] != 0; i++)
            if (buffer[i] >= '0' && buffer[i] <= '9')
                value = 10 * value + buffer[i] - '0';

        return value;

    }/* read_number */

/*
 * z_read, read a line of input and (in V5+) store the terminating key.
 *
 *	zargs[0] = address of text buffer
 *	zargs[1] = address of token buffer
 *	zargs[2] = timeout in tenths of a second (optional)
 *	zargs[3] = packed address of routine to be called on timeout
 *
 */

    public static void z_read ()
    {
        char[] buffer = new char[header.INPUT_BUFFER_SIZE];
        int addr;
        char key;
        int max, size;
        int c;
        int i;

    /* Supply default arguments */

        if (header.zargc < 3)
            header.zargs[2] = 0;

    /* Get maximum input size */

        addr = header.zargs[0];

        max = header.LOW_BYTE (addr);

        if (header.h_version <= 4)
            max--;

        if (max >= header.INPUT_BUFFER_SIZE)
            max = header.INPUT_BUFFER_SIZE - 1;

    /* Get initial input size */

        if (header.h_version >= 5) {
            addr++;
            size = header.LOW_BYTE (addr);
        } else size = 0;

    /* Copy initial input to local buffer */

        for (i = 0; i < size; i++) {
            addr++;
            c = header.LOW_BYTE (addr);
            buffer[i] = (char)text.translate_from_zscii (c);
        }

        buffer[i] = 0;

    /* Draw status line for V1 to V3 games */

        //if (header.h_version <= 3)
        //    z_show_status ();

    /* Read input from current input stream */

        char[] keys = MainActivity.stream_read_input (
                max,		/* buffer and size */
                header.zargs[2],		/* timeout value   */
                header.zargs[3],		/* timeout routine */
                true,	        	/* enable hot keys */
                header.h_version == 6);	/* no script in V6 */
        key = (char)header.ZC_RETURN;

        if (key == header.ZC_BAD)
            return;

        for (int x = 0; x < header.INPUT_BUFFER_SIZE && x < keys.length; x++)
            buffer[x] = keys[x];

    /* Perform save_undo for V1 to V4 games */

        if (header.h_version <= 4)
            fastmem.save_undo ();

    /* Copy local buffer back to dynamic memory */

        for (i = 0; buffer[i] != 0; i++) {

            if (key == header.ZC_RETURN) {

                if (buffer[i] >= 'A' && buffer[i] <= 'Z')
                    buffer[i] += 'a' - 'A';
                if (buffer[i] >= 0xc0 && buffer[i] <= 0xde && buffer[i] != 0xd7)
                    buffer[i] += 0x20;

            }

            fastmem.storeb ((int) (header.zargs[0] + ((header.h_version <= 4) ? 1 : 2) + i), text.translate_to_zscii (buffer[i]));

        }

    /* Add null character (V1-V4) or write input length into 2nd byte */

        if (header.h_version <= 4)
            fastmem.storeb ((int) (header.zargs[0] + 1 + i), 0);
        else
            fastmem.storeb ((int) (header.zargs[0] + 1), i);

    /* Tokenise line if a token buffer is present */

        if (key == header.ZC_RETURN && header.zargs[1] != 0)
            text.tokenise_line (header.zargs[0], header.zargs[1], 0, false);

    /* Store key */

        if (header.h_version >= 5)
            process.store (text.translate_to_zscii (key));

    }/* z_read */

/*
 * z_read_char, read and store a key.
 *
 *	zargs[0] = input device (must be 1)
 *	zargs[1] = timeout in tenths of a second (optional)
 *	zargs[2] = packed address of routine to be called on timeout
 *
 */

    public static void z_read_char ()
    {
        int key;

    /* Supply default arguments */

        if (header.zargc < 2)
            header.zargs[1] = 0;

    /* Read input from the current input stream */

            key = MainActivity.stream_read_key (
                header.zargs[1],	/* timeout value   */
                header.zargs[2],	/* timeout routine */
                true);  	/* enable hot keys */

        if (key == header.ZC_BAD)
            return;

    /* Store key */

    /* For timeouts, make sure translate_to_zscii() won't try to convert
     * 0x00.  We should instead return 0x00 as is.
     * Thanks to Peter Seebach.
     */
        if (key == 0)
            process.store(key);
        else
            process.store (text.translate_to_zscii ((char)key));
    }


}
