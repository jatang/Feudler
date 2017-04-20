package edu.brown.cs.termproject.networking;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;

import edu.brown.cs.termproject.game.Game;

public class Room {

  private final String roomId;
  private final Session creator;
  private final Map<Session, User> userMap = new ConcurrentHashMap<>();

  private Game game = null;
  // Store Settings if needed

  public Room(String roomId, Session creator /* , Settings */ ) {
    this.roomId = roomId;
    this.creator = creator;
  }

  public User addUser(Session session, String username) {
    return userMap.put(session, new User(0, username, false));
  }

  public User getUser(Session session) {
    return userMap.get(session);
  }

  public boolean removeUser(Session session) {
    return userMap.remove(session) != null;
  }

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
      game = new Game(playingUsers /* , Settings */);
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public String getRoomId() {
    return roomId;
  }

  public Session getCreator() {
    return creator;
  }

  public Set<Session> getUserSessions() {
    return userMap.keySet();
  }

  public Game getGame() {
    return game;
  }

}
