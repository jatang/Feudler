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
let $score
let $timer;

let $guess;

const connection = new WebSocket("ws://localhost:4567/room", 'json');
		connection.onopen = function () {
			console.log('Opened connection.');
		}
		connection.onerror = function (error) {
		  console.log('Error Logged: ' + error); //log errors
		};
		connection.onmessage = function (message) {
			switch (message.type) {
				case CREATE_ROOM:
					game.id = message.roomId;
					connection.send(createJoinMessage());
					connection.send(createNewGameMessage());
					break;
				case USER_JOIN:
					game.userId = message.userId;
					break;
				case NEW_ROUND:
					game.round.setup(message.query);
				case PLAYER_GUESS:	
				//TODO: Make sure answer is correct AND hasn't been guessed yet
					reveal(message.suggestion, message.suggestionIndex, message.score, false);
				default:
					console.log("Unknown message type received: " + message.type);
			}
		}

function (answer, index, score, flagMissed) {
	const revealedContent = "<div class='std-text'><div style='float: left; font-weight: 700;'>"
		+ answer + "</div><div style='float: right; color: blue;'>"
		+ score + "</div></div>";
	if (index >= 0) {
		let $toUpdate;
		if (index < 5) {
			$toUpdate = $("#answer-table-1 tr:nth-child(" + (index + 1) + ") td");
		} else if (this.rank <= 9) {
			$toUpdate = $("#answer-table-2 tr:nth-child(" + (index - 4) + ") td");
		}
		$toUpdate.html(revealedContent);
		if (flagMissed) {
			$toUpdate.addClass("missed");
		} else {
			answers.add(suggestion);
		}
		addPoints(score);

		if (answers.size >= game.round.size) {
			game.round.end();
		}
	} else {
		console.log("Invalid index provided: " + index);
	}
}

$(document).ready(() => {
	$home =$("#home");
	$settings = $("#settings");
	$custom = $("#custom");
	$playSingle = $("#play-single");

	$settings.addClass("hide", 0);
	$custom.addClass("hide", 0);
	$playSingle.addClass("hide", 0);

	$nextRound = $("#next-round");
	$nextRound.addClass("hide", 0);
	$nextRound.click(() => game.nextRound());

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
	})
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
	game = new Game($("#player-type-multi")[0].checked, $sliderRounds.slider("value"), true;
	game.nextRound();
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
		this.configure();
	}

	this.configure() {
		const message = (game.id === "") ? createCreateMessage() : createJoinMessage();
		connection.send(message);
	}

	createCreateMessage() {
		const message = new Object();
		message.type = CREATE_ROOM;
		return JSON.stringify(message);
	}

	createJoinMessage() {
		const message = new Object();
		message.type = USER_JOIN;
		message.roomId = this.id;
		message.username = this.username;
		return JSON.stringify(message);
	}

	createNewGameMessage() {
		const message = new Object();
		message.type = NEW_GAME;
		message.roomId = this.id;
		const settings = new Object();
		settings.type = game.multiplayer ? "multiplayer" : "singleplayer";
		settings.mode = $modeMeta[0].checked ? "meta" : "standard"
		settings.rounds = game.roundsRemaining;
		message.settings = JSON.stringify(settings);
		return JSON.stringify(message);
	}

	createNewRoundMessage() {
		const message = new Object();
		message.type = NEW_ROUND;
		message.roomId = game.id;
		return JSON.stringify(message);
	}

	createGuessMessage(query) {
		const message = new Object();
		message.type = PLAYER_GUESS;
		message.roomId = game.id;
		message.guess = query;
		return JSON.stringify(message);
	}

	nextRound() {
		if (this.roundsRemaining > 0) {
			$nextRound.hide("fade", () => {
				$submit.show("fade");
			});
			// const query = "What does the fox";
			// const answers = new Map([
			// 	["subway", new Box(0, "subway", 100000)],
			// 	["weigh", new Box(1, "weigh", 50000)],
			// 	["play", new Box(2, "play", 20000)],
			// 	["spray", new Box(3, "spray", 10000)],
			// 	["pay", new Box(4, "pay", 5000)],
			// 	["kyrie", new Box(5, "kyrie", 2000)],
			// 	["pray", new Box(6, "pray", 1000)],
			// 	["pompeii", new Box(7, "pompeii", 500)],
			// 	["neigh", new Box(8, "neigh", 100)],
			// 	["say", new Box(9, "say", 0)]
			// 	]);
			this.round = new Round(30, false);
			this.roundsRemaining--;
			this.round.start();
		} else {
			window.location.reload();
			//$.get(HOMEPAGE);d
		}
	}
}

class Round {
	// Duration is in seconds
	constructor(duration, multiplayer) {
		this.query = "";
		this.answers = new Set();
		this.over = false;
		this.size = answers.size;
		this.duration = duration;
		this.multiplayer = multiplayer;
		this.countdown = new Countdown(duration);
		this.timer = undefined;
		connection.send(createNewRoundMessage());
	}

	setUp(query) {
		this.query = query;
		const size = this.size;
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
		//TODO; fix
		this.over = true;
		clearInterval(this.timer);
		this.answers.forEach((value, key, map) => {
			const box = value;
			// Reveal missed answers
			if(!box.guessed) {
				box.reveal(true, 0);
			}
		});
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
		connection.send(createGuessMessage(val));
	}
}