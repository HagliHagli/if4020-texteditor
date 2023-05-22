package com.texteditor.cipher.hanzo.test;

import java.math.BigInteger;
import java.util.ArrayList;

import com.texteditor.cipher.hanzo.Hanzo;

public class HanzoTest {
    public static void main(String[] args) { 
        System.out.println("Hanzo test.");
        Hanzo hanzo = new Hanzo();
        //String a = "e\u0007[hpp4Ç8Î\u008Cà\u001A\u0090¼¹?I×\u0005ÑÉW¯~x7\u008B-²ò¸";
        ArrayList<Integer> result = hanzo.string_encrypt("thispoi", new BigInteger("320265757102059730318470218759311257840"));
        String cipher = result.toString();
        cipher = cipher.replace(" ","");
        cipher = cipher.replace("[","");
        cipher = cipher.replace("]","");
        System.out.println(cipher);
        System.out.println(hanzo.string_decrypt(cipher, new BigInteger("320265757102059730318470218759311257840")));
    }
}