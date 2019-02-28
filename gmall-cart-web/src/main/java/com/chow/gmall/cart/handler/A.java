package com.chow.gmall.cart.handler;

import java.util.ArrayList;
import java.util.List;

public class A {

    public static void main(String[] args) {
        List<String > list = new ArrayList<>();
        list.add("1");
        for (String s : list) {
            list.add("1111");
        }
//        for (int i = 0; i <list.size() ; i++) {
//            list.add(i+"");
//        }
    }
}
