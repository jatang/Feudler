package edu.brown.cs.termproject.scoring;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;

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
public class Clustering<T extends Cluster> {

  private List<T> clusters; // Note that these are ordered.
  private Word2VecModel model;
  private ClusterFactory<T> factory;

  // TODO: Cover the case where something fits in two clusters, and put it in
  // the closer one.

  /**
   * Factory pattern for constructing a suggestion clustering. Note that model
   * is last, because that's the constructor we want to use.
   *
   * @param answers
   *          the suggestions that google returns, as a list of strings
   * @param model
   *          the word2vec model
   * @return a clustering of the suggestions
   */
  public static Clustering<Suggestion> newSuggestionClustering(
      List<String> answers, Word2VecModel model) {
    return new Clustering<Suggestion>(answers, new SuggestionFactory(), model);
  }

  /**
   * Factory pattern for making a guess clustering. Assumes that most of the
   * time, the guesses will already be in clusters, and that we're trying to add
   * to them or make some new ones.
   * 
   * @param clusters
   *          the pre-existing clusters, an empty list if none exist
   * @param model
   *          the word2vec model
   * @return a clustering representing all the guesses for some query
   */
  public static Clustering<Guess> newGuessClustering(List<Guess> clusters,
      Word2VecModel model) {
    return new Clustering<Guess>(clusters, model, new GuessFactory());
  }

  /**
   * Creates a clustering out of the pre-existing clusters. Used when adding a
   * guess to the database.
   *
   * @param clusters
   *          the pre-existing clusters, that make up the clustering
   */
  private Clustering(List<T> clusters, Word2VecModel model,
      ClusterFactory<T> factory) {
    this.clusters = new ArrayList<>(clusters);
    this.model = model;
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
  private Clustering(List<String> answers, ClusterFactory<T> factory,
      Word2VecModel model) {
    this.clusters = new ArrayList<>();
    answers.forEach(this::add);
    this.model = model;
    this.factory = factory;
  }

  /**
   * Instantiates an empty clustering. Can add clusters.
   *
   * @param factory
   *          the factory used to create clusters
   */
  public Clustering(Word2VecModel model) {
    this.clusters = new ArrayList<>();
    this.model = model;
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
    List<WordVector> vectors = model.tokenize(phrase);
    for (T cluster : clusters) {
      if (cluster.contains(vectors)) {
        cluster.add(vectors);
        return cluster;
      }
    }
    T newCluster = factory.newInstance(vectors);
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
    List<WordVector> vectors = model.tokenize(phrase);
    for (T cluster : clusters) {
      if (cluster.contains(vectors)) {
        return Optional.of(cluster);
      }
    }
    return Optional.absent();
  }

  public int size() {
    // TODO Auto-generated method stub
    return 0;
  }
}
