package com.stebtech.zorktouch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import com.stebtech.zorktouch.header;


public class MainActivity extends AppCompatActivity {




    public static String game_file = "zork1.z5";

    public static TextView output;



    static MainActivity mActivity;
   public void init(){

       output = findViewById(R.id.output);

        /* Story file name, id number and size */

       header.story_name = "";
       header.story_id = story.UNKNOWN;
       header.story_size = 0;

    /* Story file header data */

       header.h_version = 0;
       header.h_config = 0;
       header.h_release = 0;
       header. h_resident_size = 0;
       header. h_start_pc = 0;
       header. h_dictionary = 0;
       header. h_objects = 0;
       header. h_globals = 0;
       header. h_dynamic_size = 0;
       header. h_flags = 0;
       header.h_serial = new int[]{ 0, 0, 0, 0, 0, 0 };
       header.h_abbreviations = 0;
       header.h_file_size = 0;
       header.h_checksum = 0;
       header.h_interpreter_number = 0;
       header.h_interpreter_version = 0;
       header.h_screen_rows = 0;
       header.h_screen_cols = 0;
       header.h_screen_width = 0;
       header.h_screen_height = 0;
       header.h_font_height = 0;
       header.h_font_width = 0;
       header.h_functions_offset = 0;
       header.h_strings_offset = 0;
       header.h_default_background = 0;
       header.h_default_foreground = 0;
       header.h_terminating_keys = 0;
       header.h_line_width = 0;
       header.h_standard_high = 0;
       header.h_standard_low = 0;
       header.h_alphabet = 0;
       header.h_extension_table = 0;
       header.h_user_name = new int[] { 0,0, 0, 0, 0, 0, 0, 0 };

       header.hx_table_size = 0;
       header.hx_mouse_x = 0;
       header.hx_mouse_y = 0;
       header.hx_unicode_table = 0;

    /* Stack data */

       header.stack = new int[header.STACK_SIZE];
       for (int x = 0; x < header.STACK_SIZE; x++)
           header.stack[x] = 0;

       header.sp = 0;
       header.fp = 0;
       header.frame_count = 0;

    /* IO streams */

       header.ostream_screen = true;
       header.ostream_script = false;
       header.ostream_memory = false;
       header.ostream_record = false;
       header.istream_replay = false;
       header.message = false;

    /* Current window and mouse data */

       header.cwin = 0;
       header.mwin = 0;

       header.mouse_y = 0;
       header.mouse_x = 0;

    /* Window attributes */

       header.enable_wrapping = false;
       header.enable_scripting = false;
       header.enable_scrolling = false;
       header.enable_buffering = false;


    /* Size of memory to reserve (in bytes) */

       header.reserve_mem = 0;

       //Opcode Args
       for (int x = 0; x < header.zargs.length;x++){
           header.zargs[x] = 0;
       }

        for (int x = 0; x < screen.wp.length; x++)
            screen.wp[x] = new Zwindow();
   }

    //Memory data
    static int[] memory;


    /*
 * z_piracy, branch if the story file is a legal copy.
 *
 *	no zargs used
 *
 */
    void z_piracy()
    {
        //branch (!f_setup.piracy);

    }/* z_piracy */

    public static EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;

        editText = (EditText) findViewById(R.id.editText);
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SentenceSubmitted();
                    editText.setText("");
                    return true;
                }
                return false;
            }
        });
        init();

        //Initializations
        memory = loadAssetFileAsIntArray(this, game_file);
        if (memory == null) return;

        //os_init_setup();
        //os_process_arguments();
        buffer.init_buffer();
        err.init_err();
        fastmem.init_memory();
        process.init_process();
        sound.init_sound();
        //os_init_screen();
        //init_undo();
        fastmem.z_restart();




        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                process.interpret(); //Run the game
            }
        });
        t1.start();

    }


    private int[] loadAssetFileAsIntArray(Context context, String name) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(assetManager.open(name));

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int theByte;


            while ((theByte = bis.read()) != -1) {
                buffer.write(theByte);
            }


            bis.close();
            buffer.close();
            byte[] bytes = buffer.toByteArray();
            int ints[] = new int[bytes.length];

            for (int x = 0; x < bytes.length; x++){
                ints[x] = bytes[x] & 0xff;
            }

            return ints;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    static StringBuilder sbuffer = new StringBuilder();
    public static void OutputText(String text){
        if (text.endsWith("\n")) {
            final String finalText = sbuffer.toString();
            //Log.i("alex",">>>>>>>>>>>>>>>>>>>>>> "+sbuffer.toString());
            mActivity.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    output.setText(output.getText() + finalText + "\n");
                }
            });
            sbuffer = new StringBuilder();
            return;
        }
        sbuffer.append(text);
        //Log.i("alex","added: "+text+"/ ended in "+(int)text.toCharArray()[text.length()-1]);
    }


    public static String get_input_text(){
        try {
            if (editText.getText().length() == 0) return "";
            return editText.getText().toString();
        } catch (IndexOutOfBoundsException e){
            return "";
        }
    }


    public String updateVal = "";
    public void set_input_text(String val){
        updateVal = val;
        /*
        runOnUiThread(new Runnable() {
            public void run() {
                // Update UI elements
                editText.setText(updateVal);
            }
        });
            */
    }



    static boolean submit = false;
    public static void SentenceSubmitted(){
        submit = true;
    }

    public void Submit(View view){
        submit = true;
        editText.setText("");
    }


    public static String input_text = "";
    public void InputText(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                input_text = input.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void OpenTextField(View view) {
        InputText();
    }

    public static long input_timeout = 0;
    public static int input_timeout_routine = 0;
    public static char[] stream_read_input(int max, int timeout, int timeoutRoutine, boolean enableHotkeys, boolean isV6) {
        if (timeout > 0){
            input_timeout = System.currentTimeMillis() + timeout*100;
            input_timeout_routine = timeoutRoutine;
        }

        sbuffer = new StringBuilder(); //Don't have any text waiting, while waiting for input

        while (get_input_text().length() < max && submit == false){
            if (input_timeout != 0 && input_timeout < System.currentTimeMillis()){
                process.direct_call(input_timeout_routine);
            }
        }
        String text = get_input_text();
        mActivity.set_input_text("");
        submit = false;
        max = text.length();
        return text.substring(0,max).toCharArray();
    }

    public static char stream_read_key(int timeout, int timeoutRoutine, boolean enableHotkeys) {
        if (timeout > 0){
            input_timeout = System.currentTimeMillis() + timeout*100;
            input_timeout_routine = timeoutRoutine;
        }

        while (get_input_text().length() == 0 && submit == false){
            if (input_timeout < System.currentTimeMillis()){
                process.direct_call(input_timeout_routine);
            }
        }
        String text = get_input_text();
        mActivity.set_input_text("");
        submit = false;
        return text.toCharArray()[0];
    }


    public void ShowDictionary(View view){
        int pos = header.HIGH_WORD(0x08);
        int amountInputCodes = memory[pos++];
        for (int x = 0; x < amountInputCodes; x++){
            pos++;
        }
        int entryLength = memory[pos++];
        int numberOfEntries = header.LOW_WORD(pos);
        pos += 2;

        for (int x = 0; x < numberOfEntries; x++){
            for (int y = 0; y< text.decoded.length; y++)
                text.decoded[y] = 0;
            text.decode_text(string_type.VOCABULARY, pos);
            int end = 0;
            for (int y = 0; y < text.decoded.length; y++) {
                if (text.decoded[y] == 0) {
                    end = y;
                    break;
                }
            }
            String txt = String.copyValueOf(text.decoded).substring(0,end).trim();
            Log.i("alex",x+". "+txt + " ("+pos+")");

            pos += entryLength;
        }
    }


    public static treeObj[] allObj;

    public void ShowObjectTree(View view) {
        int amount = header.h_version <= 3? 256 : 256*256;
        treeObj first = new treeObj("",0,0,0,0, allObj);
        allObj = new treeObj[amount];
        int pos = header.HIGH_WORD(0x0a);
        pos += header.h_version <= 3? 0x3e : 0x7e;
        int maxAddressObjectTable = memory.length-16;
        for (int x = 0; x < amount; x++){
            if (pos > maxAddressObjectTable) break; //Read only until property table

            int att1, att2,parent,sibling,child,propertyAdd,textSize;
            if (amount == 256) {
                att1 = header.HIGH_WORD(pos);pos += 2;
                att2 = header.HIGH_WORD(pos);pos += 2;
                parent = header.LOW_BYTE(pos) - 1;pos++;
                sibling = header.LOW_BYTE(pos) - 1;pos++;
                child = header.LOW_BYTE(pos) - 1;pos++;
                propertyAdd = header.HIGH_WORD(pos);pos += 2;
                textSize = memory[propertyAdd];
            } else {
                att1 = header.HIGH_WORD(pos);pos += 2;
                att2 = header.HIGH_WORD(pos);pos += 2;
                int att3 = header.HIGH_WORD(pos);pos += 2;
                parent = header.LOW_WORD(pos) - 1;pos+=2;
                sibling = header.LOW_WORD(pos) - 1;pos+=2;
                child = header.LOW_WORD(pos) - 1;pos+=2;
                propertyAdd = header.HIGH_WORD(pos);pos += 2;
                textSize = memory[propertyAdd];
            }
            if (propertyAdd < maxAddressObjectTable) maxAddressObjectTable = propertyAdd;

            for (int y = 0; y< text.decoded.length; y++)
                text.decoded[y] = 0;
            if (textSize > 0) text.decode_text(string_type.VOCABULARY, propertyAdd+1, textSize);
            int end = 0;
            for (int y = 0; y < text.decoded.length; y++) {
                if (text.decoded[y] == 0) {
                    end = y;
                    break;
                }
            }
            if (x == 81){
                int alex = 3;
                alex++;
            }
            String txt = String.copyValueOf(text.decoded).substring(0,end).trim();
            //Log.i("alex","Obj "+x+": atts: "+att1+","+att2+", parent: "+parent+", sibling: "+sibling+", child: "+child+", propertyAddress: "+propertyAdd+"::: "+txt);
            allObj[x] = new treeObj(txt,x,parent,sibling,child,allObj);
        }

        //Connect tree branches

        for (int x = 0; x < allObj.length; x++) {
            if (allObj[x] == null) break;

            if (allObj[x].p == -1) {
                first.children.add(allObj[x]);
                allObj[x].findAllChildren();
            }
        }







        /*for (int x = 1; x < 200; x++) {
            text.decode_text(string_type.VOCABULARY, object.object_name(x) + 1);
            int end = 0;
            for (int y = 0; y < text.decoded.length; y++) {
                if (text.decoded[y] == 0) {
                    end = y;
                    break;
                }
            }
            int length = memory[object.object_name(x)];
            if (length != 0) length = end;
            if (String.copyValueOf(text.decoded).substring(0, Math.min(end, length)).equals("mailbox")){
                int alex = 3;
                alex++;
            }
            Log.i("alex", x + ". " + String.copyValueOf(text.decoded).substring(0, Math.min(end, length)));
            Log.i("alex", "parent: " + object.get_parent(x) + ", child: " + object.get_child(x) + ", sibling: " + object.get_sibling(x));
            for (int y = 0; y < text.decoded.length; y++) {
                text.decoded[y] = 0;
            }
        }*/
    }
}


class treeObj {

    int id = 0;
    int p,s,c;
    treeObj parent;
    List<treeObj> children = new ArrayList<>();
    String name = "";
    treeObj[] all;
    public treeObj(String name, int id, int p, int s, int c, treeObj[] allRef){
        this.id = id;
        this.name = name;
        this.p = p; this.s = s; this.c = c;
        this.all = allRef;
    }

    public String toString(){
        return name;
    }

    public void findAllChildren(){
        if (c > -1){ //has 1..n children
            if (!children.contains(all[c])) {
                children.add(all[c]);
                all[c].parent = this;

                treeObj cobj = all[c];
                if (cobj == null) return;

                cobj.findAllChildren();

                while (cobj.s > -1){ //Find all siblings of child
                    if (children.contains(all[cobj.s])) break;
                    children.add(all[cobj.s]);
                    cobj = all[cobj.s];
                    if (cobj == null) break;
                    cobj.findAllChildren();


                }
            }

        }
    }
}

class RNG {
    private static Random r = new Random();


    public static void setSeed(int seed){
        if (seed == 0) {setToRandom(); return;}
        r.setSeed(seed);
    }

    public static void setToRandom(){
        r.setSeed(System.currentTimeMillis());
    }


    public static int createRandom(int max){
        return r.nextInt(max) + 1;
    }
}




