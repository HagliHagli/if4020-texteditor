package com.texteditor.cipher.aes.test;

import com.texteditor.cipher.aes.AES;

public class AESTest {
    public static void main(String[] args) { 
        System.out.println("AES test.");
        AES aes = new AES();
        //String a = "e\u0007[hpp4Ç8Î\u008Cà\u001A\u0090¼¹?I×\u0005ÑÉW¯~x7\u008B-²ò¸";
        String result = aes.encrypt("string", "");
        System.out.println(result);
        System.out.println(aes.decrypt(result, "320265757102059730318470218759311257840"));
    }
}