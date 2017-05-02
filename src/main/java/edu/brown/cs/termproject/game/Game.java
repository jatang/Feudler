package edu.brown.cs.termproject.game;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Optional;

import edu.brown.cs.termproject.networking.User;
import edu.brown.cs.termproject.queryResponses.QueryResponses;
import edu.brown.cs.termproject.scoring.Suggestion;

/**
 * A class representing an individual game on the server (either single or
 * multiplayer). Thread safe for access from players.
 *
 * @author lcohen2
 */
public class Game {

  private final Map<User, Player> playerMap = new ConcurrentHashMap<>();
  private int playerLimit = 10;
  private int currRound = -1;
  private double time = 0;

  private final List<QueryResponses> queries;
  private Set<Suggestion> alreadyGuessed;
  // Mode
  // Category

  /**
   * Creates a Game object from a List of QueryResponses and settings.
   *
   * @param queries
   *          A List of QueryResponses representing the queries for the duration
   *          of the Game.
   */
  public Game(List<QueryResponses> queries /* Settings */) {
    this.queries = queries;
  }

  /**
   * Creates a Game object from a List of QueryResponses, initial players and
   * settings.
   *
   * @param users
   *          A List of User that is to play the Game.
   * @param queries
   *          A List of QueryResponses representing the queries for the duration
   *          of the Game.
   */
  public Game(List<User> users, List<QueryResponses> queries
  /* Settings */) {

    for (User user : users) {
      if (playerMap.size() >= playerLimit) {
        break;
      }
      playerMap.put(user, new Player(user));
    }

    this.queries = queries;
  }

  /**
   * Gets the current QueryResponses from the Game if available.
   *
   * @return Returns a QueryResponses object representing the current query for
   *         the round.
   */
  private synchronized QueryResponses getCurrentQueryResponses() {
    if (currRound < 0 || currRound >= queries.size()) {
      return null;
    }
    return queries.get(currRound);
  }
  
  /**
   * Gets the current query from the Game if available.
   *
   * @return Returns a String representing the current query for
   *         the round.
   */
  public synchronized String getCurrentQuery() {
	  QueryResponses curr = getCurrentQueryResponses();
	  if(curr == null) {
		  return "";
	  }
	  return curr.getQuery();
  }
  
  /**
   * Gets the current number of responses for the current query.
   *
   * @return Returns an int representing the number of responses for the current query.
   */
  public synchronized int getCurrentNumResponses() {
	  QueryResponses curr = getCurrentQueryResponses();
	  if(curr == null) {
		  return 0;
	  }
	  return curr.getResponses().size();
  }

  /**
   * Gets the Set of Suggestion already guessed for the current round.
   *
   * @return Returns a Set of Suggestion already guessed for the current round.
   */
  public synchronized Set<Suggestion> getGuessedSuggestions() {
    return new HashSet<>(alreadyGuessed);
  }

  /**
   * Moves the Game to the next round if queries are still unplayed.
   *
   * @return Returns a QueryResponses object representing the query for the new
   *         round.
   */
  public synchronized QueryResponses newRound() {
    currRound++;
    alreadyGuessed = new HashSet<>();
    return getCurrentQueryResponses();
  }

  /**
   * Ends the current Game round.
   *
   * @return Returns a QueryResponses object representing the query for the new
   *         round.
   */
  public synchronized QueryResponses endRound() {
    QueryResponses curr = getCurrentQueryResponses();
    if (curr != null) {
      alreadyGuessed.addAll(curr.getResponses().asList());
    }
    return curr;
  }

  /**
   * Gets the current time left.
   *
   * @return Returns a double representing the time left.
   */
  public double getTime() {
    return time;
  }

  /**
   * Sets the current time left.
   *
   * @param time
   *          A double representing the time left.
   */
  public void setTime(double time) {
    this.time = time;
  }

  /**
   * Scores a users guess if the User is a Player in the Game.
   *
   * @param user
   *          A User who is presumably a Player in the Game.
   * @param guess
   *          A String representing the guess the user typed in.
   * @return Returns an Optional Suggestion for whether or not the guess was
   *         close enough to recieve a score.
   */
  public synchronized Optional<Suggestion> score(User user, String guess) {
    if (playerMap.containsKey(user)) {
      Player player = playerMap.get(user);

      if (currRound < 0 || currRound >= queries.size()) {
        return Optional.absent();
      }

      // TODO Save guess

      Optional<Suggestion> res =
          queries.get(currRound).getResponses().clusterOf(guess);
      if (res.isPresent()) {
        Suggestion closest = res.get();
        if (!alreadyGuessed.contains(closest)) {
          alreadyGuessed.add(closest);
          player.setScore(player.getScore() + (10 - closest.getScore()) * 1000);
          return res;
        }
      }
    }

    return Optional.absent();
  }

  /**
   * Ends the Game and saves all data collected during the Game.
   */
  public synchronized void endGame() {
    // Save all data
  }

  /**
   * Gets the current score of a Player in the Game.
   *
   * @param user
   *          A User who is presumably a Player in the Game.
   * @return Returns an integer representing the user's current score.
   */
  public int getPlayerScore(User user) {
    Player player = playerMap.get(user);

    if (player != null) {
      return player.getScore();
    }
    return 0;
  }

  /**
   * Adds a new User as a Player into the Game.
   *
   * @param user
   *          A User to enter as a Player into the Game.
   * @return Returns a boolean representing whether or not the player was able
   *         to be added.
   */
  public boolean addPlayer(User user) {
    if (playerMap.size() >= playerLimit) {
      return false;
    }
    return playerMap.put(user, new Player(user)) != null;
  }

  /**
   * Removes a User / Player from the Game.
   *
   * @param user
   *          A User to remove from the Game.
   * @return Returns a boolean representing whether or not the player was able
   *         to be removed.
   */
  public boolean removePlayer(User user) {
    return playerMap.remove(user) != null;
  }

}
