package com.stebtech.zorktouch;

import android.util.Log;

/**
 * Created by alexanderstebner on 25.02.18.
 */

public class process {

    static int finished = 0;

    static String[] op0_opcodes = {
            "z_rtrue",
            "z_rfalse",
            "z_print",
            "z_print_ret",
            "z_nop",
            "z_save",
            "z_restore",
            "z_restart",
            "z_ret_popped",
            "z_catch",
            "z_quit",
            "z_new_line",
            "z_show_status",
            "z_verify",
            "__extended__",
            "z_piracy"
    };

    static String[] op1_opcodes = {
            "z_jz",
            "z_get_sibling",
            "z_get_child",
            "z_get_parent",
            "z_get_prop_len",
            "z_inc",
            "z_dec",
            "z_print_addr",
            "z_call_s",
            "z_remove_obj",
            "z_print_obj",
            "z_ret",
            "z_jump",
            "z_print_paddr",
            "z_load",
            "z_call_n"
    };

    static String[] var_opcodes = {
            "__illegal__",
            "z_je",
            "z_jl",
            "z_jg",
            "z_dec_chk",
            "z_inc_chk",
            "z_jin",
            "z_test",
            "z_or",
            "z_and",
            "z_test_attr",
            "z_set_attr",
            "z_clear_attr",
            "z_store",
            "z_insert_obj",
            "z_loadw",
            "z_loadb",
            "z_get_prop",
            "z_get_prop_addr",
            "z_get_next_prop",
            "z_add",
            "z_sub",
            "z_mul",
            "z_div",
            "z_mod",
            "z_call_s",
            "z_call_n",
            "z_set_colour",
            "z_throw",
            "__illegal__",
            "__illegal__",
            "__illegal__",
            "z_call_s",
            "z_storew",
            "z_storeb",
            "z_put_prop",
            "z_read",
            "z_print_char",
            "z_print_num",
            "z_random",
            "z_push",
            "z_pull",
            "z_split_window",
            "z_set_window",
            "z_call_s",
            "z_erase_window",
            "z_erase_line",
            "z_set_cursor",
            "z_get_cursor",
            "z_set_text_style",
            "z_buffer_mode",
            "z_output_stream",
            "z_input_stream",
            "z_sound_effect",
            "z_read_char",
            "z_scan_table",
            "z_not",
            "z_call_n",
            "z_call_n",
            "z_tokenise",
            "z_encode_text",
            "z_copy_table",
            "z_print_table",
            "z_check_arg_count"
    };

    static String[] ext_opcodes = {
            "z_save",
            "z_restore",
            "z_log_shift",
            "z_art_shift",
            "z_set_font",
            "z_draw_picture",
            "z_picture_data",
            "z_erase_picture",
            "z_set_margins",
            "z_save_undo",
            "z_restore_undo",
            "z_print_unicode",
            "z_check_unicode",
            "__illegal__",
            "__illegal__",
            "__illegal__",
            "z_move_window",
            "z_window_size",
            "z_window_style",
            "z_get_wind_prop",
            "z_scroll_window",
            "z_pop_stack",
            "z_read_mouse",
            "z_mouse_window",
            "z_push_stack",
            "z_put_wind_prop",
            "z_print_form",
            "z_make_menu",
            "z_picture_table"
    };

    /*
 * init_process
 *
 * Initialize process variables.
 *
 */
    static void init_process ()
    {
        finished = 0;
    }


    /*
     * load_operand
     *
     * Load an operand, either a variable or a constant.
     *
     */
    static void load_operand (int type)
    {
        int value = 0;
        if ((type & 2) != 0){ //variable
            int variable = header.CODE_BYTE();

            if (variable == 0)
              value = header.stack[header.sp++];
            else if (variable < 16)
                value = header.stack[header.fp - variable];
            else {
                int addr = header.h_globals + 2 * (variable - 16);
                value = header.LOW_WORD(addr);
            }
        } else if ((type & 1) != 0) { //small constant
            int bvalue = header.CODE_BYTE();
            value = bvalue;
        } else { //large constant
            value = header.CODE_WORD();
        }

        header.zargs[header.zargc++] = value;
    }

    /*
 * load_all_operands
 *
 * Given the operand specifier byte, load all (up to four) operands
 * for a VAR or EXT opcode.
 *
 */
    static void load_all_operands (int specifier)
    {
        int i;

        for (i = 6; i >= 0; i -= 2) {

            int type = (specifier >> i) & 0x03;

            if (type == 3)
                break;

            load_operand (type);

        }

    }

    /*
 * interpret
 *
 * Z-code interpreter main loop
 *
 */
    static boolean debug = false;
    static boolean show_lines = false;

    static void interpret()
    {
        do {
            //Log.i("alex","@ "+fastmem.pcp);
            if (fastmem.pcp == 47857){
                int alex = 3;
                alex++;
            }
            if (show_lines) Log.i("alex","#####$"+Integer.toHexString((int)fastmem.pcp));
            if (fastmem.pcp == 0x5914){
                fastmem.pcp = fastmem.pcp+0;
            }
            int opcode = header.CODE_BYTE();

            header.zargc = 0;
            for (int x = 0; x < header.zargs.length;x++)
                header.zargs[x] = 0;

            if (opcode <= 0x80){ //2OP opcodes
                load_operand(((opcode & 0x40) != 0) ? 2 : 1);
                load_operand(((opcode & 0x20) != 0) ? 2 : 1);

                execute(var_opcodes[opcode & 0x1f]);
            } else if (opcode < 0xb0) {		/* 1OP opcodes */

                load_operand ((opcode >> 4));

                execute(op1_opcodes[opcode & 0x0f]);

            } else if (opcode < 0xc0) {		/* 0OP opcodes */

                execute(op0_opcodes[opcode - 0xb0]);

            } else {				/* VAR opcodes */
                int specifier1;
                int specifier2;
                if (opcode == 0xec || opcode == 0xfa){ //call opcides with up to 8 arguments
                    specifier1 = header.CODE_BYTE();
                    specifier2 = header.CODE_BYTE();
                    load_all_operands(specifier1);
                    load_all_operands(specifier2);
                } else {
                    specifier1 = header.CODE_BYTE();
                    load_all_operands(specifier1);

                }
                execute(var_opcodes[opcode - 0xc0]);

            }

        } while (finished == 0);
    }

    static void execute(String c){
        switch (c){
            case "z_call_s": z_call_s(); break;
            case "z_call_n": z_call_n(); break;

            case "z_ret": z_ret(); break;
            case "z_jump": z_jump(); break;
            case "z_quit": z_quit(); break;
            case "z_nop": z_nop(); break;
            case "z_ret_popped": z_ret_popped(); break;
            case "z_rfalse": z_rfalse(); break;
            case "z_rtrue": z_rtrue(); break;
            case "z_check_arg_count": z_check_arg_count(); break;
            case "z_save_undo": fastmem.z_save_undo();break;
            case "z_restart": fastmem.z_restart();break;
            case "z_restore": fastmem.z_restore();break;
            case "z_verify": fastmem.z_verify();break;
            case "z_save": fastmem.z_save();break;
            case "z_read_char": input.z_read_char(); break;
            case "z_read": input.z_read(); break;
            case "z_add": math.z_add(); break;
            case "z_and": math.z_and(); break;
            case "z_art_shift": math.z_art_shift(); break;
            case "z_div": math.z_div(); break;
            case "z_je": math.z_je(); break;
            case "z_jg": math.z_jg(); break;
            case "z_jl": math.z_jl(); break;
            case "z_jz": math.z_jz(); break;
            case "z_log_shift": math.z_log_shift(); break;
            case "z_mod": math.z_mod(); break;
            case "z_mul": math.z_mul(); break;
            case "z_not": math.z_not(); break;
            case "z_or": math.z_or(); break;
            case "z_sub": math.z_sub(); break;
            case "z_test": math.z_test(); break;
            case "z_get_child": object.z_get_child(); break;
            case "z_clear_attr": object.z_clear_attr(); break;
            case "z_jin": object.z_jin(); break;
            case "z_get_next_prop": object.z_get_next_prop(); break;
            case "z_get_parent": object.z_get_parent(); break;
            case "z_get_prop": object.z_get_prop(); break;
            case "z_get_prop_addr": object.z_get_prop_addr(); break;
            case "z_get_prop_len": object.z_get_prop_len(); break;
            case "z_get_sibling": object.z_get_sibling(); break;
            case "z_insert_obj": object.z_insert_obj(); break;
            case "z_remove_obj": object.z_remove_obj(); break;


            case "z_put_prop": object.z_put_prop(); break;
            case "z_set_attr": object.z_set_attr();break;
            case "z_test_attr": object.z_test_attr();break;
            case "z_store": variable.z_store();break;

            case "z_set_text_style": screen.z_set_text_style();break;
            case "z_set_window": screen.z_set_window();break;
            case "z_set_cursor": screen.z_set_cursor();break;
            case "z_split_window": screen.z_split_window();break;

            case "z_storeb": table.z_storeb(); break;
            case "z_storew": table.z_storew(); break;
            case "z_loadb": table.z_loadb(); break;
            case "z_loadw": table.z_loadw(); break;
            case "z_print": text.z_print(); break;
            case "z_new_line": text.z_new_line(); break;
            case "z_print_num": text.z_print_num(); break;
            case "z_print_char": text.z_print_char(); break;
            case "z_print_obj": text.z_print_obj(); break;
            case "z_print_paddr": text.z_print_paddr(); break;
            case "z_print_ret": text.z_print_ret(); break;
            case "z_tokenise": text.z_tokenise(); break;

            case "z_dec_chk": variable.z_dec_chk(); break;
            case "z_dec": variable.z_dec(); break;
            case "z_inc_chk": variable.z_inc_chk(); break;
            case "z_inc": variable.z_inc(); break;
            case "z_load": variable.z_load(); break;

            case "z_pop": variable.z_pop();break;
            case "z_pop_stack": variable.z_pop_stack();break;
            case "z_pull": variable.z_pull();break;
            case "z_push": variable.z_push();break;
            case "z_push_stack": variable.z_push_stack();break;
            case "z_random": z_random(); break;

            case "__illegal__": __illegal__();break;
            case "__extended__": __extended__();break;
            case "z_throw": z_throw(); break;
            case "z_catch": z_catch(); break;


            default:
                Log.i("alex","Opcode "+c+" undefined!");
                Log.i("alex","");

        }

        //Log.i("alex",c+": "+header.zargs[0]+", "+header.zargs[1]+", "+header.zargs[2]+", "+header.zargs[3]);
    }


    /*
 * call
 *
 * Call a subroutine. Save PC and FP then load new PC and initialise
 * new stack frame. Note that the caller may legally provide less or
 * more arguments than the function actually has. The call type "ct"
 * can be 0 (z_call_s), 1 (z_call_n) or 2 (direct call).
 *
 */
    static void call (int routine, int argc, int[] args, int ct) {
        if (header.sp < 4) err.runtime_error(header.ERR_STK_OVF);

        long pc = header.GET_PC();
        header.stack[--header.sp] = (int)((pc >> 9) & 0xffff);
        header.stack[--header.sp] = (int)(pc & 0x1ff);
        header.stack[--header.sp] = (int)((header.fp - 1) & 0xffff);
        header.stack[--header.sp] = (int)((argc | (ct << 12)) & 0xffff);
        header.fp = header.sp;
        header.frame_count++;

        /* Calculate byte address of routine */

        if (header.h_version <= 3)
            pc = (long) routine << 1;
        else if (header.h_version <= 5)
            pc = (long) routine << 2;
        else if (header.h_version <= 7)
            pc = ((long) routine << 2) + ((long) header.h_functions_offset << 3);
        else /* h_version == V8 */
            pc = (long) routine << 3;

        if (pc >= header.story_size){
            err.runtime_error(header.ERR_ILL_CALL_ADDR);
        }

        fastmem.pcp = pc;

        /* Initialise local variables */

        int count = header.CODE_BYTE();

        if (count > 15)
            err.runtime_error (header.ERR_CALL_NON_RTN);
        if (header.sp < count)
            err.runtime_error (header.ERR_STK_OVF);

        header.stack[header.fp] |= (count << 8);	/* Save local var count for Quetzal. */

        int value = 0;

        for (int i = 0; i < count; i++) {

            if (header.h_version <= 4)		/* V1 to V4 games provide default */
                value = header.CODE_WORD();		/* values for all local variables */

            header.stack[--header.sp] = (int) ((argc-- > 0) ? args[i] : value);

        }

    /* Start main loop for direct calls */

        if (ct == 2)
            interpret();

    }


    static void z_call_s(){

        if (header.zargs[0] != 0) {
            int[] _zargs = new int[header.zargs.length-1];
            for (int x=0;x < header.zargs.length-1;x++)
                _zargs[x] = header.zargs[x+1];
            call(header.zargs[0], header.zargc - 1, _zargs, 0);
        }else
            store(0);


    }

    /*
     * z_call_n, call a subroutine and discard its result.
     *
     * 	zargs[0] = packed address of subroutine
     *	zargs[1] = first argument (optional)
     *	...
     *	zargs[7] = seventh argument (optional)
     *
     */
    public static void z_call_n ()
    {
        if (header.zargs[0] != 0) {
            int[] _zargs = new int[header.zargs.length - 1];
            for (int x = 0; x < header.zargs.length - 1; x++)
                _zargs[x] = header.zargs[x + 1];
            call(header.zargs[0], header.zargc - 1, _zargs, 1);
        }
    }

    /*
 * store
 *
 * Store an operand, either as a variable or pushed on the stack.
 *
 */
    static void store (int value)
    {
        int variable =header.CODE_BYTE();

        if (process.debug) Log.i("alex","Store in Variable #"+variable+".");


        if (variable == 0) {
            if (process.debug) Log.i("alex", "(on stack)");
            header.stack[--header.sp] = value;
        }
        else if (variable < 16) {
            header.stack[header.fp - variable] = value;
        }
        else {

            int addr = header.h_globals + 2 * (variable - 16);
            header.SET_WORD (addr, value);
            if (process.debug) Log.i("alex","(global variable at address #"+addr+")");

        }

    }/* store */

    /*
 * ret
 *
 * Return from the current subroutine and restore the previous stack
 * frame. The result may be stored (0), thrown away (1) or pushed on
 * the stack (2). In the latter case a direct call has been finished
 * and we must exit the interpreter loop.
 *
 */
    public static void ret (int value)
    {
        long pc;
        int ct;

        if (header.sp > header.fp)
            err.runtime_error (header.ERR_STK_UNDF);

        header.sp = header.fp;

        ct = (header.stack[header.sp++] >> 12);
        header.frame_count--;
        header.fp = 1 + header.stack[header.sp++]; //?
        pc = header.stack[header.sp++];

        pc = ((long) header.stack[header.sp++] << 9) | pc;

        header.SET_PC (pc);

    /* Handle resulting value */

        if (ct == 0)
            store (value);
        if (ct == 2)
            header.stack[--header.sp] = value;

    /* Stop main loop for direct calls */

        if (ct == 2)
            finished++;

    }

    /*
     * branch
     *
     * Take a jump after an instruction based on the flag, either true or
     * false. The branch can be short or long; it is encoded in one or two
     * bytes respectively. When bit 7 of the first byte is set, the jump
     * takes place if the flag is true; otherwise it is taken if the flag
     * is false. When bit 6 of the first byte is set, the branch is short;
     * otherwise it is long. The offset occupies the bottom 6 bits of the
     * first byte plus all the bits in the second byte for long branches.
     * Uniquely, an offset of 0 means return false, and an offset of 1 is
     * return true.
     *
     */
    public static void branch (boolean flag)
    {
        long pc;
        int offset;
        int specifier;
        int off1;
        int off2;

        if (process.debug) Log.i("alex", "I branched. Flag is "+flag+", arg0="+header.zargs[0]+", arg1="+header.zargs[1]+", arg2="+header.zargs[2]);


        specifier = header.CODE_BYTE ();

        off1 = (specifier & 0x3f);

        if (!flag)
            specifier ^= 0x80;

        if ((specifier & 0x40) == 0) {		/* it's a long branch */

            if ((off1 & 0x20) != 0)		/* propagate sign bit */
                off1 |= 0xc0;

            off2 = header.CODE_BYTE ();

            offset = (off1 << 8) | off2;

        } else offset = off1;		/* it's a short branch */

        if ((specifier & 0x80)!=0) {

            if (offset > 1) {		/* normal branch */

                pc = header.GET_PC ();
                pc += math.intTo16BitSigned(offset - 2);
                header.SET_PC (pc);

            } else ret (offset);		/* special case, return 0 or 1 */
        }
    }


    /*
 * z_ret, return from a subroutine with the given value.
 *
 *	zargs[0] = value to return
 *
 */
    public static void z_ret ()
    {
        ret (header.zargs[0]);

    }


    /*
 * z_jump, jump unconditionally to the given address.
 *
 *	zargs[0] = PC relative address
 *
 */
    public static void z_jump ()
    {
        long pc;

        pc = header.GET_PC ();

        pc += math.intTo16BitSigned(header.zargs[0] - 2);

        if (pc >= header.story_size)
            err.runtime_error (header.ERR_ILL_JUMP_ADDR);

        header.SET_PC (pc);

    }



    public static void z_nop ()
    {
    /* Do nothing */

    }/* z_nop */


    /*
     * z_quit, stop game and exit interpreter.
     *
     *	no zargs used
     *
     */
    public static void z_quit ()
    {
        finished = 9999;

    }/* z_quit */





    /*
     * z_ret_popped, return from a subroutine with a value popped off the stack.
     *
     *	no zargs used
     *
     */
    public static void z_ret_popped ()
    {
        ret (header.stack[header.sp++]);

    }/* z_ret_popped */


    /*
     * z_rfalse, return from a subroutine with false (0).
     *
     * 	no zargs used
     *
     */
    public static void z_rfalse ()
    {
        ret (0);

    }/* z_rfalse */


    /*
     * z_rtrue, return from a subroutine with true (1).
     *
     * 	no zargs used
     *
     */
    public static void z_rtrue ()
    {
        ret (1);

    }

    /*
 * direct_call
 *
 * Call the interpreter loop directly. This is necessary when
 *
 * - a sound effect has been finished
 * - a read instruction has timed out
 * - a newline countdown has hit zero
 *
 * The interpreter returns the result value on the stack.
 *
 */
    public static int direct_call (int addr)
    {
        int[] saved_zargs = new int[8];
        int saved_zargc;
        int i;

    /* Calls to address 0 return false */

        if (addr == 0)
            return 0;

    /* Save operands and operand count */

        for (i = 0; i < 8; i++)
            saved_zargs[i] = header.zargs[i];

        saved_zargc = header.zargc;

    /* Call routine directly */

        call (addr, 0, new int[]{0}, 2);

    /* Restore operands and operand count */

        for (i = 0; i < 8; i++)
            header.zargs[i] = saved_zargs[i];

        header.zargc = saved_zargc;

    /* Resulting value lies on top of the stack */

        return math.intTo16BitSigned( header.stack[header.sp++]);

    }

    static long A = 1;

    static int interval = 0;
    static int counter = 0;

    /*
 * seed_random
 *
 * Set the seed value for the random number generator.
 *
 */
    public static void seed_random (int value)
    {

        if (value == 0) {		/* ask interface for seed value */
            A = RNG.createRandom (100000);
            interval = 0;
        } else if (value < 1000) {	/* special seed value */
            counter = 0;
            interval = value;
        } else {			/* standard seed value */
            A = value;
            interval = 0;
        }

    }

    public static void z_random(){
        if (math.intTo16BitSigned( header.zargs[0]) <= 0) {	/* set random seed */

            seed_random (- math.intTo16BitSigned( header.zargs[0]));
            store (0);

        } else {
            int result;

            if (interval != 0) {		/* ...in special mode */
                result = counter++;
                if (counter == interval) counter = 0;
            } else {			/* ...in standard mode */
                A = 0x015a4e35L * A + 1;
                result = (int)((A >> 16) & 0x7fff);
            }

            store ((int) (result % header.zargs[0] + 1));
        }

    }

    /*
 * __extended__
 *
 * Load and execute an extended opcode.
 *
 */
    static void __extended__ ()
    {
        int opcode;
        int specifier;

        opcode = header.CODE_BYTE ();
        specifier = header.CODE_BYTE ();

        load_all_operands (specifier);

        if (opcode < 0x1d)			/* extended opcodes from 0x1d on */
            execute(ext_opcodes[opcode]);		/* are reserved for future spec' */

    }/* __extended__ */


    /*
     * __illegal__
     *
     * Exit game because an unknown opcode has been hit.
     *
     */
    static void __illegal__ ()
    {
        err.runtime_error (header.ERR_ILL_OPCODE);

    }/* __illegal__ */


    /*
     * z_catch, store the current stack frame for later use with z_throw.
     *
     *	no zargs used
     *
     */
    public static void z_catch ()
    {
        store (header.frame_count);

    }/* z_catch */


    /*
     * z_throw, go back to the given stack frame and return the given value.
     *
     *	zargs[0] = value to return
     *	zargs[1] = stack frame
     *
     */
    public static void z_throw ()
    {
        if (header.zargs[1] > header.frame_count)
            err.runtime_error (header.ERR_BAD_FRAME);

    /* Unwind the stack a frame at a time. */
        for (; header.frame_count > header.zargs[1]; --header.frame_count)
            header.fp = 1 + header.fp+1;

        ret (header.zargs[0]);

    }

    /*
 * z_check_arg_count, branch if subroutine was called with >= n arg's.
 *
 * 	zargs[0] = number of arguments
 *
 */
    public static void z_check_arg_count ()
    {
        if (header.fp ==  header.STACK_SIZE)
            branch (header.zargs[0] == 0);
        else
            branch (header.zargs[0] <= (header.stack[header.fp] & 0xff));
    }
}
