package org.squiddev.plethora.gameplay;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Base class for all blocks
 */
public abstract class BlockBase<T extends TileBase> extends BlockContainer implements IClientModule {
	public final String name;
	public final Class<T> klass;

	public BlockBase(String blockName, Material material, Class<T> klass) {
		super(material);

		this.klass = klass;
		name = blockName;

		setHardness(2);
		setUnlocalizedName(Plethora.RESOURCE_DOMAIN + "." + blockName);
		setCreativeTab(Plethora.getCreativeTab());
	}

	public BlockBase(String name, Class<T> klass) {
		this(name, Material.ROCK, klass);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public T getTile(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && klass.isInstance(tile)) {
			return (T) tile;
		}

		return null;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileBase tile = getTile(world, pos);
		return tile != null && tile.onActivated(player, hand, side, new Vec3d(hitX, hitY, hitZ));
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, neighborBlock, fromPos);

		if (world.isRemote) return;

		TileBase tile = getTile(world, pos);
		if (tile != null) tile.onNeighborChanged();
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);

		if (world instanceof World && ((World) world).isRemote) return;

		TileBase tile = getTile(world, pos);
		if (tile != null) tile.onNeighborChanged();
	}

	@Override
	public void breakBlock(World world, @Nonnull BlockPos block, @Nonnull IBlockState state) {
		if (!world.isRemote) {
			T tile = getTile(world, block);
			if (tile != null) tile.broken();
		}

		super.breakBlock(world, block, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> out, ITooltipFlag advanced) {
		super.addInformation(stack, world, out, advanced);
		out.add(Helpers.translateToLocal(getUnlocalizedName(stack.getItemDamage()) + ".desc"));
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
		GameRegistry.registerTileEntity(klass, Plethora.RESOURCE_DOMAIN + ":" + name);
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(this.setRegistryName(new ResourceLocation(Plethora.RESOURCE_DOMAIN, name)));
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(new ItemBlockBase(this).setRegistryName(new ResourceLocation(Plethora.RESOURCE_DOMAIN, name)));
	}

	public String getUnlocalizedName(int meta) {
		return getUnlocalizedName();
	}

	@Override
	public void init() {
	}

	@Override
	public void postInit() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		Helpers.setupModel(Item.getItemFromBlock(this), 0, name);
	}
}
