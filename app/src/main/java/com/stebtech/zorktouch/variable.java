package com.stebtech.zorktouch;

import android.util.Log;

/**
 * Created by alexanderstebner on 25.02.18.
 */

public class variable {

    /*
 * z_store, write a value to a variable.
 *
 * 	zargs[0] = variable to be written to
 *      zargs[1] = value to write
 *
 */
    public static void z_store ()
    {
        int value = header.zargs[1];
        if (process.debug) Log.i("alex","Storing: "+value);
        if (header.zargs[0] == 0)
	header.stack[header.sp] = value;
    else if (header.zargs[0] < 16) {
            header.stack[header.fp - header.zargs[0]] = value;
            if (process.debug)  Log.i("alex", "Variable #" + header.zargs[0]);
        }
    else {
        int addr = header.h_globals + 2 * (header.zargs[0] - 16);
        header.SET_WORD (addr, value);
            if (process.debug)  Log.i("alex", "Save at @" + addr);
    }

    }


    /*
 * z_dec, decrement a variable.
 *
 * 	zargs[0] = variable to decrement
 *
 */
    public static void z_dec ()
    {
        int value;

        if (header.zargs[0] == 0)
            header.stack[header.sp]--;
    else if (header.zargs[0] < 16)
        header.stack[header.fp - header.zargs[0]]--;
    else {
        int addr = header.h_globals + 2 * (header.zargs[0] - 16);
        value = header.LOW_WORD (addr);
        value--;
        header.SET_WORD (addr, value);
    }

    }/* z_dec */


    /*
     * z_dec_chk, decrement a variable and branch if now less than value.
     *
     * 	zargs[0] = variable to decrement
     * 	zargs[1] = value to check variable against
     *
     */
    public static void z_dec_chk ()
    {
        int value;

        if (header.zargs[0] == 0)
            value = --header.stack[header.sp];
    else if (header.zargs[0] < 16)
        value = --header.stack[header.fp - header.zargs[0]];
    else {
        int addr = header.h_globals + 2 * (header.zargs[0] - 16);
        value = header.LOW_WORD (addr);
        value--;
        header.SET_WORD (addr, value);
    }

        process.branch (math.intTo16BitSigned( value) < math.intTo16BitSigned(header.zargs[1]));

    }/* z_dec_chk */


    /*
     * z_inc, increment a variable.
     *
     * 	zargs[0] = variable to increment
     *
     */
    public static void z_inc ()
    {
        int value;

        if (header.zargs[0] == 0)
            header.stack[header.sp]++;
    else if (header.zargs[0] < 16)
            header.stack[header.fp - header.zargs[0]]++;
    else {
        int addr = header.h_globals + 2 * (header.zargs[0] - 16);
            value = header.LOW_WORD (addr);
        value++;
        header.SET_WORD (addr, value);
    }

    }/* z_inc */


    /*
     * z_inc_chk, increment a variable and branch if now greater than value.
     *
     * 	zargs[0] = variable to increment
     * 	zargs[1] = value to check variable against
     *
     */
    public static void z_inc_chk ()
    {
        int value;

        if (header.zargs[0] == 0)
            value = ++header.stack[header.sp];
    else if (header.zargs[0] < 16)
            value = ++header.stack[header.fp - header.zargs[0]];
    else {
        int addr = header.h_globals + 2 * (header.zargs[0] - 16);
            value = header.LOW_WORD (addr);
        value++;
        header.SET_WORD (addr, value);
    }

        process.branch (math.intTo16BitSigned( value) > math.intTo16BitSigned( header.zargs[1]));

    }


    /*
 * z_pop, pop a value off the game stack and discard it.
 *
 *	no zargs used
 *
 */
    public static void z_pop ()
    {
        header.sp++;

    }/* z_pop */


    /*
     * z_pop_stack, pop n values off the game or user stack and discard them.
     *
     *	zargs[0] = number of values to discard
     *	zargs[1] = address of user stack (optional)
     *
     */
    public static void z_pop_stack ()
    {
        if (header.zargc == 2) {		/* it's a user stack */

            int size;
            int addr = header.zargs[1];

            size = header.LOW_WORD (addr);

            size += header.zargs[0];
            fastmem.storew (addr, size);

        } else header.sp += header.zargs[0];	/* it's the game stack */

    }/* z_pop_stack */


    /*
     * z_pull, pop a value off...
     *
     * a) ...the game or a user stack and store it (V6)
     *
     *	zargs[0] = address of user stack (optional)
     *
     * b) ...the game stack and write it to a variable (other than V6)
     *
     *	zargs[0] = variable to write value to
     *
     */
    public static void z_pull ()
    {
        int value;

        if (header.h_version != 6) {	/* not a V6 game, pop stack and write */

            value = header.stack[header.sp++];

            if (header.zargs[0] == 0)
	    header.stack[header.sp] = value;
	else if (header.zargs[0] < 16)
	    header.stack[header.fp - header.zargs[0]] = value;
	else {
                int addr = header.h_globals + 2 * (header.zargs[0] - 16);
                header.SET_WORD (addr, value);
            }

        } else {			/* it's V6, but is there a user stack? */

            if (header.zargc == 1) {	/* it's a user stack */

                int size;
                int addr = header.zargs[0];

                size = header.LOW_WORD (addr);

                size++;
                fastmem.storew (addr, size);

                addr += 2 * size;
                value = header.LOW_WORD (addr);

            } else value = header.stack[header.sp++];	/* it's the game stack */

            process.store (value);

        }

    }/* z_pull */


    /*
     * z_push, push a value onto the game stack.
     *
     *	zargs[0] = value to push onto the stack
     *
     */
    public static void z_push ()
    {
    header.stack[--header.sp] = header.zargs[0];

    }/* z_push */


    /*
     * z_push_stack, push a value onto a user stack then branch if successful.
     *
     *	zargs[0] = value to push onto the stack
     *	zargs[1] = address of user stack
     *
     */
    public static void z_push_stack ()
    {
        int size;
        int addr = header.zargs[1];

        size = header.LOW_WORD (addr);

        if (size != 0) {

            fastmem.storew ( (addr + 2 * size), header.zargs[0]);

            size--;
            fastmem.storew (addr, size);

        }

        process.branch (size != 0);

    }

    /*
 * z_load, store the value of a variable.
 *
 *	zargs[0] = variable to store
 *
 */
    public static void z_load ()
    {
        int value;

        if (header.zargs[0] == 0)
            value = header.stack[header.sp];
    else if (header.zargs[0] < 16)
        value = header.stack[(header.fp - header.zargs[0])];
    else {
        int addr = header.h_globals + 2 * (header.zargs[0] - 16);
        value = header.LOW_WORD (addr);
    }

        process.store (value);

    }
}
