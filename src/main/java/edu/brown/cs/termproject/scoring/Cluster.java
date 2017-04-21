package edu.brown.cs.termproject.scoring;

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
   * @param phrase
   *          the phrase to check
   * @return true if it belongs in the cluster, false otherwise
   */
  public boolean contains(String phrase);

  /**
   * Adds the input phrase to the cluster. Note that this implies that clusters
   * can change -- clusters are mutable collections.
   *
   * @param phrase
   *          the phrase to add to the cluster
   */
  public void add(String phrase);
}
