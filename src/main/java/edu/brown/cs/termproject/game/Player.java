package edu.brown.cs.termproject.game;

import edu.brown.cs.termproject.networking.User;

/**
 * A class representing an Player in a Room's Game.
 *
 * @author lcohen2
 */
public class Player {

  private User user;
  private int score = 0;

  /**
   * Creates a Player object representing a User in the Room's Game.
   *
   * @param user
   *          The user representing the player.
   */
  public Player(User user) {
    this.user = user;
  }

  /**
   * Gets the Player's score.
   *
   * @return Returns an int representing the Player's score.
   */
  public synchronized int getScore() {
    return score;
  }

  /**
   * Sets the Player's score.
   *
   * @param score
   *          An integer representing the new Player's score.
   */
  public synchronized void setScore(int score) {
    this.score = score;
  }

  /**
   * Gets the user id of the Player.
   *
   * @return Returns an int representing the Player's id.
   */
  public int getId() {
    return user.getId();
  }

  /**
   * Gets the username of the Player.
   *
   * @return Returns a String representing the Player's username.
   */
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Player)) {
      return false;
    }

    Player otherPlayer = (Player) other;
    return getId() == otherPlayer.getId();
  }

  @Override
  public int hashCode() {
    return user.hashCode();
  }

  @Override
  public String toString() {
    return getId() + ": " + getUsername();
  }

}
