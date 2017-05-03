package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import org.junit.Test;

public class DistanceComparatorTest {

  @Test
  public void compareTest() {
    DoublePoint p1 = new DoublePoint(-3.1, 4, 7);
    DoublePoint p2 = new DoublePoint(4, 1.2, 5);
    DoublePoint p3 = new DoublePoint(14, 21.2, 55);
    DistanceComparator<Double> d;

    d = new DistanceComparator<>(p1);
    assertTrue(d.compare(p2, p3) > 0);
    assertTrue(d.compare(p1, p3) > 0);
    assertTrue(d.compare(p3, p2) < 0);
    assertTrue(d.compare(p2, p2) == 0);

    d = new DistanceComparator<>(p2);
    assertTrue(d.compare(p2, p3) > 0);
    assertTrue(d.compare(p1, p3) > 0);
    assertTrue(d.compare(p3, p1) < 0);
    assertTrue(d.compare(p3, p3) == 0);
  }

  @Test
  public void anotherCompareTest() {
    DoublePoint p1 = new DoublePoint(ImmutableList.of(-1.1));
    DoublePoint p2 = new DoublePoint(ImmutableList.of(100.1, 342.6, -23.426));
    DoublePoint p3 = new DoublePoint(
        ImmutableList.of(250.3, 3524.6, -123.426, 5.3));
    DoublePoint p4 = new DoublePoint(Collections.emptyList());
    DistanceComparator<Double> d;

    d = new DistanceComparator<>(p1);
    assertTrue(d.compare(p2, p3) > 0);
    assertTrue(d.compare(p3, p4) < 0);
    assertTrue(d.compare(p2, p4) < 0);
    assertTrue(d.compare(p1, p2) > 0);
    assertTrue(d.compare(p1, p3) > 0);

    d = new DistanceComparator<>(p2);
    assertTrue(d.compare(p1, p3) > 0);
    assertTrue(d.compare(p3, p4) < 0);
    assertTrue(d.compare(p1, p4) < 0);
    assertTrue(d.compare(p2, p3) > 0);
    assertTrue(d.compare(p3, p3) == 0);
  }
}
