package edu.brown.cs.termproject.scoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Handles the node-level functionality, i.e. storing the left/right nodes,
 * accessing the left/right trees, and recursively building the tree.
 *
 * @param <T>
 *          the type that goes into the KDNode, i.e. the type of data that the
 *          point stores. Needs to be comparable.
 * @author asekula
 */
class KdNode<T extends Comparable<?>> {

  // point is the location of the star of the node.
  private Cartesian<T> point;
  private KdNode<T> leftNode;
  private KdNode<T> rightNode;

  /**
   * Recursively initializes the KDNodes and its child nodes. Finds the median
   * point in the input set then splits the input set to initialize the left and
   * right children.
   *
   * @param depth
   *          - the current depth of the tree
   * @param dimensions
   *          - the dimensions of the tree
   * @param stars
   *          - the set of remaining stars
   */
  KdNode(List<Cartesian<T>> stars, int dimensions, int depth) {

    /*
     * If stars is empty, everything will be null. (this won't happen in the
     * current implementation, as it checks that stars is not empty before
     * passing it to a KDNode constructor.
     */
    if (stars != null && !stars.isEmpty()) {
      point = findMedian(stars, depth);
      stars.remove(point);

      // Could save memory here. Instead of making a new list, keep track of
      // references in an array. Not implemented here.
      List<Cartesian<T>> leftOfMedian = subsetOf(stars, depth, point, true);
      List<Cartesian<T>> rightOfMedian = subsetOf(stars, depth, point, false);

      if (!leftOfMedian.isEmpty()) {
        leftNode = new KdNode<T>(leftOfMedian, dimensions,
            (depth + 1) % dimensions);
      }

      if (!rightOfMedian.isEmpty()) {
        rightNode = new KdNode<T>(rightOfMedian, dimensions,
            (depth + 1) % dimensions);
      }
    }
  }

  /**
   * Depending on the leftOf boolean flag, returns all points in stars less than
   * or equal to the median point at dimension depth, of all points greater than
   * the median point at dimension depth.
   *
   * @param stars
   *          - the set of remaining stars
   * @param depth
   *          - the current dimension
   * @param median
   *          - the median point
   * @param leftSubset
   *          - if true, returns points whose coordinate at dimension depth is
   *          less than
   * @return - a set of points relative to the median
   */
  List<Cartesian<T>> subsetOf(List<Cartesian<T>> stars, int depth,
      Cartesian<T> median, boolean leftSubset) {

    List<Cartesian<T>> subset = new ArrayList<>();

    for (Cartesian<T> star : stars) {
      if (leftSubset && star.compareAtCoordinate(median, depth) <= 0) {
        subset.add(star);
      } else if (!leftSubset && star.compareAtCoordinate(median, depth) > 0) {
        subset.add(star);
      }
    }
    return subset;
  }

  /**
   * Returns the median point of the input list of points, in terms of the
   * values at dimension depth. Using naive implementation of median finding
   * (namely, just sorting the array to find the median).
   *
   * @param stars
   *          - the input list
   * @param depth
   *          - the dimension to use
   * @return - the median of the input list at coordinate depth
   */
  Cartesian<T> findMedian(List<Cartesian<T>> stars, int depth) {

    Comparator<Cartesian<T>> pointComparator = new Comparator<Cartesian<T>>() {
      public int compare(Cartesian<T> p1, Cartesian<T> p2) {
        return p1.compareAtCoordinate(p2, depth);
      }
    };

    Collections.sort(stars, pointComparator);

    return stars.get(stars.size() / 2);
  }

  /**
   * Returns the node's point.
   *
   * @return - the point
   */
  Cartesian<T> getPoint() {
    return point;
  }

  /**
   * A simple getter for the left child.
   *
   * @return - the left child
   */
  KdNode<T> getLeft() {
    return leftNode;
  }

  /**
   * A simple getter for the right child.
   *
   * @return - the right child
   */
  KdNode<T> getRight() {
    return rightNode;
  }

  /**
   * Returns the size of the subtree, including itself.
   *
   * @return - the size, an integer representing the number of nodes in the
   *         subtree
   */
  int getSize() {
    if (rightNode == null && leftNode == null) {
      return 1;
    } else if (rightNode == null) {
      return 1 + leftNode.getSize();
    } else if (leftNode == null) {
      return 1 + rightNode.getSize();
    } else {
      return 1 + rightNode.getSize() + leftNode.getSize();
    }
  }

  /**
   * Checks if the tree is balanced, i.e. if the left subtree size is within 1
   * of the right subtree size, and if the left/right subtrees are also
   * balanced.
   *
   * @return - true if the tree is balanced, false otherwise
   */
  boolean isBalanced() {
    if (rightNode == null && leftNode == null) {
      return true;
    }

    if (rightNode == null) {
      return leftNode.getSize() == 1;
    }

    if (leftNode == null) {
      return rightNode.getSize() == 1;
    }

    return Math.abs(rightNode.getSize() - leftNode.getSize()) <= 1
        && rightNode.isBalanced() && leftNode.isBalanced();
  }
}
