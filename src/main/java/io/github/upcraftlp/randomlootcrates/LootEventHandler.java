package io.github.upcraftlp.randomlootcrates;

import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

@Mod.EventBusSubscriber(modid = LootCrates.MODID)
public class LootEventHandler {

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {
        LootCrateCommand.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if(stack.hasTag()) {
            CompoundNBT nbt = stack.getOrCreateTag();
            if(nbt.contains(LootCrateCommand.NBT_LOOT_TABLE, Constants.NBT.TAG_STRING)) {
                if(!nbt.getBoolean(LootCrateCommand.NBT_PLACE_LOOT_BOX)) {
                    if(event.getSide() == LogicalSide.SERVER) {
                        ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityPlayer();
                        ServerWorld world = player.getServerWorld();
                        openLootContainer(player, stack, world, nbt, false, player.getPosition());
                    }
                }
                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.SUCCESS);
            }
        }
    }

    private static void openLootContainer(ServerPlayerEntity player, ItemStack stack, ServerWorld world, CompoundNBT nbt, boolean place, BlockPos targetPos) {
        ResourceLocation lootTableID = new ResourceLocation(nbt.getString(LootCrateCommand.NBT_LOOT_TABLE));
        if(!place) {
            LootTable table = world.getServer().getLootTableManager().getLootTableFromLocation(lootTableID);
            LootContext lootContext = new LootContext.Builder(world)
                    .withParameter(LootParameters.field_216281_a, player)
                    .withParameter(LootParameters.field_216286_f, targetPos)
                    .build(LootParameterSets.field_216264_e); //"gift" -> required: pos, entity
            List<ItemStack> loot = table.func_216113_a(lootContext);
            loot.forEach(lootStack -> ItemHandlerHelper.giveItemToPlayer(player, lootStack));
        }
        else {
            if(world.isAirBlock(targetPos)) {
                world.setBlockState(targetPos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, player.getHorizontalFacing().getOpposite()));
                LockableLootTileEntity.setLootTable(world, player.getRNG(), targetPos, lootTableID);
            }
            else {
                return; //do not shrink the stack if placement was not successful
            }
        }
        if(!player.isCreative()) stack.shrink(1);
    }

    @SubscribeEvent
    public static void onPlaceItem(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if(stack.hasTag()) {
            CompoundNBT nbt = stack.getOrCreateTag();
            if(nbt.contains(LootCrateCommand.NBT_LOOT_TABLE, Constants.NBT.TAG_STRING)) {
                if(event.getSide() == LogicalSide.SERVER) {
                    ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityPlayer();
                    ServerWorld world = player.getServerWorld();
                    openLootContainer(player, stack, world, nbt, nbt.getBoolean(LootCrateCommand.NBT_PLACE_LOOT_BOX), event.getPos().offset(event.getFace()));
                }
                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.SUCCESS);
            }
        }
    }
}
