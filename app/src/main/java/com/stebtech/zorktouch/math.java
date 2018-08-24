package com.stebtech.zorktouch;

/**
 * Created by alexanderstebner on 25.02.18.
 */

public class math {
    public static int intTo16BitSigned(int input){
        //Interpret int as 16 bit signed short
        input = 0xffff & input;
        if (input <= 0x7fff) return input;
        else return -(0x10000-input);
    }


    public static void z_add(){
        process.store(intTo16BitSigned(header.zargs[0]) + intTo16BitSigned(header.zargs[1]));
    }
    public static void z_and(){
        process.store(header.zargs[0] & header.zargs[1]);
    }

    public static void z_art_shift(){
        if (intTo16BitSigned(header.zargs[1]) > 0){
            process.store(intTo16BitSigned(header.zargs[0]) << intTo16BitSigned(header.zargs[1]));
        } else {
            process.store(intTo16BitSigned(header.zargs[0]) >> - intTo16BitSigned(header.zargs[1]));
        }
    }

    public static void z_div(){
        if (header.zargs[1] == 0)
            err.runtime_error(header.ERR_DIV_ZERO);

            process.store(intTo16BitSigned(header.zargs[0]) / intTo16BitSigned(header.zargs[1]));


    }

    public static void z_je ()
    {
        process.branch (
                header.zargc > 1 && (header.zargs[0] == header.zargs[1] || (
                        header.zargc > 2 && (header.zargs[0] == header.zargs[2] || (
                                header.zargc > 3 && (header.zargs[0] == header.zargs[3]))))));

    }

    public static void z_jg ()
    {
        process.branch (intTo16BitSigned(header.zargs[0]) > intTo16BitSigned(header.zargs[1]));

    }

    public static void z_jl ()
    {
        process.branch (intTo16BitSigned(header.zargs[0]) < intTo16BitSigned(header.zargs[1]));

    }

    public static void z_jz ()
    {
        process.branch (intTo16BitSigned(header.zargs[0]) == 0);

    }

    public static void z_log_shift(){
        if (intTo16BitSigned(header.zargs[1]) > 0){
            process.store(header.zargs[0] << intTo16BitSigned(header.zargs[1]));
        } else {
            process.store(header.zargs[0] >> - intTo16BitSigned(header.zargs[1]));
        }
    }

    public static void z_mod(){
        if (header.zargs[1] == 0)
            err.runtime_error(header.ERR_DIV_ZERO);

            process.store(intTo16BitSigned(header.zargs[0]) % intTo16BitSigned(header.zargs[1]));


    }

    public static void z_mul(){

            process.store(intTo16BitSigned(header.zargs[0]) * intTo16BitSigned(header.zargs[1]));


    }

    public static void z_not(){

        process.store(~header.zargs[0]);
    }

    public static void z_or(){

        process.store(header.zargs[0] | header.zargs[1]);
    }

    public static void z_sub(){

        process.store(intTo16BitSigned(header.zargs[0]) - intTo16BitSigned(header.zargs[1]));


    }

    public static void z_test ()
    {
        process.branch ((header.zargs[0] & header.zargs[1]) == header.zargs[1]);

    }
}
