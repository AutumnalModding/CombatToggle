package gdn.hypercube.ctoggle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CombatToggle implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Combat Toggle");
	public static final List<UUID> TOGGLED_PLAYERS = new ArrayList<>();
	public static final List<UUID> BLOCKED_PLAYERS = new ArrayList<>();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
			dispatcher.register(CommandManager.literal("ctoggle").executes(context -> {
				boolean added;

				if (context.getSource().getEntity() instanceof PlayerEntity player) {
					UUID uuid = player.getUuid();

					if (BLOCKED_PLAYERS.contains(uuid)) {
						context.getSource().sendFeedback(() -> Text.literal("You are not allowed to do this!").formatted(Formatting.RED), false);
						return -1;
					}

					if (TOGGLED_PLAYERS.contains(uuid)) { added = false; TOGGLED_PLAYERS.remove(uuid); }
					else {
                        added = true;
                        TOGGLED_PLAYERS.add(uuid);
                    }
					context.getSource().sendFeedback(() -> Text.literal("You can " + (added ? "no longer" : "now") + " be harmed by other players."), true);
					return 1;
				}
                context.getSource().sendFeedback(() -> Text.literal("Only players can run this!").formatted(Formatting.RED), false);
				return -1;
			}).then(CommandManager.argument("target", EntityArgumentType.player()).requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK)).executes(context -> {
				boolean added;
				PlayerEntity target = EntityArgumentType.getPlayer(context, "target");
				UUID uuid = target.getUuid();
				if (TOGGLED_PLAYERS.contains(uuid)) { added = false; TOGGLED_PLAYERS.remove(uuid); }
				else {
					added = true;
					TOGGLED_PLAYERS.add(uuid);
				}
				context.getSource().sendFeedback(() -> Text.literal(target.getName().getString() + " can " + (added ? "no longer" : "now") + " be harmed by other players."), true);
				return 1;
			})));

			dispatcher.register(CommandManager.literal("ctblock").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK)).then(CommandManager.argument("target", EntityArgumentType.player()).executes(context -> {
				boolean added;
				PlayerEntity target = EntityArgumentType.getPlayer(context, "target");
				UUID uuid = target.getUuid();
				if (BLOCKED_PLAYERS.contains(uuid)) { added = false; BLOCKED_PLAYERS.remove(uuid); }
				else {
					added = true;
					BLOCKED_PLAYERS.add(uuid);
				}
				context.getSource().sendFeedback(() -> Text.literal(target.getName().getString() + " can " + (added ? "no longer" : "now") + " toggle their combat-allowed status."), true);
				return 1;
			})));
		});
	}
}