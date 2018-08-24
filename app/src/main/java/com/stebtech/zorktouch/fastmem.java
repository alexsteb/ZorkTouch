package com.stebtech.zorktouch;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexanderstebner on 24.02.18.
 */

class undo_t{
    undo_t next;
    undo_t prev;
    long pc;
    long diff_size;
    byte frame_count;
    byte stack_size;
    byte frame_offset;
}

public class fastmem {


    static int zmp; //header pointer
    static long pcp; //program counter pointer

    static List<Integer> undo_mem = new ArrayList<>();

    static int first_undo = 0;
    static int last_undo = 0;
    static int curr_undo = 0;
    static int prev_zmp;
    static int undo_diff;

    static int undo_count = 0;

 /*
 * init_memory
 *
 * Allocate memory and load the story file.
 *
 */

    static void init_memory () {
        long size;

        long n;

        record[] records = {
            new record(story.SHERLOCK, 21, "871214".toCharArray()),
            new record(story.SHERLOCK, 26, "880127".toCharArray()),
            new record(story.BEYOND_ZORK, 47, "870915" .toCharArray()),
            new record(story.BEYOND_ZORK, 49, "870917" .toCharArray()),
            new record(story.BEYOND_ZORK,  51, "870923" .toCharArray()),
            new record(story.BEYOND_ZORK,  57, "871221" .toCharArray()),
            new record(story.ZORK_ZERO, 296, "881019" .toCharArray()),
            new record(story.ZORK_ZERO, 366, "890323" .toCharArray()),
            new record(story.ZORK_ZERO, 383, "890602" .toCharArray()),
            new record(story.ZORK_ZERO, 393, "890714" .toCharArray()),
            new record(story.SHOGUN, 292, "890314" .toCharArray()),
            new record(story.SHOGUN, 295, "890321" .toCharArray()),
            new record(story.SHOGUN, 311, "890510" .toCharArray()),
            new record(story.SHOGUN, 322, "890706" .toCharArray()),
            new record(story.ARTHUR, 54, "890606" .toCharArray()),
            new record(story.ARTHUR,  63, "890622" .toCharArray()),
            new record(story.ARTHUR,  74, "890714" .toCharArray()),
            new record(story.JOURNEY,  26, "890316" .toCharArray()),
            new record(story.JOURNEY,  30, "890322" .toCharArray()),
            new record(story.JOURNEY,  77, "890616" .toCharArray()),
            new record(story.JOURNEY,  83, "890706" .toCharArray()),
            new record(story.LURKING_HORROR, 203, "870506" .toCharArray()),
            new record(story.LURKING_HORROR, 219, "870912" .toCharArray()),
            new record(story.LURKING_HORROR, 221, "870918" .toCharArray()),
            new record(story.UNKNOWN,   0, "------".toCharArray() )
        };



    /* Copy header fields to global variables */

        header.h_version = header.LOW_BYTE (header.H_VERSION);
        if (header.h_version < 1 || header.h_version > 8){
            throw new Error("Unknown Z-code version");
        }

        header.h_config = header.LOW_BYTE(header.H_CONFIG);
        header.h_release = header.LOW_WORD(header.H_RELEASE);
        header.h_resident_size = header.LOW_WORD(header.H_RESIDENT_SIZE);
        header.h_start_pc = header.LOW_WORD(header.H_START_PC);
        header.h_dictionary = header.LOW_WORD(header.H_DICTIONARY);
        header.h_objects = header.LOW_WORD(header.H_OBJECTS);
        header.h_globals = header.LOW_WORD(header.H_GLOBALS);
        header.h_dynamic_size = header.LOW_WORD(header.H_DYNAMIC_SIZE);
        header.h_flags = header.LOW_WORD(header.H_FLAGS);

        for (int i = 0, addr = header.H_SERIAL; i < 6; i++, addr++){
            header.h_serial[i] = header.LOW_BYTE(addr);
        }

         /* Auto-detect buggy story files that need special fixes */

        header.story_id = story.UNKNOWN;
        boolean double_break = false;
        for (int i = 0; records[i].story_id != story.UNKNOWN; i++){

            if (header.h_release == records[i].release) {
                for (int j = 0; j < 6; j++){
                    if (header.h_serial[j] != records[i].serial[j]) {double_break = true; break;}
                }

                header.story_id = records[i].story_id;
            }
            if (double_break) break;
        }

        header.h_abbreviations = header.LOW_WORD(header.H_ABBREVIATIONS);
        header.h_file_size = header.LOW_WORD(header.H_FILE_SIZE);

        /* Calculate story file size in bytes */

        if (header.h_file_size != 0) {
            header.story_size = (long) (2 * header.h_file_size);


        if (header.h_version >= 4) header.story_size *= 2;
        if (header.h_version >= 6) header.story_size *= 2;
        } else { /* some old games lack the file size entry */

            header.story_size = MainActivity.memory.length;

            //TODO: Check what to do with BLORB files! (actual code-file is somewhere hidden inside)
        }

        header.h_checksum = header.LOW_WORD(header.H_CHECKSUM);
        header.h_alphabet = header.LOW_WORD(header.H_ALPHABET);
        header.h_functions_offset = header.LOW_WORD(header.H_FUNCTIONS_OFFSET);
        header.h_strings_offset = header.LOW_WORD(header.H_STRINGS_OFFSET);
        header.h_terminating_keys = header.LOW_WORD(header.H_TERMINATING_KEYS);
        header.h_extension_table = header.LOW_WORD(header.H_EXTENSION_TABLE);

        /* Zork Zero Macintosh doesn't have the graphics flag set */

        if (header.story_id == story.ZORK_ZERO && header.h_release == 296)
            header.h_flags |= header.GRAPHICS_FLAG;

        /* Adjust opcode tables */

        if (header.h_version <= 4) {
            process.op0_opcodes[0x09] = "z_pop";
            process.op1_opcodes[0x0f] = "z_not";
        } else {
            process.op0_opcodes[0x09] = "z_catch";
            process.op1_opcodes[0x0f] = "z_call_n";
        }

        /* Read header extension table */

        header.hx_table_size = get_header_extension (header.HX_TABLE_SIZE);
        header.hx_unicode_table = get_header_extension (header.HX_UNICODE_TABLE);

    }


    /*
 * get_header_extension
 *
 * Read a value from the header extension (former mouse table).
 *
 */
    static int get_header_extension (int entry) {
        if (header.h_extension_table == 0 || entry > header.hx_table_size)
            return 0;

        int addr = header.h_extension_table + 2 * entry;
        int val = header.LOW_WORD(addr);

        return val;
    }


    /*
 * z_restart, re-load dynamic area, clear the stack and set the PC.
 *
 * 	no zargs used
 *
 */
    public static void z_restart () {
        RNG.setSeed(0);

        header.sp = header.fp = header.STACK_SIZE;
        header.frame_count = 0;

        if (header.h_version != 6){
            long pc = header.h_start_pc;
            pcp = pc;
        } else {
            process.call(header.h_start_pc, 0, null, 0);
        }
    }


    /*
 * storeb
 *
 * Write a byte value to the dynamic Z-machine memory.
 *
 */
    public static void storeb (int addr, int value)
    {
        if (addr >= header.h_dynamic_size)
            err.runtime_error (header.ERR_STORE_RANGE);

        if (addr == header.H_FLAGS + 1) {	/* flags register is modified */

            header.h_flags &= ~(header.SCRIPTING_FLAG | header.FIXED_FONT_FLAG);
            header.h_flags |= value & (header.SCRIPTING_FLAG | header.FIXED_FONT_FLAG);

            if ((value & header.SCRIPTING_FLAG) != 0) {
                //if (!header.ostream_script)
                    //header.script_open ();
            } else {
                //if (header.ostream_script)
                    //script_close ();
            }

            //refresh_text_style ();

        }

        header.SET_BYTE (addr, value);

    }

    /*
 * storew
 *
 * Write a word value to the dynamic Z-machine memory.
 *
 */
    public static void storew (int addr, int value)
    {
        storeb (addr + 0, header.hi (value));
        storeb (addr + 1, header.lo (value));

    }


    /*
 * save_undo
 *
 * This function does the dirty work for z_save_undo.
 *
 */
    public static int save_undo ()
    {
        /*
        long diff_size;
        int stack_size;
        int p;//undo_t *p;
        long pc;

        //if (f_setup.undo_slots == 0)	/* undo feature unavailable */
        //    return -1;

    /* save undo possible * /

        while (last_undo != curr_undo) {
            p = last_undo;
            last_undo = last_undo->prev;
            free (p);
            undo_count--;
        }
        if (last_undo)
            last_undo->next = NULL;
        else
            first_undo = NULL;

        if (undo_count == f_setup.undo_slots)
            free_undo (1);

        diff_size = mem_diff (zmp, prev_zmp, h_dynamic_size, undo_diff);
        stack_size = stack + STACK_SIZE - sp;
        do {
            p = malloc (sizeof (undo_t) + diff_size + stack_size * sizeof (*sp));
            if (p == NULL)
                free_undo (1);
        } while (!p && undo_count);
        if (p == NULL)
            return -1;
        pc = p->pc;
        GET_PC (pc);	/* Turbo C doesn't like seeing p->pc here * /
        p->pc = pc;
        p->frame_count = frame_count;
        p->diff_size = diff_size;
        p->stack_size = stack_size;
        p->frame_offset = fp - stack;
        memcpy (p + 1, undo_diff, diff_size);
        memcpy ((zbyte *)(p + 1) + diff_size, sp, stack_size * sizeof (*sp));

        if (!first_undo) {
            p->prev = NULL;
            first_undo = p;
        } else {
            last_undo->next = p;
            p->prev = last_undo;
        }
        p->next = NULL;
        curr_undo = last_undo = p;
        undo_count++;
        return 1;
*/
    return 1;
    } /* save_undo */


    /*
     * z_save_undo, save the current Z-machine state for a future undo.
     *
     *	no zargs used
     *
     */
    public static void z_save_undo ()
    {
        process.store ((int) save_undo());

    }

    /*
 * z_save, save [a part of] the Z-machine state to disk.
 *
 *	zargs[0] = address of memory area to save (optional)
 *	zargs[1] = number of bytes to save
 *	zargs[2] = address of suggested file name
 *
 */
    public static void z_save ()
    {
        char[] new_name = new char[header.MAX_FILE_NAME + 1];
        char[] default_name = new char[header.MAX_FILE_NAME + 1];
        //FILE *gfp;

        int success = 0;

        if (header.zargc != 0) {

	    //Save File

            success = 1;
        }

        finished:

        if (header.h_version <= 3)
            process.branch (success != 0);
        else
            process.store (success);

    }
    /*
 * z_restore, restore [a part of] a Z-machine state from disk
 *
 *	zargs[0] = address of area to restore (optional)
 *	zargs[1] = number of bytes to restore
 *	zargs[2] = address of suggested file name
 *
 */
    public static void z_restore ()
    {
        char[] new_name = new char[header.MAX_FILE_NAME + 1];
        char[] default_name = new char[header.MAX_FILE_NAME + 1];
        //FILE *gfp;

        int success = 1;

	    //Load file

        if (header.h_version <= 3)
            process.branch (success != 0);
        else
            process.store (success);

    }

    /*
 * z_verify, check the story file integrity.
 *
 *	no zargs used
 *
 */
    public static void z_verify ()
    {
        process.branch (true);
    }


}


class record{
    story story_id;
    int release;
    char[] serial = new char[6];
    public record(story story_id, int release, char[] serial ){
        this.story_id = story_id;
        this.release = release;
        this.serial = serial;
    }
}
