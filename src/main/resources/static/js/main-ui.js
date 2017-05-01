const CONNECT = 0;
const CREATE_ROOM = 1;
const CUSTOM_QUERY = 2;
const NEW_GAME = 3;
const NEW_ROUND = 4;
const USER_JOIN = 5;
const USER_LEFT = 6;
const PLAYER_GUESS = 7;
const USER_CHAT = 8;

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

let connection;

$(document).ready(() => {
    connection = new Connection("ws://localhost:4567/connection");
    $home =$("#home");
    $settings = $("#settings");
    $custom = $("#custom");
    $playSingle = $("#play-single");

    $settings.addClass("hide", 0);
    $custom.addClass("hide", 0);
    $playSingle.addClass("hide", 0);

    $nextRound = $("#next-round");
    $nextRound.addClass("hide", 0);
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
    })

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
            $settings.hide("drop", {direction: "left"}, () => {
                $custom.show("drop", {direction: "right"});
            });
        } else {
            startGame();
            $settings.hide("fade", () => {
                $playSingle.show("fade");
            });
        }
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
});

function reveal(answer, index, score, flagMissed) {
    const revealedContent = "<div class='std-text'><div style='float: left; font-weight: 700;'>"
        + answer + "</div><div style='float: right; color: blue;'>"
        + score + "</div></div>";
    if (index >= 0) {
        let $toUpdate;
        if (index < 5) {
            $toUpdate = $("#answer-table-1 tr:nth-child(" + (index + 1) + ") td");
        } else if (index <= 9) {
            $toUpdate = $("#answer-table-2 tr:nth-child(" + (index - 4) + ") td");
        } else {
            console.log("Error: received index=" + index);
            return;
        }
        $toUpdate.html(revealedContent);
        if (flagMissed) {
            $toUpdate.addClass("missed");
        } else {
            if (!game.round.answersSeen.has(answer)) {
                addPoints(score);
                game.round.answersSeen.add(answer);
            }
        }

        if (game.round.answersSeen.size >= game.round.size) {
            game.round.end();
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
    game = new Game($("#player-type-multi")[0].checked, $sliderRounds.slider("value"), true);
    game.configure();
    connection.sendNewRoundMessage();
}

function formatSeconds(timeInSeconds) {
    const minutes = Math.floor((timeInSeconds / (60)));
    const seconds = Math.floor((timeInSeconds % (60)));
    return minutes + ":" + ((seconds < 10) ? "0" : "") + seconds;
}

function addPoints(toAdd) {
    game.score += toAdd;
    $score.text(game.score);
}

class Countdown {
    constructor(initial) {
        this.initial = initial;
    }

    tick() {
        this.initial--;
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
        this.connection.onmessage = function (message) {
            console.log(message);
            console.log(JSON.parse(message));
            switch (message.type) {
                case CONNECT:
                    console.log("websocket connected");
                    break;
                case CREATE_ROOM:
                    this.receiveCreateMessage(message.payload);
                    break;
                case USER_JOIN:
                    this.receiveJoinMessage(message.payload);
                    break;
                case NEW_ROUND:
                    this.receiveNewRoundMessage(message.payload);
                    break;
                case PLAYER_GUESS:
                    //TODO: Make sure answer is correct AND hasn't been guessed yet
                    this.receiveGuessMessage(message.payload);
                    break;
                default:
                    console.log("Unknown message type received: " + message.type);
            }
        };
        // TODO: Remove when server side works
        this.answers = new Map([
            ["subway", new Box(0, "subway", 100000)],
            ["weigh", new Box(1, "weigh", 50000)],
            ["play", new Box(2, "play", 20000)],
            ["spray", new Box(3, "spray", 10000)],
            ["pay", new Box(4, "pay", 5000)],
            ["kyrie", new Box(5, "kyrie", 2000)],
            ["pray", new Box(6, "pray", 1000)],
            ["pompeii", new Box(7, "pompeii", 500)],
            ["say", new Box(8, "say", 0)],
            // ["say", new Box(9, "say", 0)]
        ]);
    }

    sendCreateMessage() {
        const message = {
            type: CREATE_ROOM,
            payload: {}
        };
        this.connection.send(JSON.stringify(message));
        // message.roomId = 3;
        // this.receiveCreateMessage(message)
    }

    receiveCreateMessage(message) {
        game.id = message.roomId;
        this.sendJoinMessage();
        this.sendNewGameMessage();
    }

    sendJoinMessage() {
        const message = {
            type: USER_JOIN,
            payload: {
                roomId: this.id,
                username: this.username
            }
        };
        this.connection.send(JSON.stringify(message));
        // message.userId = 5;
        // this.receiveJoinMessage(message);
    }

    receiveJoinMessage(message) {
        game.userId = message.userId;
    }

    sendNewGameMessage() {
        const message = {
            type: NEW_GAME,
            payload: {
                roomId: game.id,
                settings: {
                    type: game.multiplayer ? "multiplayer" : "singleplayer",
                    mode: $modeMeta[0].checked ? "meta" : "standard",
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

    receiveNewRoundMessage(message) {
        game.nextRound(message.query, message.numResponses, 30, false);
    }

    sendGuessMessage(query) {
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

    receiveGuessMessage(message) {
        reveal(message.suggestion, message.suggestionIndex, message.score, false);
    }

    sendGetRemainingMessage() {
        const message = {};
        // TODO: actually implement for Websockets
        // const rem = new Array();
        // for (const box of this.answers.values()) {
        //     if (!box.guessed) {
        //         rem.push(box);
        //     }
        // }
        // message.remaining = rem;
        // this.receiveGetRemainingMessage(message);
    }

    receiveGetRemainingMessage(message) {
        message.remaining.forEach((value, key, map) => {
            reveal(value.answer, value.index, value.score, true);
        });
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
    constructor(multiplayer, roundsRemaining, id, username) {
        this.score = 0;
        // Boolean flag
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

    nextRound(query, size, duration, multiplayer) {
        if (this.roundsRemaining > 0) {
            $nextRound.hide("fade", () => {
                $submit.show("fade");
            });
            this.round = new Round(query, size, duration, false);
            this.roundsRemaining--;
            this.round.start();
        } else {
            console.log("ERROR: no rounds remaining");
            window.location.replace("/room");
        }
    }
}

class Round {
    // Duration is in seconds
    constructor(query, size, duration, multiplayer) {
        this.query = query;
        this.answersSeen = new Set();
        this.over = false;
        this.size = size;
        this.duration = duration;
        this.multiplayer = multiplayer;
        this.countdown = new Countdown(duration);
        this.timer = undefined;

        $("#query").text(this.query);
        $("#answer-table-1 tr td").each((index, elt) => {
            const boxNum = index + 1;
            if (boxNum > size) {
                $(elt).addClass("unavailable");
            } else {
                $(elt).html((index, html) => {
                    return boxNum;
                }).removeClass("missed unavailable");
            }
        });
        $("#answer-table-2 tr td").each((index, elt) => {
            const boxNum = index + 6;
            if (boxNum > size) {
                $(elt).addClass("unavailable");
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
            } else {
                $timer.text(formatSeconds(curTime));
                if (curTime === 0) {
                    this.end();
                }
            }
        }, 1000);
    }

    end() {
        this.over = true;
        clearInterval(this.timer);
        connection.sendGetRemainingMessage();
        $submit.hide("fade", () => {
            $nextRound.show("fade");
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
