package net.insideseras.flyticketmod.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

public class FlyParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;

    public FlyParticle(ClientWorld world, double x, double y, double z,
                       SpriteProvider spriteProvider, double xSpeed, double ySpeed, double zSpeed) {
        super(world, x, y, z, 0.0, 0.01, 0.0); // sanft aufsteigend
        this.spriteProvider = spriteProvider;

        this.velocityMultiplier = 0.8f;
        this.scale = 0.5f; // Startgröße
        this.maxAge = 40 + world.random.nextInt(10); // Lebensdauer 40–50 Ticks

        this.setSprite(spriteProvider); // NICHT setSpriteForAge

        this.alpha = 1.0f;
    }

    @Override
    public void tick() {
        super.tick();

        // Farbverlauf von Magisch Blau → Amethyst
        float progress = (float) this.age / this.maxAge;

        this.red   = lerp(0.6f, 0.7f, progress);
        this.green = lerp(0.8f, 0.4f, progress);
        this.blue  = lerp(1.0f, 0.9f, progress);

        // Größe reduziert sich mit Alter
        this.scale = 0.5f - 0.3f * progress;

        // Transparenz nimmt ab
        this.alpha = 1.0f - progress;

        // Leichter Auftrieb
        this.velocityY += 0.002;

        // Sprite setzen
        this.setSprite(this.spriteProvider);
    }

    // Hilfsfunktion: lineare Interpolation
    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
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
            return new FlyParticle(world, x, y, z, spriteProvider, 0, 0, 0);
        }
    }
}
