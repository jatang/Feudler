# cs0320 Term Project Specification

**Meeting Requirements:**

**Page Layouts:**
<ol>
<li>Home
<li>Custom setup
<li>Singleplayer game screen
<li>Multiplayer game screen
</ol>

**Control Flow:**
<p>
Note that there are defaults for all settings, so empty form elements
cannot cause problems.
</p>

<p>
There are only two actions that can change the page:
<ul>
<li>Submit button in (1) leads to (2) if "Custom" mode is selected.
If "Singleplayer" is selected, leads to (3). If "Multiplayer" is selected,
leads to (4). 
<li>Submit button in (2) leads to (3) if "Singleplayer" is selected, and (4)
if "Multiplayer" is selected.
</ul>
</p>

**Timing:**
<p>
All page results should feel instant, with one exception: finding
whether custom queries are valid (2) may take a few seconds.
</p>

**Handling Bad Input:**
<p>
To handle very long input strings from the user, we can can the maximum
length of text fields.
</p>
<p>
We can also limit empty inputs and special characters.
If the user provides an empty string, the form should not submit.
Additionally, text inputs will only support alphanumeric characters.
</p>