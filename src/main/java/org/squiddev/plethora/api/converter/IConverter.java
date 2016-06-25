package org.squiddev.plethora.api.converter;

import javax.annotation.Nonnull;

/**
 * An implicit converter: takes an object and converts it into something else
 */
public interface IConverter<TIn, TOut> {
	/**
	 * Convert an object from one object into another.
	 * Used to provide additional objects from one
	 *
	 * @param from The object to convert from
	 * @return The converted object
	 */
	@Nonnull
	TOut convert(@Nonnull TIn from);
}
