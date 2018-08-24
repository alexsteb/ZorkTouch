package com.stebtech.zorktouch;

/**
 * Created by alexanderstebner on 25.02.18.
 */

public class sound {

    static int EFFECT_PREPARE = 1;
    static int EFFECT_PLAY = 2;
    static int EFFECT_STOP = 3;
    static int EFFECT_FINISH_WITH = 4;


    static int routine = 0;

    static int next_sample = 0;
    static int next_volume = 0;

    static boolean locked = false;
    static boolean playing = false;

    static void init_sound(){
        locked = false;
        playing = false;
    }
}
