package edu.brown.cs.termproject.scoring;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a cartesian point containing double coordinates. Can be of any
 * size.
 *
 * @author asekula
 */
public class DoublePoint implements Cartesian<Double> {

  private List<Double> coordinates;
  private int dimension;

  /**
   * Creates an point with three double coordinates. Used for shorthand, to
   * avoid repeating Arrays.asList() in the constructor.
   *
   * @param x
   *          - the first coordinate
   * @param y
   *          - the second coordinate
   * @param z
   *          - the third coordinate
   */
  public DoublePoint(double x, double y, double z) {
    this(Arrays.asList(x, y, z));
  }

  /**
   * Uses the doubles in the input list to construct the point. The resulting
   * dimension is the size of the input list.
   *
   * @param coordinates
   *          - the list of double coordinates, ordered by their dimension
   */
  public DoublePoint(List<Double> coordinates) {
    this.coordinates = coordinates;
    dimension = coordinates.size();
  }

  /**
   * Returns the squared distance to the input point. If one dimension is
   * smaller, then it treats the rest of the coordinates as zero.
   *
   * @param p
   *          - the input point
   * @return - the squared distance to the input point
   */
  @Override
  public Double squaredDistanceTo(Cartesian<Double> p) {
    double total = 0;
    int maxDimension = Math.max(p.getDimension(), dimension);

    for (int i = 0; i < maxDimension; i++) {
      total += Math.pow(p.get(i) - get(i), 2);
    }
    return total;
  }

  @Override
  public Double squaredDifferenceAtCoordinate(Cartesian<Double> p, int depth) {
    double x = get(depth) - p.get(depth);
    return x * x;
  }

  @Override
  public Double get(int coordinate) {
    if (coordinate >= dimension) {
      return 0.0;
    } else {
      return coordinates.get(coordinate);
    }
  }

  @Override
  public int getDimension() {
    return dimension;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Cartesian<?>)) {
      return false;
    }

    Cartesian<?> other = (Cartesian<?>) o;
    for (int i = 0; i < dimension; i++) {
      if (!other.get(i).equals(get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return coordinates.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append(coordinates.get(0));
    for (Double x : coordinates.subList(1, dimension)) {
      sb.append(", " + x);
    }
    sb.append(")");

    return sb.toString();
  }

  @Override
  public int compareAtCoordinate(Cartesian<Double> other, int coordinate) {
    return Double.compare(get(coordinate), other.get(coordinate));
  }
}
