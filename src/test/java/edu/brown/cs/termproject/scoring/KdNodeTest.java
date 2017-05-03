package edu.brown.cs.termproject.scoring;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

public class KdNodeTest {

  @Test
  public void testFindMedian() {

    DoublePoint p1 = new DoublePoint(3, 5, 6);
    DoublePoint p2 = new DoublePoint(1, 0, 11);
    DoublePoint p3 = new DoublePoint(2, 4, 6);
    DoublePoint p4 = new DoublePoint(13, 3, 9);
    DoublePoint p5 = new DoublePoint(7, 7, 7);

    List<Cartesian<Double>> dummyList = new ArrayList<>();
    List<Cartesian<Double>> list = new ArrayList<>();

    dummyList.add(new DoublePoint(0, 0, 0));
    list.add(p1);

    KdNode<Double> node = new KdNode<>(dummyList, 3, 0);
    assertEquals(node.findMedian(list, 0), p1);

    list.add(p2);
    assertEquals(node.findMedian(list, 0), p1);
    assertEquals(node.findMedian(list, 1), p1);
    assertEquals(node.findMedian(list, 2), p2);

    list.add(p3);
    assertEquals(node.findMedian(list, 0), p3);
    assertEquals(node.findMedian(list, 1), p3);
    assertTrue(
        node.findMedian(list, 2) == p1 || node.findMedian(list, 2) == p3);

    list.add(p4);
    list.add(p5);
    assertEquals(node.findMedian(list, 0), p1);
    assertEquals(node.findMedian(list, 1), p3);
    assertEquals(node.findMedian(list, 2), p5);
  }

  @Test
  public void testSubsetOf() {

    List<Cartesian<Double>> list = new ArrayList<Cartesian<Double>>();
    List<Cartesian<Double>> dummyList = new ArrayList<Cartesian<Double>>();
    List<Cartesian<Double>> expected;

    dummyList.add(new DoublePoint(0, 0, 0));
    KdNode<Double> node = new KdNode<>(dummyList, 3, 0);

    DoublePoint p1 = new DoublePoint(12, 35, 7);
    DoublePoint p2 = new DoublePoint(5, 16, 19);
    DoublePoint p3 = new DoublePoint(31, 22, 12);
    DoublePoint p4 = new DoublePoint(31, 24, 8);
    DoublePoint p5 = new DoublePoint(2, 2, 0);

    assertTrue(node.subsetOf(list, 0, p1, true).isEmpty());
    assertTrue(node.subsetOf(list, 0, p1, false).isEmpty());

    list.add(p2);
    assertEquals(node.subsetOf(list, 0, p1, true), list);
    assertTrue(node.subsetOf(list, 0, p1, false).isEmpty());
    assertTrue(node.subsetOf(list, 2, p1, true).isEmpty());
    assertEquals(node.subsetOf(list, 2, p1, false), list);

    list.add(p3);
    list.add(p4);
    list.add(p5);

    expected = new ArrayList<Cartesian<Double>>();
    expected.add(p2);
    expected.add(p5);
    assertEquals(node.subsetOf(list, 0, p1, true), expected);

    expected = new ArrayList<Cartesian<Double>>();
    expected.add(p3);
    expected.add(p4);
    assertEquals(node.subsetOf(list, 0, p1, false), expected);

    expected = new ArrayList<Cartesian<Double>>();
    expected.add(p2);
    expected.add(p3);
    expected.add(p4);
    expected.add(p5);
    assertEquals(node.subsetOf(list, 1, p1, true), expected);
    assertTrue(node.subsetOf(list, 1, p1, false).isEmpty());
  }

  @Test
  public void KdNodeConstructorTest() {

    KdNode<Double> node; // The testing node.

    node = new KdNode<Double>(null, 3, 0);
    assertEquals(node.getPoint(), null);
    assertEquals(node.getLeft(), null);
    assertEquals(node.getRight(), null);

    List<Cartesian<Double>> smallList = new ArrayList<Cartesian<Double>>();
    smallList.add(new DoublePoint(4, 7, 6));

    node = new KdNode<>(smallList, 3, 0);
    assertEquals(node.getPoint(), new DoublePoint(4, 7, 6));
    assertEquals(node.getLeft(), null);
    assertEquals(node.getRight(), null);

    List<Cartesian<Double>> list = new ArrayList<>();
    list.add(new DoublePoint(3, 4, 5));
    list.add(new DoublePoint(0, 1, 0));
    list.add(new DoublePoint(7, 2, 3));
    list.add(new DoublePoint(9, 1, 6));
    list.add(new DoublePoint(1, 9, 8));

    node = new KdNode<>(list, 3, 0);
    assertEquals(node.getPoint(), new DoublePoint(3, 4, 5));
    assertEquals(node.getLeft().getPoint(), new DoublePoint(1, 9, 8));
    assertEquals(node.getRight().getPoint(), new DoublePoint(7, 2, 3));
    assertEquals(node.getLeft().getLeft().getPoint(), new DoublePoint(0, 1, 0));
    assertEquals(node.getLeft().getRight(), null);
    assertEquals(node.getRight().getLeft().getPoint(),
        new DoublePoint(9, 1, 6));
    assertEquals(node.getRight().getRight(), null);

    list.add(new DoublePoint(3, 4, 5));

    // Testing different starting depth.
    node = new KdNode<>(list, 3, 1);
    assertEquals(node.getPoint(), new DoublePoint(7, 2, 3));
    assertEquals(node.getLeft().getPoint(), new DoublePoint(9, 1, 6));
    assertEquals(node.getRight().getPoint(), new DoublePoint(1, 9, 8));
    assertEquals(node.getLeft().getLeft().getPoint(), new DoublePoint(0, 1, 0));
    assertEquals(node.getLeft().getRight(), null);
    assertEquals(node.getRight().getLeft().getPoint(),
        new DoublePoint(3, 4, 5));
    assertEquals(node.getRight().getRight(), null);

    list.add(new DoublePoint(7, 2, 3));

    node = new KdNode<>(list, 3, 2);
    assertEquals(node.getPoint(), new DoublePoint(3, 4, 5));
    assertEquals(node.getLeft().getPoint(), new DoublePoint(7, 2, 3));
    assertEquals(node.getRight().getPoint(), new DoublePoint(9, 1, 6));
    assertEquals(node.getLeft().getLeft().getPoint(), new DoublePoint(0, 1, 0));
    assertEquals(node.getLeft().getRight(), null);
    assertEquals(node.getRight().getLeft().getPoint(),
        new DoublePoint(1, 9, 8));
    assertEquals(node.getRight().getRight(), null);

  }
}
