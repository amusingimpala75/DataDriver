package com.github.amusingimpala75.datadriver.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockSettings1 {

    public static final Codec<BlockSettings1> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("no_collision").orElse(false).forGetter(b -> b.noCollision),
            Codec.BOOL.fieldOf("non_opaque").orElse(false).forGetter(b -> b.nonOpaque),
            Codec.FLOAT.optionalFieldOf("slipperiness").forGetter(b -> b.slipperiness),
            Codec.FLOAT.optionalFieldOf("velocity_multiplier").forGetter(b -> b.velMult),
            Codec.FLOAT.optionalFieldOf("jump_velocity_multiplier").forGetter(b -> b.jumpMult),
            Codec.FLOAT.optionalFieldOf("hardness").forGetter(b -> b.hardness),
            Codec.FLOAT.optionalFieldOf("resistance").forGetter(b -> b.resistance),
            Codec.BOOL.fieldOf("breaks_instantly").orElse(false).forGetter(b -> b.breaksInstantly),
            Codec.BOOL.fieldOf("ticks_randomly").orElse(false).forGetter(b -> b.ticksRandomly),
            Codec.BOOL.fieldOf("dynamic_bounds").orElse(false).forGetter(b -> b.dynamicBounds),
            Codec.BOOL.fieldOf("drops_nothing").orElse(false).forGetter(b -> b.dropsNothing),
            Codec.BOOL.fieldOf("is_air").orElse(false).forGetter(b -> b.isAir),
            SpawningPossibility.CODEC.optionalFieldOf("spawning").forGetter(b -> b.allowsSpawning),
            Codec.INT.optionalFieldOf("luminance").forGetter(b -> b.luminance),
            Identifier.CODEC.optionalFieldOf("loot_table").forGetter(b -> b.lootTable)
    ).apply(inst, BlockSettings1::new));

    public boolean noCollision;
    public boolean nonOpaque;
    public Optional<Float> slipperiness;
    public Optional<Float> velMult;
    public Optional<Float> jumpMult;
    public Optional<Float> hardness;
    public Optional<Float> resistance;
    public boolean breaksInstantly;
    public boolean ticksRandomly;
    public boolean dynamicBounds;
    public boolean dropsNothing;
    public boolean isAir;
    public Optional<SpawningPossibility> allowsSpawning;
    public Optional<Integer> luminance;
    public Optional<Identifier> lootTable;

    public BlockSettings1(boolean noCollision, boolean nonOpaque, Optional<Float> slipperiness, Optional<Float> velMult, Optional<Float> jumpMult, Optional<Float> hardness, Optional<Float> resistance, boolean breaksInstantly, boolean ticksRandomly, boolean dynamicBounds, boolean dropNothing, boolean isAir, Optional<SpawningPossibility> allowsSpawning, Optional<Integer> luminance, Optional<Identifier> lootTable) {
        this.noCollision = noCollision;
        this.nonOpaque = nonOpaque;
        this.slipperiness = slipperiness;
        this.velMult = velMult;
        this.jumpMult = jumpMult;
        this.hardness = hardness;
        this.resistance = resistance;
        this.breaksInstantly = breaksInstantly;
        this.ticksRandomly = ticksRandomly;
        this.dynamicBounds = dynamicBounds;
        this.dropsNothing = dropNothing;
        this.isAir = isAir;
        this.allowsSpawning = allowsSpawning;
        this.luminance = luminance;
        this.lootTable = lootTable;
    }

    @SuppressWarnings("unused")
    public enum SpawningPossibility {
        ALL,
        HOSTILE,
        PASSIVE,
        NONE;

        public static Codec<SpawningPossibility> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("type").forGetter(s -> s.name().toLowerCase(Locale.ROOT))
        ).apply(inst, s -> SpawningPossibility.valueOf(s.toUpperCase(Locale.ROOT))));
    }
}
