package edu.brown.cs.termproject.game;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.brown.cs.termproject.networking.User;
import edu.brown.cs.termproject.queryGenerator.qGenerator;
import edu.brown.cs.termproject.queryResponses.QueryResponses;
import edu.brown.cs.termproject.queryResponses.Response;

public class Game {

  private final Map<User, Player> playerMap;
  private int playerLimit = 10;
  private int currRound = -1;
  private int numRounds;

  private final List<QueryResponses> queryResponses;
  // Mode
  // Category

  public Game(/* Settings */) throws SQLException {
    this.playerMap = new ConcurrentHashMap<>();
    this.queryResponses = new qGenerator().nRandomQrs(numRounds);
  }

  public Game(List<User> users /* , Settings */) throws SQLException {
    this.playerMap = new ConcurrentHashMap<>();
    for (User user : users) {
      playerMap.put(user, new Player(user));
    }

    this.queryResponses = new qGenerator().nRandomQrs(numRounds);
  }

  public synchronized QueryResponses newRound() {
    currRound++;
    if (currRound >= queryResponses.size()) {
      return null;
    }

    return queryResponses.get(currRound);
  }

  public synchronized Response score(User user, String guess) {
    if (playerMap.containsKey(user)) {

      // Save guess

      Response res = tempGuessValidate(guess);
      if (res != null) {
        Player player = playerMap.get(user);
        player.setScore(player.getScore() + res.getScore());
      }
      return res;
    }

    return null;
  }

  // TODO REPLACE
  private Response tempGuessValidate(String guess) {
    return queryResponses.get(currRound).getResponses().get(0);
  }

  public synchronized void endGame() {
    // Save all data
  }

  public int getPlayerScore(User user) {
    Player player = playerMap.get(user);

    if (player != null) {
      return player.getScore();
    }
    return 0;
  }

  public boolean addPlayer(User user) {
    return playerMap.put(user, new Player(user)) != null;
  }

  public boolean removePlayer(User user) {
    return playerMap.remove(user) != null;
  }

}
