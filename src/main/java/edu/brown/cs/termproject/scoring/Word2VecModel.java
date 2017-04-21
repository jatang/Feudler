package edu.brown.cs.termproject.scoring;

import com.google.common.base.Optional;
import java.util.Set;

/**
 * Represents a word2vec model object. This handles the actual model, whether in
 * embedded python or from a database. Note that this does not do any
 * autocorrecting nor does it check any similarity of word sets/phrases. This
 * merely takes in
 *
 * @author asekula
 */
public interface Word2VecModel {

  /**
   * Returns the cosine similarity between two words.
   *
   * @param word1
   *          the first word
   * @param word2
   *          the second word
   * @return the cosine similarity, absent if none exist
   */
  public Optional<Double> distanceBetween(String word1, String word2);

  /**
   * Returns the word vector given the input word.
   *
   * @param word
   *          the input word
   * @return the vector, absent if none exists
   */
  public Optional<WordVector> vectorOf(String word);

  /**
   * The vocabulary of the model.
   *
   * @return a set of all the words in the model
   */
  public Set<String> vocabulary();
}
