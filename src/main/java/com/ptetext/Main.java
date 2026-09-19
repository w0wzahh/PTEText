package com.ptetext;

import com.ptetext.ui.ConsoleApp;

/**
 * Entry point for PTEText.
 *
 * Run with:  mvn compile exec:java
 * Or build the jar with:  mvn package  ->  java -jar target/ptetext-*.jar
 */
public class Main {

    public static void main(String[] args) {
        new ConsoleApp().run();
    }
}
