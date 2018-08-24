package com.stebtech.zorktouch;

import android.util.Log;

/**
 * Created by alexanderstebner on 24.02.18.
 */


enum story{
    BEYOND_ZORK,
    SHERLOCK,
    ZORK_ZERO,
    SHOGUN,
    ARTHUR,
    JOURNEY,
    LURKING_HORROR,
    UNKNOWN
}

class Zwindow {
    public int y_pos;
    public int x_pos;
    public int y_size;
    public int x_size;
    public int y_cursor;
    public int x_cursor;
    public int left;
    public int right;
    public int nl_routine;
    public int nl_countdown;
    public int style;
    public int colour;
    public int font;
    public int font_size;
    public int attribute;
    public int line_count;
    public int true_fore;
    public int true_back;
}


/*
class zword {
    public int value = 0;
    public zword(int value){
        this.value = value;
    }
}
class zbyte {
    int value = 0;
    public zbyte(int value){
        this.value = value;
    }

    static zbyte[] fromString(String value){
        zbyte[] retVal = new zbyte[value.length()];
        int cnt = 0;
        for (char c : value.toCharArray()){
            retVal[cnt++] = new zbyte(c);
        }

        return retVal;
    }
}

class zchar {
    int value = 0;
    public zchar(int value){
        this.value = value;
    }
}
*/

public class header {
    static int STACK_SIZE = 1024;
    static int TEXT_BUFFER_SIZE = 200;
    static int INPUT_BUFFER_SIZE = 200;
    static int MAX_FILE_NAME = 80;
    static int MAX_UNDO_SLOTS = 500;

    static int ERR_NUM_ERRORS = 32;


/* Error codes */
    static int ERR_TEXT_BUF_OVF =1;	/* Text buffer overflow */
    static int ERR_STORE_RANGE =2;	/* Store out of dynamic memory */
    static int ERR_DIV_ZERO =3;		/* Division by zero */
    static int ERR_ILL_OBJ =4	;	/* Illegal object */
    static int ERR_ILL_ATTR =5	;	/* Illegal attribute */
    static int ERR_NO_PROP =6;		/* No such property */
    static int ERR_STK_OVF = 7;         /* Stack overflow */
    static int ERR_ILL_CALL_ADDR = 8;	/* Call to illegal address */
    static int ERR_CALL_NON_RTN = 9;	/* Call to non-routine */
    static int ERR_STK_UNDF =10;		/* Stack underflow */
    static int ERR_ILL_OPCODE =11;	/* Illegal opcode */
    static int ERR_BAD_FRAME =12;	/* Bad stack frame */
    static int ERR_ILL_JUMP_ADDR =13;	/* Jump to illegal address */
    static int ERR_SAVE_IN_INTER =14;	/* Can't save while in interrupt */
    static int ERR_STR3_NESTING =15;	/* Nesting stream #3 too deep */
    static int ERR_ILL_WIN =16;		/* Illegal window */
    static int ERR_ILL_WIN_PROP =17;	/* Illegal window property */
    static int ERR_ILL_PRINT_ADDR =18;	/* Print at illegal address */
    static int ERR_MAX_FATAL =18;

/* Less serious errors */
    static int ERR_JIN_0 =19;		/* @jin called with object 0 */
    static int ERR_GET_CHILD_0 =20;	/* @get_child called with object 0 */
    static int ERR_GET_PARENT_0 =21;	/* @get_parent called with object 0 */
    static int ERR_GET_SIBLING_0 =22;	/* @get_sibling called with object 0 */
    static int ERR_GET_PROP_ADDR_0 =23;	/* @get_prop_addr called with object 0 */
    static int ERR_GET_PROP_0 =24;	/* @get_prop called with object 0 */
    static int ERR_PUT_PROP_0 =25;	/* @put_prop called with object 0 */
    static int ERR_CLEAR_ATTR_0 =26;	/* @clear_attr called with object 0 */
    static int ERR_SET_ATTR_0 =27;	/* @set_attr called with object 0 */
    static int ERR_TEST_ATTR_0 =28;	/* @test_attr called with object 0 */
    static int ERR_MOVE_OBJECT_0 =29;	/* @move_object called moving object 0 */
    static int ERR_MOVE_OBJECT_TO_0 =30;	/* @move_object called moving into object 0 */
    static int ERR_REMOVE_OBJECT_0 =31;	/* @remove_object called with object 0 */
    static int ERR_GET_NEXT_PROP_0 =32;	/* @get_next_prop called with object 0 */

    /*** Story file header format ***/

    static int H_VERSION = 0;
    static int H_CONFIG = 1;
    static int H_RELEASE = 2;
    static int H_RESIDENT_SIZE = 4;
    static int H_START_PC = 6;
    static int H_DICTIONARY = 8;
    static int H_OBJECTS = 10;
    static int H_GLOBALS = 12;
    static int H_DYNAMIC_SIZE = 14;
    static int H_FLAGS = 16;
    static int H_SERIAL = 18;
    static int H_ABBREVIATIONS = 24;
    static int H_FILE_SIZE = 26;
    static int H_CHECKSUM = 28;
    static int H_INTERPRETER_NUMBER = 30;
    static int H_INTERPRETER_VERSION = 31;
    static int H_SCREEN_ROWS = 32;
    static int H_SCREEN_COLS = 33;
    static int H_SCREEN_WIDTH = 34;
    static int H_SCREEN_HEIGHT = 36;
    static int H_FONT_HEIGHT = 38; /* this is the font width in V5 */
    static int H_FONT_WIDTH = 39; /* this is the font height in V5 */
    static int H_FUNCTIONS_OFFSET = 40;
    static int H_STRINGS_OFFSET = 42;
    static int H_DEFAULT_BACKGROUND = 44;
    static int H_DEFAULT_FOREGROUND = 45;
    static int H_TERMINATING_KEYS = 46;
    static int H_LINE_WIDTH = 48;
    static int H_STANDARD_HIGH = 50;
    static int H_STANDARD_LOW = 51;
    static int H_ALPHABET = 52;
    static int H_EXTENSION_TABLE = 54;
    static int H_USER_NAME = 56;

    static int HX_TABLE_SIZE = 0;
    static int HX_MOUSE_X = 1;
    static int HX_MOUSE_Y = 2;
    static int HX_UNICODE_TABLE = 3;

    /*** Various Z-machine constants ***/

    static int  SCRIPTING_FLAG	=  0x0001; /* Outputting to transcription file  - V1+ */
    static int  FIXED_FONT_FLAG  = 0x0002; /* Use fixed width font               - V3+ */
    static int  REFRESH_FLAG 	 = 0x0004; /* Refresh the screen                 - V6  */
    static int  GRAPHICS_FLAG	 = 0x0008; /* Game wants to use graphics         - V5+ */
    static int  OLD_SOUND_FLAG	 = 0x0010; /* Game wants to use sound effects    - V3  */
    static int  UNDO_FLAG	 = 0x0010; /* Game wants to use UNDO feature     - V5+ */
    static int  MOUSE_FLAG	=  0x0020; /* Game wants to use a mouse          - V5+ */
    static int  COLOUR_FLAG	 = 0x0040; /* Game wants to use colours          - V5+ */
    static int  SOUND_FLAG	 = 0x0080; /* Game wants to use sound effects    - V5+ */
    static int  MENU_FLAG	 = 0x0100; /* Game wants to use menus            - V6  */

    static int  TRANSPARENT_FLAG = 0x0001; /* Game wants to use transparency     - V6  */

    /*** Character codes ***/

    static int ZC_TIME_OUT = 00;
    static int ZC_NEW_STYLE = 0x01;
    static int ZC_NEW_FONT = 0x02;
    static int ZC_BACKSPACE = 0x08;
    static int ZC_INDENT = 0x09;
    static int ZC_GAP = 0x0b;
    static int ZC_RETURN = 0x0d;
    static int ZC_HKEY_MIN = 0x0e;
    static int ZC_HKEY_RECORD = 0x0e;
    static int ZC_HKEY_PLAYBACK = 0x0f;
    static int ZC_HKEY_SEED = 0x10;
    static int ZC_HKEY_UNDO = 0x11;
    static int ZC_HKEY_RESTART = 0x12;
    static int ZC_HKEY_QUIT = 0x13;
    static int ZC_HKEY_DEBUG = 0x14;
    static int ZC_HKEY_HELP = 0x15;
    static int ZC_HKEY_MAX = 0x15;
    static int ZC_ESCAPE = 0x1b;
    static int ZC_ASCII_MIN = 0x20;
    static int ZC_ASCII_MAX = 0x7e;
    static int ZC_BAD = 0x7f;
    static int ZC_ARROW_MIN = 0x81;
    static int ZC_ARROW_UP = 0x81;
    static int ZC_ARROW_DOWN = 0x82;
    static int ZC_ARROW_LEFT = 0x83;
    static int ZC_ARROW_RIGHT = 0x84;
    static int ZC_ARROW_MAX = 0x84;
    static int ZC_FKEY_MIN = 0x85;
    static int ZC_FKEY_MAX = 0x90;
    static int ZC_NUMPAD_MIN = 0x91;
    static int ZC_NUMPAD_MAX = 0x9a;
    static int ZC_SINGLE_CLICK = 0x9b;
    static int ZC_DOUBLE_CLICK = 0x9c;
    static int ZC_MENU_CLICK = 0x9d;
    static int ZC_LATIN1_MIN = 0xa0;
    static int ZC_LATIN1_MAX = 0xff;




    /*** Story file header data ***/
    static int h_version;
    static int h_config;
    static int h_release;
    static int h_resident_size;
    static int h_start_pc;
    static int h_dictionary;
    static int h_objects;
    static int h_globals;
    static int h_dynamic_size;
    static int h_flags;
    static int[] h_serial = new int[6];
    static int h_abbreviations;
    static int h_file_size;
    static int h_checksum;
    static int h_interpreter_number;
    static int h_interpreter_version;
    static int h_screen_rows;
    static int h_screen_cols;
    static int h_screen_width;
    static int h_screen_height;
    static int h_font_height;
    static int h_font_width;
    static int h_functions_offset;
    static int h_strings_offset;
    static int h_default_background;
    static int h_default_foreground;
    static int h_terminating_keys;
    static int h_line_width;
    static int h_standard_high;
    static int h_standard_low;
    static int h_alphabet;
    static int h_extension_table;
    static int[] h_user_name = new int[8];

    static int hx_table_size;
    static int hx_mouse_x;
    static int hx_mouse_y;
    static int hx_unicode_table;
    static int hx_flags;
    static int hx_fore_colour;
    static int hx_back_colour;

/*** Various data ***/

    static story story_id;
    static long story_size;
    static String story_name;


    static int[] stack = new int[STACK_SIZE];
    static int sp;
    static int fp;
    static int frame_count;

    static int[] zargs = new int[8];
    static int zargc = 0;

    static boolean ostream_screen;
    static boolean ostream_script;
    static boolean ostream_memory;
    static boolean ostream_record;
    static boolean istream_replay;
    static boolean message;

    static int cwin;
    static int mwin;

    static int mouse_x;
    static int mouse_y;
    static int menu_selected;
    static int mouse_button;

    static boolean enable_wrapping;
    static boolean enable_scripting;
    static boolean enable_scrolling;
    static boolean enable_buffering;


    static String option_zcode_path;	/* dg */

    static long reserve_mem;

    public static int lo(int v){
        return v & 0xff;
    }
    public static int hi(int v){
        return v >> 8;
    }

    public static void SET_BYTE(int addr, int v){
        MainActivity.memory[addr] = v;
    }
    public static int LOW_BYTE(int addr){
        return MainActivity.memory[addr];
    }
    public static int CODE_BYTE(){
        return MainActivity.memory[(int)fastmem.pcp++];
    }


    public static void SET_WORD(int addr, int v){
        MainActivity.memory[addr] = hi(v);
        MainActivity.memory[addr+1] = lo(v);

    }
    public static int LOW_WORD(int addr){
        if (addr+1 > MainActivity.memory.length){
            Log.i("alex","index too high!");
            return (MainActivity.memory[addr % MainActivity.memory.length] << 8 | MainActivity.memory[(addr+1)% MainActivity.memory.length]);
        }
        return (MainActivity.memory[addr] << 8 | MainActivity.memory[(addr+1)]);
    }
    public static int HIGH_WORD(int addr){
        if (addr+1 > MainActivity.memory.length){
            Log.i("alex","index too high!");
            return (MainActivity.memory[addr % MainActivity.memory.length] << 8 | MainActivity.memory[(addr+1)% MainActivity.memory.length]);

        }
        return (MainActivity.memory[addr] << 8 | MainActivity.memory[(addr+1)]);
    }
    public static int CODE_WORD(){
        return (MainActivity.memory[(int)fastmem.pcp++] << 8 | MainActivity.memory[(int)fastmem.pcp++]);
    }
    public static long GET_PC(){
        return fastmem.pcp;
    }
    public static void SET_PC(long addr){
        fastmem.pcp = addr;
    }




}


