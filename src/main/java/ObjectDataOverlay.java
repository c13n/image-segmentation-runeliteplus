import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class ObjectDataOverlay extends Overlay
{
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	private final Client client;
	private final ObjectDataPlugin plugin;
	private final ObjectDataConfig config;

	@Inject
	public ObjectDataOverlay(Client client, ObjectDataConfig config, ObjectDataPlugin plugin)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		if (!config.showOverlay())
		{
			return null;
		}

		if (config.showNpcs())
		{
			for (NPC npc : plugin.npcs)
			{
				if (config.objectRenderStyle() == RenderStyle.HULL)
				{
					Polygon npcHull = npc.getConvexHull();

					renderPoly(graphics, Color.MAGENTA, npcHull);
				}
			}
		}

		if (config.showPeople())
		{
			for (Player player : plugin.players)
			{
				if (config.objectRenderStyle() == RenderStyle.HULL)
				{
					Polygon playerHull = player.getConvexHull();

					renderPoly(graphics, Color.RED, playerHull);
				}
			}
		}

		final List<TileObject> objects = new ArrayList<>();

		if (config.showGameObjects())
		{
			objects.addAll(plugin.gameObjects);
		}

		if (config.showDecorativeObjects())
		{
			objects.addAll(plugin.decorativeObjects);
		}

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
					Color color;

					if (object instanceof GameObject)
					{
						polygon = ((GameObject) object).getConvexHull();

						color = Color.YELLOW;
					}
					else if (object instanceof DecorativeObject)
					{
						polygon = ((DecorativeObject) object).getConvexHull();
						polygon2 = ((DecorativeObject) object).getConvexHull2();

						color = Color.BLUE;
					}
					else
					{
						polygon = object.getCanvasTilePoly();

						color = Color.CYAN;
					}

					if (polygon != null)
					{
						OverlayUtil.renderPolygon(graphics, polygon, color);
					}

					if (polygon2 != null)
					{
						OverlayUtil.renderPolygon(graphics, polygon2, color);
					}
					break;
				case CLICKBOX:
					if (object instanceof GameObject)
					{
						color = Color.YELLOW;
					}
					else if (object instanceof DecorativeObject)
					{
						color = Color.BLUE;
					}
					else
					{
						color = Color.CYAN;
					}

					Area clickbox = object.getClickbox();
					if (clickbox != null)
					{
						OverlayUtil.renderHoverableArea(graphics, object.getClickbox(), client.getMouseCanvasPosition(), TRANSPARENT, color, color.darker());
					}
					break;
			}
		}

		return null;
	}

	private void renderPoly(Graphics2D graphics, Color color, Polygon polygon)
	{
		if (polygon != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(1));
			graphics.draw(polygon);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(polygon);
		}
	}
}
