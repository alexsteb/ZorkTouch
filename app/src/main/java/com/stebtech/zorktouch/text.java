package com.stebtech.zorktouch;

import android.util.Log;

/**
 * Created by alexanderstebner on 25.02.18.
 */

enum string_type {
    LOW_STRING, ABBREVIATION, HIGH_STRING, EMBEDDED_STRING, VOCABULARY
}

public class text {

    static char zscii_to_latin1[] = {
            0xe4, 0xf6, 0xfc, 0xc4, 0xd6, 0xdc, 0xdf, 0xbb,
            0xab, 0xeb, 0xef, 0xff, 0xcb, 0xcf, 0xe1, 0xe9,
            0xed, 0xf3, 0xfa, 0xfd, 0xc1, 0xc9, 0xcd, 0xd3,
            0xda, 0xdd, 0xe0, 0xe8, 0xec, 0xf2, 0xf9, 0xc0,
            0xc8, 0xcc, 0xd2, 0xd9, 0xe2, 0xea, 0xee, 0xf4,
            0xfb, 0xc2, 0xca, 0xce, 0xd4, 0xdb, 0xe5, 0xc5,
            0xf8, 0xd8, 0xe3, 0xf1, 0xf5, 0xc3, 0xd1, 0xd5,
            0xe6, 0xc6, 0xe7, 0xc7, 0xfe, 0xf0, 0xde, 0xd0,
            0xa3, 0x00, 0x00, 0xa1, 0xbf
    };


    /*
 * translate_from_zscii
 *
 * Map a ZSCII character onto the ISO Latin-1 alphabet.
 *
 */
    public static int translate_from_zscii (int c)
    {
        if (c == 0xfc)
            return header.ZC_MENU_CLICK;
        if (c == 0xfd)
            return header.ZC_DOUBLE_CLICK;
        if (c == 0xfe)
            return header.ZC_SINGLE_CLICK;

        if (c >= 0x9b && header.story_id != story.BEYOND_ZORK) {

            if (header.hx_unicode_table != 0) {	/* game has its own Unicode table */

                int N;

                N = header.LOW_BYTE (header.hx_unicode_table);

                if (c - 0x9b < N) {

                    int addr = header.hx_unicode_table + 1 + 2 * (c - 0x9b);
                    int unicode;

                    unicode = header.LOW_WORD (addr);

                    return (unicode < 0x100) ? (char) unicode : '?';

                } else return '?';

            } else				/* game uses standard set */

                if (c <= 0xdf) {

                    if (c == 0xdc || c == 0xdd)	/* Oe and oe ligatures */
                        return '?';			/* are not ISO-Latin 1 */

                    return zscii_to_latin1[c - 0x9b];

                } else return '?';
        }

        return c;

    }


    /*
 * alphabet
 *
 * Return a character from one of the three character sets.
 *
 */
    static int alphabet (int set, int index)
    {
        if (header.h_alphabet != 0) {	/* game uses its own alphabet */

            int c;

            int addr = header.h_alphabet + 26 * set + index;
            c = header.LOW_BYTE (addr);

            return translate_from_zscii (c);

        } else			/* game uses default alphabet */

            if (set == 0)
                return 'a' + index;
            else if (set == 1)
                return 'A' + index;
            else if (header.h_version == 1)
                return " 0123456789.,!?_#'\"/\\<-:()".toCharArray()[index];
            else
                return " ^0123456789.,!?_#'\"/\\-:()".toCharArray()[index];

    }


    public static char[] decoded = new char[100];
    public static int[] encoded = new int[3];
    public static int ptr;

    public static void outchar(int c, string_type st){
        if (ptr >= decoded.length) return;

        if (st==string_type.VOCABULARY || (textSize>0 && st == string_type.ABBREVIATION))
            decoded[ptr++] = (char)c;
        else buffer.print_char((char)c);
    }

    private static int textSize = 0;
    public static void decode_text(string_type st, int addr, int textLength) {
        textSize = textLength;
        decode_text(st,addr);
    }

    public static void decode_text (string_type st, int addr)
    {

        long byte_addr;
        char c2;
        int code;
        int c, prev_c = 0;
        int shift_state = 0;
        int shift_lock = 0;
        int status = 0;

        byte_addr = 0;

    /* Calculate the byte address if necessary */
        if (addr == 7106){
            int alex = 1;
            alex++;
        }

        if (st == string_type.ABBREVIATION)

            byte_addr = (long) addr << 1;

        else if (st == string_type.HIGH_STRING) {

            if (header.h_version <= 3)
                byte_addr = (long) addr << 1;
            else if (header.h_version <= 5)
                byte_addr = (long) addr << 2;
            else if (header.h_version <= 7)
                byte_addr = ((long) addr << 2) + ((long) header.h_strings_offset << 3);
            else /* h_version == V8 */
                byte_addr = (long) addr << 3;

            if (byte_addr >= header.story_size)
                err.runtime_error (header.ERR_ILL_PRINT_ADDR);

        }

    /* Loop until a 16bit word has the highest bit set */

        if (st == string_type.VOCABULARY)
            ptr = 0;

        do {

            int i;

	/* Fetch the next 16bit word */

            if (st == string_type.LOW_STRING || st == string_type.VOCABULARY) {
                code = header.LOW_WORD (addr);
                addr += 2;
            } else if (st == string_type.HIGH_STRING || st == string_type.ABBREVIATION) {
                code = header.HIGH_WORD ((int)byte_addr);
                byte_addr += 2;
            } else
                code = header.CODE_WORD ();

	/* Read its three Z-characters */

            for (i = 10; i >= 0; i -= 5) {

                int abbr_addr;
                int ptr_addr;

                c = (code >> i) & 0x1f;

                switch (status) {

                    case 0:	/* normal operation */

                        if (shift_state == 2 && c == 6)
                            status = 2;

                        else if (header.h_version == 1 && c == 1)
                            buffer.new_line ();

                        else if (header.h_version >= 2 && shift_state == 2 && c == 7)
                            buffer.new_line ();

                        else if (c >= 6)
                            outchar (alphabet (shift_state, c - 6),st);


                        else if (c == 0)
                            outchar (' ',st);

                        else if (header.h_version >= 2 && c == 1)
                            status = 1;

                        else if (header.h_version >= 3 && c <= 3)
                            status = 1;

                        else {

                            shift_state = (shift_lock + (c & 1) + 1) % 3;

                            if (header.h_version <= 2 && c >= 4)
                                shift_lock = shift_state;

                            break;

                        }

                        shift_state = shift_lock;

                        break;

                    case 1:	/* abbreviation */

                        ptr_addr = header.h_abbreviations + 64 * (prev_c - 1) + 2 * c;

                        abbr_addr = header.LOW_WORD (ptr_addr);
                        decode_text (string_type.ABBREVIATION, abbr_addr);

                        status = 0;
                        break;

                    case 2:	/* ZSCII character - first part */

                        status = 3;
                        break;

                    case 3:	/* ZSCII character - second part */

                        c2 = (char)translate_from_zscii (((prev_c << 5) | c));
                        outchar (c2,st);

                        status = 0;
                        break;

                }

                prev_c = c;

            }

        } while ((code & 0x8000) == 0 && !(textSize > 0 && ptr >= textSize*3));

        if (st == string_type.VOCABULARY)
	        ptr = 0;
        textSize = 0;
    }


    /*
 * z_print, print a string embedded in the instruction stream.
 *
 *	no zargs used
 *
 */
    public static void z_print ()
    {
        decode_text (string_type.EMBEDDED_STRING, 0);

    }

    public static void z_new_line ()
    {
        buffer.new_line ();

    }

    /*
 * print_num
 *
 * Print a signed 16bit number.
 *
 */
    public static void print_num (int value)
    {
        int i;

    /* Print sign */

        if ((short) value < 0) {
            buffer.print_char ('-');
            value = - (short) value;
        }

    /* Print absolute value */

        for (i = 10000; i != 0; i /= 10)
            if (value >= i || i == 1)
                buffer.print_char ((char)('0' + (value / i) % 10));

    }

    public static void z_print_num ()
    {
        print_num (header.zargs[0]);

    }

    /*
 * z_print_char print a single ZSCII character.
 *
 *	zargs[0] = ZSCII character to be printed
 *
 */
    public static void z_print_char ()
    {
        buffer.print_char ((char)translate_from_zscii (header.zargs[0]));

    }


    /*
 * print_object
 *
 * Print an object description.
 *
 */
    public static void print_object (int obj)
    {
        int addr = object.object_name (obj);

        int code = 0x94a5;
        int length;

        length = header.LOW_BYTE (addr);
        addr++;

        if (length != 0)
            code = header.LOW_WORD (addr);

        if (code == 0x94a5) { 	/* encoded text 0x94a5 == empty string */

            print_string ("object#");	/* supply a generic name */
            print_num (obj);		/* for anonymous objects */

        } else decode_text (string_type.LOW_STRING, addr);

    }

    /*
 * z_print_obj, print an object description.
 *
 * 	zargs[0] = number of object to be printed
 *
 */
    public static void z_print_obj ()
    {
        print_object (header.zargs[0]);

    }


    /*
 * print_string
 *
 * Print a string of ASCII characters.
 *
 */
    public static void print_string (String s)
    {
        for (char c : s.toCharArray()) {

            if (c == '\n')
                buffer.new_line();
            else
                buffer.print_char(c);
        }
    }


    /*
 * translate_to_zscii
 *
 * Map an ISO Latin-1 character onto the ZSCII alphabet.
 *
 */
    public static int translate_to_zscii (char c)
    {
        int i;

        if (c == header.ZC_SINGLE_CLICK)
            return 0xfe;
        if (c == header.ZC_DOUBLE_CLICK)
            return 0xfd;
        if (c == header.ZC_MENU_CLICK)
            return 0xfc;

        if (c >= header.ZC_LATIN1_MIN) {

            if (header.hx_unicode_table != 0) {	/* game has its own Unicode table */

                int N;


                N = header.LOW_BYTE (header.hx_unicode_table);

                for (i = 0x9b; i < 0x9b + N; i++) {

                    int addr = header.hx_unicode_table + 1 + 2 * (i - 0x9b);
                    int unicode;

                    unicode = header.LOW_WORD (addr);

                    if (c == unicode)
                        return (char) i;

                }

                return '?';

            } else {			/* game uses standard set */

                for (i = 0x9b; i <= 0xdf; i++)
                    if (c == zscii_to_latin1[i - 0x9b])
                        return (char) i;

                return '?';

            }
        }

        if (c == 0)		/* Safety thing from David Kinder */
            c = '?';	/* regarding his Unicode patches */
			/* Sept 15, 2002 */

        return c;

    }


    /*
 * tokenise_line
 *
 * Split an input line into words and translate the words to tokens.
 *
 */
    public static void tokenise_line (int text, int token, int dct, boolean flag)
    {
        int addr1;
        int addr2;
        int length;
        int c;

        length = 0;		/* makes compilers shut up */

    /* Use standard dictionary if the given dictionary is zero */

        if (dct == 0)
            dct = header.h_dictionary;

    /* Remove all tokens before inserting new ones */

        fastmem.storeb ((int) (token + 1), 0);

    /* Move the first pointer across the text buffer searching for the
       beginning of a word. If this succeeds, store the position in a
       second pointer. Move the first pointer searching for the end of
       the word. When it is found, "tokenise" the word. Continue until
       the end of the buffer is reached. */

        addr1 = text;
        addr2 = 0;

        if (header.h_version >= 5) {
            addr1++;
            length = header.LOW_BYTE (addr1);
        }

        do {

            int sep_addr;
            int sep_count;
            int separator;

	/* Fetch next ZSCII character */

            addr1++;

            if (header.h_version >= 5 && addr1 == text + 2 + length)
                c = 0;
            else
                c = header.LOW_BYTE (addr1);

	/* Check for separator */

            sep_addr = dct;

            sep_count = header.LOW_BYTE (sep_addr);
            sep_addr++;

            do {

                separator = header.LOW_BYTE (sep_addr);
                sep_addr++;

            } while (c != separator && --sep_count != 0);

	/* This could be the start or the end of a word */

            if (sep_count == 0 && c != ' ' && c != 0) {

                if (addr2 == 0)
                    addr2 = addr1;

            } else if (addr2 != 0) {

                tokenise_text (
                        text,
                        (int) (addr1 - addr2),
                        (int) (addr2 - text),
                        token, dct, flag );

                addr2 = 0;

            }

	/* Translate separator (which is a word in its own right) */

            if (sep_count != 0)

                tokenise_text (
                        text,
                        (int) (1),
                        (int) (addr1 - text),
                        token, dct, flag );

        } while (c != 0);

    }/* tokenise_line */


    /*
     * z_tokenise, make a lexical analysis of a ZSCII string.
     *
     *	zargs[0] = address of string to analyze
     *	zargs[1] = address of token buffer
     *	zargs[2] = address of dictionary (optional)
     *	zargs[3] = set when unknown words cause empty slots (optional)
     *
     */
    public static void z_tokenise ()
    {
    /* Supply default arguments */

        if (header.zargc < 3)
            header.zargs[2] = 0;
        if (header.zargc < 4)
            header.zargs[3] = 0;

    /* Call tokenise_line to do the real work */

        tokenise_line (header.zargs[0], header.zargs[1], header.zargs[2], header.zargs[3] != 0);

    }


    /*
 * lookup_text
 *
 * Scan a dictionary searching for the given word. The first argument
 * can be
 *
 * 0x00 - find the first word which is >= the given one
 * 0x05 - find the word which exactly matches the given one
 * 0x1f - find the last word which is <= the given one
 *
 * The return value is 0 if the search fails.
 *
 */
    static int lookup_text (int padding, int dct)
    {
        int entry_addr;
        int entry_count;
        int entry = 0;
        int addr;
        int entry_len;
        int sep_count;
        int resolution = (header.h_version <= 3) ? 2 : 3;
        int entry_number;
        int lower, upper;
        int i;
        boolean sorted;

        encode_text (padding);

        sep_count = header.LOW_BYTE (dct);		/* skip word separators */
        dct += 1 + sep_count;
        entry_len = header.LOW_BYTE (dct);		/* get length of entries */
        dct += 1;
        entry_count = header.LOW_WORD (dct);		/* get number of entries */
        dct += 2;

        if ((short) entry_count < 0) {	/* bad luck, entries aren't sorted */

            entry_count = - (short) entry_count;
            sorted = false;

        } else sorted = true;		/* entries are sorted */

        lower = 0;
        upper = entry_count - 1;

        while (lower <= upper) {

            if (sorted)                             /* binary search */
                entry_number = (lower + upper) / 2;
            else                                    /* linear search */
                entry_number = lower;

            entry_addr = dct + entry_number * entry_len;

	/* Compare word to dictionary entry */

            addr = entry_addr;
            //decode_text(string_type.VOCABULARY,addr);
            //Log.i("alex",String.copyValueOf(text.decoded));
            //for (int x = 0; x<decoded.length;x++){
            //    decoded[x] = 0;
            //}

            boolean continuing = false;
            for (i = 0; i < resolution; i++) {
                entry = header.LOW_WORD (addr);
                if (encoded[i] != entry)
                    {continuing = true; break;}
                addr += 2;
            }

            if (!continuing) return entry_addr;		/* exact match found, return now */

            continuing:

            if (sorted)				/* binary search */

                if (encoded[i] > entry)
                    lower = entry_number + 1;
                else
                    upper = entry_number - 1;

            else lower++;                           /* linear search */

        }

    /* No exact match has been found */

        if (padding == 0x05)
            return 0;

        entry_number = (padding == 0x00) ? lower : upper;

        if (entry_number == -1 || entry_number == entry_count)
            return 0;

        return dct + entry_number * entry_len;

    }


    /*
 * tokenise_text
 *
 * Translate a single word to a token and append it to the token
 * buffer. Every token consists of the address of the dictionary
 * entry, the length of the word and the offset of the word from
 * the start of the text buffer. Unknown words cause empty slots
 * if the flag is set (such that the text can be scanned several
 * times with different dictionaries); otherwise they are zero.
 *
 */
    public static void tokenise_text (int text, int length, int from, int parse, int dct, boolean flag)
    {
        int addr;
        int token_max, token_count;

        token_max = header.LOW_BYTE (parse);
        parse++;
        token_count = header.LOW_BYTE (parse);

        if (token_count < token_max) {	/* sufficient space left for token? */

            fastmem.storeb (parse++, token_count + 1);

            load_string ((int) (text + from), length);

            addr = lookup_text (0x05, dct);

            if (addr != 0 || !flag) {

                parse += 4 * token_count;

                fastmem.storew ((int) (parse + 0), addr);
                fastmem.storeb ((int) (parse + 2), length);
                fastmem.storeb ((int) (parse + 3), from);

            }

        }

    }

    /*
 * load_string
 *
 * Copy a ZSCII string from the memory to the global "decoded" string.
 *
 */
    public static void load_string (int addr, int length)
    {
        int resolution = (header.h_version <= 3) ? 2 : 3;
        int i = 0;

        while (i < 3 * resolution)

            if (i < length) {

                int c;

                c = header.LOW_BYTE (addr);
                addr++;

                decoded[i++] = (char)translate_from_zscii (c);

            } else decoded[i++] = 0;

    }/* load_string */


    /*
     * encode_text
     *
     * Encode the Unicode text in the global "decoded" string then write
     * the result to the global "encoded" array. (This is used to look up
     * words in the dictionary.) Up to V3 the vocabulary resolution is
     * two, since V4 it is three words. Because each word contains three
     * Z-characters, that makes six or nine Z-characters respectively.
     * Longer words are chopped to the proper size, shorter words are are
     * padded out with 5's. For word completion we pad with 0s and 31s,
     * the minimum and maximum Z-characters.
     *
     */
    static void encode_text (int padding)
    {
         char again[] = { 'a', 'g', 'a', 'i', 'n', 0 };
         char examine[] = { 'e', 'x', 'a', 'm', 'i', 'n', 'e', 0 };
         char wait[] = { 'w', 'a', 'i', 't', 0 };

        char[] zchars = new char[12];
        char[] ptr = decoded;
        int ptrp = 0;
        char c;
        int resolution = (header.h_version <= 3) ? 2 : 3;
        int i = 0;

    /* Expand abbreviations that some old Infocom games lack */

    /*
        if (f_setup.expand_abbreviations)

            if (padding == 0x05 && decoded[1] == 0)

                switch (decoded[0]) {
                    case 'g': ptr = again; break;
                    case 'x': ptr = examine; break;
                    case 'z': ptr = wait; break;
                }
    */
    /* Translate string to a sequence of Z-characters */

        while (i < 3 * resolution)

            if ((c = ptr[ptrp++]) != 0) {

        int index = 0, set;
        int c2;

	    /* Search character in the alphabet */

	    boolean letter_found = false;

        for (set = 0; set < 3; set++) {
            for (index = 0; index < 26; index++)
                if (c == alphabet(set, index)) {
                    letter_found = true;
                    break;
                }
                if (letter_found) break;
        }
	    /* Character not found, store its ZSCII value */

	    if (!letter_found) {
            c2 = translate_to_zscii(c);

            zchars[i++] = 5;
            zchars[i++] = 6;
            zchars[i++] = (char) (c2 >> 5);
            zchars[i++] = (char) (c2 & 0x1f);

            continue;
        }


	    /* Character found, store its index */

        if (set != 0)
            zchars[i++] = (char)(((header.h_version <= 2) ? 1 : 3) + set);

        zchars[i++] = (char)(index + 6);

    } else zchars[i++] = (char)padding;

    /* Three Z-characters make a 16bit word */

        for (i = 0; i < resolution; i++)

            encoded[i] =
                    (zchars[3 * i + 0] << 10) |
                            (zchars[3 * i + 1] << 5) |
                            (zchars[3 * i + 2]);

        encoded[resolution - 1] |= 0x8000;

    }






    /*
     * z_print_paddr, print the string at the given packed address.
     *
     * 	zargs[0] = packed address of string to be printed
     *
     */
    public static void z_print_paddr ()
    {
        decode_text (string_type.HIGH_STRING, header.zargs[0]);

    }/* z_print_paddr */


    /*
     * z_print_ret, print the string at PC, print newline then return true.
     *
     * 	no zargs used
     *
     */
    public static void z_print_ret ()
    {
        decode_text (string_type.EMBEDDED_STRING, 0);
        buffer.new_line ();
        process.ret (1);

    }


}
