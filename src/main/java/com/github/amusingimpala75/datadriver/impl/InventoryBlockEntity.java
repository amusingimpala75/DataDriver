package com.github.amusingimpala75.datadriver.impl;

import com.github.amusingimpala75.datadriver.api.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class InventoryBlockEntity extends BlockEntity implements Inventory {

    private int size;
    private DefaultedList<ItemStack> inventory;
    private int maxStackSize;

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        Inventories.fromTag(tag, inventory);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        Inventories.toTag(tag, inventory);
        return tag;
    }

    public InventoryBlockEntity() {
        super(BlockEntityType.BANNER);
    }

    public InventoryBlockEntity(Identifier blockEntityType, int size, int maxStackSize) {
        super(Util.getOrThrow(Registry.BLOCK_ENTITY_TYPE, blockEntityType));
        this.size = size;
        this.inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);
        this.maxStackSize = maxStackSize;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack itemStack = this.inventory.get(slot);
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
        this.markDirty();
    }

    @Override
    public int getMaxCountPerStack() {
        return this.maxStackSize;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    public static InventoryBlockEntity create(Identifier name, List<Identifier> blockNames, int size, int maxStackSize) {

        Block[] blocks = new Block[blockNames.size()];

        for (int i = 0; i < blockNames.size(); i++) {
            blocks[i] = Util.getOrThrow(Registry.BLOCK, blockNames.get(i));
        }

        Registry.register(Registry.BLOCK_ENTITY_TYPE, name, BlockEntityType.Builder.create(InventoryBlockEntity::new, blocks).build(null));

        return new InventoryBlockEntity(name, size, maxStackSize);
    }
}
