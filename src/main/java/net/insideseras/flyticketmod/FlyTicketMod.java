package net.insideseras.flyticketmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.insideseras.flyticketmod.item.RainbowPaperItem;
import net.insideseras.flyticketmod.item.modItems;
import net.insideseras.flyticketmod.particle.ModParticles;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.argument.EntityArgumentType;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlyTicketMod implements ModInitializer {

	public static final String MOD_ID = "flyticket-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Items & Partikel registrieren
		modItems.registerModItems();
		ModParticles.registerParticles();

		// FlyTimer überwachen
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			long currentTick = server.getOverworld().getTime();

			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				Long endTick = RainbowPaperItem.flyTimers.get(player.getUuid());

				if (endTick != null) {
					// Creative? → Timer löschen
					if (player.isCreative()) {
						RainbowPaperItem.flyTimers.remove(player.getUuid());
						continue;
					}

					long remainingTicks = endTick - currentTick;

					if (remainingTicks <= 0) {
						player.getAbilities().allowFlying = false;
						player.getAbilities().flying = false;
						player.sendAbilitiesUpdate();
						RainbowPaperItem.flyTimers.remove(player.getUuid());

						player.sendMessage(Text.literal("❌ Dein Flug ist abgelaufen.").formatted(Formatting.RED), false);
					} else if (currentTick % 20 == 0) {
						int totalSeconds = MathHelper.floor(remainingTicks / 20.0);
						int minutes = totalSeconds / 60;
						int seconds = totalSeconds % 60;

						String timeStr = String.format("%02d:%02d", minutes, seconds);
						Text actionBar = Text.literal("✈ Flugzeit: ")
								.append(Text.literal(timeStr).formatted(Formatting.AQUA));

						player.sendMessage(actionBar, true);
					}
				}
			}
		});

		// Befehle registrieren
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("flyticket")
					.requires(source -> source.hasPermissionLevel(2)) // OP-Level 2

					// /flyticket → Ticket für sich selbst
					.executes(context -> {
						ServerPlayerEntity player = context.getSource().getPlayer();
						ItemStack ticket = new ItemStack(modItems.RAINBOW_PAPER);
						player.getInventory().insertStack(ticket);
						player.sendMessage(Text.literal("🎟 Du hast ein FlyTicket erhalten."), false);
						return 1;
					})

					// /flyticket give <Spieler>
					.then(literal("give")
							.then(argument("target", EntityArgumentType.player())
									.executes(ctx -> {
										ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
										ItemStack ticket = new ItemStack(modItems.RAINBOW_PAPER);
										target.getInventory().insertStack(ticket);
										ctx.getSource().sendFeedback(
												() -> Text.literal("🎟 FlyTicket an " + target.getName().getString() + " gegeben."), false
										);
										return 1;
									})
							)
					)

					// /flyticket cancel
					.then(literal("cancel")
							.executes(ctx -> {
								ServerPlayerEntity player = ctx.getSource().getPlayer();
								if (RainbowPaperItem.flyTimers.containsKey(player.getUuid())) {
									RainbowPaperItem.flyTimers.remove(player.getUuid());
									player.getAbilities().allowFlying = false;
									player.getAbilities().flying = false;
									player.sendAbilitiesUpdate();
									player.sendMessage(Text.literal("❌ Dein aktives FlyTicket wurde abgebrochen.")
											.formatted(Formatting.RED), false);
									return 1;
								} else {
									player.sendMessage(Text.literal("⚠ Du hast kein aktives FlyTicket."), false);
									return 0;
								}
							})
					)
			);
		});

		LOGGER.info("FlyTicketMod wurde geladen.");
	}
}
