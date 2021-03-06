package com.latmod.mods.modularpipes.tile;

import com.latmod.mods.itemfilters.api.IPaintable;
import com.latmod.mods.itemfilters.api.PaintAPI;
import com.latmod.mods.modularpipes.ModularPipesConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.HashSet;

/**
 * @author LatvianModder
 */
public class TilePipeBase extends TileBase implements IPaintable
{
	private boolean isDirty = false;
	public boolean sync = false;
	public int paint = 0;
	public boolean invisible = false;

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		if (paint != 0)
		{
			nbt.setInteger("paint", paint);
		}

		if (invisible)
		{
			nbt.setBoolean("invisible", true);
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if (capability == PaintAPI.CAPABILITY)
		{
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if (capability == PaintAPI.CAPABILITY)
		{
			return (T) this;
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		paint = nbt.getInteger("paint");
		invisible = nbt.getBoolean("invisible");
	}

	@Override
	public void invalidate()
	{
		if (hasWorld())
		{
			PipeNetwork.get(getWorld()).refresh();
		}

		super.invalidate();
	}

	@Override
	public void setWorld(World world)
	{
		super.setWorld(world);

		if (hasWorld())
		{
			PipeNetwork.get(getWorld()).refresh();
		}
	}

	public void moveItem(PipeItem item)
	{
		item.pos += Math.min(item.speed, 0.99F);
		float pipeSpeed = (float) ModularPipesConfig.pipes.base_speed;

		if (item.speed > pipeSpeed)
		{
			item.speed *= 0.99F;

			if (item.speed < pipeSpeed)
			{
				item.speed = pipeSpeed;
			}
		}
		else if (item.speed < pipeSpeed)
		{
			item.speed *= 1.3F;

			if (item.speed > pipeSpeed)
			{
				item.speed = pipeSpeed;
			}
		}
	}

	@Override
	public void markDirty()
	{
		isDirty = true;
		sync = true;
	}

	public final void sendUpdates()
	{
		if (isDirty)
		{
			super.markDirty();

			if (!world.isRemote && sync)
			{
				IBlockState state = world.getBlockState(pos);
				world.notifyBlockUpdate(pos, state, state, 11);
			}

			isDirty = false;
		}
	}

	public boolean canPipesConnect(int p)
	{
		return paint == p || paint == 0 || p == 0;
	}

	public boolean isConnected(EnumFacing facing)
	{
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void paint(IBlockState paintState, EnumFacing facing, boolean all)
	{
		if (all)
		{
			HashSet<TilePipeBase> pipes = new HashSet<>();
			paintAll(this, paint, pipes, Block.getStateId(paintState));

			for (TilePipeBase pipe : pipes)
			{
				pipe.markDirty();
				IBlockState state = world.getBlockState(pipe.getPos());
				state.getBlock().neighborChanged(state, world, pipe.getPos(), state.getBlock(), pipe.getPos().offset(facing));

				if (world.isRemote)
				{
					world.notifyBlockUpdate(pipe.getPos(), state, state, 11);
				}
			}

			PipeNetwork.get(world).refresh();
			return;
		}

		paint = Block.getStateId(paintState);
		markDirty();
		IBlockState state = world.getBlockState(pos);
		state.getBlock().neighborChanged(state, world, pos, state.getBlock(), pos.offset(facing));
		world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);

		if (world.isRemote)
		{
			world.notifyBlockUpdate(pos, state, state, 11);
		}

		PipeNetwork.get(world).refresh();
	}

	@Override
	public IBlockState getPaint()
	{
		return Block.getStateById(paint);
	}

	private void paintAll(TilePipeBase pipe, int originalPaint, HashSet<TilePipeBase> visited, int paint)
	{
		if (pipe.paint == originalPaint)
		{
			pipe.paint = paint;
			visited.add(pipe);

			for (EnumFacing facing : EnumFacing.VALUES)
			{
				TileEntity tileEntity = pipe.getWorld().getTileEntity(pipe.getPos().offset(facing));

				if (tileEntity instanceof TilePipeBase && !visited.contains(tileEntity))
				{
					paintAll((TilePipeBase) tileEntity, originalPaint, visited, paint);
				}
			}
		}
	}
}