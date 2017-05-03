package edu.brown.cs.termproject.scoring;

/**
 * Represents a point on type T. Cartesian holds a number of coordinates, each
 * of type T. Basic point requirements. Since T is not a necessarily a
 * primitive, the implementing class needs to figure out how it will handle the
 * squared distance to another point, and other methods that are easily
 * implemented with ints and doubles.
 *
 * @author asekula
 * @param <T>
 *          the type of the data stored in the points
 */
public interface Cartesian<T extends Comparable<?>> {

  /**
   * Gets the value at coordinate "dimension". Assuming the data is stored in a
   * list, this would be equivalent to calling list.get(dimension).
   *
   * @param dimension
   *          - the dimension of the coordinate to return
   * @return the value at the coordinate, of type T.
   */
  T get(int dimension);

  /**
   * Returns the square of the distance to the other input point in our
   * hypothetical domain. It is up to the implemented to decide how this will be
   * implemented, but with real numbers it should be (x1 - x2)^2 + (y1 - y2)^2 +
   * ...
   *
   * @param other
   *          - the other point, with which to compare the distance.
   * @return a distance, of type T
   */
  T squaredDistanceTo(Cartesian<T> other);

  // Same functionality as compares.
  /**
   * Compares the values of the point and the input point at "coordinate" (i.e.
   * compares get(coordinate) and other.get(coordinate)). A simple
   * implementation of this would simply call compareTo, since T is comparable.
   *
   * @param other
   *          - the point with which we are comparing
   * @param coordinate
   *          - the dimension at which to compare
   * @return a value representing how the values at the dimension compare, in
   *         the same way compareTo returns values. If they are equal it returns
   *         0, if this value is less than other's it returns a negative,
   *         otherwise positive.
   */
  int compareAtCoordinate(Cartesian<T> other, int coordinate);

  /**
   * Returns the square of the difference of the values at coordinate
   * "coordinate", i.e. (get(coordinate) - other.get(coordinate))^2.
   *
   * @param other
   *          - the point with which we are comparing
   * @param coordinate
   *          - the
   * @return a value representing the square of the difference at the input
   *         coordinate
   */
  T squaredDifferenceAtCoordinate(Cartesian<T> other, int coordinate);

  /**
   * Gets the dimension of the point, i.e. the number of values it holds. e.g.
   * for (1,2,3) it would be 3, for (1,1) it would be 2.
   *
   * @return - the dimension.
   */
  int getDimension();
}
