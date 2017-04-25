package edu.brown.cs.termproject.scoring;

import java.util.List;

/**
 * Really basic factory for suggestions. Not used outside of this package.
 * 
 * @author asekula
 */
class SuggestionFactory implements ClusterFactory<Suggestion> {
  @Override
  public Suggestion newInstance(List<WordVector> vectors) {
    return new Suggestion(vectors);
  }
}
