package edu.brown.cs.termproject.scoring;

import com.google.common.base.Optional;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class LedCorrector {

  /**
   * Returns a string of LED of 1 away from mispelled, that is also in all. Does
   * so by generating all possible strings and returning one that is in all. If
   * none or multiple words match the criteria, it returns absent.
   *
   * @param all
   *          the vocabulary
   * @param misspelled
   *          the misspelled word
   * @return the unique word that is of led 1 away from misspelled that is also
   *         in the vocab, none if multiple or none exist
   */
  public static Optional<String> fix(Set<String> all, String misspelled) {
    Optional<String> match = Optional.absent();

    if (misspelled.isEmpty()) {
      return Optional.absent();
    }

    // Using an iterator to ensure that we don't compute all values and then
    // check if they're in the set, we only compute what we need (akin to
    // streams).
    Iterator<String> allPossible = new LedIterator(misspelled);
    while (allPossible.hasNext()) {
      String str = allPossible.next();
      if (all.contains(str)) {
        if (match.isPresent() && !match.get().equals(str)) {
          return Optional.absent();
        } else {
          match = Optional.of(str);
        }
      }
    }

    return match;
  }

  private enum Phase {
    DELETION, SUBSTITUTION, INSERTION;
  }

  static class LedIterator implements Iterator<String> {
    private int position; // 0 indexed.
    private Phase phase;
    private char letter; // from 'a' to 'z'
    private String str;

    public LedIterator(String str) {
      position = -1; // Important that this is -1.
      phase = Phase.DELETION;
      letter = 'a';
      this.str = str;

      if (str.isEmpty()) {
        throw new IllegalArgumentException("Cannot use an empty string.");
      }
    }

    @Override
    public boolean hasNext() {
      return (!(phase.equals(Phase.INSERTION) && position == str.length()
          && letter == 'z'));
    }

    @Override
    public String next() {
      // 1. Increase necessary values.
      incrementValues();

      // 2. Return the string with those attributes.
      if (phase == Phase.DELETION) {
        // position will never be the length of the string
        return str.substring(0, position) + str.substring(position + 1);
      } else if (phase == Phase.SUBSTITUTION) {
        // position range: [0, length - 1]
        return str.substring(0, position) + letter
            + str.substring(position + 1);
      } else { // INSERTION
        // position range: [0, length]
        return str.substring(0, position) + letter + str.substring(position);
      }
    }

    private void incrementValues() {
      if (phase == Phase.DELETION) {
        if (position == (str.length() - 1)) {
          phase = Phase.SUBSTITUTION;
          position = 0;
          letter = 'a';
        } else {
          position += 1;
        }
      } else if (phase == Phase.SUBSTITUTION) {
        if (letter == 'z') {
          if (position == (str.length() - 1)) {
            phase = Phase.INSERTION;
            position = 0;
            letter = 'a';
          } else {
            position += 1;
            letter = 'a';
          }
        } else {
          letter += 1;
        }
      } else {
        if (letter == 'z') {
          if (position == str.length()) {
            throw new NoSuchElementException("No next string.");
          } else {
            position += 1;
            letter = 'a';
          }
        } else {
          letter += 1;
        }
      }
    }
  }
}
