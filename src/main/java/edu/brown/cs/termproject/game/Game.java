package edu.brown.cs.termproject.game;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.brown.cs.termproject.networking.User;
import edu.brown.cs.termproject.queryResponses.QueryResponses;
import edu.brown.cs.termproject.scoring.Clustering;
import edu.brown.cs.termproject.scoring.Suggestion;
import edu.brown.cs.termproject.scoring.Word2VecModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

/**
 * A class representing an individual game on the server (either single or
 * multiplayer). Thread safe for access from players.
 *
 * @author lcohen2
 */
public class Game {

  private final Map<User, Player> playerMap = new ConcurrentHashMap<>();
  private final int maxPlayers;
  private int currRound = -1;
  private double time = 0;

  private final List<QueryResponses> queries;
  private Set<Suggestion> alreadyGuessed;
  private Multimap<Integer, String> guesses;
  // Mode
  // Category

  /**
   * Creates a Game object from a List of QueryResponses and settings.
   *
   * @param queries
   *          A List of QueryResponses representing the queries for the duration
   *          of the Game.
   */
  public Game(int maxPlayers, List<QueryResponses> queries /* Settings */) {
    this.maxPlayers = maxPlayers;
    this.queries = queries;
    this.guesses = HashMultimap.create();
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
  public Game(int maxPlayers, List<User> users, List<QueryResponses> queries
  /* Settings */) {
    this.maxPlayers = maxPlayers;

    for (User user : users) {
      if (playerMap.size() >= maxPlayers) {
        break;
      }
      playerMap.put(user, new Player(user));
    }

    this.queries = queries;
    this.guesses = HashMultimap.create();
  }

  /**
   * Gets the current QueryResponses from the Game if available.
   *
   * @return Returns a QueryResponses object representing the current query for
   *         the round.
   */
  public synchronized QueryResponses getCurrentQueryResponses() {
    if (currRound < 0 || currRound >= queries.size()) {
      return null;
    }
    return queries.get(currRound);
  }

  /**
   * Gets the current query from the Game if available.
   *
   * @return Returns a String representing the current query for the round.
   */
  public synchronized String getCurrentQuery() {
    QueryResponses curr = getCurrentQueryResponses();
    if (curr == null) {
      return "";
    }
    return curr.getQuery();
  }

  public synchronized List<String> getCurrentHints() {
    QueryResponses curr = getCurrentQueryResponses();
    if (curr == null) {
      return Collections.emptyList();
    }

    Set<String> stopwords = Word2VecModel.model.getStopwords();

    List<String> res = new ArrayList<>();
    for (Suggestion sugg : curr.getResponses().asList()) {
      Iterator<String> words = Arrays.asList(sugg.getResponse().split("\\s+"))
          .iterator();

      String word;
      StringBuilder sb = new StringBuilder();

      while (words.hasNext()) {
        word = words.next();
        if (stopwords.contains(word)) {
          sb.append(word);
        } else {
          sb.append(StringUtils.repeat("_", word.length()));
        }

        if (words.hasNext()) {
          sb.append(" ");
        }
      }
      res.add(sb.toString());
    }

    return res;
  }

  /**
   * Gets the current number of responses for the current query.
   *
   * @return Returns an int representing the number of responses for the current
   *         query.
   */
  public synchronized int getCurrentNumResponses() {
    QueryResponses curr = getCurrentQueryResponses();
    if (curr == null) {
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

      QueryResponses currentQuery = queries.get(currRound);

      // Save guess
      guesses.put(currentQuery.getId(), guess);

      Optional<Suggestion> res = currentQuery.getResponses().clusterOf(guess);
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
    // System.out.println("Ending game.");

    if (guesses.isEmpty()) {
      return;
    }

    // Note: This code is self-contained. All this does is take guesses and
    // merge them with the guesses in the database. This does not alter any
    // values in memory.

    // 1. Open connection to database.
    try {
      Class.forName("org.sqlite.JDBC");
      String urlToDb = "jdbc:sqlite:data/gFeud.sqlite3";

      Connection conn = DriverManager.getConnection(urlToDb);
      Statement stat = conn.createStatement();
      stat.executeUpdate("PRAGMA foreign_keys = ON;");
      stat.close();

      PreparedStatement getStoredGuesses = conn.prepareStatement(
          "select ID, answer, score from guesses where queryID=?;");

      // System.out.println("Going through ids.");
      for (Integer id : guesses.keySet()) {
        // System.out.println("Id of " + id);

        getStoredGuesses.setInt(1, id);
        try (ResultSet rs = getStoredGuesses.executeQuery()) {

          // 2. Make a clustering out of the current guesses in the database.
          List<Suggestion> storedGuesses = new ArrayList<>();
          Map<Suggestion, Integer> suggestionIds = new HashMap<>();
          while (rs.next()) {
            int suggestionId = rs.getInt(1);
            String text = rs.getString(2);
            int score = rs.getInt(3);
            Suggestion suggestion = new Suggestion(
                Word2VecModel.model.tokenize(text), text, score);
            suggestionIds.put(suggestion, suggestionId);
            storedGuesses.add(suggestion);
          }
          Clustering<Suggestion> clustering = Clustering
              .newExistingSuggestionClustering(storedGuesses);

          // The suggestion whose scores have been modified.
          // Includes the newly made suggestions.
          Set<Suggestion> modifiedScores = new HashSet<>();
          Set<Suggestion> newScores = new HashSet<>();

          // 3. Add new guesses to clustering.
          for (String guess : guesses.get(id)) {
            Optional<Suggestion> cluster = clustering.clusterOf(guess);

            // Don't need to call add. Wouldn't do anything.
            if (cluster.isPresent()) {
              cluster.get().setScore(cluster.get().getScore() + 1);
              if (!newScores.contains(cluster.get())) {
                modifiedScores.add(cluster.get());
              }
            } else {
              // Need to call add here to create a new cluster in the
              // clustering.
              Optional<Suggestion> newCluster = clustering.add(guess);
              if (newCluster.isPresent()) {
                newCluster.get().setScore(1);
                newScores.add(newCluster.get());
              } // else do nothing, the guess was just stopwords
            }
          }

          // 4. Update the changes in the database.
          PreparedStatement update = conn
              .prepareStatement("update guesses set score=? where ID=?;");
          for (Suggestion modified : modifiedScores) {
            int suggestionId = suggestionIds.get(modified);
            int score = modified.getScore();

            update.setInt(1, score);
            update.setInt(2, suggestionId);
            update.executeUpdate();
          }
          update.close();

          PreparedStatement insert = conn.prepareStatement(
              "insert into guesses (answer, queryID, score) values (?, ?, ?)");
          for (Suggestion newSuggestion : newScores) {
            String text = newSuggestion.getResponse();
            int score = newSuggestion.getScore();

            insert.setString(1, text);
            insert.setInt(2, id);
            insert.setInt(3, score);
            insert.executeUpdate();
          }
          insert.close();

          rs.close();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      getStoredGuesses.close();

      // 5. Set guesses to an empty set (in case this gets called multiple
      // times?)
      guesses = HashMultimap.create();

    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException(ex.getMessage());
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }

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
    if (playerMap.size() >= maxPlayers) {
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
