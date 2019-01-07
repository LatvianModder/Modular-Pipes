package com.latmod.modularpipes.item;

import com.latmod.modularpipes.ModularPipes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author LatvianModder
 */
@GameRegistry.ObjectHolder(ModularPipes.MOD_ID)
@Mod.EventBusSubscriber(modid = ModularPipes.MOD_ID)
public class ModularPipesItems
{
	public static final Item CONTROLLER = Items.AIR;
	public static final Item PIPE_TRANSPORT = Items.AIR;
	public static final Item PIPE_MODULAR_MK1 = Items.AIR;
	public static final Item PIPE_MODULAR_MK2 = Items.AIR;
	public static final Item PIPE_MODULAR_MK3 = Items.AIR;

	public static final Item PAINTER = Items.AIR;
	public static final Item MODULE = Items.AIR;
	public static final Item MODULE_EXTRACT = Items.AIR;
	public static final Item MODULE_RIGHTCLICK_EXTRACT = Items.AIR;
	public static final Item MODULE_CRAFTING = Items.AIR;
	public static final Item MODULE_ITEM_STORAGE = Items.AIR;
	public static final Item MODULE_FLUID_STORAGE = Items.AIR;
}