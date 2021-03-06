package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@IConverter.Inject(value = IPeripheral.class, modId = ComputerCraft.MOD_ID)
public class ConverterTargetedPeripheral extends ConstantConverter<IPeripheral, Object> {
	private boolean fetched;

	// IPeripheral
	private Method getTarget;

	// Computronics multiperipheral
	private Class<?> multiPeripheral;
	private Field multiPeripheralPeripherals;

	private Class<?> wrappedMultiPeripheral;
	private Field wrappedMultiPeripheralPeripheral;

	private void fetchReflection() {
		if (fetched) return;

		try {
			getTarget = IPeripheral.class.getMethod("getTarget");
		} catch (ReflectiveOperationException ignored) {
		}

		try {
			multiPeripheral = Class.forName("pl.asie.computronics.cc.multiperipheral.MultiPeripheral");
			multiPeripheralPeripherals = multiPeripheral.getDeclaredField("peripherals");
			multiPeripheralPeripherals.setAccessible(true);

			wrappedMultiPeripheral = Class.forName("pl.asie.computronics.api.multiperipheral.WrappedMultiPeripheral");
			wrappedMultiPeripheralPeripheral = wrappedMultiPeripheral.getDeclaredField("peripheral");
			wrappedMultiPeripheralPeripheral.setAccessible(true);
		} catch (ReflectiveOperationException ignored) {
		}


		fetched = true;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public Object convert(@Nonnull IPeripheral from) {
		fetchReflection();

		Object to;

		to = getTarget(from);
		if (from != to) return to;

		// Try to unwrap Computronics's multi-peripherals
		if (multiPeripheral != null && multiPeripheralPeripherals != null && multiPeripheral.isInstance(from)) {
			try {
				List<IPeripheral> peripherals = (List<IPeripheral>) multiPeripheralPeripherals.get(from);
				for (IPeripheral child : peripherals) {
					to = getTarget(child);
					if (child != to) return to;

					if (wrappedMultiPeripheral != null && wrappedMultiPeripheralPeripheral != null && wrappedMultiPeripheral.isInstance(child)) {
						IPeripheral wrapped = (IPeripheral) wrappedMultiPeripheralPeripheral.get(child);

						to = getTarget(wrapped);
						if (wrapped != to) return to;
					}
				}
			} catch (ReflectiveOperationException e) {
				DebugLogger.error("Cannot extract peripherals from multi-peripheral", e);
			}
		}

		// Handle the case where the multiperipheral is wrapped
		if (wrappedMultiPeripheral != null && wrappedMultiPeripheralPeripheral != null && wrappedMultiPeripheral.isInstance(from)) {
			try {
				IPeripheral wrapped = (IPeripheral) wrappedMultiPeripheralPeripheral.get(from);

				to = getTarget(wrapped);
				if (wrapped != to) return to;
			} catch (ReflectiveOperationException e) {
				DebugLogger.error("Cannot extract peripherals from multi-peripheral", e);
			}
		}

		return null;
	}

	@Nullable
	private Object getTarget(@Nonnull IPeripheral peripheral) {
		if (getTarget != null) {
			try {
				return getTarget.invoke(peripheral);
			} catch (ReflectiveOperationException e) {
				DebugLogger.error("Cannot call IPeripheral.getTarget", e);
			}
		}

		return peripheral;
	}
}
