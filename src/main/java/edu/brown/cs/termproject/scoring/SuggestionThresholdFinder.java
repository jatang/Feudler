package edu.brown.cs.termproject.scoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A singleton used to get the optimal threshold for Suggestions.
 *
 * @author asekula
 */
final class SuggestionThresholdFinder {

  /**
   * Gets the best threshold for similarity, as a value between 0 and 1.
   *
   * @param pathToCluster
   *          the path to the file containing the desired clusterings and their
   *          weights
   * @param pathToDontCluster
   *          the path to the file containing the undesired clusterings and
   *          their weights
   * @return the threshold that minimizes the loss
   */
  static double getOptimalThreshold(String pathToCluster,
      String pathToDontCluster) {
    List<Set<Suggestion>> groups = new ArrayList<>();

    return 0.0;
  }
}
