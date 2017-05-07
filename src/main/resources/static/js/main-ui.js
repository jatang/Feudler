const CONNECT = 0;
const CREATE_ROOM = 1;
const CUSTOM_QUERY = 2;
const NEW_GAME = 3;
const NEW_ROUND = 4;
const ROUND_END = 5;
const UPDATE_TIME = 6;
const USER_JOIN = 7;
const USER_LEFT = 8;
const USER_KICK = 9;
const PLAYER_GUESS = 10;
const USER_CHAT = 11;

const MAX_ROUNDS = 5;
let room;

// Views
let $home;
let $settings;
let $custom;
let $playSingle;
// Radio buttons
let $categoryAny;
let $categoryCustom;
let $categoryScience;
let $modeMeta;
let $modeStandard;
// Sliderle
let $sliderRounds;
// Play buttons
let $nextRound;
let $submit;
// Display elements
let $roomCodeField;
let $scoreArea;
let $score;
let $timer;

let $guess;

let $dialog;

let connection;



$(document).ready(() => {
    connection = new Connection("ws://localhost:4567/connection");
    $home =$("#home");
    $settings = $("#settings").hide();
    $custom = $("#custom").hide();
    $playSingle = $("#play-single").hide();

    $scoreArea = $("#scoreArea").hide();
    $singleScore = $("#singleplayerScore").hide();
    $multiScore = $("#multiplayerScore").hide();

    $lobby = $("#lobby").hide();
    $waitingMessage = $("#waiting-message").hide();
    $roomCodeArea = $("#room-code-area");
    $roomCodeField = $("#room-code-field");
    $startFromLobby = $("#start-from-lobby").hide().click(() => {
        connection.sendNewGameMessage();
        $startFromLobby.hide("fade");
    });

    $nextRound = $("#next-round").hide();

    $nextRound.click(() => connection.sendNewRoundMessage());
    $submit = $("#submit");



    $submit.click(() => room.game.round.checkAnswer());
// Sets up buttons.
    $("input[type='radio']").checkboxradio({
        icon: false
    });
    setUpButtons();

    $(".search-button").button();
    $categoryAny = $("#category-any");
    $categoryCustom = $("#category-custom");
    $categoryScience = $("#category-science");
    $modeMeta = $("#mode-meta");
    $score = $("#score");
    $modeStandard = $("#mode-standard");
    $timer = $("#timer");
    $guess = $("#guess");

    $guess.keydown((event) => {
        if (event.keyCode == 13) {
            room.game.round.checkAnswer();
        }
    });

// Deals with making sure incompatible settings can't be chosen together
    $modeMeta.change(() => {
        if ($modeMeta[0].checked) {
            $categoryCustom.checkboxradio("disable");
        }
    });
    $modeStandard.change(() => {
        if ($modeStandard[0].checked) {
            $categoryCustom.checkboxradio("enable");
        }
    });
    $categoryCustom.change(() => {
        if ($categoryCustom[0].checked) {
            $modeMeta.checkboxradio("disable");
            $sliderRounds.slider("disable");
        }
    });
    $categoryAny.change(() => {
        if ($categoryAny[0].checked) {
            $modeMeta.checkboxradio("enable");
            $sliderRounds.slider("enable");
        }
    });
    $categoryScience.change(() => {
        if ($categoryScience[0].checked) {
            $modeMeta.checkboxradio("enable");
            $sliderRounds.slider("enable");
        }
    });

    const $setUpButton = $("#setup-game");
    $setUpButton.click(() => {
        $home.hide("fade", () => {
            $settings.show("fade");
        })
    });

// Sets up configure button and handler to switch to custom
// settings box if necessary
    const $configButton = $("#configure-game");
    $configButton.click(() => {
        if ($categoryCustom[0].checked) {
            console.log("calling custom show");
            $settings.hide("drop", {direction: "left"}, () => {
                $custom.show("drop", {direction: "right"});
            });
        } else {
        connection.sendCreateMessage();
        }
    });

// Sets up back button in custom query box
    const $backButton = $("#button-back");
    $backButton.click(() => {
        $custom.hide("drop", {direction: "right"}, () => {
            $settings.show("drop", {direction: "left"})
        });
    });

    const $backHome =  $("#button-back-home");
    $backHome.click(() => {
        $settings.hide("fade", () => {
            $home.show("fade");
        })
    });

// Sets up the new-query button in custom query box
    const $newQueryButton = $("#button-new-query");
    const $alertTooManyQueries = $("#alert-too-many-queries");
    $alertTooManyQueries.children().append("Can only add up to "
        + MAX_ROUNDS + " queries");
    $newQueryButton.click(() => {
        $queryList = $("#queries");
        numQueries = $queryList.children().length;
        if (numQueries < MAX_ROUNDS) {
            const children = $queryList.children();
            const nextNum = children.length > 0 ? Number(children.last().prop("id").substring(5)) + 1 : 0;
            $queryList.append("<li id=entry" + nextNum
                + "><input type='text' class='small-margin-bottom list-query' id=q" + nextNum
                + "><input type='button' value='x' class=delete id=d" + nextNum
                + "></li>");
            setUpButtons();
        } else {
            $alertTooManyQueries.dialog("open");
        }
    });

// Sets up slider.
    $sliderRounds = $("#slider-rounds");
    const $handle = $("#handle-rounds");
    $sliderRounds.slider({
        range: "max",
        min: 1,
        max: MAX_ROUNDS,
        value: 3,
        create: function() {
            $handle.text($(this).slider("value"));
        },
        slide: function(event, ui) {
            $handle.text(ui.value);
        }
    });

// Sets up alert boxes.
    $( ".dialog-message" ).dialog({
        modal: true,
        buttons: {
            Ok: function() {
                $( this ).dialog( "close" );
            }
        }
    }).dialog("close");

    $dialog = $( "#dialog-form" ).dialog({
        autoOpen: false,
        height: 400,
        width: 350,
        modal: true,
        buttons: {
            "Join": validateJoin,
            Cancel: function() {
                $dialog.dialog( "close" );
            }
        },
        close: function() {
            form[ 0 ].reset();
            $("#name").removeClass( "ui-state-error" );
            $("#room").removeClass( "ui-state-error" );
        }
    });

    form = $dialog.find( "form" ).on( "submit", function( event ) {
        event.preventDefault();
        validateJoin();
    });

    $( "#join-game" ).on( "click", function() {
        $dialog.dialog( "open" );
    });
});
function validateJoin() {
    let valid = true;
    const $name = $("#name").removeClass( "ui-state-error" );
    const $room = $("#room").removeClass( "ui-state-error" );
    valid = valid && checkLength( $name, "username", 1, 10 );
    valid = valid && checkLength( $room, "room number", 6, 6 );
    if (valid) {
        joinGame($room.val(), $name.val());
        $dialog.dialog( "close" );
    }
}

function checkLength( o, n, min, max ) {
    if ( o.val().length > max || o.val().length < min ) {
        o.addClass( "ui-state-error" );
        updateTips( "Length of " + n + " must be between " +
            min + " and " + max + "." );
        return false;
    } else {
        return true;
    }
}

function updateTips( t ) {
    $(".validateTips")
        .text(t)
        .addClass("ui-state-highlight");
    setTimeout(function() {
        $(".validateTips").removeClass( "ui-state-highlight", 1500 );
    }, 500 );
}

function reveal(answer, index, score, flagMissed) {
    const revealedContent = "<div class='std-text'><div style='float: left; font-weight: 700;'>"
        + answer + "</div><div style='float: right; color: blue;'>"
        + score + "</div></div>";
    if (index >= 0) {
        let $toUpdate;
        if (index < 5) {
            $toUpdate = $("#answer-table-1").find("tr:nth-child(" + (index + 1) + ")").find("td");
        } else if (index <= 9) {
            $toUpdate = $("#answer-table-2").find("tr:nth-child(" + (index - 4) + ")").find("td");
        } else {
            console.log("Error: received index=" + index);
            return;
        }
        $toUpdate.html(revealedContent);
        if (flagMissed) {
            $toUpdate.addClass("missed");
        }
    } else {
        console.log("Invalid index provided: " + index);
    }
}

// Makes sure all button inputs have been JQueryUi-ified. Also
// binds delete functionality on click event for delete buttons
function setUpButtons() {
    $("input[type='button']").button();
    $(".delete").bind("click", (event) => {
        const button = event.currentTarget;
        const idToDelete = "#entry" + button.id.substring(1);
        $(idToDelete).remove();
    });
}

function joinGame(roomId, username) {
    room = new Room(false, true, roomId, username);
    connection.sendJoinMessage();
}

function formatSeconds(timeInSeconds) {
    const minutes = Math.floor((timeInSeconds / (60)));
    const seconds = Math.floor((timeInSeconds % (60)));
    return minutes + ":" + ((seconds < 10) ? "0" : "") + seconds;
}

function setSingleplayerScore(points) {
    $score.text(points);
}

function configureMultiplayerScore(userArr) {
    userArr.forEach((elt) => {
        addMultiplayerScoreRow(elt.userId, elt.username, elt.score);
    });
}

function addMultiplayerScoreRow(userId, username, score) {
    const rowId = `usr${userId}`;
    $multiScore.append(`<li id="${rowId}"></li>`);
    updateMultiplayerScore(userId, username, score);
}

function updateMultiplayerScore(userId, username, score) {
    $multiScore.find($(`#usr${userId}`)).text(`${username} - ${score}`);
}

function removeMultiplayerScoreRow(userId) {
    $multiScore.find($(`#usr${userId}`)).remove();
}

function toLobbyFrom(eltToHide) {
    eltToHide.hide("fade", () => {
        if (room.multiplayer) {
            $lobby.show("fade");
            if (room.hosting) {
                $startFromLobby.show("fade");
            } else {
                $waitingMessage.show("fade");
            }
            $roomCodeField.text(room.id);
        }
        $scoreArea.show("fade");
    });
}

function toPlayFromLobby() {
    console.log("calling hide on lobby");
    $lobby.hide("fade", () => {
        $playSingle.show("fade");
    });
}

class Countdown {
    constructor(initial) {
        this.initial = initial;
    }

    tick() {
        this.initial--;
        if (room.multiplayer && room.hosting) {
            connection.sendUpdateTime(this.initial);
        }
        return this.initial;
    }
}

class Connection {
    constructor(socket) {
        this.connection = new WebSocket(socket);
        this.connection.onopen = function () {
            console.log('Opened connection.');
        };
        this.connection.onerror = function (error) {
            console.log('Error Logged: ' + error); //log errors
        };
        this.connection.onmessage = function (messageEvent) {
            const message = JSON.parse(messageEvent.data);
            const payload = JSON.parse(message.payload);
            console.log("RECEIVED MESSAGE");
            console.log(message);
            switch (message.type) {
                case CONNECT:
                    console.log("websocket connected");
                    break;
                case CREATE_ROOM:
                    connection.receiveCreateMessage(payload);
                    break;
                case USER_JOIN:
                    connection.receiveJoinMessage(payload);
                    break;
                case USER_LEFT:
                    connection.receiveLeftMessage(payload);
                    break;
                case NEW_GAME:
                    connection.receiveNewGameMessage();
                    break;
                case NEW_ROUND:
                    connection.receiveNewRoundMessage(payload);
                    break;
                case PLAYER_GUESS:
                    connection.receiveGuessMessage(payload);
                    break;
                case ROUND_END:
                    connection.receiveEndRound(payload);
                    break;
                default:
                    console.log("Unknown message type received: " + message.type);
            }
        };
    }

    sendCreateMessage() {
        const message = {
            type: CREATE_ROOM,
            payload: {
                maxUsers: $("#player-type-multi")[0].checked ? 10 : 1
            }
        };
        this.connection.send(JSON.stringify(message));
    }

    receiveCreateMessage(payload) {
        if (payload.roomId === "") {
            alert("The server is operating at maximum capacity. Try again later!");
            window.location.replace("/");
        } else {
            room = new Room(true, $("#player-type-multi")[0].checked, payload.roomId, "Host");
            this.sendJoinMessage();
            if(!room.multiplayer) {
                connection.sendNewGameMessage();
            }
        }
    }

    sendJoinMessage() {
        const message = {
            type: USER_JOIN,
            payload: {
                roomId: room.id,
                username: room.username //room.hosting ? "Host" : room.username
            }
        };
        this.connection.send(JSON.stringify(message));
    }

    receiveJoinMessage(payload) {
        if (payload.userId === "") {
            $("#room").addClass("ui-state-error");
            updateTips("No room exists with that ID.");
            return;
        }
        // if user is the one joining game
        if (room.userId === "") {
            room.userId = payload.userId;
            if (room.hosting) {
                toLobbyFrom($settings);
            } else {
                toLobbyFrom($home);
                if (payload.query !== "") {
                    console.log("query exists");
                    console.log(payload);
                    room.game = new Game();
                    room.game.nextRound(payload.query, payload.numResponses, payload.timeSeconds);
                    toPlayFromLobby();
                    JSON.parse(payload.guessed).forEach((elt) => {
                        reveal(elt.suggestion, elt.suggestionIndex, elt.score, false);
                    });
                }
            }
            if (room.multiplayer) {
                if (!room.hosting) {
                    configureMultiplayerScore(JSON.parse(payload.users));
                }
                $multiScore.show("fade", 0);
            } else {
                $singleScore.show("fade", 0);
            }
        }
        if (room.multiplayer) {
            addMultiplayerScoreRow(payload.userId, payload.username, payload.score);
        }
    }

    receiveLeftMessage(payload) {
        if (payload.roomClose) {
            alert("Host left, closing room!");
            window.location.replace("/");
        } else {
            removeMultiplayerScoreRow(payload.userId);
        }
    }

    sendNewGameMessage() {
        const message = {
            type: NEW_GAME,
            payload: {
                roomId: room.id,
                settings: {
                    type: room.multiplayer ? "multiplayer" : "singleplayer",
                    // maxPlayers: room.multiplayer ? 5 : 1,
                    mode: $modeMeta[0].checked ? "meta" : "standard",
                    mode: "standard",
                    rounds: $sliderRounds.slider("value")
                }
            }
        };
        this.connection.send(JSON.stringify(message));
    }

    receiveNewGameMessage() {
        room.startGame();
    }

    sendNewRoundMessage() {
        const message = {
            type: NEW_ROUND,
            payload: {
                roomId: room.id
            }
        };
        this.connection.send(JSON.stringify(message));
    }

    receiveNewRoundMessage(payload) {
        if (payload.query === "") {
            window.location.replace("/");
        } else {
            $guess.val("");
            room.game.nextRound(payload.query, payload.numResponses, 30);
        }
    }

    sendGuessMessage(query) {
        const message = {
            type: PLAYER_GUESS,
            payload: {
                roomId: room.id,
                guess: query
            }
        };
        this.connection.send(JSON.stringify(message));
    }

    receiveGuessMessage(payload) {
        if (payload.suggestion === "") {
            // TODO: action on wrong guess
        } else {
            reveal(payload.suggestion, payload.suggestionIndex, payload.score, false);
            if(room.multiplayer) {
                updateMultiplayerScore(payload.userId, payload.username, payload.playerScore);
            } else {
                setSingleplayerScore(payload.playerScore);
            }
        }
    }

    sendEndRound() {
        const message = {
            type: ROUND_END,
            payload: {
                roomId: room.id
            }
        };
        this.connection.send(JSON.stringify(message));
    }

    receiveEndRound(payload) {
        JSON.parse(payload.suggestions).forEach((elt) => {
            reveal(elt.suggestion, elt.suggestionIndex, elt.score, true);
        });
        room.game.round.end();
    }

    sendUpdateTime(timeSeconds) {
        const message = {
            type: UPDATE_TIME,
            payload: {
                roomId: room.id,
                timeSeconds: timeSeconds
            }
        };
        this.connection.send(JSON.stringify(message));
    }
}

class Box {
    constructor(index, answer, score) {
        this.index = index;
        this.answer = answer;
        this.score = score;
        this.guessed = false;
    }
}

class Room {
    constructor(hosting, multiplayer, id, username) {
        this.hosting = hosting;
        this.multiplayer = multiplayer;
        this.id = id;
        this.username = username;
        this.userId = "";
    }

    startGame() {
        toPlayFromLobby();
        this.game = new Game();
        connection.sendNewRoundMessage();
    }
}

// hosting is a boolean flag, true if the user creating the game is the host;
// false if they're just joining an existing game
class Game {
    constructor() {
        this.round = undefined;
    }

    nextRound(query, size, duration) {
        $nextRound.hide("fade", () => {
            $submit.show("fade");
        });
        this.round = new Round(query, size, duration);
        this.round.start();
    }
}

class Round {
    // Duration is in seconds
    constructor(query, size, duration) {
        this.query = query;
        this.over = false;
        this.size = size;
        this.duration = duration;
        this.countdown = new Countdown(duration);
        this.timer = undefined;

        $("#query").text(this.query);
        $("#answer-table-1").find("td").each((index, elt) => {
            const boxNum = index + 1;
            if (boxNum > size) {
                $(elt).text("").addClass("unavailable");
            } else {
                $(elt).html((index, html) => {
                    return boxNum;
                }).removeClass("missed unavailable");
            }
        });
        $("#answer-table-2").find("td").each((index, elt) => {
            const boxNum = index + 6;
            if (boxNum > size) {
                $(elt).text("").addClass("unavailable");
            } else {
                $(elt).html((index, html) => {
                    return boxNum;
                }).removeClass("missed unavailable");
            }
        });
    }

    start() {
        $timer.text(formatSeconds(this.duration));
        this.timer = setInterval(() => {
            const curTime = this.countdown.tick();
            if (curTime < 0) {
                this.end();
                if (room.hosting) {
                    connection.sendEndRound();
                }
            } else {
                $timer.text(formatSeconds(curTime));
                if (curTime === 0) {
                    this.end();
                    if (room.hosting) {
                        connection.sendEndRound();
                    }
                }
            }
        }, 1000);
    }

    end() {
        this.over = true;
        clearInterval(this.timer);
        $submit.hide("fade", () => {
            if (room.hosting) {
                $nextRound.show("fade")
            }
        });
    }

    isOver() {
        return this.over;
    }

    getQuery() {
        return this.query;
    }

    checkAnswer() {
        if (this.isOver()) {
            return;
        }
        const val = $guess.val();
        $guess.val("");
        connection.sendGuessMessage(val);
    }
}
