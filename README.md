# image-segmentation-runeliteplus
initial build and readme by Owain#4216
### A Runelite Plus plugin that creates screenshot/polygon pairs for use in image segmentation

1. Build the source in Maven
   - if using IntelliJ, there's gonna be a maven panel on the right and (nder the lifecycle tab) click "install"
   - this will generate a .jar file (under the "target" folder) that you can use for loading the plugin into [Runelite Plus](https://runelitepl.us/RuneLitePlusSetup.exe)
2. Confirm external plugins option is enabled
   - click the wrench icon on the right side
   - click the cog next to the plugin "runelite"
   - make sure "Enable loading of external plugins" is checked
   - close the client
3. Find the plugins folder
   - %userprofile%\.runelite\plugins on windows
   - $HOME/.runelite/plugins on Linux/MacOS
   - if the folder doesn't exist just create one
4. Copy the compiled .jar file into the plugins folder
5. Activate plugin on client
   - run the runlite plus client
   - in the list of plugins on the right panel there should be a new plugin called "object data"
6. Use the client with the following options/features:
   - Save capture (Click the box next to it to set a hotkey, when you hit the hetkey in game it'll spit out plygon data and take a screenshot of the game canvas and save it to  %userprofile%.runelite\screenshots<playername> on Windows or to $HOME/.runelite/screenshots/<playernameon Linux/MacOS)
   - Highlight style (Hull or clickbox -> default is hull)
   - Show overlay (Show an overlay in game with the Hulls of the selected items that are below)
   - Game objects (Show game objects)
   - Decorative objects (Show decorative objects -> mostly objects that or on a wall like a painting)
   - NPCs (All the non player characters)
   - Other Players (Other players)
