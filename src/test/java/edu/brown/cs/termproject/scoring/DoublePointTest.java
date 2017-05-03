package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class DoublePointTest {

  @Test
  public void constructorBasicTest() {
    DoublePoint p = new DoublePoint(4, 5.7, 6.1);
    assertTrue(Double.compare(p.get(0), 4) == 0);
    assertTrue(Double.compare(p.get(1), 5.7) == 0);
    assertTrue(Double.compare(p.get(2), 6.1) == 0);
    assertTrue(Double.compare(p.get(3), 0) == 0);
    assertTrue(Double.compare(p.get(4), 0) == 0);

    p = new DoublePoint(-1.0, -0.034, 1002.1);
    assertTrue(Double.compare(p.get(0), -1.0) == 0);
    assertTrue(Double.compare(p.get(1), -0.034) == 0);
    assertTrue(Double.compare(p.get(2), 1002.1) == 0);
    assertTrue(Double.compare(p.get(3), 0) == 0);
    assertTrue(Double.compare(p.get(4), 0) == 0);
  }

  @Test
  public void constructorListTest() {
    DoublePoint p = new DoublePoint(ImmutableList.of(3.9));
    assertTrue(Double.compare(p.get(0), 3.9) == 0);
    assertTrue(Double.compare(p.get(1), 0) == 0);

    p = new DoublePoint(ImmutableList.of());
    assertTrue(Double.compare(p.get(0), 0) == 0);
    assertTrue(Double.compare(p.get(1), 0) == 0);

    p = new DoublePoint(ImmutableList.of(2.1, 3.2, 4.0, -4.5, 6.2));
    assertTrue(Double.compare(p.get(0), 2.1) == 0);
    assertTrue(Double.compare(p.get(1), 3.2) == 0);
    assertTrue(Double.compare(p.get(2), 4.0) == 0);
    assertTrue(Double.compare(p.get(3), -4.5) == 0);
    assertTrue(Double.compare(p.get(4), 6.2) == 0);
    assertTrue(Double.compare(p.get(5), 0) == 0);
  }

  @Test
  public void squaredDistanceToTest() {
    DoublePoint p1 = new DoublePoint(ImmutableList.of(3.0));
    DoublePoint p2 = new DoublePoint(ImmutableList.of(7.0));
    assertTrue(p1.squaredDistanceTo(p2) == 16);

    DoublePoint p3 = new DoublePoint(ImmutableList.of(6.0, 4.0));
    assertTrue(Double.compare(p1.squaredDistanceTo(p3), 25) == 0);
    assertTrue(Double.compare(p3.squaredDistanceTo(p1), 25) == 0);
    assertTrue(Double.compare(p2.squaredDistanceTo(p3), 17) == 0);
    assertTrue(Double.compare(p3.squaredDistanceTo(p2), 17) == 0);

    DoublePoint p4 = new DoublePoint(ImmutableList.of(2.5, 1.0, 1.0));
    assertTrue(Double.compare(p1.squaredDistanceTo(p4), 2.25) == 0);
    assertTrue(Double.compare(p4.squaredDistanceTo(p2), 22.25) == 0);
    assertTrue(Double.compare(p3.squaredDistanceTo(p4), 22.25) == 0);
    assertTrue(Double.compare(p4.squaredDistanceTo(p4), 0) == 0);
  }

  @Test
  public void squaredDifferenceAtCoordinateTest() {
    DoublePoint p1 = new DoublePoint(ImmutableList.of(4.5, 1.0, 5.6, 7.8, 3.1));
    DoublePoint p2 = new DoublePoint(ImmutableList.of(4.1, 4.0, 1.6, 7.75));

    assertTrue(closeEnough(p1.squaredDifferenceAtCoordinate(p2, 0), 0.16));
    assertTrue(closeEnough(p1.squaredDifferenceAtCoordinate(p2, 1), 9));
    assertTrue(closeEnough(p1.squaredDifferenceAtCoordinate(p2, 2), 16));
    assertTrue(closeEnough(p1.squaredDifferenceAtCoordinate(p2, 3), 0.0025));
    assertTrue(closeEnough(p1.squaredDifferenceAtCoordinate(p2, 4), 9.61));
    assertTrue(closeEnough(p1.squaredDifferenceAtCoordinate(p2, 5), 0));
  }

  @Test
  public void squaredDifferenceAtCoordinateSymmetryTest() {
    DoublePoint p1 = new DoublePoint(
        ImmutableList.of(3.234, 193.3423, 1992.31, -0.2833, -6457.3, 956.3));
    DoublePoint p2 = new DoublePoint(
        ImmutableList.of(1.323, 1293.123, 0.3303, 1923.6));

    for (int i = 0; i < p1.getDimension(); i++) {
      assertTrue(closeEnough(p1.squaredDifferenceAtCoordinate(p2, i),
          p2.squaredDifferenceAtCoordinate(p1, i)));
    }
  }

  @Test
  public void equalsTest() {
    assertEquals(new DoublePoint(4, 5, 6), new DoublePoint(4, 5, 6));
    assertEquals(new DoublePoint(4, 5.0, -6), new DoublePoint(4, 5, -6.0));
    assertEquals(new DoublePoint(ImmutableList.of(4.0, 5.5)),
        new DoublePoint(4, 5.5, 0));
    assertEquals(new DoublePoint(ImmutableList.of(4.0, 0.0, 0.0, 0.0, 0.0)),
        new DoublePoint(4, 0, 0));
    assertEquals(new DoublePoint(ImmutableList.of(4.0, 0.0, 0.0, 0.0, 0.0)),
        new DoublePoint(4, 0, 0));
  }

  private boolean closeEnough(double a, double b) {
    return Math.abs(a - b) <= 0.000001;
  }
}
