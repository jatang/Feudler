package edu.brown.cs.termproject.networking;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@WebSocket
public class ServerSocket {
  private static final Gson GSON = new Gson();

  private static final Set<String> ROOM_IDS = new ConcurrentHashSet<>();
  private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();

  private static String generateRoomId() {
    return null;
  }

  private static enum MESSAGE_TYPE {
    CONNECT, CREATE_ROOM, CUSTOM_QUERY, NEW_GAME, NEW_ROUND, USER_JOIN,
    USER_LEFT, PLAYER_GUESS, USER_CHAT
  }

  private static final MESSAGE_TYPE[] MESSAGE_VALUES = MESSAGE_TYPE.values();

  @OnWebSocketConnect
  public void connected(Session session) throws IOException {
    JsonObject connMessage = new JsonObject();

    connMessage.addProperty("type", MESSAGE_TYPE.CONNECT.ordinal());
    connMessage.addProperty("payload", "");

    session.getRemote().sendString(connMessage.toString());
  }

  @OnWebSocketClose
  public void closed(Session session, int statusCode, String reason) {

    // Compose and send USER_LEFT message here
  }

  @OnWebSocketMessage
  public void message(Session session, String message) {

    JsonObject received;
    JsonObject payload;
    try {
      received = GSON.fromJson(message, JsonObject.class);
      payload = received.get("payload").getAsJsonObject();
    } catch (JsonSyntaxException ex) {
      return;
    }

    Room room;
    switch (MESSAGE_VALUES[received.get("type").getAsInt()]) {
      case CREATE_ROOM:
        // Payload contains nothing

        room = createRoom(session, payload);
        ROOM_IDS.add(room.getRoomId());
        ROOMS.put(room.getRoomId(), room);

        // Send back CREATE_ROOM success and room link.
        break;
      case CUSTOM_QUERY:
        // Payload contains query text.

        // Check whether or not custom query is valid. Send back response on
        // CUSTOM_QUERY.
        break;
      case NEW_GAME:
        // Payload contains room link/id, settings

        // Check whether user requesting new game is owner. If so, start new
        // game with same room otherwise do nothing.

        room = ROOMS.get(payload.get("id").getAsString());
        if (room == null) {
          return;
        }

        if (session.equals(room.getCreator())) {
          room.newGame(/* Settings */);
        }

        // Send back response on NEW_GAME.
        break;
      case NEW_ROUND:
        // Payload contains room link/id

        // Check whether user requesting new round is owner. If so, start round
        // game in room otherwise do nothing. Send back response (first query)
        // on NEW_ROUND.

        room = ROOMS.get(payload.get("id").getAsString());
        if (room == null) {
          return;
        }

        if (session.equals(room.getCreator())) {
          room.getGame().newRound();
        }
        break;
      case USER_JOIN:
        // Payload contains room link/id, user username

        // Check whether room id is valid and is accepting users. If so, add
        // user to room and send back response (user id) on USER_JOIN.
        break;
      case PLAYER_GUESS:
        // Payload contains room link/id, guess text

        // Check whether room id is valid, contains the User with Session
        // session, and the game has a Player with that user. If so, perform
        // guessing game logic on room with player and
        // guess (check if valid guess, check if already guessed),
        // otherwise do nothing. Send back response (valid (w/score), invalid)
        // on PLAYER_GUESS.
        break;
      case USER_CHAT:
        // Payload contains room link/id, user message

        // Check whether room id is valid and contains the User with Session
        // session. If so, send username, message to all users in the room on
        // USER_CHAT.
        break;
      default:
        // Send error
    }
  }

  private Room createRoom(Session session, JsonObject payload) {
    // Settings include:
    // - Single Player / Multiplayer (w/ player limit)
    // - Mode: Google Feud, Meta Mode
    // - Number of Rounds
    // - (Category type)

    return new Room(generateRoomId(), session /* ,Settings from payload */);
  }
}
