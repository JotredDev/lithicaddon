package com.redstoneguy10ls.lithicaddon.common.capabilities.moth;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MothHandler implements IMoth, ICapabilitySerializable<CompoundTag> {

    private final LazyOptional<IMoth> capability;
    private final ItemStack stack;
    private boolean hasLarva;
    private boolean hasCocoon;
    private boolean isMoth;
    private int[] abilities;

    private int daysAlive = 0;
    private boolean initialized = false;


    public MothHandler(ItemStack stack)
    {
        capability = LazyOptional.of(() -> this);
        hasLarva = false;
        isMoth = false;
        this.stack = stack;
        hasCocoon = false;
        this.abilities = new int[MothAbility.SIZE];

    }

    @Override
    public int daysAlive() {
        return daysAlive;
    }
    @Override
    public boolean isMoth()
    {
        return isMoth;
    }

    @Override
    public void setDaysAlive(int value) {
        daysAlive = value;
        save();
    }

    @Override
    public void setHasCocoon(boolean exists) {
        hasCocoon = exists;
        save();
    }

    @Override
    public void setIsMoth(boolean exists) {
        isMoth = exists;
        save();
    }


    @Override
    public int[] getAbilityMap() {
        return abilities;
    }

    @Override
    public void setAbilities(int[] abilities) {
        this.abilities = abilities;
        save();
    }

    @Override
    public void setAbility(MothAbility ability, int value) {
        abilities[ability.ordinal()] = Mth.clamp(value, 1, 10);
        save();
    }

    @Override
    public boolean hasCocoon() {
        return hasCocoon;
    }

    @Override
    public void setHasLarva(boolean exists) {
        hasLarva = exists;
        save();
    }

    @Override
    public boolean hasLarva() {
        return hasLarva;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == MothCapability.CAPABILITY)
        {
            load();
            return capability.cast();
        }
        return LazyOptional.empty();
    }

    private void load()
    {
        if(!initialized)
        {
            initialized = true;

            final CompoundTag tag = stack.getOrCreateTag();
            if(tag.contains("larva"))
            {
                hasCocoon = tag.getBoolean("cocoon");
                hasLarva = tag.getBoolean("larva");
                daysAlive = tag.getInt("daysAlive");
                abilities = tag.getIntArray("abilities");
                isMoth = tag.getBoolean("isMoth");
            }
        }
    }

    private void save()
    {
        final CompoundTag tag = stack.getOrCreateTag();
        tag.putIntArray("abilities", abilities);
        tag.putBoolean("larva", hasLarva);
        tag.putBoolean("cocoon", hasCocoon);
        tag.putInt("daysAlive", daysAlive);
        tag.putBoolean("isMoth",isMoth);
    }
/*
    @Override
    public void addTooltipInfo(ItemStack stack, List<Component> tooltip)
    {
        if(hasCocoon())
        {
            tooltip.add(Component.translatable("lithic.moth.cocoon").withStyle(ChatFormatting.GOLD));

        }
        else {
            tooltip.add(Component.translatable("lithic.moth.till_cocoon", String.valueOf(DAYS_TILL_COCOON -daysAlive()) ).withStyle(ChatFormatting.WHITE));

        }
    }

 */

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
