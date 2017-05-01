package edu.brown.cs.termproject.networking;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import edu.brown.cs.termproject.queryResponses.QueryResponses;
import edu.brown.cs.termproject.scoring.Suggestion;

@WebSocket
public class ServerSocket {
  private static final Gson GSON = new Gson();

  private static final Set<String> ROOM_IDS =
      Collections.synchronizedSet(new HashSet<String>());
  private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();

  private static final String roomChars = "23456789ABCDEFGHJKLMNPQRSTUVWXY";

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
    CONNECT, CREATE_ROOM, CUSTOM_QUERY, NEW_GAME, NEW_ROUND, ROUND_END, USER_JOIN,
    USER_LEFT, PLAYER_GUESS, USER_CHAT
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

    // Compose and send USER_LEFT message here if necessary
    for (Room room : ROOMS.values()) {
      room.removeUser(session);
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
    Room room;

    try {

      switch (MESSAGE_VALUES[received.get("type").getAsInt()]) {
        case CREATE_ROOM:
          // Payload contains nothing

          room = createRoom(session, payload);
          ROOM_IDS.add(room.getRoomId());
          ROOMS.put(room.getRoomId(), room);

          updateMessage = new JsonObject();
          updatePayload = new JsonObject();

          updatePayload.addProperty("roomId", room.getRoomId());

          updateMessage.addProperty("type", MESSAGE_TYPE.CREATE_ROOM.ordinal());
          updateMessage.addProperty("payload", updatePayload.toString());

          // Send back CREATE_ROOM success and room link.
          session.getRemote().sendString(updateMessage.toString());
          break;
        case CUSTOM_QUERY:
          // Payload contains query text.

          // Check whether or not custom query is valid.
          updateMessage = new JsonObject();
          updatePayload = new JsonObject();

          updateMessage.addProperty("type",
              MESSAGE_TYPE.CUSTOM_QUERY.ordinal());
          updateMessage.addProperty("payload", updatePayload.toString());

          payload.addProperty("valid", false);

          // Send back response on CUSTOM_QUERY.
          session.getRemote().sendString(updateMessage.toString());

          break;
        case NEW_GAME:
          // Payload contains room link/id, settings

          // Check whether user requesting new game is owner. If so, start new
          // game with same room otherwise do nothing.

          room = ROOMS.get(payload.get("roomId").getAsString());
          if (room == null) {
            return;
          }

          if (session.equals(room.getCreator())) {
        	  JsonObject settings = payload.get("settings").getAsJsonObject();
            room.newGame(settings.get("rounds").getAsInt());
            
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
        	
          room = ROOMS.get(payload.get("roomId").getAsString());
          if (room == null) {
            return;
          }

          if (session.equals(room.getCreator())) {
            QueryResponses roundQuery = room.getGame().newRound();
            if (roundQuery != null) {
              updateMessage = new JsonObject();
              updatePayload = new JsonObject();

              updatePayload.addProperty("query", roundQuery.getQuery());
              updatePayload.addProperty("numResponses",
                  roundQuery.getResponses().size());

              updateMessage.addProperty("type",
                  MESSAGE_TYPE.NEW_ROUND.ordinal());
              updateMessage.addProperty("payload", updatePayload.toString());
              updateMessageString = updateMessage.toString();

              // Send back response (round query) on NEW_ROUND.
              for (Session sess : room.getUserSessions()) {
                sess.getRemote().sendString(updateMessageString);
              }
            }
          }
          break;
        case ROUND_END:
        	// Payload contains room link/id

            // Check whether user requesting new round is owner. If so, end
            // current round in room. Otherwise do nothing.
          	
            room = ROOMS.get(payload.get("roomId").getAsString());
            if (room == null) {
              return;
            }

            if (session.equals(room.getCreator())) {
              QueryResponses roundQuery = room.getGame().endRound();
              if (roundQuery != null) {
                updateMessage = new JsonObject();
                updatePayload = new JsonObject();
                JsonArray suggestions = new JsonArray();
                
                for(Suggestion sugg : roundQuery.getResponses().asList()) {
                	JsonObject suggestionData = new JsonObject();
                	suggestionData.addProperty("suggestion", sugg.getResponse());
                	suggestionData.addProperty("suggestionIndex", sugg.getScore());
                	suggestionData.addProperty("score", (10 - sugg.getScore()) * 1000);
                	
                	suggestions.add(suggestionData);
                }

                updatePayload.addProperty("suggestions", suggestions.toString());

                updateMessage.addProperty("type",
                    MESSAGE_TYPE.ROUND_END.ordinal());
                updateMessage.addProperty("payload", updatePayload.toString());
                updateMessageString = updateMessage.toString();

                // Send back response (round query) on NEW_ROUND.
                for (Session sess : room.getUserSessions()) {
                  sess.getRemote().sendString(updateMessageString);
                }
              }
            }
        	break;
        case USER_JOIN:
          // Payload contains room link/id, user username

          // Check whether room id is valid and is accepting users. If so, add
          // user to room.
        	
          room = ROOMS.get(payload.get("roomId").getAsString());
          if (room != null) {

	          User addedUser =
	              room.addUser(session, payload.get("username").getAsString());
	          
	          if (addedUser != null) {
	        	int score;
	            updateMessage = new JsonObject();
	            updatePayload = new JsonObject();
	
	            updatePayload.addProperty("userId", addedUser.getId());
	            updatePayload.addProperty("username", addedUser.getUsername());
	            score = room.getGame() == null ? 0 : room.getGame().getPlayerScore(addedUser);
	            updatePayload.addProperty("score", score);
	
	            updateMessage.addProperty("type", MESSAGE_TYPE.USER_JOIN.ordinal());
	            updateMessage.addProperty("payload", updatePayload.toString());
	            updateMessageString = updateMessage.toString();
	
	            // Send back response (user id, username) on USER_JOIN
	            for (Session sess : room.getUserSessions()) {
	            	if(!sess.equals(session)) {
	            		sess.getRemote().sendString(updateMessageString);
	            	}
	            }
	            
	            JsonArray users = new JsonArray();
	            for(User user : room.getUsers()) {
	            	if(!user.equals(addedUser)) {
		            	JsonObject userData = new JsonObject();
		            	userData.addProperty("userId", user.getId());
		            	userData.addProperty("username", user.getUsername());
		            	score = room.getGame() == null ? 0 : room.getGame().getPlayerScore(user);
		 	            updatePayload.addProperty("score", score);
		            	
		            	users.add(userData);
	            	}
	            }
	            
	            JsonArray guessed = new JsonArray();
	            if(room.getGame() != null) {
		            for(Suggestion sugg : room.getGame().getGuessedSuggestions()) {
		            	JsonObject suggestionData = new JsonObject();
		            	suggestionData.addProperty("suggestion", sugg.getResponse());
		            	suggestionData.addProperty("suggestionIndex", sugg.getScore());
		            	suggestionData.addProperty("score", (10 - sugg.getScore()) * 1000);
		            	
		            	guessed.add(suggestionData);
		            }
	            }
	            
	            updatePayload.addProperty("users", users.toString());
	            updatePayload.addProperty("guessed", guessed.toString());
	            
	            updateMessage.addProperty("payload", updatePayload.toString());
	            
	            session.getRemote().sendString(updateMessage.toString());
	            return;
	          }
          }
          
          updateMessage = new JsonObject();
          updatePayload = new JsonObject();

          updatePayload.addProperty("userId", "");
          updatePayload.addProperty("username", "");
          updatePayload.addProperty("score", "");

          updateMessage.addProperty("type", MESSAGE_TYPE.USER_JOIN.ordinal());
          updateMessage.addProperty("payload", updatePayload.toString());
            
          session.getRemote().sendString(updateMessage.toString());

          break;
        case PLAYER_GUESS:
          // Payload contains room link/id, guess text

          // Check whether room id is valid, contains the User with Session
          // session, and the game has a Player with that user. If so, perform
          // guessing game logic on room with player and
          // guess (check if valid guess, check if already guessed),
          // otherwise do nothing.

          room = ROOMS.get(payload.get("roomId").getAsString());
          if (room == null) {
            return;
          }

          User found = room.getUser(session);
          if (found == null) {
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
            updatePayload.addProperty("playerScore",
                room.getGame().getPlayerScore(found));
          } else {
            updatePayload.addProperty("suggestion", "");
            updatePayload.addProperty("suggestionIndex", "");
            updatePayload.addProperty("score", "");
            updatePayload.addProperty("userId", found.getId());
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

          break;
        case USER_CHAT:
          // Payload contains room link/id, user message

          // Check whether room id is valid and contains the User with Session
          // session. If so, send username, message to all users in the room on
          // USER_CHAT.

          room = ROOMS.get(payload.get("roomId").getAsString());
          if (room == null) {
            return;
          }

          User chatUser = room.getUser(session);
          if (chatUser == null) {
            return;
          }

          updateMessage = new JsonObject();
          updatePayload = new JsonObject();

          updatePayload.addProperty("username", chatUser.getUsername());
          updatePayload.addProperty("message",
              payload.get("message").getAsString());

          updateMessage.addProperty("type",
              MESSAGE_TYPE.USER_CHAT.ordinal());
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
    	e.printStackTrace();
      System.out.println("ERROR: Unexpected message: " + received.toString());
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
