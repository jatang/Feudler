package edu.brown.cs.termproject.networking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import edu.brown.cs.termproject.queryGenerator.qGenerator;
import edu.brown.cs.termproject.queryResponses.QueryResponses;
import edu.brown.cs.termproject.scoring.Suggestion;

@WebSocket
public class ServerSocket {
  private static final Gson GSON = new Gson();

  private static final Set<String> ROOM_IDS = Collections
      .synchronizedSet(new HashSet<String>());
  private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();
  private static final String roomChars = "23456789abcdefghjklmnpqrstuvwxyz";
  private static final int MAX_ROOMS = 20;

  private static String generateRoomId() {
    Random random = new Random();
    while (true) {
      String id = "";
      for (int i = 0; i < 6; i++) {
        id += roomChars.charAt(random.nextInt(roomChars.length() - 1));
      }

      if (!ROOM_IDS.contains(id)) {
        ROOM_IDS.add(id);
        return id;
      }
    }
  }

  private static enum MESSAGE_TYPE {
    CONNECT, CREATE_ROOM, CUSTOM_QUERY, NEW_GAME, NEW_ROUND, ROUND_END, UPDATE_TIME, USER_JOIN, USER_LEFT, USER_KICK, PLAYER_GUESS, USER_CHAT
  }

  private static final MESSAGE_TYPE[] MESSAGE_VALUES = MESSAGE_TYPE.values();

  @OnWebSocketConnect
  public void connected(Session session) throws IOException {
    JsonObject connMessage = new JsonObject();

    connMessage.addProperty("type", MESSAGE_TYPE.CONNECT.ordinal());
    connMessage.addProperty("payload", new JsonObject().toString());

    session.getRemote().sendString(connMessage.toString());
  }

  @OnWebSocketClose
  public void closed(Session session, int statusCode, String reason) {

    try {
      // Compose and send USER_LEFT message here if necessary
      for (Room room : ROOMS.values()) {
        User user = room.getUser(session);
        if (room.removeUser(session)) {
          boolean roomClose = false;
          if (session.equals(room.getCreator())) {
            if (room.getGame() != null) {
              room.getGame().endGame();
            }
            ROOM_IDS.remove(room.getRoomId());
            ROOMS.remove(room.getRoomId());
            roomClose = true;
          }

          JsonObject updateMessage = new JsonObject();
          JsonObject updatePayload = new JsonObject();

          updatePayload.addProperty("userId", user.getId());
          updatePayload.addProperty("username", user.getUsername());
          updatePayload.addProperty("roomClose", roomClose);

          updateMessage.addProperty("type", MESSAGE_TYPE.USER_LEFT.ordinal());
          updateMessage.addProperty("payload", updatePayload.toString());
          String updateMessageString = updateMessage.toString();

          // Send back response on USER_LEFT.
          for (Session sess : room.getUserSessions()) {
            sess.getRemote().sendString(updateMessageString);
          }
          break;
        }
      }
    } catch (Exception e) {
      System.out.println("ERROR: Unable to send USER_LEFT");
    }
  }

  @OnWebSocketMessage
  public void message(Session session, String message) {

    JsonObject received;
    JsonObject payload;
    try {
      received = GSON.fromJson(message, JsonObject.class);
      payload = received.get("payload").getAsJsonObject();
    } catch (JsonSyntaxException ex) {
      System.out.print("ERROR: Malformed message: " + message);
      return;
    }

    JsonObject updateMessage;
    JsonObject updatePayload;
    String updateMessageString;
    qGenerator generator;
    Room room;

    try {

      switch (MESSAGE_VALUES[received.get("type").getAsInt()]) {
        case CREATE_ROOM:
          // Payload contains nothing

          room = createRoom(session, payload);
          String roomId = "";
          if (room != null) {
            ROOM_IDS.add(room.getRoomId());
            ROOMS.put(room.getRoomId(), room);

            roomId = room.getRoomId();
          }

          updateMessage = new JsonObject();
          updatePayload = new JsonObject();

          updatePayload.addProperty("roomId", roomId);

          updateMessage.addProperty("type", MESSAGE_TYPE.CREATE_ROOM.ordinal());
          updateMessage.addProperty("payload", updatePayload.toString());

          // Send back CREATE_ROOM success and room link.
          session.getRemote().sendString(updateMessage.toString());
          break;
        case CUSTOM_QUERY:
          // Payload contains query text.

        	generator = new qGenerator();
        	
        	JsonArray valid = new JsonArray();
        	for(JsonElement query : payload.get("queries").getAsJsonArray()) {
        		valid.add(generator.validateQuery(query.getAsString()) == null);
        	}
        	
          // Check whether or not custom query is valid.
          updateMessage = new JsonObject();
          updatePayload = new JsonObject();

          updateMessage.addProperty("type",
              MESSAGE_TYPE.CUSTOM_QUERY.ordinal());
          updateMessage.addProperty("payload", updatePayload.toString());

          updatePayload.addProperty("valid", valid.toString());
          // Send back response on CUSTOM_QUERY.
          session.getRemote().sendString(updateMessage.toString());

          break;
        case NEW_GAME:
          // Payload contains room link/id, settings

          // Check whether user requesting new game is owner. If so, start new
          // game with same room otherwise do nothing.

          room = ROOMS.get(payload.get("roomId").getAsString().toLowerCase());
          if (room == null) {
            return;
          }

          if (session.equals(room.getCreator())) {
            JsonObject settings = payload.get("settings").getAsJsonObject();

            List<QueryResponses> customQueries = new ArrayList<>();

            if (payload.get("queries") != null) {
              generator = new qGenerator();
              JsonArray custom = payload.get("queries").getAsJsonArray();

              for (JsonElement query : custom.getAsJsonArray()) {
                QueryResponses q = generator.validateQuery(query.getAsString());
                if (q != null) {
                  customQueries.add(q);
                }
              }
            }

            room.newGame(settings.get("rounds").getAsInt(),
                settings.get("mode").getAsString(), customQueries);

            updateMessage = new JsonObject();
            updatePayload = new JsonObject();

            updateMessage.addProperty("type", MESSAGE_TYPE.NEW_GAME.ordinal());
            updateMessage.addProperty("payload", updatePayload.toString());
            updateMessageString = updateMessage.toString();

            // Send back response on NEW_GAME.
            for (Session sess : room.getUserSessions()) {
              sess.getRemote().sendString(updateMessageString);
            }
          }

          break;
        case NEW_ROUND:
          // Payload contains room link/id

          // Check whether user requesting new round is owner. If so, start
          // round
          // game in room otherwise do nothing.

          room = ROOMS.get(payload.get("roomId").getAsString().toLowerCase());
          if (room == null) {
            return;
          }

          if (session.equals(room.getCreator()) && room.getGame() != null) {
            QueryResponses roundQuery = room.getGame().newRound();

            updateMessage = new JsonObject();
            updatePayload = new JsonObject();

            if (roundQuery != null) {
              updatePayload.addProperty("query", roundQuery.getQuery());
              updatePayload.addProperty("numResponses",
                  roundQuery.getResponses().size());
            } else {
              updatePayload.addProperty("query", "");
              updatePayload.addProperty("numResponses", 0);
            }

            updateMessage.addProperty("type", MESSAGE_TYPE.NEW_ROUND.ordinal());
            updateMessage.addProperty("payload", updatePayload.toString());
            updateMessageString = updateMessage.toString();

            // Send back response (round query) on NEW_ROUND.
            for (Session sess : room.getUserSessions()) {
              sess.getRemote().sendString(updateMessageString);
            }
          }
          break;
        case ROUND_END:
          // Payload contains room link/id

          // Check whether user requesting new round is owner. If so, end
          // current round in room. Otherwise do nothing.
          roundEnd(payload, session, false);
          // false because we only want this to work if the user is the host.

          break;
        case UPDATE_TIME:
          // Payload contains room link/id, time

          // Check whether room id is valid and whether user is owner. If so,
          // update the time

          room = ROOMS.get(payload.get("roomId").getAsString().toLowerCase());
          if (room == null) {
            return;
          }

          if (session.equals(room.getCreator()) && room.getGame() != null) {
            double time = payload.get("timeSeconds").getAsDouble();

            room.getGame().setTime(time);
          }
          break;
        case USER_JOIN:
          // Payload contains room link/id, user username

          // Check whether room id is valid and is accepting users. If so, add
          // user to room.
        	
          String error = "";

          room = ROOMS.get(payload.get("roomId").getAsString().toLowerCase());
          if (room != null) {
            final boolean added = room.addUser(session,
                payload.get("username").getAsString());
            final boolean gameInProgress = room.getGame() == null;

            if (added) {
              User addedUser = room.getUser(session);
              updateMessage = new JsonObject();
              updatePayload = new JsonObject();

              updatePayload.addProperty("userId", addedUser.getId());
              updatePayload.addProperty("username", addedUser.getUsername());

              if (gameInProgress) {
                updatePayload.addProperty("score", 0);
                updatePayload.addProperty("query", "");
                updatePayload.addProperty("timeSeconds", 0);
              } else {
                updatePayload.addProperty("score",
                    room.getGame().getPlayerScore(addedUser));
                updatePayload.addProperty("query",
                    room.getGame().getCurrentQuery());
                updatePayload.addProperty("timeSeconds",
                    room.getGame().getTime());
              }

              updateMessage.addProperty("type",
                  MESSAGE_TYPE.USER_JOIN.ordinal());
              updateMessage.addProperty("payload", updatePayload.toString());
              updateMessageString = updateMessage.toString();

              // Send back response (user id, username) on USER_JOIN
              for (Session sess : room.getUserSessions()) {
                if (!sess.equals(session)) {
                  sess.getRemote().sendString(updateMessageString);
                }
              }

              JsonArray users = new JsonArray();
              for (User user : room.getUsers()) {
                if (!user.equals(addedUser)) {
                  JsonObject userData = new JsonObject();
                  userData.addProperty("userId", user.getId());
                  userData.addProperty("username", user.getUsername());
                  int score = gameInProgress ? 0
                      : room.getGame().getPlayerScore(user);
                  userData.addProperty("score", score);

                  users.add(userData);
                }
              }

              JsonArray guessed = new JsonArray();
              if (room.getGame() != null) {
                updatePayload.addProperty("numResponses",
                    room.getGame().getCurrentNumResponses());
                for (Suggestion sugg : room.getGame().getGuessedSuggestions()) {
                  JsonObject suggestionData = new JsonObject();
                  suggestionData.addProperty("suggestion", sugg.getResponse());
                  suggestionData.addProperty("suggestionIndex",
                      sugg.getScore());
                  suggestionData.addProperty("score",
                      (10 - sugg.getScore()) * 1000);

                  guessed.add(suggestionData);
                }
              } else {
                updatePayload.addProperty("numResponses", 0);
              }

              updatePayload.addProperty("users", users.toString());
              updatePayload.addProperty("guessed", guessed.toString());

              updateMessage.addProperty("payload", updatePayload.toString());

              session.getRemote().sendString(updateMessage.toString());
              return;
            } else {
            	error = "Room is full";
            }
          } else {
        	  error = "Room does not exist";
          }

          updateMessage = new JsonObject();
          updatePayload = new JsonObject();

          updatePayload.addProperty("userId", "");
          updatePayload.addProperty("username", "");
          updatePayload.addProperty("score", "");
          updatePayload.addProperty("error", error);

          updateMessage.addProperty("type", MESSAGE_TYPE.USER_JOIN.ordinal());
          updateMessage.addProperty("payload", updatePayload.toString());

          session.getRemote().sendString(updateMessage.toString());

          break;
        case USER_KICK:
          room = ROOMS.get(payload.get("roomId").getAsString().toLowerCase());
          if (room != null && session.equals(room.getCreator())) {

            User kickUser = room.getUser(payload.get("userId").getAsInt());
            if (kickUser != null) {
              updateMessage = new JsonObject();
              updatePayload = new JsonObject();

              updatePayload.addProperty("userId", kickUser.getId());
              updatePayload.addProperty("username", kickUser.getUsername());

              updateMessage.addProperty("type",
                  MESSAGE_TYPE.USER_KICK.ordinal());
              updateMessage.addProperty("payload", updatePayload.toString());
              updateMessageString = updateMessage.toString();

              // Send back response on USER_KICK.
              for (Session sess : room.getUserSessions()) {
                sess.getRemote().sendString(updateMessageString);
              }
              room.removeUser(kickUser.getSession());
            }
          }
          break;
        case PLAYER_GUESS:
          // Payload contains room link/id, guess text

          // Check whether room id is valid, contains the User with Session
          // session, and the game has a Player with that user. If so, perform
          // guessing game logic on room with player and
          // guess (check if valid guess, check if already guessed),
          // otherwise do nothing.

          room = ROOMS.get(payload.get("roomId").getAsString().toLowerCase());
          if (room == null) {
            return;
          }

          User found = room.getUser(session);
          if (room.getGame() == null || found == null) {
            return;
          }

          Optional<Suggestion> res = room.getGame().score(found,
              payload.get("guess").getAsString());

          updateMessage = new JsonObject();
          updatePayload = new JsonObject();

          if (res.isPresent()) {
            Suggestion sugg = res.get();

            updatePayload.addProperty("suggestion", sugg.getResponse());
            updatePayload.addProperty("suggestionIndex", sugg.getScore());
            updatePayload.addProperty("score", (10 - sugg.getScore()) * 1000);
            updatePayload.addProperty("userId", found.getId());
            updatePayload.addProperty("username", found.getUsername());
            updatePayload.addProperty("playerScore",
                room.getGame().getPlayerScore(found));
          } else {
            updatePayload.addProperty("suggestion", "");
            updatePayload.addProperty("suggestionIndex", "");
            updatePayload.addProperty("score", "");
            updatePayload.addProperty("userId", found.getId());
            updatePayload.addProperty("username", found.getUsername());
            updatePayload.addProperty("playerScore",
                room.getGame().getPlayerScore(found));
          }

          updateMessage.addProperty("type",
              MESSAGE_TYPE.PLAYER_GUESS.ordinal());
          updateMessage.addProperty("payload", updatePayload.toString());
          updateMessageString = updateMessage.toString();

          // Send back response if valid (suggestion, score, user id,
          // playerScore) on PLAYER_GUESS.
          for (Session sess : room.getUserSessions()) {
            sess.getRemote().sendString(updateMessageString);
          }

          // If everything has been guessed, end the round.
          if (room.getGame().getGuessedSuggestions().size() == room.getGame()
              .getCurrentNumResponses()) {
            roundEnd(payload, session, true); // true because we want to force
                                              // the round to end
          }

          break;
        case USER_CHAT:
          // Payload contains room link/id, user message

          // Check whether room id is valid and contains the User with Session
          // session. If so, send username, message to all users in the room on
          // USER_CHAT.

          room = ROOMS.get(payload.get("roomId").getAsString().toLowerCase());
          if (room == null) {
            return;
          }

          User chatUser = room.getUser(session);
          if (chatUser == null) {
            return;
          }

          updateMessage = new JsonObject();
          updatePayload = new JsonObject();

          updatePayload.addProperty("userId", chatUser.getId());
          updatePayload.addProperty("username", chatUser.getUsername());
          updatePayload.addProperty("message",
              payload.get("message").getAsString());

          updateMessage.addProperty("type", MESSAGE_TYPE.USER_CHAT.ordinal());
          updateMessage.addProperty("payload", updatePayload.toString());
          updateMessageString = updateMessage.toString();

          for (Session sess : room.getUserSessions()) {
            sess.getRemote().sendString(updateMessageString);
          }
          break;
        default:
          // Send error
      }

    } catch (Exception e) {
      System.out.println("ERROR: Unexpected message: " + received.toString());
    }
  }

  private synchronized Room createRoom(Session session, JsonObject payload) {
    // Settings include:
    // - Single Player / Multiplayer (w/ player limit)
    // - Mode: Google Feud, Meta Mode
    // - Number of Rounds
    // - (Category type)

    if (ROOMS.size() < MAX_ROOMS) {
      return new Room(generateRoomId(), session,
          payload.get("maxUsers").getAsInt() /* ,Settings from payload */);
    }

    return null;
  }

  private void roundEnd(JsonObject payload, Session session,
      boolean forceRoundEnd) throws IOException {
    Room room = ROOMS.get(payload.get("roomId").getAsString().toLowerCase());
    if (room == null) {
      return;
    }

    // If the host is calling round end, or it's being forced (because all
    // answers were guessed already).
    if ((session.equals(room.getCreator()) || forceRoundEnd)
        && room.getGame() != null) {
      Set<Suggestion> alreadyGuessed = room.getGame().getGuessedSuggestions();
      QueryResponses roundQuery = room.getGame().endRound();
      if (roundQuery != null) {
        JsonObject updateMessage = new JsonObject();
        JsonObject updatePayload = new JsonObject();
        JsonArray suggestions = new JsonArray();

        for (Suggestion sugg : roundQuery.getResponses().asList()) {
          if (!alreadyGuessed.contains(sugg)) {
            JsonObject suggestionData = new JsonObject();
            suggestionData.addProperty("suggestion", sugg.getResponse());
            suggestionData.addProperty("suggestionIndex", sugg.getScore());
            suggestionData.addProperty("score", (10 - sugg.getScore()) * 1000);

            suggestions.add(suggestionData);
          }
        }

        updatePayload.addProperty("suggestions", suggestions.toString());

        updateMessage.addProperty("type", MESSAGE_TYPE.ROUND_END.ordinal());
        updateMessage.addProperty("payload", updatePayload.toString());
        String updateMessageString = updateMessage.toString();

        // Send back response (round query) on NEW_ROUND.
        for (Session sess : room.getUserSessions()) {
          sess.getRemote().sendString(updateMessageString);
        }
      }
    }
  }
}
