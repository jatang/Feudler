package edu.brown.cs.termproject.networking;

import java.util.Objects;

public class User {

  // Might also store Session in user.
  private final int id;
  private String username;
  // Don't know if we want spectating. Default will be false unless we do.
  private boolean spectating;

  public User(int id, String username, boolean spectating) {
    this.id = id;
    this.username = username;
    this.spectating = spectating;
  }

  public int getId() {
    return id;
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

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof User)) {
      return false;
    }

    User otherUser = (User) other;
    return id == otherUser.getId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return id + " " + username;
  }

}
