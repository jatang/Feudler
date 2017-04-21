package edu.brown.cs.termproject.scoring;

/**
 * A factory for the type T. Used by Clustering when it needs to make new
 * clusters.
 * 
 * @author asekula
 *
 * @param <T>
 *          the type of the cluster being made
 */
interface ClusterFactory<T extends Cluster> {

  /**
   * Creates a cluster from the input phrase.
   * 
   * @param phrase
   *          the phrase representing the initial point in the cluster
   * @return the cluster created
   */
  T newInstance(String phrase);
}
