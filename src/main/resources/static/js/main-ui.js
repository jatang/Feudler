const CONNECT = 0;
const CREATE_ROOM = 1;
const CUSTOM_QUERY = 2;
const NEW_GAME = 3;
const NEW_ROUND = 4;
const ROUND_END = 5;
const UPDATE_TIME = 6;
const USER_JOIN = 7;
const USER_LEFT = 8;
const PLAYER_GUESS = 9;
const USER_CHAT = 10;

const MAX_ROUNDS = 5;
let game;

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
let $score;
let $timer;

let $guess;

let $dialog;

let connection;

$(document).ready(() => {
    connection = new Connection("ws://localhost:4567/connection");
    $home =$("#home");
    $settings = $("#settings").addClass("hide", 0);
    $custom = $("#custom").addClass("hide", 0);
    $playSingle = $("#play-single").addClass("hide", 0);
    $singleScore = $("#singleplayerScore").addClass("hide", 0);
    $multiScore = $("#multiplayerScore").addClass("hide", 0);

    $nextRound = $("#next-round").addClass("hide", 0);
    $nextRound.click(() => connection.sendNewRoundMessage());

    $submit = $("#submit");
    $submit.click(() => game.round.checkAnswer());



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
    $modeStandard = $("#mode-standard");
    $score = $("#score");
    $timer = $("#timer");
    $guess = $("#guess");

    $guess.keydown((event) => {
        if (event.keyCode == 13) {
            game.round.checkAnswer();
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
        // if ($categoryCustom[0].checked) {
        //     $settings.hide("drop", {direction: "left"}, () => {
        //         $custom.show("drop", {direction: "right"});
        //     });
        // } else {
        startGame();
        // }
    });

// Sets up back button in custom query box
    const $backButton = $("#button-back");
    $backButton.click(() => {
        $custom.hide("drop", {direction: "right"}, () => {
            $settings.show("drop", {direction: "left"})
        });
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
            const nextNum = $queryList.children().last().prop("id").substring(5) + 1;
            $queryList.append("<li id=entry" + nextNum
                + "><input type='text' class=small-margin-bottom id=q" + nextNum
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
    console.log("ROOM:" + $room.val())
    valid = valid && checkLength( $name, "username", 1, 10 );
    valid = valid && checkLength( $room, "room number", 6, 6 );
    if (valid) {
        joinGame($room.val(), $name.val());
        $dialog.dialog( "close" );
        console.log(`room: ${$room.val()}`);
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
        } else {
            // TODO: remove once server can handle scoring
            // if (!game.round.answersSeen.has(answer)) {
            //     addPoints(score);
            //     game.round.answersSeen.add(answer);
            // }
        }

        if (game.round.answersSeen.size >= game.round.size) {
            game.round.end();
            if (game.hosting) {
                connection.sendEndRound();
            }
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

// Is called when the play button is first clicked. Should construct a new
// Round with a query appropriate to the user's chosen settings.
function startGame() {
    game = new Game(true, $("#player-type-multi")[0].checked, $sliderRounds.slider("value"), "", "");
    game.configure();
}

function joinGame(roomId, username) {
    game = new Game(false, true, null, roomId, username);
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
    console.log("configScore called on: ");
    console.log(userArr);
    userArr.forEach((elt) => {
        console.log("adding row for " + elt.username);
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

class Countdown {
    constructor(initial) {
        this.initial = initial;
    }

    tick() {
        this.initial--;
        if (game.multiplayer && game.hosting) {
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
                case NEW_GAME:
                    // Nothing needed.
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
        // TODO: Remove when server side works
        // this.answers = new Map([
        //     ["subway", new Box(0, "subway", 100000)],
        //     ["weigh", new Box(1, "weigh", 50000)],
        //     ["play", new Box(2, "play", 20000)],
        //     ["spray", new Box(3, "spray", 10000)],
        //     ["pay", new Box(4, "pay", 5000)],
        //     ["kyrie", new Box(5, "kyrie", 2000)],
        //     ["pray", new Box(6, "pray", 1000)],
        //     ["pompeii", new Box(7, "pompeii", 500)],
        //     ["say", new Box(8, "say", 0)],
        //     // ["say", new Box(9, "say", 0)]
        // ]);
    }

    sendCreateMessage() {
        const message = {
            type: CREATE_ROOM,
            payload: {
                maxUsers: $("#player-type-multi")[0].checked ? 5 : 1
            }
        };
        this.connection.send(JSON.stringify(message));
        // message.roomId = 3;
        // this.receiveCreateMessage(message)
    }

    receiveCreateMessage(payload) {
        game.id = payload.roomId;
        this.sendJoinMessage();
        this.sendNewGameMessage();
        this.sendNewRoundMessage();
    }

    sendJoinMessage() {
        const message = {
            type: USER_JOIN,
            payload: {
                roomId: game.id,
                username: game.hosting ? "Host" : game.username
            }
        };
        this.connection.send(JSON.stringify(message));
        console.log("join msg sent");
        // message.userId = 5;
        // this.receiveJoinMessage(message);
    }

    receiveJoinMessage(payload) {
        console.log("RECEIVED JOIN w/ username=" + payload.username);
        if (payload.userId === "") {
            $("#room").addClass("ui-state-error");
            updateTips("No room exists with that ID.");
            return;
        }
        // if user is the one joining game
        if (game.userId === "") {
            game.userId = payload.userId;
            if (game.hosting) {
                $settings.hide("fade", () => {
                    $playSingle.show("fade");
                });
            } else {
                $home.hide("fade", () => {
                    $playSingle.show("fade");
                });
                game.nextRound(payload.query, payload.numResponses, payload.timeSeconds);
                JSON.parse(payload.guessed).forEach((elt) => {
                    reveal(elt.response, elt.responseIndex, elt.score, false);
                });
            }
            if (game.multiplayer) {
                if (!game.hosting) {
                    configureMultiplayerScore(JSON.parse(payload.users));
                }
                $multiScore.show("fade", 0);
            } else {
                $singleScore.show("fade", 0);
            }
        }
        if (game.multiplayer) {
            addMultiplayerScoreRow(payload.userId, payload.username, payload.score);
        }
    }

    sendNewGameMessage() {
        const message = {
            type: NEW_GAME,
            payload: {
                roomId: game.id,
                settings: {
                    type: game.multiplayer ? "multiplayer" : "singleplayer",
                    // maxPlayers: game.multiplayer ? 5 : 1,
                    // mode: $modeMeta[0].checked ? "meta" : "standard",
                    rounds: game.roundsRemaining
                }
            }
        };
        this.connection.send(JSON.stringify(message));
    }

    sendNewRoundMessage() {
        const message = {
            type: NEW_ROUND,
            payload: {
                roomId: game.id
            }
        };
        this.connection.send(JSON.stringify(message));
        // message.query = "What does the fox";
        // message.numResponses = 9;
        // // TODO: Remove when necessary
        // this.answers.forEach((val, key, map) => {
        //     val.guessed = false;
        // })
        // this.receiveNewRoundMessage(message);
    }

    receiveNewRoundMessage(payload) {
        if (payload.query === "") {
            console.log("ERROR: no rounds remaining");
            window.location.replace("/");
        } else {
            $guess.val("");
            game.nextRound(payload.query, payload.numResponses, 30);
        }
    }

    sendGuessMessage(query) {
        console.log("GUESSING: " + query);
        const message = {
            type: PLAYER_GUESS,
            payload: {
                roomId: game.id,
                guess: query
            }
        };
        this.connection.send(JSON.stringify(message));
        // if (this.answers.has(query)) {
        //     const answer = this.answers.get(query);
        //     answer.guessed = true;
        //     message.suggestion = answer.answer;
        //     message.suggestionIndex = answer.index;
        //     message.score = answer.score;
        //     this.receiveGuessMessage(message);
        // }
    }

    receiveGuessMessage(payload) {
        if (payload.suggestion === "") {
            // TODO: action on wrong guess
        } else {
            reveal(payload.suggestion, payload.suggestionIndex, payload.score, false);
            if(game.multiplayer) {
                console.log(payload);
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
                roomId: game.id
            }
        };
        this.connection.send(JSON.stringify(message));
        // const rem = new Array();
        // for (const box of this.answers.values()) {
        //     if (!box.guessed) {
        //         rem.push(box);
        //     }
        // }
        // message.remaining = rem;
        // this.receiveGetRemainingMessage(message);
    }

    receiveEndRound(payload) {
        JSON.parse(payload.suggestions).forEach((elt) => {
            // if (!game.round.answersSeen.has(elt.suggestion))
            reveal(elt.suggestion, elt.suggestionIndex, elt.score, true);
        });
    }

    sendUpdateTime(timeSeconds) {
        const message = {
            type: UPDATE_TIME,
            payload: {
                roomId: game.id,
                timeSeconds: timeSeconds
            }
        };
        this.connection.send(JSON.stringify(message));
        console.log("updating time");
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

// hosting is a boolean flag, true if the user creating the game is the host;
// false if they're just joining an existing game
class Game {
    constructor(hosting, multiplayer, roundsRemaining, id, username) {
        // Boolean flag
        this.hosting = hosting;
        this.multiplayer = multiplayer;
        this.round = undefined;
        this.roundsRemaining = roundsRemaining;
        this.id = id;
        this.userId = "";
        this.username = username;
    }

    configure() {
        const message = (this.id === "") ? connection.sendCreateMessage() : connection.sendJoinMessage();
    }

    nextRound(query, size, duration) {
        console.log("received nextRound");
        $nextRound.hide("fade", () => {
            $submit.show("fade");
        });
        this.round = new Round(query, size, duration, this.hosting);
        this.roundsRemaining--;
        this.round.start();
    }
}

class Round {
    // Duration is in seconds
    constructor(query, size, duration, hosting) {
        this.query = query;
        this.answersSeen = new Set();
        this.over = false;
        this.size = size;
        this.duration = duration;
        this.hosting = hosting;
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
                if (this.hosting) {
                    connection.sendEndRound();
                }
            } else {
                $timer.text(formatSeconds(curTime));
                if (curTime === 0) {
                    this.end();
                    if (this.hosting) {
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
            if (this.hosting) {
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
