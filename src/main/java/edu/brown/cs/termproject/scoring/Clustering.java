package edu.brown.cs.termproject.scoring;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a set of clusters for either a set of suggestions or a set of
 * guesses. Used to group suggestions during the game that have roughly the same
 * meaning, and used to group guesses for meta-mode that also have roughly the
 * same meaning. This class is very not immutable -- as phrases get added, the
 * clusters inside the clustering change, and more clusters may be created.
 * Example of how to use this: Given a list of Strings representing phrases and
 * an EmbeddingCluster factory, do new Clustering[EmbeddingCluster](list,
 * factory) and that will create a clustering of Embedding clusters. Then to
 * check if a word is in a cluster, call .clusterOf(word) and it will return
 * Optiona.of(cluster) if it's in a cluster, and absent otherwise.
 *
 * @author asekula
 *
 * @param <T>
 *          the type of the cluster, either WordCluster or EmbeddingCluster
 */
class Clustering<T extends Cluster> {

  private List<T> clusters;
  private ClusterFactory<T> factory;

  /**
   * Creates a clustering out of the pre-existing clusters. Used when adding a
   * guess to the database.
   *
   * @param clusters
   *          the pre-existing clusters, that make up the clustering
   */
  public Clustering(Set<T> clusters, ClusterFactory<T> factory) {
    this.clusters = new ArrayList<>(clusters);
    this.factory = factory;
  }

  /**
   * Groups the answers into clusters, sorted such that if two answers become a
   * single cluster, the cluster word/embedding is the higher ranked answer.
   * Used when getting suggestions from google, to group them for
   * playing/storing. Stores the resulting clusters.
   * 
   * @param answers
   *          the phrases that google suggests
   */
  public Clustering(List<String> answers, ClusterFactory<T> factory) {
    this.clusters = new ArrayList<>();
    answers.forEach(this::add);
    this.factory = factory;
  }

  /**
   * Instantiates an empty clustering. Can add clusters.
   *
   * @param factory
   *          the factory used to create clusters
   */
  public Clustering(ClusterFactory<T> factory) {
    this.clusters = new ArrayList<>();
    this.factory = factory;
  }

  /**
   * Adds the guess/answer to the clustering, creating a new cluster if need be,
   * and returns the cluster it was added to (the updated cluster if the cluster
   * gets updated). Used when making a cluster out of suggestions returned from
   * Google, or when adding a guess to a pre-existing cluster.
   * 
   * @param phrase
   *          the guess or answer to add to the cluster
   * @return the updated cluster that the word was put into
   */
  public T add(String phrase) {
    for (T cluster : clusters) {
      if (cluster.contains(phrase)) {
        cluster.add(phrase);
        return cluster;
      }
    }
    T newCluster = factory.newInstance(phrase);
    clusters.add(newCluster);
    return newCluster;
  }

  /**
   * Gets the first cluster in the clustering that the word belongs to, returns
   * absent if no such cluster exists. Used to check if a player gets points for
   * their guess.
   *
   * @param phrase
   *          the user's guess while playing the game
   * @return the cluster that the word belongs to, none if no such cluster
   *         exists in the clustering. does not make a new cluster out of the
   *         word
   */
  public Optional<T> clusterOf(String phrase) {
    for (T cluster : clusters) {
      if (cluster.contains(phrase)) {
        return Optional.of(cluster);
      }
    }
    return Optional.absent();
  }
}
