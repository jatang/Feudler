package edu.brown.cs.termproject.networking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;

import edu.brown.cs.termproject.game.Game;
import edu.brown.cs.termproject.game.Player;

public class Room {

  private final String roomId;
  private final Session creator;
  private final Map<Session, User> userMap = new ConcurrentHashMap<>();

  private Game game;
  // Store Settings if needed

  public Room(String roomId, Session creator /* , Settings */ ) {
    this.roomId = roomId;
    this.creator = creator;
  }

  public void addUser(Session session, User user) {
    userMap.put(session, user);
  }

  public User getUser(Session session) {
    return userMap.get(session);
  }

  public boolean removeUser(Session session) {
    return userMap.remove(session) != null;
  }

  public void newGame(/* Settings */) {
    game = new Game(/* Settings */);

    for (User user : userMap.values()) {
      if (!user.isSpectating()) {
        game.addPlayer(new Player(user));
      }
    }
  }

  public String getRoomId() {
    return roomId;
  }

  public Session getCreator() {
    return creator;
  }

  public Map<Session, User> getUsers() {
    return userMap;
  }

  public Game getGame() {
    return game;
  }

}
