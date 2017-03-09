# cs0320 Term Project

**Team Members:** Alex Sekula (asekula), Jake Tango (jtango), Loudon Cohen (lcohen2), Sam Waxman (swaxman1)

**Project Idea:** 
	A game similar to autocorrect, but the players guess what the suggestions will be, and gain points based on how high up their guess was. It would be multiplayer, and players would compete against each other in timed matches. Google mode would consist of some text that represents the incomplete search, and players would have to guess what google would autocomplete for the search. AskReddit mode would consist of a question posted on reddit.com/r/AskReddit and users would have to guess the answers (it would only take answers with short responses so the users can realistically guess them). A third mode would be "meta mode", where the suggestions are the most common guesses based on what users in the game suggested. Similar to "meta mode", a bonus feature would be that this game learns what people would suggest given an incomplete search, and the data collected could be used to build a better autocomplete system.

**Mentor TA:** _Put your mentor TA's name and email here once you're assigned one!_

## Project Requirements

**User Base/Stakeholders:**
- The casual in-browser videogamer. Possibly linked from social media.
- TA's and instructors of the cs032 course.

**Interview/Survey Questions and Results (https://goo.gl/forms/dvTZsWj1tCV85ALt1):**
* Describe the Type of User that might play Google Feud.
	- A casual, in-browser gamer.
	- A person linked to it from social media
* How do you think Google Feud scores responses? How would you score responses?
	- I think it takes the top 10 Google Searches and simply ranks them in increments of 1000 going up to 10000 if you get them exactly. I would score by making the more uncommon searches worth more as they are harder to guess.
	- It looks like it just does the google search for the prompt and then ranks the answers in order. However, it does look like some prompts are predefined
	- Include ranking for how high up the suggestion is. Also give partial credit to suggestions that are kinda right.
* Imagine Google Feud with more than one person. How do you think this would work?
	- I think it would be like pretendyourexyzzy--there would be lobbies and chat and the person who scored the most would win. It could also be time based.
	- This could be multiplayer if you added a rankings board or head to head matches with the same queries. It would be hard to prevent cheating though
	- Either real time competition (all players in a timed match), or turn based game (so the users play a round every once in a while, like Words with Friends).
* Think of some other web services like Google Search (AskReddit, UrbanDictionary, etc.). Describe the service and why it would or would not work as a Feud game.
	- I think that Feud games need to have short answers in order to work well. Otherwise it is too hard to guess correctly.
	- Feud could also work with the youtube search bar since it's like google
	- Yahoo Answers	
* Any other comments about your experience with online, competitive games?
	- N/A

**User Defined Problems:**
- Scoring guesses in any feud game appropriately. It should be forgiving enough to give points to "close" answers but reward accuracy.
- Having fair, unrepeating prompts. 
- Smooth multiplayer lobbying/rooms with private options.
- Preventing cheating (against other users simply performing the search).

**Optimal Solution Outline:**
1. Google Feud.
2. Smart scoring.
3. Multiplayer functionality.
4. Meta-mode.
	
**Features:**
* Required:
	1. A fully functional implementation of Google Feud. Loads suggestions from Google based on what it would suggest given a query, and uses the suggestions as answers that the users try to guess. Allows users to enter their own query, or have a random one provided to them. Google search queries are mostly up-to-date.		
	2. Scores suggestions based on how high up they are in the suggestion box. Grants users points if they are "close enough", i.e. the words weren't exactly correct but the meaning was roughly the same.
	3. Allows multiplayer, where users can compete in timed matches.
	4. "Meta-mode", where the new suggestions are guesses that users made during the game for the input query both during Google Feud and during Meta mode. The system would filter junk guesses, and aggregate suggestions that are similar.
* Optional:
	1. Other mediums through which to play the game: 
		a) AskReddit, where the "queries" are questions, and the "suggestions" are users' answers (only short ones so that the game's players can guess them).
		b) Yahoo Answers, in the same format as above.
	2. A better autocorrect/autocomplete system that uses user-generated data from the game. This would be similar to meta-mode, but would be more adaptable to different queries, and would still use user data on queries it has never seen before.
	
**Limitations:**
- As Google depreciated their Web Search API, we cannot use their more straightforwards tools. We will have to do manual parsing of search results for at least this service.
	
**Acceptance Criteria:**


## Project Specs and Mockup
_A link to your specifications document and your mockup will go here!_

## Project Design Presentation
_A link to your design presentation/document will go here!_

## How to Build and Run
_A necessary part of any README!_
