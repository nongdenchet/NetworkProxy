package com.rain.networkproxy.support;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PairTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructor_whenFirstIsNull_shouldThrowException() {
        new Pair<String, String>(null, "second");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_whenSecondIsNull_shouldThrowException() {
        new Pair<String, String>("first", null);
    }

    @Test
    public void constructor_whenArgumentsNotNull() {
        Pair<String, String> pair = new Pair<>("first", "second");
        assertEquals("first", pair.first);
        assertEquals("second", pair.second);
    }
}
