package edu.brown.cs.termproject.scoring;

import java.util.List;

/**
 * Really basic factory for guesses.
 *
 * @author asekula
 */
public class GuessFactory implements ClusterFactory<Guess> {
  @Override
  public Guess newInstance(List<WordVector> vectors, String phrase, int score) {
    return new Guess(vectors, phrase);
  }
}
