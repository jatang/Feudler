package edu.brown.cs.termproject.scoring;

import com.google.common.collect.ImmutableList;

/**
 * Stores the word and word vector. This can calculate cosine similarity with
 * other vectors. Note that if multiple threads have access to the same
 * WordVector object, they can both call similarity while still being thread
 * safe. This is because the vector is stored as an immutable list and the
 * reference itself never changes, so the whole class is immutable.
 */
public class WordVector {

  private ImmutableList<Double> vector;

  public WordVector(String vectorString) {
    // TODO
  }

  public double similarity(WordVector other) {
    // TODO
    // Throws runtime exception if the vectors are of different length.
    return 0.0;
  }

  public ImmutableList<Double> getVector() {
    return vector;
  }
}
