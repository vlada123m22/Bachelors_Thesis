package com.example.timesaver.service;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PipeListTest {

    @Test
    public void testJoinNormal() {
        List<String> items = Arrays.asList("A", "B", "C");
        assertEquals("A|B|C", PipeList.join(items));
    }

    @Test
    public void testJoinWithDuplicatesAndWhitespace() {
        List<String> items = Arrays.asList(" A ", "B", "A", null, "", "C ");
        assertEquals("A|B|C", PipeList.join(items));
    }

    @Test
    public void testJoinEmpty() {
        assertNull(PipeList.join(null));
        assertNull(PipeList.join(Collections.emptyList()));
        assertNull(PipeList.join(Arrays.asList(null, "  ", "")));
    }

    @Test
    public void testJoinWithPipeThrows() {
        List<String> items = Arrays.asList("A", "B|C");
        assertThrows(IllegalArgumentException.class, () -> PipeList.join(items));
    }

    @Test
    public void testSplitNormal() {
        String s = "A|B|C";
        List<String> expected = Arrays.asList("A", "B", "C");
        assertEquals(expected, PipeList.split(s));
    }

    @Test
    public void testSplitWithWhitespace() {
        String s = " A | Borrow Milk | C ";
        List<String> expected = Arrays.asList("A", "Borrow Milk", "C");
        assertEquals(expected, PipeList.split(s));
    }

    @Test
    public void testSplitEmpty() {
        assertTrue(PipeList.split(null).isEmpty());
        assertTrue(PipeList.split("").isEmpty());
        assertTrue(PipeList.split("   ").isEmpty());
        assertTrue(PipeList.split("|||").isEmpty());
    }

    @Test
    public void testSplitSingleValue() {
        assertEquals(Arrays.asList("A"), PipeList.split("A"));
        assertEquals(Arrays.asList("A"), PipeList.split(" A "));
    }

    @Test
    public void testJoinSingleValue() {
        assertEquals("A", PipeList.join(Arrays.asList("A")));
        assertEquals("A", PipeList.join(Arrays.asList(" A ")));
    }
}