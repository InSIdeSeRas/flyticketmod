package net.insideseras.flyticketmod.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.insideseras.flyticketmod.FlyTicketMod;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModParticles {
    public static final SimpleParticleType FLY_PARTICLE =
            registerParticle(  "fly_particle", FabricParticleTypes.simple());

    private static SimpleParticleType registerParticle(String name, SimpleParticleType particleType){
        return Registry.register(Registries.PARTICLE_TYPE, Identifier.of(FlyTicketMod.MOD_ID, name), particleType);
    }

    public static void registerParticles(){
        FlyTicketMod.LOGGER.info("Registering Particles for " + FlyTicketMod.MOD_ID);
    }


}
