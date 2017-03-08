# cs0320 Term Project

**Team Members:** Alex Sekula (asekula), Jake Tango (jtango), Loudon Cohen (lcohen2), Sam Waxman (swaxman1)

**Project Idea:** A game similar to autocorrect, but the players guess what the suggestions will be, and gain points based on how high up their guess was. It would be multiplayer, and players would compete against each other in timed matches. Google mode would consist of some text that represents the incomplete search, and players would have to guess what google would autocomplete for the search. AskReddit mode would consist of a question posted on reddit.com/r/AskReddit and users would have to guess the answers (it would only take answers with short responses so the users can realistically guess them). A third mode would be "meta mode", where the suggestions are the most common guesses based on what users in the game suggested. Similar to "meta mode", a bonus feature would be that this game learns what people would suggest given an incomplete search, and the data collected could be used to build a better autocomplete system.

**Mentor TA:** _Put your mentor TA's name and email here once you're assigned one!_

## Project Requirements

**User Base/Stakeholders:**
	-The casual in-browser videogamer. Possibly linked from social media.
	-TA's and instructors of the cs032 course.

**User Defined Problems:**
	-Scoring guesses in any feud game appropriately. It should be forgiving enough to give points to "close" answers but reward accuracy.
	-Having fair, unrepeating prompts. 
	-Smooth multiplayer lobbying/rooms with private options.
	-Preventing cheating (against other users simply performing the search).

**Interview/Survey Questions and Results (https://goo.gl/forms/dvTZsWj1tCV85ALt1):**

	Describe the Type of User that might play Google Feud.

		-A casual, in-browser gamer.

		-A person linked to it from social media

	How do you think Google Feud scores responses? How would you score responses?

		-I think it takes the top 10 Google Searches and simply ranks them in increments of 1000 going up to 10000 if you get them exactly. I would score by making the more uncommon searches worth more as they are harder to guess.

		-It looks like it just does the google search for the prompt and then ranks the answers in order. However, it does look like some prompts are predefined

		-Include ranking for how high up the suggestion is. Also give partial credit to suggestions that are kinda right.

	Imagine Google Feud with more than one person. How do you think this would work?

		-I think it would be like pretendyourexyzzy--there would be lobbies and chat and the person who scored the most would win. It could also be time based.

		-This could be multiplayer if you added a rankings board or head to head matches with the same queries. It would be hard to prevent cheating though
		
		-Either real time competition (all players in a timed match), or turn based game (so the users play a round every once in a while, like Words with Friends).

	Think of some other web services like Google Search (AskReddit, UrbanDictionary, etc.). Describe the service and why it would or would not work as a Feud game.

		-I think it would be like pretendyourexyzzy--there would be lobbies and chat and the person who scored the most would win. It could also be time based.

		-This could be multiplayer if you added a rankings board or head to head matches with the same queries. It would be hard to prevent cheating though

		-Either real time competition (all players in a timed match), or turn based game (so the users play a round every once in a while, like Words with Friends).

	Any other comments about your experience with online, competitive games?

		-N/A

**Summary:**

_Fill in your project requirements here!_

## Project Specs and Mockup
_A link to your specifications document and your mockup will go here!_

## Project Design Presentation
_A link to your design presentation/document will go here!_

## How to Build and Run
_A necessary part of any README!_
