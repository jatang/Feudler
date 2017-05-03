package edu.brown.cs.termproject.scoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * This class is in charge of the tree-level functionality, i.e. performing the
 * nearest neighbor search, and radius search.
 *
 * @param <T>
 *          the type that is stored in the point's coordinates. needs to be
 *          comparable.
 * @author asekula
 *
 */
public class KdTree<T extends Comparable<T>> {

  // The head of the tree.
  private KdNode<T> head;

  // The number of dimensions (k) of the tree.
  private int dimension;

  /**
   * Initializes the KD tree with the list of stars. Not assuming anything about
   * the ordering of stars. All this really does is initializes the head node
   * and sets the dimension.
   *
   * @param stars
   *          - the list of points in space
   */
  public KdTree(List<Cartesian<T>> stars) {
    // Converting to mutable list in case immutable list was inputted.
    List<Cartesian<T>> mutableStars = new ArrayList<>(stars);
    if (!mutableStars.isEmpty()) {
      dimension = mutableStars.get(0).getDimension();
      head = new KdNode<T>(mutableStars, dimension, 0);
    }
  }

  /**
   * Finds the nearest neighbors in the KDTree to an input point p.
   *
   * @param numNeighbors
   *          - the number of neighbors to find.
   * @param point
   *          - the input point
   * @return - a priority queue of the nearest neighbors to p
   */
  public List<Cartesian<T>> nearestNeighbors(int numNeighbors,
      Cartesian<T> point) {

    if (head == null || numNeighbors == 0) {
      return Collections.emptyList();
    }

    /*
     * Note: saying that if a point is farther away it should have higher
     * priority, so that when we call dequeue we get the farthest point (and
     * thus remove it from the queue).
     */
    Queue<Cartesian<T>> nearest = new PriorityQueue<Cartesian<T>>(numNeighbors,
        new DistanceComparator<T>(point));

    searchForNeighbors(nearest, numNeighbors, point, head, 0);
    return convertToList(nearest);
  }

  /**
   * Converts a priorityQueue to an arrayList. There's probably a better way to
   * do this, I didn't find one.
   *
   * @param queue
   *          - the priority queue
   * @return - an ordered list of elements, ordered in reverse of the priority
   *         queue's ordering.
   */
  private List<Cartesian<T>> convertToList(Queue<Cartesian<T>> queue) {
    List<Cartesian<T>> arrayForm = new ArrayList<>();
    Cartesian<T> next;

    while ((next = queue.poll()) != null) {
      arrayForm.add(0, next);
    }

    return arrayForm;
  }

  /**
   * Performs the recursive search for the nearest neighbors.
   *
   * @param nearest
   *          - the nearest neighbors found so far. Size is not necessarily
   *          numNeighbors, could be lower.
   * @param numNeighbors
   *          - the number of neighbors we want to find.
   * @param p
   *          - the original point whose neighbors we want to find.
   * @param current
   *          - the KDNode we are currently looking at in the search.
   * @param depth
   *          - the depth of the KDNode
   */
  private void searchForNeighbors(Queue<Cartesian<T>> nearest, int numNeighbors,
      Cartesian<T> p, KdNode<T> current, int depth) {

    if (current == null) {
      return;
    }

    boolean searchesLeft = p.compareAtCoordinate(current.getPoint(),
        depth) <= 0;
    KdNode<T> next = (searchesLeft ? current.getLeft() : current.getRight());
    KdNode<T> other = (searchesLeft ? current.getRight() : current.getLeft());

    searchForNeighbors(nearest, numNeighbors, p, next, (depth + 1) % dimension);

    nearest.add(current.getPoint());

    if (nearest.size() > numNeighbors) { // Too many neighbors.
      if (!current.getPoint().equals(nearest.poll())) {
        searchForNeighbors(nearest, numNeighbors, p, other,
            (depth + 1) % dimension);
      } else {
        T squaredDistToSplit = current.getPoint()
            .squaredDifferenceAtCoordinate(p, depth);

        if (squaredDistToSplit
            .compareTo(nearest.peek().squaredDistanceTo(p)) <= 0) {
          searchForNeighbors(nearest, numNeighbors, p, other,
              (depth + 1) % dimension);
        }
      }
    } else {
      searchForNeighbors(nearest, numNeighbors, p, other,
          (depth + 1) % dimension);
    }
  }

  /**
   * Finds the points within a radius of p.
   *
   * @param point
   *          - the input point
   * @param squaredRadius
   *          - the square of the radius within which to find points
   * @return - a sorted list of points, all within radius distance of p (sorted
   *         in increasing order of distance to p).
   */
  public List<Cartesian<T>> withinSquaredRadius(T squaredRadius,
      Cartesian<T> point) {

    if (head == null) {
      return Collections.emptyList();
    }

    Queue<Cartesian<T>> within = new PriorityQueue<Cartesian<T>>(1,
        new DistanceComparator<T>(point));

    radiusSearch(within, point, squaredRadius, head, 0);
    return convertToList(within);
  }

  /**
   * Performs the recursion of the radius search. Searches down one path of the
   * tree, checking the other subtrees if the split line is within the radius.
   *
   * @param within
   *          - the points found so far
   * @param p
   *          - the point to compare distances
   * @param squaredRadius
   *          - the arithmetic square of the radius
   * @param current
   *          - the current node being inspected
   * @param depth
   *          - the current depth of the node
   */
  private void radiusSearch(Queue<Cartesian<T>> within, Cartesian<T> p,
      T squaredRadius, KdNode<T> current, int depth) {

    if (current == null) {
      return;
    }

    boolean searchesLeft = p.get(depth)
        .compareTo(current.getPoint().get(depth)) <= 0;
    KdNode<T> next = (searchesLeft ? current.getLeft() : current.getRight());
    KdNode<T> other = (searchesLeft ? current.getRight() : current.getLeft());

    radiusSearch(within, p, squaredRadius, next, (depth + 1) % dimension);

    if (p.squaredDistanceTo(current.getPoint()).compareTo(squaredRadius) <= 0) {
      within.add(current.getPoint());
    }

    T squaredDistToSplit = current.getPoint().squaredDifferenceAtCoordinate(p,
        depth);

    if (squaredDistToSplit.compareTo(squaredRadius) <= 0) {
      radiusSearch(within, p, squaredRadius, other, (depth + 1) % dimension);
    }
  }

  /**
   * A simple getter for the head of the tree. Only used in testing.
   *
   * @return - the head of the tree
   */
  public KdNode<T> getHead() {
    return head;
  }

  boolean isBalanced() {
    if (head == null) {
      return true;
    }

    return head.isBalanced();
  }
}
