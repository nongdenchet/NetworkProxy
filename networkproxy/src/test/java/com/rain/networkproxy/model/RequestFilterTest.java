package com.rain.networkproxy.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RequestFilterTest {

    @Test
    public void isMatch_checkAgainstResources() {
        RequestFilter requestFilter = new RequestFilter(Collections.singletonList("https://www.test.com/todos/*"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/1"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/1/"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/123e4567-e89b-12d3-a456-426655440000"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos"));
    }

    @Test
    public void isMatch_checkAgainstPrefix() {
        RequestFilter requestFilter = new RequestFilter(Collections.singletonList("/todos/*"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/1"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/1/"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/123e4567-e89b-12d3-a456-426655440000"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos"));
    }

    @Test
    public void isMatch_checkAgainstPrefixWithoutSlash() {
        RequestFilter requestFilter = new RequestFilter(Collections.singletonList("todos/*"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/1"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/1/"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/123e4567-e89b-12d3-a456-426655440000"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos"));
    }

    @Test
    public void isMatch_checkWithoutResources() {
        RequestFilter requestFilter = new RequestFilter(Collections.singletonList("/todos/"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/1"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/123e4567-e89b-12d3-a456-426655440000"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos?query=1"));
    }

    @Test
    public void isMatch_checkWithoutResourcesWithoutSlash() {
        RequestFilter requestFilter = new RequestFilter(Collections.singletonList("/todos"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/1"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/123e4567-e89b-12d3-a456-426655440000"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos?query=1"));
    }

    @Test
    public void isMatch_exact() {
        RequestFilter requestFilter = new RequestFilter(Collections.singletonList("https://www.test.com/todos"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/1"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/1/"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos?query=1"));
    }

    @Test
    public void isMatch_againstMultipleRules() {
        RequestFilter requestFilter = new RequestFilter(Arrays.asList(
                "/todos",
                "/todos/*",
                "/todos/*/comments/*",
                "/todos/*/comments/*/users"
        ));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/1"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/1/"));
        assertTrue(requestFilter.isMatch("/todos/1/"));
        assertTrue(requestFilter.isMatch("todos/1/"));
        assertTrue(requestFilter.isMatch("www.test.com/todos"));
        assertTrue(requestFilter.isMatch("www.test.com/todos/"));
        assertTrue(requestFilter.isMatch("todos?query=1"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/1/comments"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/1/comments/"));
        assertTrue(requestFilter.isMatch("/todos/1/comments/1"));
        assertTrue(requestFilter.isMatch("/todos/1/comments/1/"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/1/comments/1/users"));
        assertTrue(requestFilter.isMatch("https://www.test.com/todos/1/comments/1/users/"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/1/comments/1/users/20"));
        assertFalse(requestFilter.isMatch("https://www.test.com/todos/1/comments/1/users/20/"));
        assertFalse(requestFilter.isMatch("/comments"));
    }
}
