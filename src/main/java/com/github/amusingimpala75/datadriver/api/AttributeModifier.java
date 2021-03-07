package com.github.amusingimpala75.datadriver.api;

import com.github.amusingimpala75.datadriver.impl.CodecsImpl;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;

import java.util.HashMap;
import java.util.Map;

public class AttributeModifier {

    private static final Map<EntityAttribute, Pair<String, String>> attribute2uuid = new HashMap<>();

    public static final Codec<AttributeModifier> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            CodecsImpl.ENTITY_ATTRIBUTE.fieldOf("attribute").forGetter(am -> am.attrib),
            Codec.DOUBLE.fieldOf("amount").forGetter(am -> am.amt),
            CodecsImpl.ATTRIBUTE_OPERATION.fieldOf("operation").forGetter(am -> am.op),
            Codec.BOOL.fieldOf("increase_or_decrease").forGetter(am -> am.increase)
    ).apply(inst, AttributeModifier::new));

    private final EntityAttribute attrib;
    private final double amt;
    private final EntityAttributeModifier.Operation op;
    private final boolean increase;

    public AttributeModifier(EntityAttribute attrib, double amt, EntityAttributeModifier.Operation op, boolean increase) {
        this.attrib = attrib;
        this.amt = amt;
        this.op = op;
        this.increase = increase;
    }

    public void apply(StatusEffect e) {
        Pair<String, String> pair = attribute2uuid.get(attrib);
        String uuid = increase ? pair.getFirst() : pair.getSecond();
        e.addAttributeModifier(attrib, uuid, amt, op);
    }

    static {
        //Todo
        attribute2uuid.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new Pair<>("648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", "22653B89-116E-49DC-9B6B-9971489B5BE5"));
        attribute2uuid.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new Pair<>("91AEAA56-376B-4498-935B-2F7F68070635", "7107DE5E-7CE8-4030-940E-514C1F160890"));
        attribute2uuid.put(EntityAttributes.GENERIC_ATTACK_SPEED, new Pair<>("AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", "55FCED67-E92A-486E-9800-B47F202C4386"));
        attribute2uuid.put(EntityAttributes.GENERIC_LUCK, new Pair<>("03C3C89D-7037-4B42-869F-B146BCB64D2E", "CC5AF142-2BD2-4215-B636-2605AED11727"));
        attribute2uuid.put(EntityAttributes.GENERIC_MAX_HEALTH, new Pair<>("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC", "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC"));
    }
}
