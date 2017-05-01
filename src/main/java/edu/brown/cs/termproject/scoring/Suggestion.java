package edu.brown.cs.termproject.scoring;

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
  private static final double THRESHOLD = 0.6;

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
    this.vectors = vectors;
    this.originalPhrase = phrase;
    this.score = score;
  }

  @Override
  public double similarity(List<WordVector> otherSuggestion) {
    // Dividing by 2 is redundant but it keeps the scaling correct (max possible
    // is 1, min is -1).
    return (avgSimilarity(vectors, otherSuggestion)
        + avgSimilarity(otherSuggestion, vectors)) / 2;
  }

  @Override
  public double similarityThreshold() {
    return THRESHOLD;
  }

  /*
   * Computes the avg similarity of each word in the first list to the closest
   * match in the second.
   */
  private double avgSimilarity(List<WordVector> these,
      List<WordVector> others) {
    double total = 0;
    for (WordVector vector : these) {
      double maxSimilarity = -1;
      for (WordVector otherVector : others) {
        double similarity = vector.similarity(otherVector);
        if (similarity > maxSimilarity) {
          maxSimilarity = similarity;
        }
      }
      total += maxSimilarity;
    }
    return these.isEmpty() ? 0 : total / these.size();
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
	  if(!(other instanceof Suggestion)) {
		  return false;
	  }
	  
	  Suggestion otherSuggestion = (Suggestion) other;
	  return originalPhrase.equals(otherSuggestion.getResponse());
  }
  
  @Override
  public int hashCode() {
	  return Objects.hash(getResponse());
  }
}
