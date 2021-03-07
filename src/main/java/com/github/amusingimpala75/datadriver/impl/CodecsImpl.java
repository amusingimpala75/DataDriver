package com.github.amusingimpala75.datadriver.impl;

import com.github.amusingimpala75.datadriver.api.AttributeModifier;
import com.github.amusingimpala75.datadriver.api.Codecs;
import com.github.amusingimpala75.datadriver.api.EffectUpdate;
import com.github.amusingimpala75.datadriver.api.Util;
import com.github.amusingimpala75.datadriver.duck.BlockDuck;
import com.github.amusingimpala75.datadriver.duck.ItemDuck;
import com.github.amusingimpala75.datadriver.mixin.IngredientAccessor;
import com.github.amusingimpala75.datadriver.mixin.block.*;
import com.github.amusingimpala75.datadriver.mixin.item.*;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Ingredient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("ConstantConditions")
public class CodecsImpl {

    private static final MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

    public static final Codec<Rarity> RARITY = Util.createEnumCodec(Rarity.class);

    public static final Codec<PressurePlateBlock.ActivationRule> ACTIVATION_RULE = Util.createEnumCodec(PressurePlateBlock.ActivationRule.class);

    public static final Codec<Pair<StatusEffectInstance, Float>> STATUS_EFFECT_INSTANCE_FOOD = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("effect").forGetter(e -> Registry.STATUS_EFFECT.getId(e.getFirst().getEffectType())),
            Codec.FLOAT.fieldOf("duration").forGetter(Pair::getSecond)
    ).apply(inst, (effect, duration) -> new Pair<>(new StatusEffectInstance(Util.getOrThrow(Registry.STATUS_EFFECT, effect)), duration)));

    public static final Codec<StatusEffectInstance> STATUS_EFFECT_INSTANCE = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("effect").forGetter(e -> Registry.STATUS_EFFECT.getId(e.getEffectType())),
            Codec.INT.fieldOf("duration").orElse(0).forGetter(StatusEffectInstance::getDuration),
            Codec.INT.fieldOf("amplifier").orElse(0).forGetter(StatusEffectInstance::getAmplifier),
            Codec.BOOL.fieldOf("ambient").orElse(false).forGetter(StatusEffectInstance::isAmbient),
            Codec.BOOL.fieldOf("show_particles").orElse(true).forGetter(StatusEffectInstance::shouldShowParticles),
            Codec.BOOL.fieldOf("show_icon").orElse(true).forGetter(StatusEffectInstance::shouldShowIcon)
    ).apply(inst, (e, d, amp, amb, sp, si) -> new StatusEffectInstance(Util.getOrThrow(Registry.STATUS_EFFECT, e), d, amp, amb, sp, si)));

    public static final Codec<FoodComponent> FOOD_COMPONENT = RecordCodecBuilder.create(inst  -> inst.group(
            Codec.INT.optionalFieldOf("hunger").forGetter(f -> Optional.of(f.getHunger())),
            Codec.FLOAT.optionalFieldOf("saturation_modifier").forGetter(f -> Optional.of(f.getSaturationModifier())),
            Codec.BOOL.fieldOf("is_meat").orElse(false).forGetter(FoodComponent::isMeat),
            Codec.BOOL.fieldOf("is_always_edible").orElse(false).forGetter(FoodComponent::isAlwaysEdible),
            Codec.BOOL.fieldOf("is_snack").orElse(false).forGetter(FoodComponent::isAlwaysEdible),
            STATUS_EFFECT_INSTANCE_FOOD.listOf().fieldOf("effect").forGetter(FoodComponent::getStatusEffects)
    ).apply(inst, (hunger, saturation, isMeat, isAlwaysEdible, isSnack, effect) -> {
        FoodComponent.Builder builder = new FoodComponent.Builder();
        hunger.ifPresent(builder::hunger);
        saturation.ifPresent(builder::saturationModifier);
        if (isMeat) {
            builder.meat();
        }
        if (isAlwaysEdible) {
            builder.alwaysEdible();
        }
        if (isSnack) {
            builder.snack();
        }
        for (Pair<StatusEffectInstance, Float> e : effect) {

            builder.statusEffect(e.getFirst(), e.getSecond());
        }
        return builder.build();
    }));

    public static final Codec<EquipmentSlot> EQUIPMENT_SLOT = Util.createEnumCodec(EquipmentSlot.class);

    public static final Codec<BoatEntity.Type> BOAT_TYPE = Util.createEnumCodec(BoatEntity.Type.class);

    public static final Codec<DyeColor> DYE_COLOR = Codec.STRING.comapFlatMap(str -> {
        try {
            return DataResult.success(DyeColor.byName(str, DyeColor.BLACK));
        } catch (InvalidIdentifierException var2) {
            return DataResult.error("Not a valid resource location: " + str + " " + var2.getMessage());
        }
    }, DyeColor::asString);

    public static final Codec<MaterialColor> MATERIAL_COLOR = Codec.INT.comapFlatMap(i -> {
        try {
            return DataResult.success(MaterialColor.COLORS[i]);
        } catch (InvalidIdentifierException var2) {
            return DataResult.error("Not a valid resource location: " + i + " " + var2.getMessage());
        }
    }, mat -> mat.id);

    public static final Codec<PistonBehavior> PISTON_BEHAVIOR = Util.createEnumCodec(PistonBehavior.class);

    public static final Codec<Material> MATERIAL = RecordCodecBuilder.create(inst -> inst.group(
            MATERIAL_COLOR.fieldOf("color").forGetter(Material::getColor),
            Codec.BOOL.fieldOf("is_liquid").orElse(false).forGetter(Material::isLiquid),
            Codec.BOOL.fieldOf("is_solid").orElse(true).forGetter(Material::isSolid),
            Codec.BOOL.fieldOf("blocks_movement").orElse(true).forGetter(Material::blocksMovement),
            Codec.BOOL.fieldOf("blocks_light").orElse(true).forGetter(Material::blocksLight),
            Codec.BOOL.fieldOf("break_by_hand").orElse(true).forGetter(Material::isBurnable),
            //Yarn did an oopsie
            Codec.BOOL.fieldOf("burnable").orElse(false).forGetter(Material::isReplaceable),
            PISTON_BEHAVIOR.fieldOf("piston_behavior").orElse(PistonBehavior.NORMAL).forGetter(Material::getPistonBehavior)
    ).apply(inst, Material::new));

    public static final Codec<BlockSoundGroup> BLOCK_SOUND_GROUP = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("volume").forGetter(BlockSoundGroup::getVolume),
            Codec.FLOAT.fieldOf("pitch").forGetter(BlockSoundGroup::getPitch),
            SoundEvent.CODEC.listOf().fieldOf("sounds").forGetter(g -> {
                List<SoundEvent> sounds = new ArrayList<>();
                sounds.add(g.getBreakSound());
                sounds.add(g.getStepSound());
                sounds.add(g.getPlaceSound());
                sounds.add(g.getHitSound());
                sounds.add(g.getFallSound());
                return sounds;
            })
    ).apply(inst, (v, p, s) -> new BlockSoundGroup(v, p, s.get(0), s.get(1), s.get(2), s.get(3), s.get(5))));

    public static final Codec<ItemGroup> ITEM_GROUP = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("identifier").forGetter(Util::group2Id),
            Identifier.CODEC.fieldOf("display_stack").forGetter(g -> Registry.ITEM.getId(g.getIcon().getItem()))
    ).apply(inst, (id, stack) -> FabricItemGroupBuilder.build(id, () -> new ItemStack(Util.getOrThrow(Registry.ITEM, stack)))));

    //Simplify durability, protection
    public static final Codec<ArmorMaterial> ARMOR_MATERIAL = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.listOf().fieldOf("durability").forGetter(a -> {
                List<Integer> durabilities = new ArrayList<>();
                durabilities.add(a.getDurability(EquipmentSlot.HEAD));
                durabilities.add(a.getDurability(EquipmentSlot.CHEST));
                durabilities.add(a.getDurability(EquipmentSlot.LEGS));
                durabilities.add(a.getDurability(EquipmentSlot.FEET));
                return durabilities;
            }),
            Codec.INT.listOf().fieldOf("protection").forGetter(a -> {
                List<Integer> prot = new ArrayList<>();
                prot.add(a.getDurability(EquipmentSlot.HEAD));
                prot.add(a.getDurability(EquipmentSlot.CHEST));
                prot.add(a.getDurability(EquipmentSlot.LEGS));
                prot.add(a.getDurability(EquipmentSlot.FEET));
                return prot;
            }),
            Codec.INT.fieldOf("enchantability").forGetter(ArmorMaterial::getEnchantability),
            SoundEvent.CODEC.fieldOf("equip_sound").forGetter(ArmorMaterial::getEquipSound),
            Identifier.CODEC.listOf().fieldOf("repair_ingredient").forGetter(a -> {
                List<Identifier> ids = new ArrayList<>();
                Arrays.stream(((IngredientAccessor)(Object) a.getRepairIngredient()).accessor$getEntries()).forEach(e -> e.getStacks().forEach(stack -> Util.getIdOrThrow(Registry.ITEM, stack.getItem())));
                return ids;
            }),
            Codec.STRING.fieldOf("name").forGetter(ArmorMaterial::getName),
            Codec.FLOAT.fieldOf("toughness").orElse(0.0F).forGetter(ArmorMaterial::getToughness),
            Codec.FLOAT.fieldOf("knockback_resistance").orElse(0.0F).forGetter(ArmorMaterial::getKnockbackResistance)
    ).apply(inst, (d, p, e, es, ri, n, t, kr) -> new ArmorMaterial() {
        @Override
        public int getDurability(EquipmentSlot slot) {
            return d.get(slot.getEntitySlotId());
        }

        @Override
        public int getProtectionAmount(EquipmentSlot slot) {
            return p.get(slot.getEntitySlotId());
        }

        @Override
        public int getEnchantability() {
            return e;
        }

        @Override
        public SoundEvent getEquipSound() {
            return es;
        }

        @Override
        public Ingredient getRepairIngredient() {
            List<ItemStack> stacks = new ArrayList<>();
            ri.forEach(id -> stacks.add(new ItemStack(Util.getOrThrow(Registry.ITEM, id))));
            return Ingredient.ofStacks(stacks.stream());
        }

        @Override
        public String getName() {
            return n;
        }

        @Override
        public float getToughness() {
            return t;
        }

        @Override
        public float getKnockbackResistance() {
            return kr;
        }
    }));

    public static final Codec<ToolMaterial> TOOL_MATERIAL = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("durability").forGetter(ToolMaterial::getDurability),
            Codec.FLOAT.fieldOf("mining_speed").forGetter(ToolMaterial::getMiningSpeedMultiplier),
            Codec.FLOAT.fieldOf("attack_damage").forGetter(ToolMaterial::getAttackDamage),
            Codec.INT.fieldOf("mining_level").forGetter(ToolMaterial::getMiningLevel),
            Codec.INT.fieldOf("enchantability").forGetter(ToolMaterial::getEnchantability),
            Identifier.CODEC.listOf().fieldOf("repair_ingredient").forGetter(a -> {
                List<Identifier> ids = new ArrayList<>();
                Arrays.stream(((IngredientAccessor)(Object) a.getRepairIngredient()).accessor$getEntries()).forEach(e -> e.getStacks().forEach(stack -> Util.getIdOrThrow(Registry.ITEM, stack.getItem())));
                return ids;
            })
    ).apply(inst, (d, msm, ad, ml, e, ri) -> new ToolMaterial() {
        @Override
        public int getDurability() {
            return d;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return msm;
        }

        @Override
        public float getAttackDamage() {
            return ad;
        }

        @Override
        public int getMiningLevel() {
            return ml;
        }

        @Override
        public int getEnchantability() {
            return e;
        }

        @Override
        public Ingredient getRepairIngredient() {
            List<ItemStack> stacks = new ArrayList<>();
            ri.forEach(id -> stacks.add(new ItemStack(Util.getOrThrow(Registry.ITEM, id))));
            return Ingredient.ofStacks(stacks.stream());
        }
    }));

    //TODO: Implement that needed for: MinecartItem, BedItem, SpawnEggItem, BannerItem
    @SuppressWarnings({"unused"})
    public static class Items {
        public static final Codec<Item> ITEM = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(i -> ((ItemDuck)i).getSettings())
        ).apply(inst, Item::new));

        public static final Codec<FlintAndSteelItem> FLINT_AND_STEEL = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, FlintAndSteelItem::new));

        public static final Codec<MushroomStewItem> MUSHROOM_STEW = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, MushroomStewItem::new));

        public static final Codec<BoatItem> BOAT = RecordCodecBuilder.create(inst -> inst.group(
                BOAT_TYPE.fieldOf("boat").forGetter(t -> ((BoatItemAccessor)t).accessor$getType()),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, BoatItem::new));

        public static final Codec<BookItem> BOOK = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings()),
                Codec.INT.optionalFieldOf("enchantability").forGetter(f -> Optional.of(1))
        ).apply(inst, (set, ench) -> new BookItem(set) {
            @Override
            public int getEnchantability() {
                return ench.orElse(super.getEnchantability());
            }
        }));

        public static final Codec<CompassItem> COMPASS = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, CompassItem::new));

        public static final Codec<FishingRodItem> FISHING_ROD = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings()),
                Codec.INT.optionalFieldOf("enchantability").forGetter(f -> Optional.of(1))
        ).apply(inst, (set, ench) -> new FishingRodItem(set) {
            @Override
            public int getEnchantability() {
                return ench.orElse(super.getEnchantability());
            }
        }));

        public static final Codec<DyeItem> DYE = RecordCodecBuilder.create(inst -> inst.group(
                DYE_COLOR.fieldOf("color").forGetter(c -> ((DyeItemAccessor)c).accessor$getColor()),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, DyeItem::new));

        public static final Codec<BoneMealItem> BONE_MEAL = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, BoneMealItem::new));

        public static final Codec<EnderEyeItem> EYE_OF_ENDER = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, EnderEyeItem::new));

        public static final Codec<EnderPearlItem> ENDER_PEARL = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, EnderPearlItem::new));

        public static final Codec<FireChargeItem> FIRE_CHARGE = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, FireChargeItem::new));

        public static final Codec<FireworkChargeItem> FIREWORK_CHARGE = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, FireworkChargeItem::new));

        public static final Codec<ExperienceBottleItem> XP_BOTTLE = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings()),
                Codec.BOOL.fieldOf("has_glint").orElse(true).forGetter(f -> true)
        ).apply(inst, (set, glint) -> new ExperienceBottleItem(set) {
            @Override
            public boolean hasGlint(ItemStack stack) {
                return glint;
            }
        }));

        public static final Codec<FireworkItem> FIREWORK = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, FireworkItem::new));

        public static final Codec<ChorusFruitItem> CHORUS_FRUIT = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, ChorusFruitItem::new));

        public static final Codec<MusicDiscItem> MUSIC_DISC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("comparator_output").forGetter(MusicDiscItem::getComparatorOutput),
                SoundEvent.CODEC.fieldOf("sound").forGetter(MusicDiscItem::getSound),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, (out, sound, set) -> new MusicDiscItem(out, sound, set){}));

        public static final Codec<HorseArmorItem> HORSE_ARMOR = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("protection").forGetter(HorseArmorItem::getBonus),
                Codec.STRING.fieldOf("armor_name").forGetter(f -> ((HorseItemAccessor)f).accessor$getName()),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, HorseArmorItem::new));

        public static final Codec<ShieldItem> SHIELD = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings()),
                Codec.INT.optionalFieldOf("max_use_time").forGetter(f -> Optional.of(72000))
        ).apply(inst, (set, time) -> new ShieldItem(set) {
            @Override
            public int getMaxUseTime(ItemStack stack) {
                return time.orElse(super.getMaxUseTime(stack));
            }
        }));

        public static final Codec<ElytraItem> ELYTRA = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings()),
                Identifier.CODEC.optionalFieldOf("repair").forGetter(f -> Optional.empty())
        ).apply(inst, (set, repair) -> new ElytraItem(set) {
            @Override
            public boolean canRepair(ItemStack stack, ItemStack ingredient) {
                if (repair.isPresent()) {
                    Identifier id = repair.get();
                    if (id.getPath().contains("#")) {
                        return Ingredient.fromTag(TagRegistry.item(new Identifier(id.getNamespace(), id.getPath().substring(1)))).test(ingredient);
                    }
                    return Util.getOrThrow(Registry.ITEM, id) == ingredient.getItem();
                }
                return super.canRepair(stack, ingredient);
            }
        }));

        public static final Codec<ArmorItem> ARMOR_PIECE = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("material").forGetter(item -> Util.getIdOrThrow(Registries.ARMOR_MATERIALS, item.getMaterial())),
                EQUIPMENT_SLOT.fieldOf("slot").forGetter(ArmorItem::getSlotType),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, (mat, slot, set) -> new ArmorItem(Util.getOrThrow(Registries.ARMOR_MATERIALS, mat), slot, set)));

        public static final Codec<CrossbowItem> CROSSBOW = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings()),
                Codec.INT.optionalFieldOf("range").forGetter(f -> Optional.of(8))
        ).apply(inst, (set, range) -> new CrossbowItem(set) {
            @Override
            public int getRange() {
                return range.orElse(super.getRange());
            }
        }));

        public static final Codec<BowItem> BOW = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings()),
                Codec.INT.optionalFieldOf("max_use_time").forGetter(f -> Optional.of(72000)),
                Codec.INT.optionalFieldOf("range").forGetter(f -> Optional.of(15))
        ).apply(inst, (set, use, range) -> new BowItem(set) {
            @Override
            public int getRange() {
                return range.orElse(super.getRange());
            }

            @Override
            public int getMaxUseTime(ItemStack stack) {
                return use.orElse(super.getMaxUseTime(stack));
            }
        }));

        public static final Codec<AxeItem> AXE = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("material").forGetter(a -> Util.getIdOrThrow(Registries.TOOL_MATERIALS, a.getMaterial())),
                Codec.FLOAT.fieldOf("attack_damage").forGetter(MiningToolItem::getAttackDamage),
                Codec.FLOAT.fieldOf("attack_speed").forGetter(a -> 1.0F),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, (mat, ad, as, set) -> new AxeItem(Util.getOrThrow(Registries.TOOL_MATERIALS, mat), ad, as, set){}));

        public static final Codec<HoeItem> HOE = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("material").forGetter(a -> Util.getIdOrThrow(Registries.TOOL_MATERIALS, a.getMaterial())),
                Codec.INT.fieldOf("attack_damage").forGetter(a -> (int) a.getAttackDamage()),
                Codec.FLOAT.fieldOf("attack_speed").forGetter(a -> 1.0F),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, (mat, ad, as, set) -> new HoeItem(Util.getOrThrow(Registries.TOOL_MATERIALS, mat), ad, as, set){}));

        public static final Codec<PickaxeItem> PICKAXE = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("material").forGetter(a -> Util.getIdOrThrow(Registries.TOOL_MATERIALS, a.getMaterial())),
                Codec.INT.fieldOf("attack_damage").forGetter(a -> (int) a.getAttackDamage()),
                Codec.FLOAT.fieldOf("attack_speed").forGetter(a -> 1.0F),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, (mat, ad, as, set) -> new PickaxeItem(Util.getOrThrow(Registries.TOOL_MATERIALS, mat), ad, as, set){}));

        public static final Codec<ShovelItem> SHOVEL = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("material").forGetter(a -> Util.getIdOrThrow(Registries.TOOL_MATERIALS, a.getMaterial())),
                Codec.FLOAT.fieldOf("attack_damage").forGetter(MiningToolItem::getAttackDamage),
                Codec.FLOAT.fieldOf("attack_speed").forGetter(a -> 1.0F),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, (mat, ad, as, set) -> new ShovelItem(Util.getOrThrow(Registries.TOOL_MATERIALS, mat), ad, as, set)));

        public static final Codec<SwordItem> SWORD = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("material").forGetter(a -> Util.getIdOrThrow(Registries.TOOL_MATERIALS, a.getMaterial())),
                Codec.INT.fieldOf("attack_damage").forGetter(a -> (int) a.getAttackDamage()),
                Codec.FLOAT.fieldOf("attack_speed").forGetter(a -> 1.0F),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, (mat, ad, as, set) -> new SwordItem(Util.getOrThrow(Registries.TOOL_MATERIALS, mat), ad, as, set)));

        public static final Codec<MiningToolItem> MINING_TOOL = RecordCodecBuilder.create(inst -> inst.group(
                Codec.FLOAT.fieldOf("attack_damage").forGetter(MiningToolItem::getAttackDamage),
                Codec.FLOAT.fieldOf("attack_speed").forGetter(m -> 1.0F),
                Identifier.CODEC.fieldOf("material").forGetter(mat -> Util.getIdOrThrow(Registries.TOOL_MATERIALS, mat.getMaterial())),
                Identifier.CODEC.listOf().fieldOf("effective_blocks").forGetter(mat -> {
                    List<Identifier> ids = new ArrayList<>();
                    ((MiningToolItemAccessor)mat).accessor$getEffectiveBlocks().forEach(b -> ids.add(Util.getIdOrThrow(Registry.BLOCK, b)));
                    return ids;
                }),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, (ad, as, mat, ids, set) -> {
            Set<Block> blocks = Sets.newHashSet();
            ids.forEach(id -> blocks.add(Util.getOrThrow(Registry.BLOCK, id)));
            return new MiningToolItem(ad, as, Util.getOrThrow(Registries.TOOL_MATERIALS, mat), blocks, set){};
        }));

        public static final Codec<TridentItem> TRIDENT = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings()),
                Codec.INT.optionalFieldOf("max_use_time").forGetter(f -> Optional.of(72000))
        ).apply(inst, (set, delay) -> new TridentItem(set) {
            @Override
            public int getMaxUseTime(ItemStack stack) {
                return delay.orElseGet(() -> super.getMaxUseTime(stack));
            }
        }));

        public static final Codec<BlockItem> BLOCK_ITEM = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings()),
                Identifier.CODEC.fieldOf("block").forGetter(f -> Util.getIdOrThrow(Registry.BLOCK, f.getBlock()))
        ).apply(inst, (set, bl) -> new BlockItem(Util.getOrThrow(Registry.BLOCK, bl), set)));

        public static final Codec<BucketItem> BUCKET = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("fluid").forGetter(b -> new Identifier("Later")),
                Codecs.ITEM_SETTINGS.fieldOf("settings").forGetter(f -> ((ItemDuck)f).getSettings())
        ).apply(inst, (f, set) -> {
            Fluid fluid;
            try {
                fluid = Util.getOrThrow(Registry.FLUID, f);
            } catch (IllegalStateException e) {
                fluid = Util.getOrThrow(Registry.FLUID, new Identifier(f.getNamespace(), "still_"+f.getPath()));
            }
            return new BucketItem(fluid, set);
        }));
    }

    //TODO: Need more: SaplingBlock
    //TorchBlock
    @SuppressWarnings("unused")
    public static class Blocks {
        public static final Codec<Block> BLOCK = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings()),
                Codec.INT.listOf().optionalFieldOf("dimensions").forGetter(b -> Optional.empty())
        ).apply(inst, (set, dims) -> new Block(set) {
            @SuppressWarnings("deprecation")
            @Override
            public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
                if (dims.isPresent()) {
                    List<Integer> p = dims.get();
                    return Block.createCuboidShape(p.get(0), p.get(1), p.get(2), p.get(3), p.get(4), p.get(5));
                }
                return super.getCollisionShape(state, world, pos, context);
            }
        }));

        public static final Codec<OreBlock> ORE = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings()),
                Codec.INT.optionalFieldOf("max_xp").forGetter(b -> Optional.empty()),
                Codec.INT.optionalFieldOf("min_xp").forGetter(b -> Optional.empty())
        ).apply(inst, (set, maxXp, minXp) -> new OreBlock(set) {
            @Override
            protected int getExperienceWhenMined(Random random) {
                if (maxXp.isPresent()) {
                    if (minXp.isPresent()) {
                        MathHelper.nextInt(random, minXp.get(), maxXp.get());
                    } else {
                        MathHelper.nextInt(random, 0, maxXp.get());
                    }
                }
                return super.getExperienceWhenMined(random);
            }
        }));

        public static final Codec<FallingBlock> FALLING_BLOCK = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, FallingBlock::new));

        public static final Codec<PillarBlock> PILLAR = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, PillarBlock::new));

        public static final Codec<LeavesBlock> LEAVES = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, LeavesBlock::new));

        public static final Codec<BedBlock> BED = RecordCodecBuilder.create(inst -> inst.group(
                DYE_COLOR.fieldOf("color").forGetter(BedBlock::getColor),
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, BedBlock::new));

        public static final Codec<RailBlock> RAIL = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, set -> new RailBlock(set){}));

        public static final Codec<FlowerBlock> FLOWER = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("effect").forGetter(f -> Util.getIdOrThrow(Registry.STATUS_EFFECT, f.getEffectInStew())),
                Codec.INT.fieldOf("duration").forGetter(FlowerBlock::getEffectInStewDuration),
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, (id, d, set) -> new FlowerBlock(Util.getOrThrow(Registry.STATUS_EFFECT, id), d, set)));

        public static final Codec<FlowerPotBlock> FLOWER_POT = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("contents").forGetter(f -> Util.getIdOrThrow(Registry.BLOCK, f.getContent())),
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, (id, set) -> new FlowerPotBlock(Util.getOrThrow(Registry.BLOCK, id), set)));

        public static final Codec<CropBlock> CROP = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings()),
                Codec.INT.optionalFieldOf("max_age").forGetter(c -> Util.getOrEmpty(c.getMaxAge()))
        ).apply(inst, (set, age) -> new CropBlock(set) {
            @Override
            public int getMaxAge() {
                return age.orElseGet(super::getMaxAge);
            }
        }));

        public static final Codec<LadderBlock> LADDER = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, set -> new LadderBlock(set){}));

        public static final Codec<StairsBlock> STAIRS = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings()),
                Identifier.CODEC.fieldOf("base_state").forGetter(b -> Util.getIdOrThrow(Registry.BLOCK, ((StairsBlockAccessor)b).accessor$getBlock()))
        ).apply(inst, (set, base) -> new StairsBlock(Util.getOrThrow(Registry.BLOCK, base).getDefaultState(), set){}));

        public static final Codec<SlabBlock> SLAB = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, SlabBlock::new));

        public static final Codec<DoorBlock> DOOR = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, (set) -> new DoorBlock(set){}));

        public static final Codec<TrapdoorBlock> TRAPDOOR = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, set -> new TrapdoorBlock(set){}));

        public static final Codec<PressurePlateBlock> PRESSURE_PLATE = RecordCodecBuilder.create(inst -> inst.group(
                ACTIVATION_RULE.fieldOf("activation_rule").forGetter(p -> ((PressurePlateBlockAccessor)p).accessor$getRule()),
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings()),
                Codec.INT.optionalFieldOf("power_output").forGetter(b -> Optional.empty())
        ).apply(inst, (rule, set, power) -> new PressurePlateBlock(rule, set) {
            @Override
            protected int getRedstoneOutput(BlockState state) {
                if (power.isPresent() && state.get(PressurePlateBlock.POWERED)) {
                    return power.get();
                }
                return super.getRedstoneOutput(state);
            }
        }));

        @SuppressWarnings("ConstantConditions")
        public static final Codec<WoodenButtonBlock> BUTTON = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings()),
                SoundEvent.CODEC.optionalFieldOf("sound").forGetter(b -> Optional.empty()),
                Codec.BOOL.fieldOf("is_wooden").orElse(true).forGetter(b -> true)
        ).apply(inst, (set, sound, wooden) -> {
            WoodenButtonBlock button = new WoodenButtonBlock(set) {
            @Override
            protected SoundEvent getClickSound(boolean powered) {
                return sound.orElse(super.getClickSound(powered));
            }
        };
            ((AbstractButtonAccessor)button).accessor$setIsWooden(wooden);
            return button;
        }));

        public static final Codec<HorizontalFacingBlock> HORIZONTAL_FACING = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, set -> new HorizontalFacingBlock(set) {
            @Override
            protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
                super.appendProperties(builder);
                builder.add(FACING);
            }
        }));

        public static final Codec<CakeBlock> CAKE = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, set -> new CakeBlock(set){}));

        public static final Codec<GlassBlock> GLASS = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, GlassBlock::new));

        public static final Codec<PaneBlock> PANE = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, set -> new PaneBlock(set){}));

        public static final Codec<FenceBlock> FENCE = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, FenceBlock::new));

        public static final Codec<FenceGateBlock> FENCE_GATE = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, FenceGateBlock::new));

        public static final Codec<WallBlock> WALL = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, WallBlock::new));

        public static final Codec<CarpetBlock> CARPET = RecordCodecBuilder.create(inst -> inst.group(
                DYE_COLOR.fieldOf("color").forGetter(CarpetBlock::getColor),
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, (dye, set) -> new CarpetBlock(dye, set){}));

        public static final Codec<FluidBlock> FLUID_BLOCK = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("fluid").forGetter(f -> new Identifier("Later")),
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings())
        ).apply(inst, (f, set) -> {
            FlowableFluid fluid;
            try {
                fluid = (FlowableFluid) Util.getOrThrow(Registry.FLUID, f);
            } catch (IllegalStateException e) {
                fluid = (FlowableFluid) Util.getOrThrow(Registry.FLUID, new Identifier(f.getNamespace(), "still_"+f.getPath()));
            }
            return new FluidBlock(fluid, set){};
        }));

        public static final Codec<BlockWithEntity> BLOCK_ENTITY = RecordCodecBuilder.create(inst -> inst.group(
                Codecs.BLOCK_SETTINGS.fieldOf("settings").forGetter(b -> ((BlockDuck)b).getSettings()),
                Identifier.CODEC.fieldOf("fluid").forGetter(f -> new Identifier("Later"))
                ).apply(inst, (settings, be) -> new BlockWithEntity(settings) {
            @Nullable
            @Override
            public net.minecraft.block.entity.BlockEntity createBlockEntity(BlockView world) {
                return Util.getOrThrow(Registries.BLOCK_ENTITIES, be);
            }

            @Override
            public BlockRenderType getRenderType(BlockState state) {
                return BlockRenderType.MODEL;
            }
        }));
    }

    public static class Fluids {
        public static final Codec<Pair<Fluid, Fluid>> FLUIDS = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("block").forGetter(p -> new Identifier("WHY")),
                Codec.FLOAT.fieldOf("blast_resistance").orElse(100.0F).forGetter(p -> 1.0F),
                Codec.INT.fieldOf("tick_rate").orElse(5).forGetter(p -> 1),
                Codec.BOOL.fieldOf("can_have_infinite_source").orElse(false).forGetter(b -> false),
                Codec.INT.fieldOf("flow_speed").orElse(4).forGetter(p -> 1),
                Codec.INT.fieldOf("decrease_per_block").orElse(1).forGetter(p -> 1),
                Identifier.CODEC.fieldOf("bucket").forGetter(p -> new Identifier("WHY")),
                Identifier.CODEC.fieldOf("flowing_texture").orElse(new Identifier("block/water_flow")).forGetter(p -> new Identifier("WHY")),
                Identifier.CODEC.fieldOf("still_texture").orElse(new Identifier("block/water_still")).forGetter(p -> new Identifier("WHY")),
                Codec.INT.optionalFieldOf("tint").forGetter(p -> Optional.empty())
        ).apply(inst, (bl, br, tr, chis, fs, dpb, bu, ft, st, t) -> {
            Pair<Fluid, Fluid> pair = DataDrivenFluid.create(bl, br, tr, chis, fs, dpb, bu);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                // b/c flowing is the first of the pair
                setupFluidRendering(pair.getSecond(), pair.getFirst(), ft, st, t.orElse(0));
            }
            return pair;
        }));
    }

    @SuppressWarnings("deprecation")
    private static void setupFluidRendering(final Fluid still, final Fluid flowing, final Identifier flowingSpriteId, final Identifier stillSpriteId, final int color) {

        // If they're not already present, add the sprites to the block atlas
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(stillSpriteId);
            registry.register(flowingSpriteId);
        });

        final Function<Identifier, Sprite> atlasOriginal = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);

        final Sprite[] fluidSprites = {atlasOriginal.apply(stillSpriteId), atlasOriginal.apply(flowingSpriteId)};

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("datadriver", "fluid_renderer"+this.hashCode());
            }

            /**
             * Get the sprites from the block atlas when resources are reloaded
             */
            @Override
            public void apply(ResourceManager resourceManager) {
                final Function<Identifier, Sprite> atlas = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
                fluidSprites[0] = atlas.apply(stillSpriteId);
                fluidSprites[1] = atlas.apply(flowingSpriteId);
            }
        });

        // The FluidRenderer gets the sprites and color from a FluidRenderHandler during rendering
        final FluidRenderHandler renderHandler = new FluidRenderHandler() {
            @Override
            public Sprite[] getFluidSprites(BlockRenderView view, BlockPos pos, FluidState state) {
                return fluidSprites;
            }

            @Override
            public int getFluidColor(BlockRenderView view, BlockPos pos, FluidState state) {
                return color;
            }
        };

        FluidRenderHandlerRegistry.INSTANCE.register(still, renderHandler);
        FluidRenderHandlerRegistry.INSTANCE.register(flowing, renderHandler);
    }

    public static final Codec<Potion> POTION = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("name").forGetter(p -> Optional.ofNullable(p.finishTranslationKey(""))),
            STATUS_EFFECT_INSTANCE.listOf().fieldOf("effects").forGetter(Potion::getEffects)
    ).apply(inst, (name, effects) -> {
        StatusEffectInstance[] effs = new StatusEffectInstance[effects.size()];
        for (int i = 0; i < effects.size(); i++) {
            effs[i] = effects.get(i);
        }
        return new Potion(name.orElse(null), effs);
    }));

    public static final Codec<PaintingMotive> PAINTING = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("height").orElse(16).forGetter(PaintingMotive::getHeight),
            Codec.INT.fieldOf("width").orElse(16).forGetter(PaintingMotive::getWidth)
    ).apply(inst, PaintingMotive::new));

    public static class BlockEntity {
        public static final Codec<InventoryBlockEntity> INVENTORY_BLOCK_ENTITY = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("name").forGetter(p -> new Identifier("no")),
                Identifier.CODEC.listOf().fieldOf("blocks").forGetter(p -> new ArrayList<>()),
                Codec.INT.fieldOf("size").forGetter(InventoryBlockEntity::size),
                Codec.INT.fieldOf("max_stack_size").orElse(64).forGetter(InventoryBlockEntity::getMaxCountPerStack)
        ).apply(inst, InventoryBlockEntity::create));
    }

    private static final Map<String, DamageSource> damageSrcMap = new HashMap<>();

    //TODO: Make sure remapping works
    static {
        for (Field field : DamageSource.class.getFields()) {
            if (field.getType().equals(DamageSource.class)) {
                try {
                    damageSrcMap.put(remapper.mapFieldName("intermediary", remapper.mapClassName("intermediary", DamageSource.class.getName()), field.getName(), "L"+remapper.mapClassName("intermediary", DamageSource.class.getName())+";").toLowerCase(Locale.ROOT), (DamageSource) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static final Codec<DamageSource> DAMAGE_SOURCE = Codec.STRING.comapFlatMap((s) -> DataResult.success(damageSrcMap.get(s)), DamageSource::getName);

    private static final Map<String, EntityAttribute> modifierMap = new HashMap<>();

    static {
        for (Field field : EntityAttributes.class.getFields()) {
            if (field.getType().equals(EntityAttribute.class)) {
                try {
                    modifierMap.put(remapper.mapFieldName("intermediary", remapper.mapClassName("intermediary", DamageSource.class.getName()), field.getName(), "L"+remapper.mapClassName("intermediary", DamageSource.class.getName())+";").toLowerCase(Locale.ROOT), (EntityAttribute) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static final Codec<EntityAttribute> ENTITY_ATTRIBUTE = Codec.STRING.comapFlatMap(s -> DataResult.success(modifierMap.get(s)), ea -> ea.getTranslationKey().substring(8));

    public static final Codec<EntityAttributeModifier.Operation> ATTRIBUTE_OPERATION = Util.createEnumCodec(EntityAttributeModifier.Operation.class);

    public static final Codec<StatusEffectType> EFFECT_TYPE = Util.createEnumCodec(StatusEffectType.class);

    public static final Codec<StatusEffect> EFFECT = RecordCodecBuilder.create(inst -> inst.group(
        EFFECT_TYPE.fieldOf("type").orElse(StatusEffectType.NEUTRAL).forGetter(StatusEffect::getType),
        Codec.INT.fieldOf("color").forGetter(StatusEffect::getColor),
        Codec.BOOL.fieldOf("is_instant").orElse(true).forGetter(StatusEffect::isInstant),
        EffectUpdate.CODEC.listOf().fieldOf("duration_effects").forGetter(s -> new ArrayList<>()),
        EffectUpdate.CODEC.listOf().fieldOf("instant_effects").forGetter(s -> new ArrayList<>()),
        AttributeModifier.CODEC.listOf().fieldOf("modifiers").forGetter(s -> new ArrayList<>())
    ).apply(inst, (type, color, instant, longTermUpdates, instantUpdates, modifiers) -> {
        StatusEffect effect = new StatusEffect(type, color) {
            @Override
            public void applyUpdateEffect(LivingEntity entity, int amplifier) {
                super.applyUpdateEffect(entity, amplifier);
                for (EffectUpdate eu : longTermUpdates) {
                    eu.applyEffect(entity, amplifier);
                }
            }

            @Override
            public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
                super.applyInstantEffect(source, attacker, target, amplifier, proximity);
                for (EffectUpdate eu : instantUpdates) {
                    eu.applyEffect(target, amplifier);
                }
            }

            @Override
            public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
                super.onRemoved(entity, attributes, amplifier);
                if (entity.getHealth() > entity.getMaxHealth()) {
                    entity.setHealth(entity.getMaxHealth());
                }
            }
            //@Override
            //public boolean canApplyUpdateEffect(int duration, int amplifier) {
            //    return super.canApplyUpdateEffect(duration, amplifier);
            //}

            @Override
            public boolean isInstant() {
                return instant;
            }
        };
        for (AttributeModifier mod : modifiers) {
            mod.apply(effect);
        }
        return effect;
    }));

}
