package edu.brown.cs.termproject.scoring;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a unique suggestion that google returns. This is one of the
 * answers of the given round. Note that this implements Cluster, so the
 * suggestions for the given round would be of type Clustering[Suggestion].
 *
 * @author asekula
 */
public class Suggestion implements Cluster {

  // Test the hyperparameter with different values.
  private static final double THRESHOLD = 0.40;

  private List<WordVector> vectors;
  private String originalPhrase;
  private int score;

  /**
   * Initializes the suggestion with the input phrase embeddings.
   *
   * @param vectors
   *          the tokenized embeddings of the phrase
   */
  public Suggestion(List<WordVector> vectors, String phrase, int score) {
    this.vectors = ImmutableList.copyOf(vectors);
    this.originalPhrase = phrase.toLowerCase();
    this.score = score;
  }

  /**
   * A simple method that allows checking similarity of other suggestions. Calls
   * similarity on the suggestion's WordVectors.
   *
   * @param other
   *          the other suggestion
   * @return a double representing how similar the suggestion is to this
   */
  public double similarity(Suggestion other) {
    return similarity(other.getVectors());
  }

  @Override
  public double similarity(List<WordVector> otherSuggestion) {
    return ((avgSimilarity(vectors, otherSuggestion) * (vectors.size()))
        + (avgSimilarity(otherSuggestion, vectors) * (otherSuggestion.size())))
        / (vectors.size() + otherSuggestion.size());
  }

  @Override
  public double similarityThreshold() {
    return THRESHOLD;
  }

  /*
   * Computes the average similarity of each word in the first list to the
   * closest match in the second.
   */
  private double avgSimilarity(List<WordVector> these,
      List<WordVector> others) {
    double totalMinSimilarity = 1.01;
    for (WordVector vector : these) {
      double maxSimilarity = -1;
      for (WordVector otherVector : others) {
        double similarity = vector.similarity(otherVector);
        if (similarity > maxSimilarity) {
          maxSimilarity = similarity;
        }
      }
      if (maxSimilarity < totalMinSimilarity) {
        totalMinSimilarity = maxSimilarity;
      }
    }

    return totalMinSimilarity;
  }

  @Override
  public void add(List<WordVector> added) {
    // Important: This method does nothing. Adding the phrase merely gets rid of
    // it in the clustering.
  }

  @Override
  public String getResponse() {
    return originalPhrase;
  }

  @Override
  public int getScore() {
    return score;
  }

  @Override
  public String toString() {
    return getResponse();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Suggestion)) {
      return false;
    }

    Suggestion otherSuggestion = (Suggestion) other;
    return originalPhrase.equals(otherSuggestion.getResponse());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getResponse());
  }

  @Override
  public List<WordVector> getVectors() {
    return vectors;
  }

  /**
   * Used to alter the suggestions' scores in meta mode. Should not be used for
   * anything else.
   */
  public void setScore(int newScore) {
    score = newScore;
  }
}
