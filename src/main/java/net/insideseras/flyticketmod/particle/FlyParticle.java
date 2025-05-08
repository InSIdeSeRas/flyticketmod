package net.insideseras.flyticketmod.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class FlyParticle extends SpriteBillboardParticle {

    public FlyParticle(ClientWorld world, double x, double y, double z,
                       SpriteProvider spriteProvider, double xSpeed, double ySpeed, double zSpeed) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);

        // Bewegung aktivieren
        this.velocityX = xSpeed;
        this.velocityY = ySpeed;
        this.velocityZ = zSpeed;

        this.velocityMultiplier = 0.9f; // damit es langsamer ausläuft

        this.scale = 0.3f;
        this.maxAge = 40 + world.random.nextInt(10);
        this.setSpriteForAge(spriteProvider);

        this.red = 1f;
        this.green = 1f;
        this.blue = 1f;
        this.alpha = 1.0f;
    }

    @Override
    public void tick() {
        super.tick();

        // Fade-out Effekt
        this.alpha = 1.0f - ((float) this.age / this.maxAge);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z,
                                                 double velocityX, double velocityY, double velocityZ) {
            return new FlyParticle(world, x, y, z, spriteProvider, velocityX, velocityY, velocityZ);
        }
    }
}
