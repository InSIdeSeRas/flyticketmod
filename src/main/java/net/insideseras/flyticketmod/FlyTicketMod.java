package net.insideseras.flyticketmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.insideseras.flyticketmod.item.RainbowPaperItem;
import net.insideseras.flyticketmod.item.modItems;
import net.insideseras.flyticketmod.particle.ModParticles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlyTicketMod implements ModInitializer {

	public static final String MOD_ID = "flyticket-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Registriere Items und Partikel
		modItems.registerModItems();
		ModParticles.registerParticles();

		// Server-Tick: Überwacht, ob Fly-Ablauf erreicht ist
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			server.getPlayerManager().getPlayerList().forEach(player -> {
				Long endTick = RainbowPaperItem.flyTimers.get(player.getUuid());
				if (endTick != null && server.getOverworld().getTime() >= endTick) {
					player.getAbilities().allowFlying = false;
					player.getAbilities().flying = false;
					player.sendAbilitiesUpdate();
					RainbowPaperItem.flyTimers.remove(player.getUuid());
				}
			});
		});

		LOGGER.info("FlyTicketMod wurde geladen.");
	}
}
