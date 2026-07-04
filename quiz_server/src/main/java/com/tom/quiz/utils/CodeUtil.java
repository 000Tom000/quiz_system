package com.tom.quiz.utils;

import java.util.Random;

/**
 * 验证码工具类
 */
public class CodeUtil {

    private static final Random RANDOM = new Random();

    public static String generate(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public static String generate() {
        return generate(6);
    }
}
