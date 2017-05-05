package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import edu.brown.cs.termproject.scoring.LedCorrector.LedIterator;
import org.junit.Test;

public class LedCorrectorTest {

  @Test
  public void iteratorTest() {
    LedIterator iter = new LedIterator("test");
    assertEquals(iter.next(), "est");
    assertEquals(iter.next(), "tst");
    assertEquals(iter.next(), "tet");
    assertEquals(iter.next(), "tes");
    assertEquals(iter.next(), "aest");
    assertEquals(iter.next(), "best");
    assertEquals(iter.next(), "cest");
    assertEquals(iter.next(), "dest");
    assertEquals(iter.next(), "eest");
    assertEquals(iter.next(), "fest");
    assertEquals(iter.next(), "gest");

    iter = new LedIterator("a");
    assertEquals(iter.next(), "");
    for (char c = 'a'; c <= 'z'; c++) {
      assertEquals(iter.next(), c + "");
    }
    for (char c = 'a'; c <= 'z'; c++) {
      assertEquals(iter.next(), c + "a");
    }
    for (char c = 'a'; c <= 'z'; c++) {
      assertEquals(iter.next(), "a" + c);
    }

    assertFalse(iter.hasNext());

    iter = new LedIterator("red");
    while (iter.hasNext()) {
      iter.next(); // Print this to see results.
    }

  }

  @Test
  public void fixTest() {
    assertEquals(LedCorrector.fix(ImmutableSet.of("tree", "oak"), "tre"),
        Optional.of("tree"));
    assertEquals(LedCorrector.fix(ImmutableSet.of("tree", "oak"), "tak"),
        Optional.of("oak"));
    assertEquals(LedCorrector.fix(ImmutableSet.of("tree", "oak"), "treat"),
        Optional.absent());
    assertEquals(LedCorrector
        .fix(ImmutableSet.of("tree", "ten", "tea", "red", "apple"), "tee"),
        Optional.absent());
    assertEquals(LedCorrector
        .fix(ImmutableSet.of("man", "men", "mand", "qwert"), "wert"),
        Optional.of("qwert"));
    assertEquals(LedCorrector
        .fix(ImmutableSet.of("man", "men", "mand", "qwert"), "mant"),
        Optional.absent());
  }
}
