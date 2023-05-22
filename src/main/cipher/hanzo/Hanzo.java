package com.texteditor.cipher.hanzo;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Hanzo {

    public void HanzoEncryptor(){}

    private final ArrayList<Integer> s_box = new ArrayList<Integer>(Arrays.asList(3, 12, 4, 9, 8, 10, 14, 1, 6, 13, 7, 0, 5, 15, 2, 11));

    private final ArrayList<Integer> p_layer_order = new ArrayList<Integer>(Arrays.asList(0, 16, 32, 48, 1, 17, 33, 49, 2, 18, 34, 50, 3, 19, 35, 51, 4, 20, 36, 52, 5, 21, 37, 53, 6, 22, 38, 54, 7, 23, 39, 55, 8, 24, 40, 56, 9, 25, 41, 57, 10, 26, 42, 58, 11, 27, 43, 59, 12, 28, 44, 60, 13, 29, 45, 61, 14, 30, 46, 62, 15, 31, 47, 63));

    public BigInteger round_function(BigInteger state, BigInteger key) {
        BigInteger new_state = state.xor(key);

        ArrayList<Integer> state_nibs = new ArrayList<Integer>();
        int block_size = 64;
        for (int x = 0; x < block_size; x += 4) {
            BigInteger nib = (new_state.shiftRight(x));
            BigInteger F = new BigInteger("15");
            nib = nib.and(F);
            int sb_nib = s_box.get(nib.intValue());
            state_nibs.add(sb_nib);
        }

        ArrayList<Integer> state_bits = new ArrayList<Integer>();
        for (Integer state_nib : state_nibs) {
            String t = Integer.toBinaryString(state_nib);
            StringBuilder string_builder = new StringBuilder(t).reverse();
            t = string_builder.toString();
            for (int i = 0; i < 4; i += 1) {
                if (i >= t.length()) {
                    state_bits.add(0);
                } else {
                    int a = t.charAt(i) - '0';
                    state_bits.add(a);
                }
            }
        }

        ArrayList<Integer> state_p_layer = new ArrayList<Integer>(Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0));
        for (int i = 0; i < 64; i += 1) {
            state_p_layer.set(p_layer_order.get(i), state_bits.get(i));
        }

        BigInteger round_output = new BigInteger("0");
        for (int i = 0; i < 64; i += 1) {
            BigInteger mn = BigInteger.valueOf(state_p_layer.get(i));
            mn = mn.shiftLeft(i);
            round_output = round_output.add(mn);
        }

        return round_output;
    }

    public BigInteger key_function(BigInteger key, int round_count) {
        ArrayList<Integer> r = new ArrayList<Integer>();
        String a = key.toString(2);
        StringBuilder string_builder = new StringBuilder(a).reverse();
        a = string_builder.toString();
        for (int i = 0; i < 128; i += 1) {
            if (i >= a.length()) {
                r.add(0);
            }
            else {
                int t = a.charAt(i) - '0';
                r.add(t);
            }
        }

        List<Integer> head = r.subList(0, 67);
        List<Integer> tail = r.subList(67, 128);
        tail.addAll(head);
        ArrayList<Integer> h = new ArrayList<Integer>(tail);

        BigInteger round_key_int = new BigInteger("0");
        for (int i = 0; i < 128; i += 1) {
            BigInteger mn = BigInteger.valueOf(h.get(i));
            mn = mn.shiftLeft(i);
            round_key_int = round_key_int.add(mn);
        }

        BigInteger F = new BigInteger("15");
        BigInteger upper_nibble = round_key_int.shiftRight(124);
        upper_nibble = upper_nibble.and(F);
        BigInteger second_nibble = round_key_int.shiftRight(120);
        second_nibble = second_nibble.and(F);
        upper_nibble = BigInteger.valueOf(s_box.get(upper_nibble.intValue()));
        second_nibble = BigInteger.valueOf(s_box.get(second_nibble.intValue()));

        BigInteger xor_portion = round_key_int.shiftRight(62);
        BigInteger F1 = new BigInteger("31");
        xor_portion = xor_portion.and(F1);
        xor_portion = xor_portion.xor(BigInteger.valueOf(round_count));

        upper_nibble = upper_nibble.shiftLeft(124);
        second_nibble = second_nibble.shiftLeft(120);
        xor_portion = xor_portion.shiftLeft(62);
        BigInteger F2 = new BigInteger("1329227995784915729941540489031319551");

        round_key_int = round_key_int.and(F2);
        round_key_int = round_key_int.add(upper_nibble);
        round_key_int = round_key_int.add(second_nibble);
        round_key_int = round_key_int.add(xor_portion);

        return round_key_int;
    }

    public String encrypt (BigInteger block, BigInteger ext_key) {
        ArrayList<BigInteger> key_schedule = new ArrayList<BigInteger>();
        BigInteger current_round_key = ext_key;
        BigInteger round_state = block;

        int ROUND_LIMIT = 16;
        for (int i = 0; i < ROUND_LIMIT +1; i += 1) {
            key_schedule.add(current_round_key.shiftRight(64));
            current_round_key = key_function(current_round_key, i+1);
        }

        for (int i = 0; i < ROUND_LIMIT; i += 1) {
            BigInteger upper_64 = round_state.shiftRight(64);
            BigInteger F = new BigInteger("18446744073709551615");
            BigInteger lower_64 = round_state.and(F);
            BigInteger rounded_lower = round_function(lower_64, key_schedule.get(i));
            BigInteger rounded_upper = upper_64.xor(rounded_lower);
            round_state = lower_64.shiftLeft(64);
            round_state = rounded_upper.add(round_state);
        }

        round_state = round_state.xor(key_schedule.get(ROUND_LIMIT));

        return String.format("%064x", round_state);

    }

    public ArrayList<Integer> string_encrypt(String string, BigInteger key) {
        byte[] arr_0 = string.getBytes(StandardCharsets.US_ASCII);
        int act_length = arr_0.length;
        if (arr_0.length % 4 != 0) {
            act_length = act_length + 4 - (arr_0.length % 4);
        }
        byte[] arr = new byte[act_length];
        for (int i = 0; i < act_length; i += 1) {
            if (i >= arr_0.length - (arr_0.length % 4)) {
                int k = 0;
                for (int j = 0; j < 4 - (arr_0.length % 4); j += 1) {
                    k = i+j;
                    arr[k] = (byte) 0x00;
                }
                for (int j = 0; j < arr_0.length % 4; j += 1) {
                    arr[k+j+1] = arr_0[i+j];
                }
                break;
            }
            else {
                arr[i] = arr_0[i];
            }
        }


        ArrayList<BigInteger> blocks = new ArrayList<BigInteger>();
        for (int i = 0; i < act_length; i += 4) {
            ByteBuffer wrapped = ByteBuffer.wrap(Arrays.copyOfRange(arr, i, i + 4));
            blocks.add(new BigInteger(Integer.toString(wrapped.getInt())));
        }

        ArrayList<BigInteger> enc_blocks = new ArrayList<>();
        for (BigInteger block : blocks) {
            String res = encrypt(block, key);
            enc_blocks.add(new BigInteger(res, 16));
        }

        ArrayList<byte[]> enc_stream_byte = new ArrayList<>();
        for (int i = 0; i < enc_blocks.size(); i += 1) {
            enc_stream_byte.add(enc_blocks.get(i).toByteArray());
        }



        ArrayList<Integer> kek = new ArrayList<>();
        for (byte[] stream : enc_stream_byte) {
            for (byte b : stream) {
                int i = b;
                if (i < 0) {
                    i += 256;
                }
                kek.add(i);
            }
        }

        String enc_stream = "";
        for (int a : kek) {
            enc_stream = enc_stream + (char) a;
        }

        return kek;
    }

    public String decrypt (BigInteger block, BigInteger ext_key) {
        ArrayList<BigInteger> key_schedule = new ArrayList<BigInteger>();
        BigInteger current_round_key = ext_key;
        BigInteger round_state = block;

        int ROUND_LIMIT = 16;
        for (int i = 0; i < ROUND_LIMIT +1; i += 1) {
            key_schedule.add(current_round_key.shiftRight(64));
            current_round_key = key_function(current_round_key, i+1);
        }

        round_state = round_state.xor(key_schedule.get(ROUND_LIMIT));

        for (int i = ROUND_LIMIT-1; i >= 0; i -= 1) {
            BigInteger upper_64 = round_state.shiftRight(64);
            BigInteger F = new BigInteger("18446744073709551615");
            BigInteger lower_64 = round_state.and(F);
            BigInteger rounded_upper = round_function(upper_64, key_schedule.get(i));
            BigInteger prev_upper = lower_64.xor(rounded_upper);
            round_state = prev_upper.shiftLeft(64);
            round_state = upper_64.add(round_state);
        }

        return String.format("%064x", round_state);
    }

    public String string_decrypt (String byte_stream_string, BigInteger key) {

        ArrayList<Integer> a = new ArrayList<>();
        ArrayList<String> arrayList = new ArrayList<String> (Arrays.asList(byte_stream_string.split(",")));
        for(String ch:arrayList){
            a.add(Integer.parseInt(ch.trim()));
        }
        /*for (char p : byte_stream_string.toCharArray()) {
            int p0 = (int) p;
            /*if (p0 > 127) {
                p0 -= 256;
            }
            a.add(p0);
        }*/
        byte[] byte_stream = new byte[a.size()];
        for (int i = 0; i < a.size(); i += 1) {
            byte_stream[i] = a.get(i).byteValue();
        }

        ArrayList<BigInteger> blocks = new ArrayList<BigInteger>();
        for (int i = 0; i < byte_stream.length; i += 16) {
            blocks.add(new BigInteger(Arrays.copyOfRange(byte_stream, i,i+16)));
        }

        ArrayList<BigInteger> dec_blocks = new ArrayList<>();
        for (BigInteger block : blocks) {
            String res = decrypt(block, key);
            dec_blocks.add(new BigInteger(res, 16));
        }

        String dec_string = "";
        for (BigInteger block : dec_blocks) {
            dec_string = dec_string + new String(block.toByteArray());
        }

        return dec_string;
    }


}
