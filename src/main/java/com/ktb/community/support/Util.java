package com.ktb.community.support;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class Util {
    public static boolean checkStringLengthOrThrow(String str, long length){
        if(!str.isEmpty() && str.length() <= length){
            return true;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too Long content.");
    }
}
