package com.stebtech.zorktouch;

import android.util.Log;

/**
 * Created by alexanderstebner on 25.02.18.
 */

public class table {

    public static void z_copy_table(){
        int addr;
        int size = header.zargs[2];
        int value;
        int i;

        if (header.zargs[1] == 0)      				/* zero table */

            for (i = 0; i < size; i++)
                fastmem.storeb ( header.zargs[0] + i,0);

        else if (math.intTo16BitSigned(size) < 0 || header.zargs[0] > header.zargs[1])	/* copy forwards */

            for (i = 0; i < (( math.intTo16BitSigned(size) < 0) ? - math.intTo16BitSigned(size) : size); i++) {
                addr = header.zargs[0] + i;
                value = header.LOW_BYTE (addr);
                fastmem.storeb ( header.zargs[1] + i, value);
            }

        else						/* copy backwards */

            for (i = size - 1; i >= 0; i--) {
                addr = header.zargs[0] + i;
                value = header.LOW_BYTE (addr);
                fastmem.storeb ((header.zargs[1] + i), value);
            }
    }


    /*
  * z_loadb, store a value from a table of bytes.
  *
  *	zargs[0] = address of table
  *	zargs[1] = index of table entry to store
  *
  */
    public static void z_loadb ()
    {

        int addr = header.zargs[0] + header.zargs[1];
        if (process.debug) Log.i("alex","Retrieve value at address "+addr);
        int value;

        value = header.LOW_BYTE (addr);
        if (process.debug) Log.i("alex","That value is: "+value+". Save it now.");

        process.store (value);

    }

    /*
 * z_loadw, store a value from a table of words.
 *
 *	zargs[0] = address of table
 *	zargs[1] = index of table entry to store
 *
 */
    public static void z_loadw ()
    {
        int addr = header.zargs[0] + 2 * header.zargs[1];
        int value;

        value = header.LOW_WORD (addr);

        process.store (value);
// 78 01 42 42 02 01 00 00 00 00
    }


    /*
 * z_storeb, write a byte into a table of bytes.
 *
 *	zargs[0] = address of table
 *	zargs[1] = index of table entry
 *	zargs[2] = value to be written
 *
 */
    public static void z_storeb ()
    {
        fastmem.storeb (header.zargs[0] + header.zargs[1], header.zargs[2]);

    }

    /*
 * z_storew, write a word into a table of words.
 *
 *	zargs[0] = address of table
 *	zargs[1] = index of table entry
 *	zargs[2] = value to be written
 *
 */
    public static void z_storew ()
    {

        fastmem.storew ( (header.zargs[0] + 2 * header.zargs[1]) & 0xffff, header.zargs[2]);

    }
}
