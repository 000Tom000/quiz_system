package com.tom.quiz.utils;

import java.util.UUID;

/**
 * Token 工具类
 */
public class TokenUtil {

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
