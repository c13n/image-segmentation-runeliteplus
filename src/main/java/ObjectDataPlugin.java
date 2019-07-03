import com.google.inject.Provides;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import static net.runelite.client.RuneLite.SCREENSHOT_DIR;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "Object Data",
	type = PluginType.EXTERNAL
)
public class ObjectDataPlugin extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(ObjectDataPlugin.class);

	final List<TileObject> gameObjects = new ArrayList<>();
	final List<TileObject> decorativeObjects = new ArrayList<>();
	final List<NPC> npcs = new ArrayList<>();
	final List<Player> players = new ArrayList<>();

	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	private static String format(Date date)
	{
		synchronized (TIME_FORMAT)
		{
			return TIME_FORMAT.format(date);
		}
	}

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ObjectDataOverlay overlay;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private DrawManager drawManager;

	@Inject
	private ObjectDataConfig config;

	@Inject
	private KeyManager keyManager;

	@Provides
	ObjectDataConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ObjectDataConfig.class);
	}

	@Override
	protected void startUp()
	{
		logger.info("Example plugin started!");
		overlayManager.add(overlay);
		keyManager.registerKeyListener(hotkeyListener);
	}

	@Override
	protected void shutDown()
	{
		logger.info("Example plugin stopped!");
		gameObjects.clear();
		decorativeObjects.clear();
		overlayManager.remove(overlay);
		keyManager.unregisterKeyListener(hotkeyListener);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			gameObjects.clear();
			decorativeObjects.clear();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject eventObject = event.getGameObject();

		gameObjects.add(eventObject);

	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		final DecorativeObject eventObject = event.getDecorativeObject();

		decorativeObjects.add(eventObject);

	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject eventObject = event.getGameObject();

		gameObjects.remove(event.getGameObject());

	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
	{
		final DecorativeObject eventObject = event.getDecorativeObject();

		decorativeObjects.remove(event.getDecorativeObject());
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		final NPC npc = npcSpawned.getNpc();

		npcs.add(npc);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		final NPC npc = npcDespawned.getNpc();

		npcs.remove(npc);
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned event)
	{
		final Player player = event.getPlayer();

		players.add(player);
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned event)
	{
		final Player player = event.getPlayer();

		players.remove(player);
	}
	public String ListToJson(List<String> list) {
		String result = "";
		for(int i=0; i<list.size(); i++) {
			if (i > 0)
				result += ",";
			result += list.get(i);
		}
		return "["+result+"]";
	}

	public String PolygonToJson(Polygon hull) {
		List<String> points = new ArrayList<String>();
		for(int i=0; i<hull.npoints; i++) {
			int x = hull.xpoints[i], y = hull.ypoints[i];
			points.add(Integer.toString(x)+","+Integer.toString(y));
		}
		return ListToJson(points);
	}

	public String DictionaryToJson(Dictionary<String, String> dataObj) {
			String result = "";
			int i = 0;
			for (Enumeration ii = dataObj.keys(); ii.hasMoreElements();)
      {
				 String key = ii.nextElement().toString();
					if (i > 0)
						result += ",";
		      result +=  "\""+key + "\": " + dataObj.get(key);
					i++;
			}
			return "{"+result+"}";
	}



	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.hotkeyToggle())
	{
		@Override
		public void hotkeyPressed()
		{

			Dictionary<String, String> jsonPolygonData = new Hashtable<String, String>();
			List<String> polygonList = null;
			takeScreenshot(format(new Date()));


			polygonList = new ArrayList<String>();
			if (config.showNpcs())
			{
				for (NPC npc : npcs)
				{
					if (config.objectRenderStyle() == RenderStyle.HULL)
					{
						Polygon npcHull = npc.getConvexHull();
						polygonList.add(PolygonToJson(npcHull));
						System.out.println(npcHull);
					}
				}
			}
			jsonPolygonData.put("npcs", ListToJson(polygonList));

			polygonList = new ArrayList<String>();
			if (config.showPeople())
			{
				for (Player player : players)
				{
					if (config.objectRenderStyle() == RenderStyle.HULL)
					{
						Polygon playerHull = player.getConvexHull();
						polygonList.add(PolygonToJson(playerHull));
						System.out.println(playerHull);
					}
				}
			}
			jsonPolygonData.put("players", ListToJson(polygonList));

			final List<TileObject> objects = new ArrayList<>();

			if (config.showGameObjects())
			{
				objects.addAll(gameObjects);
			}

			if (config.showDecorativeObjects())
			{
				objects.addAll(decorativeObjects);
			}

			List<String> objectDataList = new ArrayList<String>();
			for (TileObject object : objects)
			{
				if (object.getPlane() != client.getPlane())
				{
					continue;
				}

				switch (config.objectRenderStyle())
				{
					case HULL:
						final Polygon polygon;
						Polygon polygon2 = null;

						if (object instanceof GameObject)
						{
							polygon = ((GameObject) object).getConvexHull();
						}
						else if (object instanceof DecorativeObject)
						{
							polygon = ((DecorativeObject) object).getConvexHull();
							polygon2 = ((DecorativeObject) object).getConvexHull2();
						}
						else
						{
							polygon = object.getCanvasTilePoly();
						}

						List<String> polygons = new ArrayList<String>();

						if (polygon != null)
						{
							System.out.println(polygon);
							polygons.add(PolygonToJson(polygon));
						}

						if (polygon2 != null)
						{
							System.out.println(polygon2);
							polygons.add(PolygonToJson(polygon2));
						}

						objectDataList.add(ListToJson(polygons));

						break;
					case CLICKBOX:
						Area clickbox = object.getClickbox();
						if (clickbox != null)
						{
							System.out.println(clickbox);
						}
						break;
				}
			}

			jsonPolygonData.put("objects", ListToJson(objectDataList));

			SaveStringToFile(DictionaryToJson(jsonPolygonData), format(new Date()));

		}
	};

	/**
	 * Saves a screenshot of the client window to the screenshot folder as a PNG,
	 * and optionally uploads it to an image-hosting service.
	 *
	 * @param fileName    Filename to use, without file extension.
	 */
	private void takeScreenshot(String fileName)
	{
		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
			// Prevent the screenshot from being captured
			return;
		}

		Consumer<Image> imageCallback = (img) ->
		{
			// This callback is on the game thread, move to executor thread
			executor.submit(() -> takeScreenshot(fileName, img));
		};

		drawManager.requestNextFrameListener(imageCallback);
	}

	private void takeScreenshot(String fileName, Image image)
	{
		BufferedImage screenshot = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics graphics = screenshot.getGraphics();

		int gameOffsetX = 0;
		int gameOffsetY = 0;

		// Draw the game onto the screenshot
		graphics.drawImage(image, gameOffsetX, gameOffsetY, null);

		File playerFolder;
		if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
		{
			String playerDir = client.getLocalPlayer().getName();
			playerFolder = new File(SCREENSHOT_DIR, playerDir);
		}
		else
		{
			playerFolder = SCREENSHOT_DIR;
		}

		playerFolder.mkdirs();

		try
		{
			File screenshotFile = new File(playerFolder, fileName + ".png");

			ImageIO.write(screenshot, "PNG", screenshotFile);
		}
		catch (IOException ex)
		{
		}
	}

	public void SaveStringToFile(String json, String fileName) {

		File playerFolder;
		if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
		{
			String playerDir = client.getLocalPlayer().getName();
			playerFolder = new File(SCREENSHOT_DIR, playerDir);
		}
		else
		{
			playerFolder = SCREENSHOT_DIR;
		}

		playerFolder.mkdirs();


			File stringFile = new File(playerFolder, fileName + ".json");
			try {
				Files.write(stringFile.toPath(), json.getBytes());
			}
			catch(IOException e) {
			  e.printStackTrace();
			}

	}
}
