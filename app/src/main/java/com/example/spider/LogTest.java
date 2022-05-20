package com.example.spider;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LogTest {
    static void test(){
        Log.i("hello","test change ");
    }

    static void before(){
        Log.i("hello","i am  a before method");
    }

    static void after(){
        Log.i("hello","i am an after method");


    }
}
