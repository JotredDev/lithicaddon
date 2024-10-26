package com.redstoneguy10ls.lithicaddon.common.blocks;

import com.redstoneguy10ls.lithicaddon.common.blockentities.LithicBlockEntities;
import com.redstoneguy10ls.lithicaddon.common.blockentities.MothBlockEntity;
import com.redstoneguy10ls.lithicaddon.common.capabilities.moth.IMoth;
import com.redstoneguy10ls.lithicaddon.common.capabilities.moth.MothAbility;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public class MothboxBlock extends DeviceBlock implements HoeOverlayBlock {

    public static final BooleanProperty LARVA = LithicStateProperties.LARVA;

    public MothboxBlock(ExtendedProperties properties)
    {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(getStateDefinition().any().setValue(LARVA, false));

    }
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        if(!player.isShiftKeyDown())
        {
            if (player instanceof ServerPlayer serverPlayer)
            {
                level.getBlockEntity(pos, LithicBlockEntities.MOTHBOX.get()).ifPresent(box -> Helpers.openScreen(serverPlayer, box,pos));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);

        }
        return InteractionResult.PASS;

    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand)
    {
        level.getBlockEntity(pos, LithicBlockEntities.MOTHBOX.get()).ifPresent(MothBlockEntity::tryPeriodicUpdate);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(LARVA));
    }

    @Override
    public void addHoeOverlayInfo(Level level, BlockPos pos, BlockState blockState, List<Component> text, boolean debug)
    {

        level.getBlockEntity(pos, LithicBlockEntities.MOTHBOX.get()).ifPresent(box -> {
            box.setLeaves(box.calculateLeaves());
            if(box.getLeaves() > 0)
            {

                text.add(Component.translatable("lithic.moth.leaves",String.valueOf(box.getLeaves())).withStyle(ChatFormatting.GREEN));
            }
            else
            {
                text.add(Component.translatable("lithic.moth.no_leaves").withStyle(ChatFormatting.RED));
            }
            final float temp = Climate.getTemperature(level,pos);
            final float rain = Climate.getRainfall(level,pos);
            int ord = 0;
            final List<IMoth> moths = new ArrayList<>();
            int noLarva = 0;
            for(IMoth moth : box.getCachedMoths())
            {
                if(ord > 5)
                {
                    ord++;
                    continue;
                }
                if(moth == null || !moth.hasLarva())
                {
                    noLarva++;
                }
                MutableComponent mothText = Component.translatable("lithic.moth.lattices", (ord)+1);
                ord++;
                if(moth != null && moth.hasLarva())
                {
                    int bonus = MothAbility.getTimeBonus(moth.getAbility(MothAbility.FASTING),moth.getAbility(MothAbility.HUNGER));
                    if(moth.isMoth())
                    {
                        mothText.append(Component.translatable("lithic.moth.moth").withStyle(ChatFormatting.GOLD));
                    } else if (moth.hasCocoon())
                    {
                        mothText.append(Component.translatable("lithic.moth.cocoon").withStyle(ChatFormatting.GOLD));
                        mothText.append(Component.translatable("lithic.moth.till_moth", String.valueOf(moth.getDaysTillMoth()+bonus - moth.daysAlive()+1)).withStyle(ChatFormatting.WHITE));
                    }
                    else {
                        mothText.append(Component.translatable("lithic.moth.larva").withStyle(ChatFormatting.GOLD));
                        mothText.append(Component.translatable("lithic.moth.till_cocoon", String.valueOf(moth.getDaysTillCocoon()+bonus -moth.daysAlive()+1) ).withStyle(ChatFormatting.WHITE));
                    }

                    //temp and rain
                    if(temp > box.MAX_TEMP+MothAbility.getMaxTemperature(moth.getAbility(MothAbility.HARDINESS)))
                    {
                        mothText.append(Component.translatable("lithic.moth.too_hot",
                                box.MAX_TEMP+MothAbility.getMaxTemperature(moth.getAbility(MothAbility.HARDINESS)),
                                String.format("%.2f", temp)).withStyle(ChatFormatting.RED));
                    }
                    if(temp < box.MIN_TEMP +MothAbility.getMinTemperature(moth.getAbility(MothAbility.HARDINESS)))
                    {
                        mothText.append(Component.translatable("lithic.moth.too_cold",
                                box.MIN_TEMP+MothAbility.getMinTemperature(moth.getAbility(MothAbility.HARDINESS)),
                                String.format("%.2f", temp)).withStyle(ChatFormatting.AQUA));
                    }
                    if(rain > box.MAX_RAIN+MothAbility.getMaxRainfall(moth.getAbility(MothAbility.HARDINESS)))
                    {
                        mothText.append(Component.translatable("lithic.moth.too_wet",
                                box.MAX_RAIN+MothAbility.getMaxRainfall(moth.getAbility(MothAbility.HARDINESS)),
                                String.format("%.2f", rain)).withStyle(ChatFormatting.BLUE));
                    }
                    if(rain < box.MIN_RAIN+MothAbility.getMinRainfall(moth.getAbility(MothAbility.HARDINESS)))
                    {
                        mothText.append(Component.translatable("lithic.moth.too_dry",
                                box.MIN_RAIN+MothAbility.getMinRainfall(moth.getAbility(MothAbility.HARDINESS)),
                                String.format("%.2f", rain)).withStyle(ChatFormatting.YELLOW));
                    }
                    text.add(mothText);
                }

                //lights

            }

            if(noLarva >= 4)
            {
                if(temp > box.MAX_TEMP)
                {
                    text.add(Component.translatable("lithic.moth.too_hot", box.MAX_TEMP, String.format("%.2f", temp)).withStyle(ChatFormatting.RED));
                }
                if(temp < box.MIN_TEMP)
                {
                    text.add(Component.translatable("lithic.moth.too_cold", box.MIN_TEMP, String.format("%.2f", temp)).withStyle(ChatFormatting.AQUA));
                }
                if(rain > box.MAX_RAIN)
                {
                    text.add(Component.translatable("lithic.moth.too_wet", box.MAX_RAIN, String.format("%.2f", rain)).withStyle(ChatFormatting.BLUE));
                }
                if(rain < box.MIN_RAIN)
                {
                    text.add(Component.translatable("lithic.moth.too_dry", box.MIN_RAIN, String.format("%.2f", rain)).withStyle(ChatFormatting.YELLOW));
                }
            }
            final List<IMoth> usableMoths = box.getUsableMoths(temp,rain);
            final int lights = box.getLight();
            text.add(Component.translatable("lithic.moth.lights",  lights));
            //checks how many light blocks are around
            if(lights < MothBlockEntity.MIN_LIGHTS)
            {
                    text.add(Component.translatable("lithic.moth.min_lights"));

            }
            else
            {
                if(moths.size() < 5) {
                    if(box.calculateLeaves() > 0){
                            int breed = box.getBreedTickChanceInverted(usableMoths,lights);
                            if (breed == 0) text.add(Component.translatable("lithic.moth.larva_chance_100"));
                            else text.add(Component.translatable("lithic.moth.larva_chance", breed));
                    }
                }
            }


        });
    }



}
/*
                if((temp <= box.MAX_TEMP && temp >= box.MIN_TEMP) && (rain <= box.MAX_RAIN && rain >= box.MIN_RAIN))
                {
                    //if has larva
                    if (moth != null && moth.hasLarva()) {
                        mothText.append(Component.translatable("lithic.moth.has_larva"));
                        if (!moth.hasCocoon()) {
                            mothText.append(Component.translatable("lithic.moth.till_cocoon",
                                    String.valueOf(moth.getDaysTillCocoon() - moth.daysAlive())).withStyle(ChatFormatting.WHITE));
                        } else {
                            mothText.append(Component.translatable("lithic.moth.cocoon").withStyle(ChatFormatting.GOLD));
                        }
                    }
                    else {
                        if(moth != null) noLarva++;
                        mothText.append(Component.translatable("lithic.moth.empty"));
                    }
                    text.add(mothText);

                }
                else {
                    if(temp > box.MAX_TEMP)
                    {
                        mothText.append(Component.translatable("lithic.moth.too_hot", box.MAX_TEMP, String.format("%.2f", temp)).withStyle(ChatFormatting.RED));
                    }
                    if(temp < box.MIN_TEMP)
                    {
                        mothText.append(Component.translatable("lithic.moth.too_cold", box.MIN_TEMP, String.format("%.2f", temp)).withStyle(ChatFormatting.AQUA));
                    }
                    if(rain > box.MAX_RAIN)
                    {
                        mothText.append(Component.translatable("lithic.moth.too_wet", box.MAX_RAIN, String.format("%.2f", rain)).withStyle(ChatFormatting.BLUE));
                    }
                    if(rain < box.MIN_RAIN)
                    {
                        mothText.append(Component.translatable("lithic.moth.too_dry", box.MIN_RAIN, String.format("%.2f", rain)).withStyle(ChatFormatting.YELLOW));
                    }
                    text.add(mothText);


                }
 */