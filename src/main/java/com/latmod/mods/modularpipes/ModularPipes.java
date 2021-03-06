package com.latmod.mods.modularpipes;

import com.latmod.mods.modularpipes.gui.ModularPipesGuiHandler;
import com.latmod.mods.modularpipes.item.ModularPipesItems;
import com.latmod.mods.modularpipes.item.module.PipeModule;
import com.latmod.mods.modularpipes.net.ModularPipesNet;
import com.latmod.mods.modularpipes.tile.PipeNetwork;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod(
		modid = ModularPipes.MOD_ID,
		name = ModularPipes.MOD_NAME,
		version = ModularPipes.VERSION,
		acceptedMinecraftVersions = "[1.12,)",
		dependencies = "required-after:itemfilters"
)
public class ModularPipes
{
	public static final String MOD_ID = "modularpipes";
	public static final String MOD_NAME = "Modular Pipes";
	public static final String VERSION = "0.0.0.modularpipes";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	@Mod.Instance(MOD_ID)
	public static ModularPipes INSTANCE;

	@SidedProxy(serverSide = "com.latmod.mods.modularpipes.ModularPipesCommon", clientSide = "com.latmod.mods.modularpipes.client.ModularPipesClient")
	public static ModularPipesCommon PROXY;

	public static final CreativeTabs TAB = new CreativeTabs(MOD_ID)
	{
		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(ModularPipesItems.PIPE_MODULAR_MK1);
		}
	};

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		ModularPipesConfig.sync();

		CapabilityManager.INSTANCE.register(PipeModule.class, new Capability.IStorage<PipeModule>()
		{
			@Nullable
			@Override
			public NBTBase writeNBT(Capability<PipeModule> capability, PipeModule instance, EnumFacing side)
			{
				return instance instanceof INBTSerializable ? ((INBTSerializable) instance).serializeNBT() : null;
			}

			@Override
			public void readNBT(Capability<PipeModule> capability, PipeModule instance, EnumFacing side, NBTBase nbt)
			{
				if (nbt != null && instance instanceof INBTSerializable)
				{
					((INBTSerializable) instance).deserializeNBT(nbt);
				}
			}
		}, () -> null);

		CapabilityManager.INSTANCE.register(PipeNetwork.class, new Capability.IStorage<PipeNetwork>()
		{
			@Nullable
			@Override
			public NBTBase writeNBT(Capability<PipeNetwork> capability, PipeNetwork instance, EnumFacing side)
			{
				return null;
			}

			@Override
			public void readNBT(Capability<PipeNetwork> capability, PipeNetwork instance, EnumFacing side, NBTBase nbt)
			{
			}
		}, () -> null);

		NetworkRegistry.INSTANCE.registerGuiHandler(this, ModularPipesGuiHandler.INSTANCE);
		ModularPipesNet.init();
	}
}