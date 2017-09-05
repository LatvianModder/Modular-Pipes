package com.latmod.modularpipes.item;

import com.feed_the_beast.ftbl.lib.block.ItemBlockBase;
import com.feed_the_beast.ftbl.lib.client.ClientUtils;
import com.latmod.modularpipes.ModularPipes;
import com.latmod.modularpipes.block.BlockModularPipe;
import com.latmod.modularpipes.block.BlockPipeBasic;
import com.latmod.modularpipes.block.BlockPipeBasicNode;
import com.latmod.modularpipes.block.EnumTier;
import com.latmod.modularpipes.client.RenderModularPipe;
import com.latmod.modularpipes.item.module.ItemModuleExtract;
import com.latmod.modularpipes.item.module.ItemModuleRightClickExtract;
import com.latmod.modularpipes.item.module.ModuleCrafting;
import com.latmod.modularpipes.tile.TileModularPipe;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
@GameRegistry.ObjectHolder(ModularPipes.MOD_ID)
@Mod.EventBusSubscriber(modid = ModularPipes.MOD_ID)
public class ModularPipesItems
{
	public static final Block PIPE_BASIC = Blocks.AIR;
	public static final Block PIPE_MODULAR_BASIC = Blocks.AIR;
	public static final Block PIPE_MODULAR_IRON = Blocks.AIR;
	public static final Block PIPE_MODULAR_GOLD = Blocks.AIR;
	public static final Block PIPE_MODULAR_QUARTZ = Blocks.AIR;
	public static final Block PIPE_MODULAR_LAPIS = Blocks.AIR;
	public static final Block PIPE_MODULAR_ENDER = Blocks.AIR;
	public static final Block PIPE_MODULAR_EMERALD = Blocks.AIR;
	public static final Block PIPE_MODULAR_STAR = Blocks.AIR;
	public static final Block PIPE_NODE = Blocks.AIR;

	public static final Item MODULE = Items.AIR;
	public static final Item DEBUG = Items.AIR;

	public static final Item MODULE_EXTRACT = Items.AIR;
	public static final Item MODULE_RIGHTCLICK_EXTRACT = Items.AIR;
	public static final Item MODULE_CRAFTING = Items.AIR;

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().registerAll(
				new BlockPipeBasic("pipe_basic", MapColor.GRAY),
				new BlockModularPipe("pipe_modular_basic", EnumTier.BASIC),
				new BlockModularPipe("pipe_modular_iron", EnumTier.IRON),
				new BlockModularPipe("pipe_modular_gold", EnumTier.GOLD),
				new BlockModularPipe("pipe_modular_quartz", EnumTier.QUARTZ),
				new BlockModularPipe("pipe_modular_lapis", EnumTier.LAPIS),
				new BlockModularPipe("pipe_modular_ender", EnumTier.ENDER),
				new BlockModularPipe("pipe_modular_emerald", EnumTier.EMERALD),
				new BlockModularPipe("pipe_modular_star", EnumTier.STAR),
				new BlockPipeBasicNode("pipe_node"));

		GameRegistry.registerTileEntity(TileModularPipe.class, ModularPipes.MOD_ID + ":pipe_modular");
	}

	@SubscribeEvent
	public static void registerItem(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(
				new ItemBlockBase(PIPE_BASIC),
				new ItemBlockBase(PIPE_MODULAR_BASIC),
				new ItemBlockBase(PIPE_MODULAR_IRON),
				new ItemBlockBase(PIPE_MODULAR_GOLD),
				new ItemBlockBase(PIPE_MODULAR_QUARTZ),
				new ItemBlockBase(PIPE_MODULAR_LAPIS),
				new ItemBlockBase(PIPE_MODULAR_ENDER),
				new ItemBlockBase(PIPE_MODULAR_EMERALD),
				new ItemBlockBase(PIPE_MODULAR_STAR),
				new ItemBlockBase(PIPE_NODE, true),
				new ItemMPBase("module"),
				new ItemDebug("debug"),
				new ItemModuleExtract("module_extract"),
				new ItemModuleRightClickExtract("module_rightclick_extract"),
				new ModuleCrafting("module_crafting"));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void registerModels(ModelRegistryEvent event)
	{
		ClientUtils.registerModel(PIPE_BASIC, 0, ModularPipes.MOD_ID + ":pipe_item#variant=basic");

		registerModularPipe(PIPE_MODULAR_BASIC);
		registerModularPipe(PIPE_MODULAR_IRON);
		registerModularPipe(PIPE_MODULAR_GOLD);
		registerModularPipe(PIPE_MODULAR_QUARTZ);
		registerModularPipe(PIPE_MODULAR_LAPIS);
		registerModularPipe(PIPE_MODULAR_ENDER);
		registerModularPipe(PIPE_MODULAR_EMERALD);
		registerModularPipe(PIPE_MODULAR_STAR);

		ClientUtils.registerModel(PIPE_NODE, 0, PIPE_BASIC.getRegistryName() + "#model=none");
		registerModularPipe(PIPE_NODE);

		ClientUtils.registerModel(MODULE);
		ClientUtils.registerModel(DEBUG);

		ClientUtils.registerModel(MODULE_EXTRACT);
		ClientUtils.registerModel(MODULE_RIGHTCLICK_EXTRACT);
		ClientUtils.registerModel(MODULE_CRAFTING);

		ClientRegistry.bindTileEntitySpecialRenderer(TileModularPipe.class, new RenderModularPipe());
	}

	@SideOnly(Side.CLIENT)
	private static void registerModularPipe(Block block)
	{
		if (block instanceof BlockModularPipe)
		{
			ClientUtils.registerModel(block, 0, ModularPipes.MOD_ID + ":pipe_item#variant=modular_" + ((BlockModularPipe) block).tier.getName());
		}

		ModelLoader.setCustomStateMapper(block, new DefaultStateMapper()
		{
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state)
			{
				return new ModelResourceLocation("modularpipes:pipe_modular#" + getPropertyString(state.getProperties()));
			}
		});
	}
}