# ZorkTouch

Created in Android Studio 3.0.1

This tool simulates a complete Z-Machine (Zork and similar text adventures).

The final goal is to detect any interactable verbs and nouns within the description texts and make them selectable by touch and this way greatly enhance the mobile experience of text adventures.

This is however mostly complicated by an free-form Assembly-like code structure and no standards for how the object tree is to be formed.

Possible solutions are to:
1. Manually enter all possible locations and objects for each game
2. intelligently simulate the to-be-executed code, at any point in the game, to see which words are interactable and which result in a "I don't know what you mean." message. (Perhaps this default message can be retrieved as well by simulating entering random gibberish.)
