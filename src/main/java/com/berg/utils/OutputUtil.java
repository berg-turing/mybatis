package com.berg.utils;

import java.util.List;

public class OutputUtil {

    public static <T> void OutputList(List<T> list){
        for (T t : list) {
            System.out.println(t.toString());
        }
    }
}
