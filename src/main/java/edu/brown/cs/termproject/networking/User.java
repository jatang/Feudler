package edu.brown.cs.termproject.networking;

import java.util.Objects;

import org.eclipse.jetty.websocket.api.Session;

/**
 * A class representing an User connected to a Room. The User may or may not be
 * in a Game.
 *
 * @author lcohen2
 */
public class User {

  private final Session session;
  private final int id;
  private String username;
  // Don't know if we want spectating. Default will be false unless we do.
  private boolean spectating;

  /**
   * Creates a User object representing a person in a Room.
   *
   * @param session
   * 	The session of the User.
   * @param id
   *          The id of the User to distinguish between players on the Client
   *          side.
   * @param username
   *          A String representing the User's username.
   * @param spectacting
   *          A boolean representing whether or not the User is just watching
   *          the Game.
   */
  public User(Session session, int id, String username, boolean spectating) {
	this.session = session;
    this.id = id;
    this.username = username;
    this.spectating = spectating;
  }

  /**
   * Gets the Session of the User.
   *
   * @return Returns the User's Session.
   */
  public Session getSession() {
    return session;
  }
  
  /**
   * Gets the id of the User.
   *
   * @return Returns an integer representing the user's id.
   */
  public int getId() {
    return id;
  }

  /**
   * Gets the username of the User.
   *
   * @return Returns a String representing the User's username.
   */
  public synchronized String getUsername() {
    return username;
  }

  /**
   * Sets the username of the User.
   *
   * @param username
   *          A String representing the new username of the User.
   */
  public synchronized void setUsername(String username) {
    this.username = username;
  }

  /**
   * Gets the spectating status of the User.
   *
   * @return Returns a boolean representing whether or not the User is
   *         spectating.
   */
  public synchronized boolean isSpectating() {
    return spectating;
  }

  /**
   * Sets the spectating status of the User.
   *
   * @param spectating
   *          A boolean representing whether or not the User wishes to spectate.
   */
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
    return id + ": " + username;
  }

}
