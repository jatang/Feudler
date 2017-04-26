package edu.brown.cs.termproject.scoring;

import java.util.List;

/**
 * Represents a cluster of phrases that are close enough to each other, that the
 * game will treat them as the same phrase.
 *
 * @author asekula
 *
 */
interface Cluster {

  /**
   * Checks if an input phrase belongs in the cluster.
   *
   * @param other
   *          the phrase to check, as a tokenized list of embeddings
   * @return true if it belongs in the cluster, false otherwise
   */
  default boolean contains(List<WordVector> other) {
    return similarity(other) >= similarityThreshold();
  }

  /**
   * Adds the input phrase to the cluster. Note that this implies that clusters
   * can change -- clusters are mutable collections.
   *
   * @param other
   *          the phrase to add to the cluster, as a tokenized list of
   *          embeddings
   */
  public void add(List<WordVector> other);

  /**
   * Returns the original string.
   * 
   * @return the original string associated with the cluster. Only used for
   *         suggestions.
   */
  public String getResponse();

  /**
   * Gets the score. Important: for suggestions, scores are 1-indexed. The top
   * suggestion will have a score of 1. For guesses, the score is the number of
   * phrases in the cluster.
   * 
   * @return the score associated with the cluster.
   */
  public int getScore();

  /**
   * Computes how similar the input phrase is to the cluster.
   *
   * @param other
   *          the phrase as a list of wordvectors
   * @return the similarity as a double from -1 to 1, the higher, the more
   *         similar
   */
  public double similarity(List<WordVector> other);

  /**
   * Gets the minimum similarity value that is allowed for a phrase to belong in
   * the cluster.
   * 
   * @return the similarity threshold
   */
  public double similarityThreshold();
}
