package edu.brown.cs.termproject.networking;

public class User {

  // Might also store Session in user.
  private String username;
  // Don't know if we want spectating. Default will be false unless we do.
  private boolean spectating;

  public User(String username, boolean spectating) {
    this.username = username;
    this.spectating = spectating;
  }

  public synchronized String getUsername() {
    return username;
  }

  public synchronized void setUsername(String username) {
    this.username = username;
  }

  public synchronized boolean isSpectating() {
    return spectating;
  }

  public synchronized void setSpectating(boolean spectating) {
    this.spectating = spectating;
  }

}
