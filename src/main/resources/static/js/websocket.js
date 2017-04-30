const MESSAGE_TYPE = {
  CONNECT: 0,
  CREATE_ROOM: 1, 
  CUSTOM_QUERY: 2, 
  NEW_GAME: 3, 
  NEW_ROUND: 4, 
  USER_JOIN: 5,
  USER_LEFT: 6, 
  PLAYER_GUESS: 7, 
  USER_CHAT: 8
};

let conn;

const setup_live_scores = () => {
  conn = new WebSocket("ws://localhost:4567/room")

  conn.onerror = err => {
    console.log('Connection error:', err);
  };

  conn.onmessage = msg => {
    const data = JSON.parse(msg.data);

    switch (data.type) {
      default:
        console.log('Unknown message type!', data.type);
        break;
      case MESSAGE_TYPE.CONNECT:

        break;
      case MESSAGE_TYPE.CREATE_ROOM:

        break;
      case MESSAGE_TYPE.CUSTOM_QUERY:

      break;
      case MESSAGE_TYPE.NEW_GAME:

      break;
      case MESSAGE_TYPE.NEW_ROUND:

      break;
      case MESSAGE_TYPE.USER_JOIN:

      break;
      case MESSAGE_TYPE.USER_LEFT:

      break;
      case MESSAGE_TYPE.PLAYER_GUESS:

      break;
      case MESSAGE_TYPE.USER_CHAT:

      break;
    }
  };
}

const new_guess = guesses => {

  conn.send(JSON.stringify(params));
}
