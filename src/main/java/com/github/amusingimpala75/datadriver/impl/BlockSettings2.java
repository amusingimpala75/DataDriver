package com.github.amusingimpala75.datadriver.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.MaterialColor;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockSettings2 {

    public static final Codec<BlockSettings2> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            CodecsImpl.BLOCK_SOUND_GROUP.optionalFieldOf("sounds").forGetter(s -> s.bsg),
            Identifier.CODEC.optionalFieldOf("drops_like").forGetter(s -> s.dropBlockId),
            Codec.BOOL.fieldOf("is_solid").orElse(true).forGetter(s -> s.solidBlock),
            Codec.BOOL.fieldOf("suffocates").orElse(true).forGetter(s -> s.suffocates),
            Codec.BOOL.fieldOf("blocks_vision").orElse(true).forGetter(s -> s.blocksVision),
            Codec.BOOL.fieldOf("postProcess").orElse(true).forGetter(s -> s.postProcess),
            Codec.BOOL.fieldOf("emissive_lighting").orElse(false).forGetter(s -> s.emissiveLighting),
            CodecsImpl.MATERIAL_COLOR.optionalFieldOf("material_color").forGetter(s -> s.matColor),
            CodecsImpl.DYE_COLOR.optionalFieldOf("dye_color").forGetter(s -> s.dyeColor),
            Codec.BOOL.fieldOf("collidable").orElse(true).forGetter(s -> s.collidable),
            Codec.BOOL.fieldOf("break_by_hand").orElse(true).forGetter(s -> s.breakByHand),
            Identifier.CODEC.optionalFieldOf("break_tag").forGetter(s -> s.breakTag),
            Codec.INT.optionalFieldOf("required_mining_level").forGetter(s -> s.requiredMiningLevel)
    ).apply(inst, BlockSettings2::new));

    public final Optional<BlockSoundGroup> bsg;
    public final Optional<Identifier> dropBlockId;
    public final boolean solidBlock;
    public final boolean suffocates;
    public final boolean blocksVision;
    public final boolean postProcess;
    public final boolean emissiveLighting;
    public final Optional<MaterialColor> matColor;
    public final Optional<DyeColor> dyeColor;
    public final boolean collidable;
    public final boolean breakByHand;
    public final Optional<Identifier> breakTag;
    public final Optional<Integer> requiredMiningLevel;

    public BlockSettings2(Optional<BlockSoundGroup> bsg, Optional<Identifier> dropBlockId, boolean solidBlock, boolean suffocates, boolean blocksVision, boolean postProcess, boolean emissiveLighting, Optional<MaterialColor> matColor, Optional<DyeColor> dyeColor, boolean collidable, boolean breakByHand, Optional<Identifier> breakTag, Optional<Integer> requiredMiningLevel) {
        this.bsg = bsg;
        this.dropBlockId = dropBlockId;
        this.solidBlock = solidBlock;
        this.suffocates = suffocates;
        this.blocksVision = blocksVision;
        this.postProcess = postProcess;

        this.emissiveLighting = emissiveLighting;
        this.matColor = matColor;
        this.dyeColor = dyeColor;
        this.collidable = collidable;
        this.breakByHand = breakByHand;
        this.breakTag = breakTag;
        this.requiredMiningLevel = requiredMiningLevel;
    }
}
