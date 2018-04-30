package com.latmod.modularpipes.tile;

import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftblib.lib.util.LangKey;
import com.latmod.modularpipes.ModularPipesConfig;
import com.latmod.modularpipes.ModularPipesItems;
import com.latmod.modularpipes.data.IPipeBlock;
import com.latmod.modularpipes.data.Module;
import com.latmod.modularpipes.data.ModuleContainer;
import com.latmod.modularpipes.data.Node;
import com.latmod.modularpipes.data.PipeNetwork;
import com.latmod.modularpipes.data.TransportedItem;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileModularPipe extends TileBase implements IModularPipeNetworkTile
{
	public static final LangKey CANT_INSERT = LangKey.of("item.modularpipes.module.cant_insert");

	public ModularPipesConfig.Tier tier;
	private int connections = -1;
	public final ModuleContainer[] modules;
	private PipeNetwork network;
	private BlockPos controllerPos;
	private TileController cachedController;

	public TileModularPipe()
	{
		this(ModularPipesConfig.tiers.basic);
	}

	public TileModularPipe(ModularPipesConfig.Tier t)
	{
		tier = t;
		modules = new ModuleContainer[6];

		for (int i = 0; i < 6; i++)
		{
			modules[i] = new ModuleContainer(this, EnumFacing.VALUES[i], ItemStack.EMPTY);
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) ? (T) modules[facing.getIndex()] : super.getCapability(capability, facing);
	}

	public void clearModules()
	{
		for (int i = 0; i < 6; i++)
		{
			modules[i].setStack(ItemStack.EMPTY);
		}
	}

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		ModularPipesConfig.tiers.getNameMap().writeToNBT(nbt, "Tier", type, tier);

		if (type.save || connections != 0)
		{
			nbt.setByte("Connections", (byte) connections);
		}

		NBTTagList moduleList = new NBTTagList();

		for (ModuleContainer c : modules)
		{
			NBTTagCompound nbt1 = c.writeToNBT(type);

			if (!nbt1.hasNoTags())
			{
				moduleList.appendTag(nbt1);
			}
		}

		if (type.save || !moduleList.hasNoTags())
		{
			nbt.setTag("Modules", moduleList);
		}

		if (controllerPos != null)
		{
			nbt.setIntArray("Controller", new int[] {controllerPos.getX(), controllerPos.getY(), controllerPos.getZ()});
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		tier = ModularPipesConfig.tiers.getNameMap().readFromNBT(nbt, "Tier", type);
		connections = nbt.getByte("Connections") & 0xFF;

		clearModules();

		NBTTagList moduleList = nbt.getTagList("Modules", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < moduleList.tagCount(); i++)
		{
			ModuleContainer c = new ModuleContainer(this, moduleList.getCompoundTagAt(i), type);
			modules[c.facing.getIndex()] = c;
		}

		controllerPos = null;
		cachedController = null;

		if (nbt.hasKey("Controller"))
		{
			int[] ai = nbt.getIntArray("Controller");

			if (ai.length == 3)
			{
				controllerPos = new BlockPos(ai[0], ai[1], ai[2]);
			}
		}
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		connections = -1;
		cachedController = null;
	}

	@Override
	public void updateNetworkTile()
	{
		for (ModuleContainer c : modules)
		{
			c.update();
		}

		if (!world.isRemote)
		{
			getConnections();
		}

		checkIfDirty();
	}

	public void onRightClick(EntityPlayer playerIn, EnumHand hand)
	{
		if (world.isRemote)
		{
			return;
		}

		RayTraceResult ray = MathUtils.rayTrace(playerIn, false);

		if (ray == null)
		{
			return;
		}

		int facing = ray.subHit;

		if (facing >= 6 || facing < 0)
		{
			facing = ray.sideHit.getIndex();
		}

		ItemStack stack = playerIn.getHeldItem(hand);
		ModuleContainer c = modules[facing];

		if (stack.isEmpty() && playerIn.isSneaking())
		{
			if (!c.getItemStack().isEmpty())
			{
				c.getModule().removeFromPipe(c, playerIn);

				if (!c.getData().isEmpty())
				{
					NBTTagCompound nbt1 = new NBTTagCompound();
					c.getData().serializeNBT(nbt1, EnumSaveType.SAVE);
					c.getItemStack().setTagInfo("ModuleData", nbt1);
				}

				if (!playerIn.inventory.addItemStackToInventory(c.getItemStack()) && !c.getItemStack().isEmpty())
				{
					world.spawnEntity(new EntityItem(world, playerIn.posX, playerIn.posY, playerIn.posZ, c.getItemStack()));
				}

				c.setStack(ItemStack.EMPTY);
				updateContainingBlockInfo();
				markDirty();
				return;
			}
		}

		if (c.getItemStack().isEmpty() && stack.getItem() instanceof Module)
		{
			int modulesSize = 0;

			for (ModuleContainer module : modules)
			{
				if (module.hasModule())
				{
					modulesSize++;
				}
			}

			if (tier.modules <= modulesSize)
			{
				CANT_INSERT.sendMessage(playerIn);
				return;
			}

			c.setStack(ItemHandlerHelper.copyStackWithSize(stack, 1));

			if (c.hasModule() && c.getModule().insertInPipe(c, playerIn))
			{
				stack.shrink(1);
				updateContainingBlockInfo();
				markDirty();
				return;
			}
			else
			{
				c.setStack(ItemStack.EMPTY);
			}
		}

		if (!c.getModule().onModuleRightClick(c, playerIn, hand))
		{
			playerIn.sendMessage(new TextComponentString("GUI Not Implemented!")); //TODO: Open GUI
		}

		/*
		List<TileModularPipe> list = PipeNetwork.findPipes(this, false);
		List<String> list1 = new ArrayList<>();

		for (TileModularPipe t : list)
		{
			list1.add("[" + t.getPos().getX() + ", " + t.getPos().getY() + ", " + t.getPos().getZ() + "]");
		}

		playerIn.sendMessage(new TextComponentString("Found " + list.size() + " pipes on network: " + list1));
		*/
	}

	public void onBroken()
	{
		for (ModuleContainer c : modules)
		{
			c.getModule().pipeBroken(c);

			if (!c.getData().isEmpty())
			{
				NBTTagCompound nbt1 = new NBTTagCompound();
				c.getData().serializeNBT(nbt1, EnumSaveType.SAVE);
				c.getItemStack().setTagInfo("ModuleData", nbt1);
			}

			if (!c.getItemStack().isEmpty())
			{
				EntityItem entityItem = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, c.getItemStack());
				entityItem.setPickupDelay(10);
				world.spawnEntity(entityItem);
			}
		}

		clearModules();
	}

	public EnumFacing getItemDirection(TransportedItem item, EnumFacing source)
	{
		return source;
	}

	public int getConnections()
	{
		if (connections == -1)
		{
			connections = 0;

			for (EnumFacing facing : EnumFacing.VALUES)
			{
				if (canConnectTo0(facing))
				{
					connections |= 1 << facing.getIndex();
				}
			}

			CommonUtils.notifyBlockUpdate(world, pos, null);
		}

		return connections;
	}

	public boolean canConnectTo(EnumFacing facing)
	{
		return (getConnections() & (1 << facing.getIndex())) != 0;
	}

	private boolean canConnectTo0(EnumFacing facing)
	{
		BlockPos pos1 = pos.offset(facing);
		IBlockState state1 = world.getBlockState(pos1);
		Block block1 = state1.getBlock();
		return block1 == ModularPipesItems.CONTROLLER || block1 instanceof IPipeBlock && ((IPipeBlock) block1).canPipeConnect(world, pos1, state1, facing.getOpposite());
	}

	public PipeNetwork getNetwork()
	{
		if (network == null)
		{
			network = PipeNetwork.get(world);
		}

		return network;
	}

	@Override
	public void markDirty()
	{
		sendDirtyUpdate();
	}

	public void onNeighborChange()
	{
		updateContainingBlockInfo();
		getConnections();

		if (world != null)
		{
			CommonUtils.notifyBlockUpdate(world, pos, null);

			if (!world.isRemote)
			{
				Node node = getNetwork().getNode(pos);

				if (node != null)
				{
					node.clearCache();
					node.network.markDirty();
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB(pos, pos.add(1, 1, 1));
	}

	@Nullable
	@Override
	public TileController getController()
	{
		if (cachedController == null && controllerPos != null)
		{
			TileEntity tileEntity = world.getTileEntity(controllerPos);

			if (tileEntity instanceof TileController)
			{
				cachedController = (TileController) tileEntity;
			}
		}

		return cachedController;
	}

	@Override
	public void setControllerPosition(BlockPos pos)
	{
		controllerPos = pos;
		cachedController = null;
	}
}