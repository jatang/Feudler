package edu.brown.cs.termproject.scoring;

import java.util.List;

/**
 * Represents a unique suggestion that google returns. This is one of the
 * answers of the given round. Note that this implements Cluster, so the
 * suggestions for the given round would be of type Clustering[Suggestion].
 *
 * @author asekula
 */
public class Suggestion implements Cluster {

  // Test the hyperparameters with different values.
  private static final double SUGGESTION_THRESHOLD = 0.6;
  private static final double GUESS_THRESHOLD = 0.6;

  private List<WordVector> vectors;

  /**
   * Initializes the suggestion with the input phrase embeddings.
   *
   * @param vectors
   *          the tokenized embeddings of the phrase
   */
  public Suggestion(List<WordVector> vectors) {
    this.vectors = vectors;
  }

  /**
   * Checks if an input guess matches the answer/suggestion.
   *
   * @param guess
   *          the user's guess
   * @param model
   *          the word2vec model, used to check similarity
   * @return true if the guess is close enough, false otherwise
   */
  public boolean contains(String guess, Word2VecModel model) {
    // TODO
    return false;
  }

  @Override
  public boolean contains(List<WordVector> otherSuggestion) {
    // This is the **not** the same contains that is used to check if a guess is
    // correct and whether another phrase should join the cluster.
    // That is done by .contains(String, Word2VecModel).

    double totalAvgSimilarity = (avgSimilarity(vectors, otherSuggestion)
        + avgSimilarity(otherSuggestion, vectors)) / 2;
    // Dividing by 2 is redundant but it keeps the scaling correct (max possible
    // is 1, min is -1).

    return totalAvgSimilarity >= SUGGESTION_THRESHOLD;
  }

  /*
   * Computes the avg similarity of each word in $these$ to the closest match in
   * $others$.
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
}
