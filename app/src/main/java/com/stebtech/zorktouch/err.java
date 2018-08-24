package com.stebtech.zorktouch;

import android.util.Log;

/**
 * Created by alexanderstebner on 24.02.18.
 */

public class err {
    static int[] error_count = new int[header.ERR_NUM_ERRORS];

    static String err_messages[] = {
        "Text buffer overflow",
                "Store out of dynamic memory",
                "Division by zero",
                "Illegal object",
                "Illegal attribute",
                "No such property",
                "Stack overflow",
                "Call to illegal address",
                "Call to non-routine",
                "Stack underflow",
                "Illegal opcode",
                "Bad stack frame",
                "Jump to illegal address",
                "Can't save while in interrupt",
                "Nesting stream #3 too deep",
                "Illegal window",
                "Illegal window property",
                "Print at illegal address",
                "@jin called with object 0",
                "@get_child called with object 0",
                "@get_parent called with object 0",
                "@get_sibling called with object 0",
                "@get_prop_addr called with object 0",
                "@get_prop called with object 0",
                "@put_prop called with object 0",
                "@clear_attr called with object 0",
                "@set_attr called with object 0",
                "@test_attr called with object 0",
                "@move_object called moving object 0",
                "@move_object called moving into object 0",
                "@remove_object called with object 0",
                "@get_next_prop called with object 0"
    };

    static void init_err(){

         /* Initialize the counters. */

        for (int i = 0; i < header.ERR_NUM_ERRORS; i++)
            error_count[i] = 0;
    }

    static void runtime_error(int errnum){
        if (errnum <= 0 || errnum > header.ERR_NUM_ERRORS)
            return;

        long pc = header.GET_PC();
        Log.e("alex","Warning: " + err_messages[errnum-1]+" (PC = "+pc+")");

    }
}
