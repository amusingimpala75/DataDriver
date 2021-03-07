package com.github.amusingimpala75.datadriver.api;

import com.github.amusingimpala75.datadriver.impl.CodecsImpl;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class EffectUpdate {

    public static final Codec<EffectUpdate> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("type").forGetter(eu -> eu.type),
            Codec.FLOAT.optionalFieldOf("float_amount").forGetter(eu -> eu.fAmount),
            Codec.INT.optionalFieldOf("int_amount").forGetter(eu -> eu.iAmount),
            CodecsImpl.DAMAGE_SOURCE.optionalFieldOf("damage_source").forGetter(eu -> eu.source),
            Codec.BOOL.fieldOf("can_be_amplified").orElse(false).forGetter(eu -> eu.canBeAmplified)
    ).apply(inst, EffectUpdate::new));

    private final String type;
    private final Optional<Float> fAmount;
    private final Optional<Integer> iAmount;
    private final Optional<DamageSource> source;
    private final boolean canBeAmplified;

    private EffectUpdate(String type, Optional<Float> floatAmount, Optional<Integer> intAmount, Optional<DamageSource> source, boolean canBeAmplified) {
        this.fAmount = floatAmount;
        this.type = type;
        this.iAmount = intAmount;
        this.source = source;
        this.canBeAmplified = canBeAmplified;
    }

    public void applyEffect(LivingEntity entity, int amplifier) {
        switch (type) {
            case "heal":
                entity.heal(fAmount.get());
                break;
            case "damage":
                entity.damage(source.get(), fAmount.get());
                break;
            case "exhaustion":
                float val = fAmount.get();
                if (canBeAmplified) {
                    val *= (amplifier+1);
                }
                ((PlayerEntity) entity).addExhaustion(val);
                break;
            case "hunger":
                int fo = iAmount.get();
                float f = fAmount.get();
                if (canBeAmplified) {
                    f *= amplifier;
                    fo *= amplifier;
                }
                ((PlayerEntity) entity).getHungerManager().add(fo, f);
                break;
            default:
                throw new IllegalStateException("Expected String type to be one of {heal, damage, exhaustion, hunger}, but was instead " + type);
        }
    }
}
