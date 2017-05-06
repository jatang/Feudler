package edu.brown.cs.termproject.game;
//

//import com.google.common.collect.ImmutableList;
//
//import edu.brown.cs.termproject.networking.User;
//import edu.brown.cs.termproject.queryResponses.QueryResponses;
//import edu.brown.cs.termproject.scoring.Clustering;
//import edu.brown.cs.termproject.scoring.Suggestion;
//import edu.brown.cs.termproject.scoring.Word2VecModel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GameTest {

  /*
   * The following method is used to check whether inserting into the guesses
   * table of gFeud.sqlite3 actually works. Modify the query response ids and
   * the guesses in order to add to the db.
   */
  @Test
  public void testMetaMode() {
    // Clustering<Suggestion> clustering = Clustering
    // .newExistingSuggestionClustering(ImmutableList.of(),
    // Word2VecModel.model);
    //
    // Game game = new Game(1,
    // ImmutableList.of(new QueryResponses(1, "", clustering),
    // new QueryResponses(4, "", clustering)));
    // User user = new User(null, 1, "", false);
    // game.addPlayer(user);
    // game.newRound();
    // game.score(user, "should be id of one");
    // game.score(user, "test");
    // game.score(user, "the meta");
    // game.score(user, "random response");
    // game.newRound();
    // game.score(user, "should be id of 4");
    // game.score(user, "okay");
    // game.score(user, "very big");
    // game.endRound();
    // game.endGame();
  }

  @Test
  public void testGuessIsntJunk() {
    Game game = new Game(1, null);

    assertTrue(game.guessIsntJunk("red"));
    assertTrue(game.guessIsntJunk("apple"));
    assertTrue(game.guessIsntJunk("apple tree"));
    assertTrue(game.guessIsntJunk("don't stop"));
    assertTrue(game.guessIsntJunk("can't won't y'all"));
    assertTrue(game.guessIsntJunk("surf"));
    assertTrue(game.guessIsntJunk("fly"));
    assertTrue(game.guessIsntJunk("bae"));
    assertTrue(game.guessIsntJunk("in labor"));
    assertTrue(game.guessIsntJunk("boston"));

    assertFalse(game.guessIsntJunk("ksljdf;lasjd;alskdja"));
    assertFalse(game.guessIsntJunk("1 pear"));
    assertFalse(game.guessIsntJunk("14"));
    assertFalse(game.guessIsntJunk("cs32"));
    assertFalse(game.guessIsntJunk("sdlfkjslkdfjs; sddfd"));
    assertFalse(game.guessIsntJunk("sa la jnwdka n nwkak "));
    assertFalse(game.guessIsntJunk("penis"));
    assertFalse(game.guessIsntJunk("xxx"));
  }
}
