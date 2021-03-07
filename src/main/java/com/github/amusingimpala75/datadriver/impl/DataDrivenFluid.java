package com.github.amusingimpala75.datadriver.impl;

import com.github.amusingimpala75.datadriver.api.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class DataDrivenFluid extends FlowableFluid {

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid.equals(getFlowing()) || fluid.equals(getStill());
    }

    public final Identifier block;
    private final float blastRes;
    private final int tickRate;
    private final boolean isSource;
    private final int speed;
    private final int decreasePerBlock;
    private final Identifier bucket;

    public DataDrivenFluid(Identifier block, float blastRes, int tickRate, boolean isSource, int speed, int decreasePerBlock, Identifier bucket) {
        this.block = block;
        this.blastRes = blastRes;
        this.tickRate = tickRate;
        this.isSource = isSource;
        this.speed = speed;
        this.decreasePerBlock = decreasePerBlock;
        this.bucket = bucket;
    }

    @Override
    public Fluid getFlowing() {
        Identifier me = Util.getIdOrThrow(Registry.FLUID, this);
        if (me.getPath().contains("flowing_")) {
            return this;
        } else if (me.getPath().contains("still_")) {
            Identifier flow = new Identifier(me.getNamespace(), "flowing_"+me.getPath().substring(6));
            return Util.getOrThrow(Registry.FLUID, flow);
        } else throw new IllegalStateException("Could not figure out what fluid "+me.toString()+" is!");
    }

    @Override
    public Fluid getStill() {
        Identifier me = Util.getIdOrThrow(Registry.FLUID, this);
        if (me.getPath().contains("still_")) {
            return this;
        } else if (me.getPath().contains("flowing_")) {
            Identifier flow = new Identifier(me.getNamespace(), "still_"+me.getPath().substring(8));
            return Util.getOrThrow(Registry.FLUID, flow);
        } else throw new IllegalStateException("Could not figure out what fluid "+me.toString()+" is!");
    }

    @Override
    protected boolean isInfinite() {
        return this.isSource;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {

    }

    @Override
    protected int getFlowSpeed(WorldView world) {
        return this.speed;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return this.decreasePerBlock;
    }

    @Override
    public Item getBucketItem() {
        return Util.getOrThrow(Registry.ITEM, this.bucket);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public int getTickRate(WorldView world) {
        return this.tickRate;
    }

    @Override
    protected float getBlastResistance() {
        return this.blastRes;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return Util.getOrThrow(Registry.BLOCK, this.block).getDefaultState().with(Properties.LEVEL_15, method_15741(state));
    }

    public static Pair<Fluid, Fluid> create(Identifier block, float blastRes, int tickRate, boolean isSource, int speed, int decreasePerBlock, Identifier bucket) {
        Fluid flow = new Flowing(block, blastRes, tickRate, isSource, speed, decreasePerBlock, bucket);
        Fluid still = new Still(block, blastRes, tickRate, isSource, speed, decreasePerBlock, bucket);
        return new Pair<>(flow, still);
    }

    public static class Flowing extends DataDrivenFluid {

        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        public Flowing(Identifier block, float blastRes, int tickRate, boolean isSource, int speed, int decreasePerBlock, Identifier bucket) {
            super(block, blastRes, tickRate, isSource, speed, decreasePerBlock, bucket);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }
    }

    public static class Still extends DataDrivenFluid {

        public Still(Identifier block, float blastRes, int tickRate, boolean isSource, int speed, int decreasePerBlock, Identifier bucket) {
            super(block, blastRes, tickRate, isSource, speed, decreasePerBlock, bucket);
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }

        @Override
        public int getLevel(FluidState state) {
            return 8;
        }
    }
}
