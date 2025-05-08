package net.insideseras.flyticketmod;

import net.fabricmc.api.ModInitializer;

import net.insideseras.flyticketmod.item.modItems;
import net.insideseras.flyticketmod.particle.ModParticles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlyTicketMod implements ModInitializer {
	public static final String MOD_ID = "flyticket-mod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		modItems.registerModItems();

		ModParticles.registerParticles();
	}
}