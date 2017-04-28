package edu.brown.cs.termproject.networking;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;

import edu.brown.cs.termproject.game.Game;
import edu.brown.cs.termproject.queryGenerator.qGenerator;
import edu.brown.cs.termproject.queryResponses.QueryResponses;

/**
 * A class representing a Room on the server.
 *
 * @author lcohen2
 */
public class Room {

  private final String roomId;
  private final Session creator;
  private final Map<Session, User> userMap = new ConcurrentHashMap<>();
  private int userId = -1;

  private Game game = null;
  // Store Settings if needed

  /**
   * Creates a Room object.
   *
   * @param roomId
   *          A String representing the id of the Room.
   * @param creator
   *          A Session representing the session that instantiated the Game.
   */
  public Room(String roomId, Session creator /* , Settings */ ) {
    this.roomId = roomId;
    this.creator = creator;
  }

  /**
   * Adds a new User as a Player into the Game.
   *
   * @param session
   *          A Session representing the session of the new User.
   * @param username
   *          A String representing the username of the new User.
   * @return Returns the User entered if adding succeeded.
   */
  public synchronized User addUser(Session session, String username) {
    return userMap.put(session, new User(userId++, username, false));
  }

  /**
   * Gets a User based on a Session.
   *
   * @param session
   *          A Session presumably for a User in the Room.
   * @return Returns the User with Session session if found.
   */
  public User getUser(Session session) {
    return userMap.get(session);
  }

  /**
   * Removes a User from the Game based on a Session.
   *
   * @param session
   *          A Session presumably for a User in the Room.
   * @return Returns a boolean representing whether or not the User was able to
   *         be removed.
   */
  public boolean removeUser(Session session) {
    return userMap.remove(session) != null;
  }

  /**
   * Creates a new Game for the Room.
   */
  public void newGame(/* Settings */) {
    if (game != null) {
      game.endGame();
    }

    List<User> playingUsers = new ArrayList<>();
    for (User user : userMap.values()) {
      if (!user.isSpectating()) {
        playingUsers.add(user);
      }
    }

    try {
      List<QueryResponses> queryResponses = new qGenerator().nRandomQrs(5);
      game = new Game(playingUsers, queryResponses /* , Settings */);
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  /**
   * Gets the id of the Room.
   *
   * @return Returns a String representing the Player's username.
   */
  public String getRoomId() {
    return roomId;
  }

  /**
   * Gets the creator of the Room.
   *
   * @return Returns a Session representing the User who created the Room.
   */
  public Session getCreator() {
    return creator;
  }

  /**
   * Gets all the Room sessions.
   *
   * @return Returns a Set of Session representing all the Users in the Room.
   */
  public Set<Session> getUserSessions() {
    return userMap.keySet();
  }

  /**
   * Gets the Game associated with the Room.
   *
   * @return Returns the Game associated with the Room.
   */
  public Game getGame() {
    return game;
  }

}
