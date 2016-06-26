package org.squiddev.plethora.modules;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.BlockBase;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.IModuleItem;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.client.tile.RenderManipulator;
import org.squiddev.plethora.impl.MethodRegistry;
import org.squiddev.plethora.impl.PeripheralMethodWrapper;
import org.squiddev.plethora.impl.UnbakedContext;

import java.util.Collection;
import java.util.List;

import static org.squiddev.plethora.api.reference.Reference.id;
import static org.squiddev.plethora.api.reference.Reference.tile;

public final class BlockManipulator extends BlockBase<TileManipulator> implements IPeripheralProvider {
	public static final double OFFSET = 10.0 / 16.0;

	public BlockManipulator() {
		super("manipulator", TileManipulator.class);
		setBlockBounds(0, 0, 0, 1, (float) OFFSET, 1);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileManipulator();
	}

	@Override
	public void init() {
		super.init();
		ComputerCraftAPI.registerPeripheralProvider(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		super.clientInit();
		ClientRegistry.bindTileEntitySpecialRenderer(TileManipulator.class, new RenderManipulator());
	}

	@Override
	public boolean isFullBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public IPeripheral getPeripheral(World world, BlockPos blockPos, EnumFacing enumFacing) {
		TileEntity te = world.getTileEntity(blockPos);
		if (!(te instanceof TileManipulator)) return null;
		TileManipulator manipulator = (TileManipulator) te;
		ItemStack stack = manipulator.getStack();

		if (stack == null) return null;

		if (!(stack.getItem() instanceof IModuleItem)) return null;
		IModuleItem item = (IModuleItem) stack.getItem();

		IModule module = item.getModule(stack);
		Collection<IReference<?>> additionalContext = item.getAdditionalContext(stack);

		IReference<?>[] contextData = new IReference[additionalContext.size() + 2];
		additionalContext.toArray(contextData);
		contextData[contextData.length - 2] = tile(te);
		contextData[contextData.length - 1] = new WorldLocation(world, blockPos);

		IUnbakedContext<IModule> context = new UnbakedContext<IModule>(id(module), contextData);

		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(context);
		if (paired.getFirst().size() > 0) {
			return new PeripheralMethodWrapper(te, paired.getFirst(), paired.getSecond());
		} else {
			return null;
		}
	}
}
