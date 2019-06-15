package io.github.upcraftlp.randomlootcrates;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.impl.LootCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class LootCrateCommand {

    public static final String NBT_LOOT_TABLE = "rlc_loot_table";
    public static final String NBT_PLACE_LOOT_BOX = "rlc_place_loot_box";

    //syntax: /lootcrate <loot_table> [loot_box|loot_bag]; defaults to loot_bag
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("lootcrate")
                .requires(source -> source.hasPermissionLevel(2))
                .then(Commands.argument("loot_table", ResourceLocationArgument.resourceLocation()).suggests(LootCommand.field_218904_a)
                        .executes(context -> execute(context, false))
                        .then(Commands.literal("loot_box")
                                .executes(context -> execute(context, true)))
                        .then(Commands.literal("loot_bag")
                                .executes(context -> execute(context, false)))
                )
        );
    }

    private static int execute(CommandContext<CommandSource> context, boolean isLootBox) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity = context.getSource().asPlayer();
        ItemStack stack = playerEntity.getHeldItemMainhand();
        if(stack.isEmpty()) stack = new ItemStack(Blocks.CHEST);
        ResourceLocation tableID = ResourceLocationArgument.getResourceLocation(context, "loot_table");
        stack.setDisplayName(new TranslationTextComponent(isLootBox ? "Loot Box" : "Loot Bag")); //this is a hack to make it work server-only, but still provide localization support if installed on the client
        stack.getOrCreateTag().putString(NBT_LOOT_TABLE, tableID.toString());
        stack.getOrCreateTag().putBoolean(NBT_PLACE_LOOT_BOX, isLootBox);
        playerEntity.setHeldItem(Hand.MAIN_HAND, stack);
        return 1;
    }
}
