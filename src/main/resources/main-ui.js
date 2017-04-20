const MAX_ROUNDS = 5;
let roundsRemaining;
let roundOver;
let currentQuery;
let boxesFilled = [false, false, false, false, false, false, false, false, false, false];
// Views
let $home;
let $custom;
let $playSingle;
// Radio buttons
let $categoryAny;
let $categoryCustom;
let $categoryScience;
let $modeMeta;
let $modeStandard;
// Slider
let $sliderRounds;
// Play buttons
let $nextRound;
let $submit;

$(document).ready(() => {
	$home = $("#home");
	$custom = $("#custom");
	$playSingle = $("#play-single");
	$custom.addClass("hide", 0);
	$playSingle.addClass("hide", 0);

	$nextRound = $("#next-round");
	$nextRound.addClass("hide", 0);
	$nextRound.click(() => newRoundFromQuery("What does the fox"));

	$submit = $("#submit");
	$submit.click(() => checkAnswerSingleplayer());


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

	// Deals with making sure incompatible settings can't be chosen together
	$modeMeta.change(() => {
		if ($modeMeta[0].checked) {
			$categoryCustom.checkboxradio("disable");
			$sliderRounds.slider("disable");
		}
	})
	$modeStandard.change(() => {
		if ($modeStandard[0].checked) {
			$categoryCustom.checkboxradio("enable");
			$sliderRounds.slider("enable");
		}
	});
	$categoryCustom.change(() => {
		if ($categoryCustom[0].checked) {
			$modeMeta.checkboxradio("disable");
		}
	});
	$categoryAny.change(() => {
		if ($categoryAny[0].checked) {
			$modeMeta.checkboxradio("enable");
		}
	});
	$categoryScience.change(() => {
		if ($categoryScience[0].checked) {
			$modeMeta.checkboxradio("enable");
		}
	});

	// Sets up configure button and handler to switch to custom
	// settings box if necessary
	const $configButton = $("#configure-game");
	$configButton.click(() => {
		if ($categoryCustom[0].checked) {
			$home.hide("drop", {direction: "left"}, () => {
				$custom.show("drop", {direction: "right"});
			});
		} else {
			startGame();
			$home.hide("fade", () => {
				$playSingle.show("fade");
			});
		}
	});

	// Sets up back button in custom query box
	const $backButton = $("#button-back");
	$backButton.click(() => {
		$custom.hide("drop", {direction: "right"}, () => {
			$home.show("drop", {direction: "left"})
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

// In singleplayer, checks the user's answer and updates the board or alerts as
// necessary.
function checkAnswerSingleplayer() {
	// This is a stub implementation.
	if (roundOver) {
		return;
	}
	const $guess = $("#guess");
	const val = $guess.val();
	$guess.val("");
	switch (val) {
		case "subway":
			reveal(0, "subway", 100000);
			break;
		case "weigh":
			reveal(1, "weigh", 50000);
			break;
		case "play":
			reveal(2, "play", 20000);
			break;
		case "spray":
			reveal(3, "spray", 10000)
			break;
		case "pay":
			reveal(4, "pay", 5000);
			break;
		case "kyrie":
			reveal(5, "kyrie", 2000);
			break;
		case "pray":
			reveal(6, "pray", 1000);
			break;
		case "pompeii":
			reveal(7, "pompeii", 500);
			break;
		case "neigh":
			reveal(8, "neigh", 100);
			break;
		case "say":
			reveal(9, "say", 0);
			break;
		default:
			return;
	}
}

function startGame() {
	roundsRemaining = $sliderRounds.slider("value");
	newRoundFromQuery("What does the fox");
}

// Updates the corresponding box with the given word and score.
function reveal(boxNumber, word, score) {
	const revealedContent = "<div class='std-text'><div style='float: left; font-weight: 700;'>" + word + "</div><div style='float: right; color: blue;'>" + score + "</div></div>";
	if (boxNumber >= 0) {
		if (boxNumber < 5) {
			$("#answer-table-1 tr:nth-child(" + (boxNumber + 1) + ") td").html(revealedContent);
		} else if (boxNumber <= 9) {
			$("#answer-table-2 tr:nth-child(" + (boxNumber - 4) + ") td").html(revealedContent);
		}
	}
	boxesFilled[boxNumber] = true;
	if (boxesFilled.filter((elt) => {return elt}).length >= 2) {
		endRound();
	}
}

function endRound() {
	roundOver = true;
	$submit.hide("fade", () => {
		$nextRound.show("fade");
	});
}

function newRoundFromQuery(query) {
	if (roundsRemaining > 0) {
		$nextRound.hide("fade", () => {
			$submit.show("fade");
		});
		currentQuery = query;
		boxesFilled = boxesFilled.fill(false, 0, 10);
		$("#query").text(currentQuery);
		$("#answer-table-1 tr td").html((index, html) => {
			return index + 1;
		})
		$("#answer-table-2 tr td").html((index, html) => {
			return index + 6;
		})
		roundOver = false;
		roundsRemaining--;
	} else {
		window.location.reload();
		//$.get(HOMEPAGE);
	}
}

function newRound() {
	// Get new query from server, then call newRound(newQuery);
}

// TODO: Set up checkAnswerSingleplayer() function, and call it when submit button is clicked;
// should send the user's input via POST to the server; if wrong, the server should
// indicate so; if right, the server should send (1) the rank of the guessed suggestion
// (1 - 10), and (2) the # of points awarded to the guesser.

// TODO: Set up checkAnswerMultiplayer() function, which is similar to the above,
// except it also passes the player (id, probably) who made the guess. If they were
// right, returns the same data, and the server should also update its internal
// scoreboard and board (words guessed yet, etc.) representation, which will be
// provided in constant update() queries by all clients. 

// For the turn-based mode: If they were wrong, indicate so, and also change
// whose turn it is on the server side. If it isn't their turn, indicate so.

// For the FFA mode: If they were wrong, indicate so.

// TODO: (Needed for multiplayer only) Set up update() function, which will
// grab score, turn, and board updates from the server.