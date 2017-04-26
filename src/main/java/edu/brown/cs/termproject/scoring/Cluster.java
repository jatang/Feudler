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
  public boolean contains(List<WordVector> other);

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
   * Gets the score.
   * 
   * @return the score associated with the cluster. Only used for suggestions.
   */
  public int getScore();
}
