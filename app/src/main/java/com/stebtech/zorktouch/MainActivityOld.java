package com.stebtech.zorktouch;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by alexanderstebner on 24.02.18.
 */

public class MainActivityOld extends AppCompatActivity {
    List<Integer> stack = new ArrayList<>();
    int PC = 0x0000;
    int dynamicMemorySize;
    int endOfStaticMemory;
    int highMemoryMark;
    int alphabetLocation;
    int largestWordSize;

    int routineOffset = 0x00;
    int stringsOffset = 0x00;
    int[] memory;
    int version;
    Alphabet alphabet = Alphabet.lowerCase;
    boolean shiftLock = false;
    List<String> abbreviationsTable = new ArrayList<>();
    List<String> dictionary = new ArrayList<>();

    List<String> separators = new ArrayList<>();

    int abbreviationsLocation;
    String[] extraCharacters = new String[97];
    String A0v5 = "", A1v5 = "", A2v5 = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        memory = loadAssetFileAsIntArray(this, "zork1.z5");
        if (memory == null) return;



        dynamicMemorySize = getShortAtAddress(0x0e);
        highMemoryMark = getShortAtAddress(0x04);
        endOfStaticMemory = Math.min(0xffff,memory.length-1);
        alphabetLocation = getShortAtAddress(0x34);
        abbreviationsLocation = getShortAtAddress(0x18);
        version = memory[0x00];


        retrieveAbbreviationsTable();

        retrieveDictionary();


        if (version == 6 || version == 7){
            routineOffset = memory[0x28];
            stringsOffset = memory[0x2a];
        }

        if (version >= 5 && alphabetLocation != 0x00){
            RetrieveExtraAlphabets();
        }



    }

    private void retrieveDictionary(){
        //String alex = zToString(readZCharacters(0x3b52,false));
        int location = getShortAtAddress(0x08);
        int amountSeparators = memory[location];

        for (int x = location+1; x < location+1+amountSeparators; x++){
            separators.add(getZSCII(memory[x]));
        }
        location += amountSeparators + 1;
        largestWordSize = memory[location];
        int dictionarySize = getShortAtAddress(++location);
        location += 2;
        int finalAddress = location + dictionarySize * largestWordSize;
        //Retrieve Words
        for (; location < finalAddress; location += largestWordSize){
            dictionary.add(zToString(readZCharacters(location,false)));
        }

    }



    private void retrieveAbbreviationsTable(){
        int amount = 32;
        if (version >= 3){
            amount = 96;
        }

        for (int x = abbreviationsLocation; x < abbreviationsLocation + 2*amount; x += 2){
            int location = getShortAtAddress(x);

            abbreviationsTable.add(zToString(readZCharacters(location*2, false)));
        }
    }

    private String zToString(List<Z_Char> chars){
        StringBuilder sb = new StringBuilder();
        for (Z_Char z : chars){
            if (z.value.startsWith("#") && z.value.endsWith("#")) continue;
            sb.append(z.value);
        }

        return sb.toString();
    }


    private void RetrieveExtraAlphabets(){
        StringBuilder sb0 = new StringBuilder();
        for (int x = alphabetLocation; x < alphabetLocation+26; x++){
            sb0.append((char)memory[x]);
        }
        A0v5 = "      "+sb0.toString();
        StringBuilder sb1 = new StringBuilder();
        for (int x = alphabetLocation+26; x < alphabetLocation+52; x++){
            sb1.append((char)memory[x]);
        }
        A1v5 = "      "+sb1.toString();
        StringBuilder sb2 = new StringBuilder();
        for (int x = alphabetLocation+52; x < alphabetLocation+78; x++){
            sb2.append((char)memory[x]);
        }
        A2v5 = "      "+sb2.toString();
    }

    private String getZSCII(int zscii) {
        if (version == 6) {
            if (zscii == 9) return "\t";
            if (zscii == 11) return "  ";
        }
        if (zscii == 13) return "\n";

        if (zscii == 27) return "#Escape#";
        if (zscii >= 32 && zscii <= 126) return ""+((char)zscii); //standard ASCII

        if (zscii == 129) return "#Up#";
        if (zscii == 130) return "#Down#";
        if (zscii == 131) return "#Left#";
        if (zscii == 132) return "#Right#";

        if (zscii >= 133 && zscii <= 144) return "#F"+(zscii-132)+"#";
        if (zscii >= 145 && zscii <= 154) return "#Num"+(zscii-145)+"#";
        if (zscii >= 155 && zscii <= 251) return extraCharacters[zscii-155];
        if (zscii == 252) return "#MenuClick#";
        if (zscii == 253) return "#DoubleClick#";
        if (zscii == 254) return "#SingleClick#";
        return "";
    }

    private List<Z_Char> readZCharacters(int location, boolean allowAbbreviation){
        int nextWord;
        boolean readMore = true;
        List<Z_Char> chars = new ArrayList<>();

        while (readMore) {
            nextWord = getShortAtAddress(location);
            chars.add(new Z_Char((nextWord >> 10) & 0x1F)); //1. zchar
            chars.add(new Z_Char((nextWord >>  5) & 0x1F)); //2. zchar
            chars.add(new Z_Char(nextWord & 0x1F));         //3. zchar


            if (nextWord >= 0x8000) readMore = false;
            location += 2;
        }

        int cnt = 0;
        boolean skip = false;
        boolean doubleSkip = false;

        for (Z_Char z : chars){
            if (z.intValue == 5){
                int a = 3;
                a++;
            }
            if (skip) {skip = false; continue;}
            if (doubleSkip) {skip = true; doubleSkip = false; continue;}


            changeAlphabet(z.intValue);
            if (version >= 3) shiftLock = false; //No shiftlock possible

            //Abbreviation String
            if ((version == 2 && z.intValue == 1) || (version >= 3 && z.intValue >= 1 && z.intValue <= 3)){

                if (cnt+1 >= chars.size()) break;

                int abbrev = z.intValue;
                int nextChar = chars.get(cnt+1).intValue;

                if (allowAbbreviation) z.value = abbreviationsTable.get(32 * (nextChar-1) + abbrev);

                //skip
                cnt++;
                skip = true;
                if (!shiftLock) alphabet = Alphabet.lowerCase;
                continue;
            }

            //Skip if shift character
            if ((version <= 2 && z.intValue >= 2 && z.intValue <= 5) || (version >= 3 && z.intValue >= 4 && z.intValue <= 5)){
                cnt++;

                continue;
            }

            //Start a ten-Bit z-character
            if (alphabet == Alphabet.punctuation && z.intValue == 6){
                if (cnt+1 >= chars.size()) break;

                int topBit = chars.get(cnt+1).intValue;
                int bottomBit = chars.get(cnt+2).intValue;
                int zscii = (topBit << 5) | bottomBit;

                z.value = getZSCII(zscii);

                cnt += 2;
                doubleSkip = true;
                if (!shiftLock) alphabet = Alphabet.lowerCase;
                continue;
            }

            //Set Character to alphabet table (if empty so far - see special rules above)
            String usedSet = getUsedAlphabet("");

            z.value = usedSet.substring(z.intValue,z.intValue+1);

            if (!shiftLock) alphabet = Alphabet.lowerCase;
            cnt++;
        }
        alphabet = Alphabet.lowerCase;
        return chars;
    }

    private String getUsedAlphabet(String byContent){
        String A0v1 = " \n    abcdefghijklmnopqrstuvwxyz";
        String A1v1 = " \n    ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String A2v1 = " \n     0123456789.,!?_#'\"/\\<-:()";
        String A0v2_4 = "      abcdefghijklmnopqrstuvwxyz";
        String A1v2_4 = "      ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String A2v2_4 = "       \n0123456789.,!?_#'\"/\\-:()";


        if (byContent.equals("")){ //Retrieve alphabet by current settings and version
            String usedSet = "";

            if (version == 1 && alphabet == Alphabet.lowerCase) usedSet = A0v1;
            if (version == 1 && alphabet == Alphabet.upperCase) usedSet = A1v1;
            if (version == 1 && alphabet == Alphabet.punctuation) usedSet = A2v1;
            if (version >= 2 && version <= 4 && alphabet == Alphabet.lowerCase) usedSet = A0v2_4;
            if (version >= 2 && version <= 4 && alphabet == Alphabet.upperCase) usedSet = A1v2_4;
            if (version >= 2 && version <= 4 && alphabet == Alphabet.punctuation) usedSet = A2v2_4;

            if (version >= 5){
                if (alphabetLocation != 0x00){ //Use special character sets
                    if (alphabet == Alphabet.lowerCase) usedSet = A0v5;
                    if (alphabet == Alphabet.upperCase) usedSet = A1v5;
                    if (alphabet == Alphabet.punctuation) usedSet = A2v5;
                } else {
                    if (alphabet == Alphabet.lowerCase) usedSet = A0v2_4;
                    if (alphabet == Alphabet.upperCase) usedSet = A1v2_4;
                    if (alphabet == Alphabet.punctuation) usedSet = A2v2_4;
                }
            }

            return usedSet;

        } else { //Retrieve alphabet by character in sets
            alphabet = Alphabet.lowerCase;
            if (version == 1) {
                if (byContent.equals(" ")) return A0v1;
                if (A0v1.contains(byContent)) return A0v1;
                if (A1v1.contains(byContent)) {
                    alphabet = Alphabet.upperCase;
                    return A1v1;
                }
                if (A2v1.contains(byContent)) {
                    alphabet = Alphabet.punctuation;
                    return A2v1;
                }
            } else if ((version >= 2 && version <= 4) || (version >= 5 && alphabetLocation == 0)) {
                if (byContent.equals(" ")) return A0v2_4;
                if (A0v2_4.contains(byContent)) return A0v2_4;
                if (A1v2_4.contains(byContent)) {
                    alphabet = Alphabet.upperCase;
                    return A1v2_4;
                }
                if (A2v2_4.contains(byContent)) {
                    alphabet = Alphabet.punctuation;
                    return A2v2_4;
                }
            } else { //has alphabet location
                if (byContent.equals(" ")) return A0v5;
                if (A0v5.contains(byContent)) return A0v5;
                if (A1v5.contains(byContent)) {
                    alphabet = Alphabet.upperCase;
                    return A1v5;
                }
                if (A2v5.contains(byContent)) {
                    alphabet = Alphabet.punctuation;
                    return A2v5;
                }
            }
        }
        return "";
    }

    private List<Integer> encryptInput(String input){
        input = input.toLowerCase();
        List<Integer> retVal = new ArrayList<>();
        List<Integer> zCodes = new ArrayList<>();

        Alphabet lastAlphabet = Alphabet.lowerCase;

        for (int x = 0; x < input.length(); x++){
            String substr = input.substring(x,x+1);
            String usedSet = getUsedAlphabet(substr);
            int location = usedSet.indexOf(substr);
            if (substr.equals(" ")) location = 0;

            zCodes.add(location);

            lastAlphabet = alphabet;
        }
        int padUntil = Math.max(6,(int)Math.ceil(zCodes.size()/3f) * 3);

        while (zCodes.size() < padUntil){
            zCodes.add(5);
        }

        //TODO: Improve later
        for (int x = 0; x < zCodes.size(); x += 3){
            int newVal = 0;
            newVal += zCodes.get(x) << 10;
            newVal += zCodes.get(x+1) << 5;
            newVal += zCodes.get(x+2);

            if (x == zCodes.size() - 3) newVal += 0x8000;

            retVal.add(newVal);
        }

        return retVal;
    }




    private void changeAlphabet(int z){
        if (z < 2 || z > 5) return;
        if (version < 3 && z >= 4) {
            shiftLock = true;
            z -= 2;
        }
        if (version >= 3 && z >= 4){
            shiftLock = false;
            alphabet = Alphabet.lowerCase;
            z -= 2;
        }

        if (alphabet == Alphabet.lowerCase && z == 2) alphabet = Alphabet.upperCase;
        else if (alphabet == Alphabet.lowerCase && z == 3) alphabet = Alphabet.punctuation;
        else if (alphabet == Alphabet.upperCase && z == 2) alphabet = Alphabet.punctuation;
        else if (alphabet == Alphabet.upperCase && z == 3) alphabet = Alphabet.lowerCase;
        else if (alphabet == Alphabet.punctuation && z == 2) alphabet = Alphabet.lowerCase;
        else if (alphabet == Alphabet.punctuation && z == 3) alphabet = Alphabet.upperCase;

    }


    private int decodePackedAddress(int packed, boolean isRoutineCall){
        if (version <= 3){
            return (packed * 2);
        }
        if (version == 4 || version == 5){
            return (packed * 4);
        }
        if (version == 6 || version == 7){
            if (isRoutineCall){
                return (packed * 4 + 8 * routineOffset);
            } else {
                return (packed * 4 + 8 * stringsOffset);
            }
        }
        if (version == 8){
            return (8*packed);
        }

        return 0;
    }

    private int getShortAtAddress(int address){
        int value = ((memory[address] << 8) | memory[address+1]);

        return value;
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
}



class Z_Char {
    String value = "";
    int intValue = 0;
    public Z_Char(int val){
        intValue = val;
    }
}

enum Alphabet {
    lowerCase, upperCase, punctuation
}