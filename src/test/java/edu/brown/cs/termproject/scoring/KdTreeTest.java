package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class KdTreeTest {

  // Note: Not testing constructor

  @Test
  public void nearestNeighborsEmptyTreeTest() {
    List<Cartesian<Double>> list = new ArrayList<>();
    KdTree<Double> tree = new KdTree<>(list);

    assertTrue(tree.nearestNeighbors(0, new DoublePoint(0, 0, 0)).isEmpty());
    assertTrue(tree.nearestNeighbors(1, new DoublePoint(0, 0, 0)).isEmpty());
    assertTrue(tree.nearestNeighbors(5, new DoublePoint(0, 0, 0)).isEmpty());

    list.clear(); // Not necessary, including it for clarity.
  }

  @Test
  public void nearestNeighborsSmallTreeTest() {
    List<Cartesian<Double>> list = new ArrayList<>();
    List<Cartesian<Double>> expected = new ArrayList<>();
    KdTree<Double> tree;

    list.add(new DoublePoint(2, 4, 1));
    tree = new KdTree<>(list);

    assertTrue(tree.nearestNeighbors(0, new DoublePoint(1, 6, 8)).isEmpty());

    expected.add(new DoublePoint(2, 4, 1));
    assertEquals(tree.nearestNeighbors(1, new DoublePoint(10, 11, 12)),
        expected);
    assertEquals(tree.nearestNeighbors(4, new DoublePoint(4, 2, 6)), expected);
    assertEquals(tree.nearestNeighbors(1, new DoublePoint(2, 4, 1)), expected);
  }

  @Test
  public void nearestNeighborsComplexTreeTest() {
    List<Cartesian<Double>> list = new ArrayList<>();
    List<Cartesian<Double>> expected = new ArrayList<>();
    KdTree<Double> tree;

    list.add(new DoublePoint(15, 3, 11));
    list.add(new DoublePoint(1, 0, 0));
    list.add(new DoublePoint(0, 0, 0));
    list.add(new DoublePoint(19, 19, 12));
    list.add(new DoublePoint(2, 8, 10));
    tree = new KdTree<>(list);

    assertTrue(tree.nearestNeighbors(0, new DoublePoint(1, 1, 1)).isEmpty());

    expected.add(new DoublePoint(1, 0, 0));
    assertEquals(tree.nearestNeighbors(1, new DoublePoint(1, 1, 1)), expected);

    expected.add(new DoublePoint(0, 0, 0));
    assertEquals(tree.nearestNeighbors(2, new DoublePoint(1, 1, 1)), expected);

    expected.add(new DoublePoint(2, 8, 10));
    assertEquals(tree.nearestNeighbors(3, new DoublePoint(1, 1, 1)), expected);

    expected.add(new DoublePoint(15, 3, 11));
    assertEquals(tree.nearestNeighbors(4, new DoublePoint(1, 1, 1)), expected);

    expected.clear();
    expected.add(new DoublePoint(2, 8, 10));
    assertEquals(tree.nearestNeighbors(1, new DoublePoint(4, 15, 14)),
        expected);

    expected.add(new DoublePoint(19, 19, 12));
    assertEquals(tree.nearestNeighbors(2, new DoublePoint(4, 15, 14)),
        expected);

    expected.add(new DoublePoint(15, 3, 11));
    assertEquals(tree.nearestNeighbors(3, new DoublePoint(4, 15, 14)),
        expected);

    expected.add(new DoublePoint(1, 0, 0));
    assertEquals(tree.nearestNeighbors(4, new DoublePoint(4, 15, 14)),
        expected);

    expected.add(new DoublePoint(0, 0, 0));
    assertEquals(tree.nearestNeighbors(5, new DoublePoint(4, 15, 14)),
        expected);

    assertEquals(tree.nearestNeighbors(8, new DoublePoint(4, 15, 14)),
        expected);
  }

  @Test
  public void nearestNeighborsSimpleTreeTest() {
    ArrayList<Cartesian<Double>> list = new ArrayList<>();
    ArrayList<Cartesian<Double>> expected = new ArrayList<>();
    KdTree<Double> tree;

    list.add(new DoublePoint(1, 1, 0));
    list.add(new DoublePoint(0, 1, 1));
    list.add(new DoublePoint(2, 0, 0));
    tree = new KdTree<>(list);

    expected.add(new DoublePoint(0, 1, 1));
    assertEquals(tree.nearestNeighbors(1, new DoublePoint(0, 0, 1)), expected);

    expected.add(new DoublePoint(1, 1, 0));
    assertEquals(tree.nearestNeighbors(2, new DoublePoint(0, 0, 1)), expected);

    expected.add(new DoublePoint(2, 0, 0));
    assertEquals(tree.nearestNeighbors(3, new DoublePoint(0, 0, 1)), expected);
  }

  @Test
  public void nearestNeighborsSameDistanceTest() {
    List<Cartesian<Double>> list = new ArrayList<>();
    List<Cartesian<Double>> expected = new ArrayList<>();
    List<Cartesian<Double>> alsoExpected = new ArrayList<>();
    List<Cartesian<Double>> result;

    list.add(new DoublePoint(1, 1, 0));
    list.add(new DoublePoint(0, 1, 1));
    list.add(new DoublePoint(0, 0, 0));
    expected.add(new DoublePoint(1, 1, 0));
    alsoExpected.add(new DoublePoint(0, 1, 1));
    KdTree<Double> tree = new KdTree<>(list);

    result = tree.nearestNeighbors(1, new DoublePoint(1, 0, 1));
    assertTrue(result.equals(expected) || result.equals(alsoExpected));

    result = tree.nearestNeighbors(1, new DoublePoint(1, 3, 1));
    assertTrue(result.equals(expected) || result.equals(alsoExpected));

    result = tree.nearestNeighbors(1, new DoublePoint(18, 20, 18));
    assertTrue(result.equals(expected) || result.equals(alsoExpected));
  }

  @Test
  public void nearestNeighborsLengthTest() {
    List<Cartesian<Double>> list = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        list.add(new DoublePoint(i, j, i + j));
      }
    }

    KdTree<Double> tree = new KdTree<>(list);

    for (int i = 0; i <= 100; i++) {
      assertTrue(
          tree.nearestNeighbors(i, new DoublePoint(3, 3, 3)).size() == i);
    }
  }

  @Test
  public void nearestNeighborsOrderingTest() {
    List<Cartesian<Double>> list = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        list.add(new DoublePoint(i, j, (int) Math.ceil(Math.random() * 10)));
      }
    }

    KdTree<Double> tree = new KdTree<>(list);
    Cartesian<Double> p = new DoublePoint(5, 4, 7);
    List<Cartesian<Double>> result = tree.nearestNeighbors(100, p);
    DistanceComparator<Double> c = new DistanceComparator<>(p);

    for (int i = 0; i < 99; i++) {
      assertTrue(c.compare(result.get(i), result.get(i + 1)) >= 0);
    }
  }

  @Test
  public void nearestNeighborsAnotherTest() {
    List<Cartesian<Double>> list = new ArrayList<>();
    List<Cartesian<Double>> expected = new ArrayList<>();
    KdTree<Double> tree;

    list.add(new DoublePoint(3, 1, 6));
    list.add(new DoublePoint(1, 0, 7));
    list.add(new DoublePoint(9, 8, 4));
    list.add(new DoublePoint(2, 5, 5));
    list.add(new DoublePoint(10, 3, 1));
    list.add(new DoublePoint(3, 9, 8));
    list.add(new DoublePoint(3, 8, 8));
    tree = new KdTree<>(list);

    Cartesian<Double> p = new DoublePoint(4, 5, 6);

    assertTrue(tree.nearestNeighbors(0, p).isEmpty());

    expected.add(new DoublePoint(2, 5, 5));
    assertEquals(tree.nearestNeighbors(1, p), expected);

    expected.add(new DoublePoint(3, 8, 8));
    assertEquals(tree.nearestNeighbors(2, p), expected);

    expected.add(new DoublePoint(3, 1, 6));
    assertEquals(tree.nearestNeighbors(3, p), expected);

    expected.add(new DoublePoint(3, 9, 8));
    assertEquals(tree.nearestNeighbors(4, p), expected);

    expected.add(new DoublePoint(1, 0, 7));
    assertEquals(tree.nearestNeighbors(5, p), expected);

    expected.add(new DoublePoint(9, 8, 4));
    assertEquals(tree.nearestNeighbors(6, p), expected);

    expected.add(new DoublePoint(10, 3, 1));
    assertEquals(tree.nearestNeighbors(7, p), expected);
  }

  @Test
  public void nearestNeighborsFarFromMedianTest() {
    ArrayList<Cartesian<Double>> list = new ArrayList<>();
    ArrayList<Cartesian<Double>> expected = new ArrayList<>();
    KdTree<Double> tree;

    list.add(new DoublePoint(1, 0, 0));
    list.add(new DoublePoint(2, 0, 0));
    list.add(new DoublePoint(3, 0, 0));
    list.add(new DoublePoint(0, 100, 0));
    list.add(new DoublePoint(4, 100, 0));
    tree = new KdTree<>(list);

    Cartesian<Double> p = new DoublePoint(0, 100, 0);

    expected.add(new DoublePoint(0, 100, 0));
    expected.add(new DoublePoint(4, 100, 0));
    assertEquals(tree.nearestNeighbors(2, p), expected);
    assertEquals(new DoublePoint(2, 0, 0), tree.getHead().getPoint());
  }

  @Test
  public void withinRadiusSmallTest() {
    ArrayList<Cartesian<Double>> list = new ArrayList<>();
    ArrayList<Cartesian<Double>> expected = new ArrayList<>();
    KdTree<Double> tree;

    tree = new KdTree<>(list);
    assertTrue(
        withinSquaredRadius(tree, 16, new DoublePoint(3, 3, 3)).isEmpty());
    assertTrue(
        withinSquaredRadius(tree, 0, new DoublePoint(1, 1, 1)).isEmpty());

    list.add(new DoublePoint(3, 3, 3));
    expected.add(new DoublePoint(3, 3, 3));
    tree = new KdTree<>(list);
    assertTrue(
        withinSquaredRadius(tree, 0, new DoublePoint(4, 4, 4)).isEmpty());
    assertEquals(withinSquaredRadius(tree, 0, new DoublePoint(3, 3, 3)),
        expected);
    assertEquals(withinSquaredRadius(tree, 25, new DoublePoint(3, 3, 3)),
        expected);
    assertEquals(withinSquaredRadius(tree, 1, new DoublePoint(4, 3, 3)),
        expected);
    assertEquals(withinSquaredRadius(tree, 10000, new DoublePoint(4, 3, 3)),
        expected);
    assertTrue(withinSquaredRadius(tree, 10000, new DoublePoint(100, 100, 100))
        .isEmpty());
  }

  @Test
  public void withinRadiusBasicLinearTest() {
    ArrayList<Cartesian<Double>> list = new ArrayList<>();
    ArrayList<Cartesian<Double>> expected = new ArrayList<>();
    KdTree<Double> tree;

    list.add(new DoublePoint(0, 0, 1));
    list.add(new DoublePoint(0, 0, 2));
    list.add(new DoublePoint(0, 0, 3));
    list.add(new DoublePoint(0, 0, 4));
    list.add(new DoublePoint(0, 0, 5));
    list.add(new DoublePoint(0, 0, 6));
    tree = new KdTree<>(list);

    assertTrue(
        withinSquaredRadius(tree, 0, new DoublePoint(0, 0, 0)).isEmpty());

    expected.add(new DoublePoint(0, 0, 1));
    assertEquals(withinSquaredRadius(tree, 1, new DoublePoint(0, 0, 0)),
        expected);

    expected.add(new DoublePoint(0, 0, 2));
    assertEquals(withinSquaredRadius(tree, 4, new DoublePoint(0, 0, 0)),
        expected);

    expected.add(new DoublePoint(0, 0, 3));
    assertEquals(withinSquaredRadius(tree, 9, new DoublePoint(0, 0, 0)),
        expected);

    expected.add(new DoublePoint(0, 0, 4));
    assertEquals(withinSquaredRadius(tree, 16, new DoublePoint(0, 0, 0)),
        expected);

    expected.add(new DoublePoint(0, 0, 5));
    assertEquals(withinSquaredRadius(tree, 25, new DoublePoint(0, 0, 0)),
        expected);

    expected.add(new DoublePoint(0, 0, 6));
    assertEquals(withinSquaredRadius(tree, 36, new DoublePoint(0, 0, 0)),
        expected);
  }

  @Test
  public void withinRadiusOnlyHeadOrLeafTest() {
    // Only includes the head and a leaf.
    // Checks that the recursion goes up to the head.
    ArrayList<Cartesian<Double>> list = new ArrayList<>();
    ArrayList<Cartesian<Double>> expected = new ArrayList<>();
    KdTree<Double> tree;

    list.add(new DoublePoint(5, 5, 0)); // Head.
    list.add(new DoublePoint(6, 4, 0));
    list.add(new DoublePoint(7, 10, 0));
    list.add(new DoublePoint(20, 7, 0));
    list.add(new DoublePoint(0, 100, 0));
    list.add(new DoublePoint(0, 101, 0));
    list.add(new DoublePoint(0, 102, 0));
    tree = new KdTree<>(list);

    // Equal distance, so ordering is sensitive. This ordering is correct.
    expected.add(new DoublePoint(5, 5, 0));
    expected.add(new DoublePoint(6, 4, 0));
    assertEquals(withinSquaredRadius(tree, 1, new DoublePoint(6, 5, 0)),
        expected);
  }

  @Test
  public void withinRadiusThreeDimensions() {
    ArrayList<Cartesian<Double>> list = new ArrayList<>();
    ArrayList<Cartesian<Double>> expected = new ArrayList<>();
    KdTree<Double> tree;

    list.add(new DoublePoint(1, 1, 0));
    list.add(new DoublePoint(1, 3, 3));
    list.add(new DoublePoint(3, 2, 2));
    list.add(new DoublePoint(2, 3, 1));
    list.add(new DoublePoint(2, 0, 0));
    list.add(new DoublePoint(2, 0, 3));
    tree = new KdTree<>(list);

    Cartesian<Double> p = new DoublePoint(2, 2, 2);

    expected.add(new DoublePoint(3, 2, 2));
    assertEquals(withinSquaredRadius(tree, 1, p), expected);

    expected.add(new DoublePoint(2, 3, 1));
    expected.add(new DoublePoint(1, 3, 3));
    assertEquals(withinSquaredRadius(tree, 4, p), expected);

    expected.add(new DoublePoint(2, 0, 3));
    expected.add(new DoublePoint(1, 1, 0));
    expected.add(new DoublePoint(2, 0, 0));
    assertEquals(withinSquaredRadius(tree, 9, p), expected);
    assertEquals(withinSquaredRadius(tree, 400, p), expected);
  }

  @Test
  public void withinRadiusAcrossSplitLine() {
    // Checks that withinRadius checks across the split line when necessary.
    ArrayList<Cartesian<Double>> list = new ArrayList<>();
    ArrayList<Cartesian<Double>> expected = new ArrayList<>();
    KdTree<Double> tree;

    list.add(new DoublePoint(0, 0, 100));
    list.add(new DoublePoint(1, 0, 0));
    list.add(new DoublePoint(2, 0, 100));
    tree = new KdTree<>(list);

    Cartesian<Double> p = new DoublePoint(3, 0, 100);

    expected.add(new DoublePoint(2, 0, 100));
    assertEquals(withinSquaredRadius(tree, 1, p), expected);

    expected.add(new DoublePoint(0, 0, 100));
    assertEquals(withinSquaredRadius(tree, 9, p), expected);

    assertEquals(withinSquaredRadius(tree, 2500, p), expected);

    expected.add(new DoublePoint(1, 0, 0));
    assertEquals(withinSquaredRadius(tree, 10816, p), expected);
  }

  @Test
  public void treeIsBalancedTest() {
    // Tests that the tree is balanced. Uses an oracle.
    KdTree<Double> tree;
    int numTests = 10;
    int maxSize = 100;
    for (int size = 0; size < maxSize; size++) {
      for (int i = 0; i < numTests; i++) {
        tree = new KdTree<Double>(generateRandomPointList(size));
        assertTrue(tree.isBalanced());
      }
    }
  }

  @Test
  public void nearestNeighborsOracleTest() {
    KdTree<Double> tree;
    int numTests = 10;
    int maxSize = 50;
    for (int size = 0; size < maxSize; size++) {
      for (int i = 0; i < numTests; i++) {
        List<Cartesian<Double>> randList = generateRandomPointList(size);
        Cartesian<Double> p = randomPoint();
        tree = new KdTree<Double>(randList);

        DistanceComparator<Double> d = new DistanceComparator<>(p);
        randList.sort(d);
        Collections.reverse(randList);

        for (int j = 0; j < size; j++) {
          assertEquals(tree.nearestNeighbors(j, p), randList.subList(0, j));
        }
      }
    }
  }

  @Test
  public void nearestNeighborsDoubleTest() {
    Cartesian<Double> p = new DoublePoint(92.72326389423155, 66.54198621638051,
        33.024823139317824);
    Cartesian<Double> star = new DoublePoint(40.24504507290967,
        49.56917047710224, 99.70480105612694);
    KdTree<Double> tree = new KdTree<>(ImmutableList.of(star));

    assertTrue(tree.nearestNeighbors(1, p).size() == 1);
    assertTrue(tree.nearestNeighbors(1, p).get(0).equals(star));
  }

  @Test
  public void withinRadiusOracleTest() {
    KdTree<Double> tree;
    int numTests = 10;
    int maxSize = 50;
    for (int size = 0; size < maxSize; size++) {
      for (int i = 0; i < numTests; i++) {
        List<Cartesian<Double>> randList = generateRandomPointList(size);
        Cartesian<Double> p = randomPoint();
        tree = new KdTree<Double>(randList);

        DistanceComparator<Double> d = new DistanceComparator<>(p);
        randList.sort(d);
        Collections.reverse(randList);

        for (double j = 0.5; j < size; j += 0.7) {

          List<Cartesian<Double>> withinList = new ArrayList<>();
          for (Cartesian<Double> point : randList) {
            if (p.squaredDistanceTo(point) <= j) {
              withinList.add(point);
            }
          }

          assertEquals(tree.withinSquaredRadius((double) j, p), withinList);
        }
      }
    }
  }

  /**
   * A simple wrapper for tree.withinSquaredRadius, to allow integer inputs for
   * the radius (to retain old test examples).
   * 
   * @param tree
   *          - the KdTree
   * @param r
   *          - the radius, an integer
   * @param p
   *          - the input point
   * @return - the points within r distances of the input point
   */
  private List<Cartesian<Double>> withinSquaredRadius(KdTree<Double> tree,
      int r, Cartesian<Double> p) {
    return tree.withinSquaredRadius((double) r, p);
  }

  private List<Cartesian<Double>> generateRandomPointList(int size) {
    List<Cartesian<Double>> l = new ArrayList<>();
    while (l.size() < size) {
      Cartesian<Double> p = randomPoint();
      if (!l.contains(p)) {
        l.add(p);
      }
    }
    return l;
  }

  private Cartesian<Double> randomPoint() {
    return new DoublePoint(Math.random() * 100, Math.random() * 100,
        Math.random() * 100);
  }
}
