package edu.brown.cs.termproject.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game {

  private List<Player> players;
  private int playerLimit;
  private int currRound;
  private int numRounds;
  // All queries for game
  // Current Query num
  // Mode
  // Category

  public Game(/* Settings */) {
    this.players = Collections.synchronizedList(new ArrayList<Player>());
  }

  public Game(List<Player> players /* , Settings */) {
    this.players = players;
  }

  public synchronized void newRound() {

  }

  public boolean addPlayer(Player player) {
    return players.add(player);
  }

  public boolean removePlayer(Player player) {
    return players.remove(player);
  }

}
