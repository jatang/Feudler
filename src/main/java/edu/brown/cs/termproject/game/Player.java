package edu.brown.cs.termproject.game;

import edu.brown.cs.termproject.networking.User;

public class Player {

  private User user;
  private int score = 0;

  public Player(User user) {
    this.user = user;
  }

  public synchronized int getScore() {
    return score;
  }

  public synchronized void setScore(int score) {
    this.score = score;
  }

  public String getUsername() {
    return user.getUsername();
  }

}
