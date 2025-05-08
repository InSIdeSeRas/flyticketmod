package net.insideseras.flyticketmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.insideseras.flyticketmod.particle.FlyParticle;
import net.insideseras.flyticketmod.particle.ModParticles;
import net.minecraft.client.particle.ParticleFactory;

public class flyticketmodClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(ModParticles.FLY_PARTICLE, FlyParticle.Factory::new);
    }
}
