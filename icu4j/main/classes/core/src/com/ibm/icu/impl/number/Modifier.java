// © 2017 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package com.ibm.icu.impl.number;

import com.ibm.icu.impl.StandardPlural;
import com.ibm.icu.impl.number.modifiers.AffixModifier;
import com.ibm.icu.impl.number.modifiers.ConstantAffixModifier;
import com.ibm.icu.impl.number.modifiers.GeneralPluralModifier;
import com.ibm.icu.impl.number.modifiers.SimpleModifier;

/**
 * A Modifier is an immutable object that can be passed through the formatting pipeline until it is
 * finally applied to the string builder. A Modifier usually contains a prefix and a suffix that are
 * applied, but it could contain something else, like a {@link com.ibm.icu.text.SimpleFormatter}
 * pattern.
 *
 * @see AffixModifier
 * @see ConstantAffixModifier
 * @see GeneralPluralModifier
 * @see SimpleModifier
 */
public interface Modifier {

  /**
   * Apply this Modifier to the string builder.
   *
   * @param output The string builder to which to apply this modifier.
   * @param leftIndex The left index of the string within the builder. Equal to 0 when only one
   *     number is being formatted.
   * @param rightIndex The right index of the string within the string builder. Equal to length-1
   *     when only one number is being formatted.
   * @return The number of characters (UTF-16 code units) that were added to the string builder.
   */
  public int apply(DoubleSidedStringBuilder output, int leftIndex, int rightIndex);

  /**
   * The number of characters that {@link #apply} would add to the string builder.
   *
   * @return The number of characters (UTF-16 code units) that would be added to a string builder.
   */
  public int length();

  /**
   * An interface for a modifier that contains both a positive and a negative form. Note that a
   * class implementing {@link PositiveNegativeModifier} is not necessarily a {@link Modifier}
   * itself. Rather, it returns a {@link Modifier} when {@link #getModifier} is called.
   */
  public static interface PositiveNegativeModifier extends Exportable {
    /**
     * Converts this {@link PositiveNegativeModifier} to a {@link Modifier} given the negative sign.
     *
     * @param isNegative true if the negative form of this modifier should be used; false if the
     *     positive form should be used.
     * @return A Modifier corresponding to the negative sign.
     */
    public Modifier getModifier(boolean isNegative);
  }

  /**
   * An interface for a modifier that contains both a positive and a negative form for all six
   * standard plurals. Note that a class implementing {@link PositiveNegativePluralModifier} is not
   * necessarily a {@link Modifier} itself. Rather, it returns a {@link Modifier} when {@link
   * #getModifier} is called.
   */
  public static interface PositiveNegativePluralModifier extends Exportable {
    /**
     * Converts this {@link PositiveNegativePluralModifier} to a {@link Modifier} given the negative
     * sign and the standard plural.
     *
     * @param plural The StandardPlural to use.
     * @param isNegative true if the negative form of this modifier should be used; false if the
     *     positive form should be used.
     * @return A Modifier corresponding to the negative sign.
     */
    public Modifier getModifier(StandardPlural plural, boolean isNegative);
  }

  /**
   * A starter implementation with defaults for some of the basic methods.
   *
   * <p>Implements {@link PositiveNegativeModifier} so that instances of this class can be used when
   * a {@link PositiveNegativeModifier} is required.
   */
  public abstract static class BaseModifier extends Format.BeforeFormat
      implements Modifier, PositiveNegativeModifier, Exportable {

    @Override
    public void before(FormatQuantity input, ModifierHolder mods) {
      mods.add(this);
    }

    @Override
    public Modifier getModifier(boolean isNegative) {
      return this;
    }
  }
}
