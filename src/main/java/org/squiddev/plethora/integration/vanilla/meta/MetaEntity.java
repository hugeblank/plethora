package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(Entity.class)
public class MetaEntity extends BaseMetaProvider<Entity> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<Entity> context) {
		Entity entity = context.getTarget();
		IWorldLocation location = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);

		Map<Object, Object> result = getBasicProperties(entity, location);

		{
			Map<String, Double> subBlock = new HashMap<>();
			result.put("withinBlock", subBlock);

			double posY = entity.posY + entity.getEyeHeight();
			subBlock.put("x", entity.posX - MathHelper.floor(entity.posX));
			subBlock.put("y", posY - MathHelper.floor(posY));
			subBlock.put("z", entity.posZ - MathHelper.floor(entity.posZ));
		}

		return result;
	}

	public static HashMap<Object, Object> getBasicProperties(@Nonnull Entity entity, @Nullable IWorldLocation location) {
		HashMap<Object, Object> result = Maps.newHashMap();
		result.put("id", entity.getUniqueID().toString());

		result.put("name", Helpers.getName(entity));
		result.put("displayName", entity.getName());

		result.put("motionX", entity.motionX);
		result.put("motionY", entity.motionY);
		result.put("motionZ", entity.motionZ);

		result.put("pitch", entity.rotationPitch);
		result.put("yaw", entity.rotationYaw);

		if (location != null && location.getWorld() == entity.getEntityWorld()) {
			Vec3d pos = location.getLoc();
			result.put("x", entity.posX - pos.x);
			result.put("y", entity.posY + entity.getEyeHeight() - pos.y);
			result.put("z", entity.posZ - pos.z);
		}

		return result;
	}

}
