package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class WordVectorTest {

  @Test
  public void testEmptyWordVector() {
    try {
      new WordVector("", "");
      fail("Cannot have empty vector.");
    } catch (RuntimeException ex) {
      // Want this.
    }
  }

  @Test
  public void testMalformedVector() {
    try {
      new WordVector("", "a,b,c");
      fail("Invalid vector values.");
    } catch (RuntimeException ex) {
      // Do nothing.
    }

    try {
      new WordVector("", "1.3,4.5e3,3.1ee10");
      fail("Invalid vector values.");
    } catch (RuntimeException ex) {
      // Do nothing.
    }

    try {
      new WordVector("", "34,#,12.");
      fail("Invalid vector values.");
    } catch (RuntimeException ex) {
      // Do nothing.
    }

    try {
      new WordVector("", "4?1,4e--1");
      fail("Invalid vector values.");
    } catch (RuntimeException ex) {
      // Do nothing.
    }
  }

  @Test
  public void testConstructor() {
    WordVector vec;

    vec = new WordVector("something", "23,2.5,3.1");
    assertEquals(vec.getVector().get(), ImmutableList.of(23.0, 2.5, 3.1));

    vec = new WordVector("", "1");
    assertEquals(vec.getVector().get(), ImmutableList.of(1.0));

    vec = new WordVector("", "1.2343243");
    assertEquals(vec.getVector().get(), ImmutableList.of(1.2343243));

    vec = new WordVector("", "-1.2343243");
    assertEquals(vec.getVector().get(), ImmutableList.of(-1.2343243));

    vec = new WordVector("", "-1.3e12");
    assertEquals(vec.getVector().get(), ImmutableList.of(-1.3e12));

    vec = new WordVector("", "-1.3e-08");
    assertEquals(vec.getVector().get(), ImmutableList.of(-1.3e-8));

    vec = new WordVector("", "-1.3e-08,3.41111e23");
    assertEquals(vec.getVector().get(), ImmutableList.of(-1.3e-08, 3.41111e23));

    vec = new WordVector("",
        "-1.3e-08,3.41111e23,1e1,2e3,2.3e4,2.3e-04,-4,-5.2e56");
    assertEquals(vec.getVector().get(), ImmutableList.of(-1.3e-08, 3.41111e23,
        1.0e1, 2.0e3, 2.3e4, 2.3e-04, -4.0, -5.2e56));
  }

  @Test
  public void testMagnitude() {
    WordVector vec;

    vec = new WordVector("something", "3,4");
    assertEquals(vec.getMagnitude(), 5, 0.0001);

    vec = new WordVector("something", "1.324e45");
    assertEquals(vec.getMagnitude(), 1.324e45, 0.0001);

    vec = new WordVector("something", "1,2");
    assertEquals(vec.getMagnitude(), Math.sqrt(5), 0.0001);

    vec = new WordVector("something", "1,-2,4,5.00");
    assertEquals(vec.getMagnitude(), Math.sqrt(1 + 4 + 16 + 25), 0.0001);

    vec = new WordVector("something", "12,100,1");
    assertEquals(vec.getMagnitude(), Math.sqrt(144 + 10001), 0.0001);

    vec = new WordVector("something", "1,1,0.4");
    assertEquals(vec.getMagnitude(), Math.sqrt(2.16), 0.0001);

    vec = new WordVector("something", "-1,1,-0.4");
    assertEquals(vec.getMagnitude(), Math.sqrt(2.16), 0.0001);
  }

  @Test
  public void testSimilarity() {
    WordVector v1;
    WordVector v2;

    // try {
    // v1 = new WordVector("", "1,2");
    // v2 = new WordVector("", "1,3,8");
    // v1.similarity(v2);
    // fail("Similarity should have thrown a runtime exception.");
    // } catch (RuntimeException ex) {
    // // Do nothing.
    // }

    v1 = new WordVector("a", "1,2");
    v2 = new WordVector("", "3,1");
    assertEquals(v1.similarity(v2), v2.similarity(v1), 0.00001);
    assertEquals(v1.similarity(v2), 5 / Math.sqrt(50), 0.00001);

    v1 = new WordVector("a", "1");
    v2 = new WordVector("", "2");
    assertEquals(v1.similarity(v2), v2.similarity(v1), 0.00001);
    assertEquals(v1.similarity(v2), 1, 0.00001);

    v1 = new WordVector("a", "1,4,1");
    v2 = new WordVector("", "1,3,1");
    assertEquals(v1.similarity(v2), v2.similarity(v1), 0.00001);
    assertEquals(v1.similarity(v2), 14 / Math.sqrt(198), 0.00001);

    v1 = new WordVector("a", "1,4,2,5,3");
    v2 = new WordVector("", "0.3,1,1,1,1");
    assertEquals(v1.similarity(v2), v2.similarity(v1), 0.00001);
    assertEquals(v1.similarity(v2), 14.3 / Math.sqrt(55 * 4.09), 0.00001);

    v1 = new WordVector("a", "1,-4,-2,5,-3");
    v2 = new WordVector("", "1,1,1,1,-1");
    assertEquals(v1.similarity(v2), v2.similarity(v1), 0.00001);
    assertEquals(v1.similarity(v2), 3 / Math.sqrt(55 * 5), 0.00001);

    v1 = new WordVector("a", "0.04,0.1");
    v2 = new WordVector("", "-0.1,0.05");
    assertEquals(v1.similarity(v2), v2.similarity(v1), 0.00001);
    assertEquals(v1.similarity(v2), 0.001 / Math.sqrt(0.0125 * 0.0116),
        0.00001);

    v1 = new WordVector("a", "0.04,0.1");
    v2 = new WordVector("", "-0.1,0.04");
    assertEquals(v1.similarity(v2), v2.similarity(v1), 0.00001);
    assertEquals(v1.similarity(v2), 0, 0.00001);
  }
}
