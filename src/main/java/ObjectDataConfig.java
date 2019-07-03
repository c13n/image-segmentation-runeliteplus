import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("ObjectData")
public interface ObjectDataConfig extends Config
{
	@ConfigItem(
		keyName = "hotkeyToggle",
		name = "Save capture",
		description = "",
		position = 0
	)
	default Keybind hotkeyToggle()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 1,
		keyName = "objectMarkerRenderStyle",
		name = "Highlight Style",
		description = "Highlight setting"
	)
	default RenderStyle objectRenderStyle()
	{
		return RenderStyle.HULL;
	}

	@ConfigItem(
		position = 2,
		keyName = "showOverlay",
		name = "Show overlay",
		description = ""
	)
	default boolean showOverlay()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "showGameObjects",
		name = "Game objects",
		description = ""
	)
	default boolean showGameObjects()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "showDecorativeObjects",
		name = "Decorative objects",
		description = ""
	)
	default boolean showDecorativeObjects()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "showNpcs",
		name = "NPCs",
		description = ""
	)
	default boolean showNpcs()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "showPeople",
		name = "Other players",
		description = ""
	)
	default boolean showPeople()
	{
		return true;
	}
}
