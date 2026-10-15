package com.jhy.inspien.common;

import java.util.Random;

public class OrderIdGenerator {
    private static final Random RANDOM = new Random();

    public static String generate() {
        char prefixChar = (char) ('A' + RANDOM.nextInt(26)); // 대문자 A-Z 중 1개
        int randomNumber = RANDOM.nextInt(1000);              // 0 ~ 999 숫자
        return String.format("%c%03d", prefixChar, randomNumber); // 형식: A113
    }
}