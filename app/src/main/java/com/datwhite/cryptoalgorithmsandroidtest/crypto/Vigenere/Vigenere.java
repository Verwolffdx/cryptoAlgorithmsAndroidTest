package com.datwhite.cryptoalgorithmsandroidtest.crypto.Vigenere;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

public class Vigenere {
    private static Alphabet alphabet = new Alphabet();

    public static String encrypt (String word, String keyword) {
        String result = "";
        String[] words = word.split(" ");

        LinkedList<String> list = new LinkedList<>(Arrays.asList(alphabet.alphabet));
        int n = list.size();

        for (String s : words) {
            for(int i = 0, j = 0; i < s.length(); i++, j++) {
                if (j == keyword.length()) { j = 0; }
                int c1 = list.indexOf(String.valueOf(s.charAt(i)));
                int c2 = list.indexOf(String.valueOf(keyword.charAt(j)));
                int c = (c1 + c2) % n;

                result += list.get(c);
            }
            result += " ";
        }



        return result;
    }

    public static String decrypt (String word, String keyword) {
        String result = "";
        String[] words = word.split(" ");

        LinkedList<String> list = new LinkedList<>(Arrays.asList(alphabet.alphabet));
        int n = list.size();
        for (String s : words) {
            for(int i = 0, j = 0; i < s.length(); i++, j++) {
                if (j == keyword.length()) { j = 0; }
                int c1 = list.indexOf(String.valueOf(s.charAt(i)));
                int c2 = list.indexOf(String.valueOf(keyword.charAt(j)));
                int c = (c1 + n - c2) % n;

                result += list.get(c);
            }
            result += " ";
        }

        return result;
    }
}
