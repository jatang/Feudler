package edu.brown.cs.termproject.scoring;

import java.util.Comparator;

/**
 * Comparator class for two points. Placed in a separate class file to promote
 * code reuse (used in a few places).
 *
 * @author asekula
 * @param <T>
 *          The type that we are using to compare values. Same as the type of
 *          the input points.
 */
public class DistanceComparator<T extends Comparable<T>>
    implements Comparator<Cartesian<T>> {

  private Cartesian<T> point;

  /**
   * Initializes a distanceComparator. The distanceComparator requires a point
   * to compare other points to, to figure out which points are closer to the
   * input point. That point is passed in here, upon initialization.
   *
   * @param point
   *          - the point that we use to compare distances to other points.
   */
  public DistanceComparator(Cartesian<T> point) {
    this.point = point;
  }

  /**
   * Compares the distance from p to p1 and p to p2. This says that longer
   * distances have a higher priority if used in a PriorityQueue. Note that the
   * ordering makes this so: p2 is being compared to p1, so if p1 is closer to
   * p, then compare returns a positive value.
   *
   * @param p1
   *          - a point to compare the distance to p
   * @param p2
   *          - another point to compare the distance to p
   * @return - negative if p2 is closer to p than p1 is to p, 0 if distances are
   *         equal, positive otherwise.
   */
  public int compare(Cartesian<T> p1, Cartesian<T> p2) {
    return point.squaredDistanceTo(p2).compareTo(point.squaredDistanceTo(p1));
  }
}
