package net.insideseras.flyticketmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.insideseras.flyticketmod.item.RainbowPaperItem;
import net.insideseras.flyticketmod.item.modItems;
import net.insideseras.flyticketmod.particle.ModParticles;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

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

		// Ticker zur Überwachung des Fly-Timers
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			long currentTick = server.getOverworld().getTime();

			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				Long endTick = RainbowPaperItem.flyTimers.get(player.getUuid());

				if (endTick != null) {
					// Wenn der Spieler im Creative ist → Timer sofort löschen
					if (player.isCreative()) {
						RainbowPaperItem.flyTimers.remove(player.getUuid());
						continue;
					}

					long remainingTicks = endTick - currentTick;

					if (remainingTicks <= 0) {
						// Flug deaktivieren
						player.getAbilities().allowFlying = false;
						player.getAbilities().flying = false;
						player.sendAbilitiesUpdate();
						RainbowPaperItem.flyTimers.remove(player.getUuid());

						player.sendMessage(Text.literal("❌ Dein Flug ist abgelaufen.").formatted(Formatting.RED), false);
					} else if (currentTick % 20 == 0) {
						// Action Bar alle 20 Ticks (1 Sekunde)
						int totalSeconds = MathHelper.floor(remainingTicks / 20.0);
						int minutes = totalSeconds / 60;
						int seconds = totalSeconds % 60;

						String timeStr = String.format("%02d:%02d", minutes, seconds);
						Text actionBar = Text.literal("✈ Flugzeit: ")
								.append(Text.literal(timeStr).formatted(Formatting.AQUA));

						player.sendMessage(actionBar, true); // true = Action Bar
					}
				}
			}
		});

		LOGGER.info("FlyTicketMod wurde geladen.");
	}
}
