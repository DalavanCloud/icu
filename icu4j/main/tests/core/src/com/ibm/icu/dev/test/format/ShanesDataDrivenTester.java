// © 2017 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package com.ibm.icu.dev.test.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.ParsePosition;

import com.ibm.icu.dev.test.format.DataDrivenNumberFormatTestUtility.CodeUnderTest;
import com.ibm.icu.impl.number.Endpoint;
import com.ibm.icu.impl.number.Format;
import com.ibm.icu.impl.number.FormatQuantity;
import com.ibm.icu.impl.number.FormatQuantity1;
import com.ibm.icu.impl.number.FormatQuantity2;
import com.ibm.icu.impl.number.Parse;
import com.ibm.icu.impl.number.PatternString;
import com.ibm.icu.impl.number.Properties;
import com.ibm.icu.impl.number.formatters.PaddingFormat.PaddingLocation;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.util.ULocale;

public class ShanesDataDrivenTester extends CodeUnderTest {
  static final String dataPath =
      "../../../icu4j-core-tests/src/com/ibm/icu/dev/data/numberformattestspecification.txt";

  public static void run() {
    CodeUnderTest tester = new ShanesDataDrivenTester();
    DataDrivenNumberFormatTestUtility.runSuite(dataPath, tester);
  }

  @Override
  public Character Id() {
    return 'S';
  }

  /**
   * Runs a single formatting test. On success, returns null. On failure, returns the error. This
   * implementation just returns null. Subclasses should override.
   *
   * @param tuple contains the parameters of the format test.
   */
  @Override
  public String format(NumberFormatTestData tuple) {
    String pattern = (tuple.pattern == null) ? "0" : tuple.pattern;
    ULocale locale = (tuple.locale == null) ? ULocale.ENGLISH : tuple.locale;
    Format fmt;
    try {
      Properties properties = PatternString.parseToProperties(pattern);
      propertiesFromTuple(tuple, properties);
      System.out.println(properties);
      fmt = Endpoint.fromBTA(properties, locale);
    } catch (ParseException e) {
      e.printStackTrace();
      return e.getLocalizedMessage();
    }
    FormatQuantity q1, q2, q3;
    if (tuple.format.equals("NaN")) {
      q1 = q2 = new FormatQuantity1(Double.NaN);
      q3 = new FormatQuantity2(Double.NaN);
    } else if (tuple.format.equals("-Inf")) {
      q1 = q2 = new FormatQuantity1(Double.NEGATIVE_INFINITY);
      q3 = new FormatQuantity1(Double.NEGATIVE_INFINITY);
    } else if (tuple.format.equals("Inf")) {
      q1 = q2 = new FormatQuantity1(Double.POSITIVE_INFINITY);
      q3 = new FormatQuantity1(Double.POSITIVE_INFINITY);
    } else {
      q1 = new FormatQuantity1(new BigDecimal(tuple.format));
      q2 = new FormatQuantity1(Double.parseDouble(tuple.format));
      q3 = new FormatQuantity2(new BigDecimal(tuple.format));
    }
    String expected = tuple.output;
    String actual1 = fmt.format(q1);
    if (!expected.equals(actual1)) {
      return "Expected \"" + expected + "\", got \"" + actual1 + "\" on BigDecimal";
    }
    String actual2 = fmt.format(q2);
    if (!expected.equals(actual2)) {
      return "Expected \"" + expected + "\", got \"" + actual2 + "\" on double";
    }
    String actual3 = fmt.format(q3);
    if (!expected.equals(actual3)) {
      return "Expected \"" + expected + "\", got \"" + actual3 + "\" on double";
    }
    return null;
  }

  /**
   * Runs a single toPattern test. On success, returns null. On failure, returns the error. This
   * implementation just returns null. Subclasses should override.
   *
   * @param tuple contains the parameters of the format test.
   */
  @Override
  public String toPattern(NumberFormatTestData tuple) {
    String pattern = (tuple.pattern == null) ? "0" : tuple.pattern;
    Properties properties;
    try {
      properties = PatternString.parseToProperties(pattern);
      propertiesFromTuple(tuple, properties);
      System.out.println(properties);
    } catch (ParseException e) {
      e.printStackTrace();
      return e.getLocalizedMessage();
    }

    if (tuple.toPattern != null) {
      String expected = tuple.toPattern;
      String actual = PatternString.propertiesToString(properties);
      if (!expected.equals(actual)) {
        return "Expected toPattern='" + expected + "'; got '" + actual + "'";
      }
    }
    if (tuple.toLocalizedPattern != null) {
      String expected = tuple.toLocalizedPattern;
      String actual = PatternString.propertiesToString(properties);
      if (!expected.equals(actual)) {
        return "Expected toLocalizedPattern='" + expected + "'; got '" + actual + "'";
      }
    }
    return null;
  }

  /**
   * Runs a single parse test. On success, returns null. On failure, returns the error. This
   * implementation just returns null. Subclasses should override.
   *
   * @param tuple contains the parameters of the format test.
   */
  @Override
  public String parse(NumberFormatTestData tuple) {
    String pattern = (tuple.pattern == null) ? "0" : tuple.pattern;
    Properties properties;
    ParsePosition ppos = new ParsePosition(0);
    Number actual;
    try {
      properties = PatternString.parseToProperties(pattern);
      Parse.ParseMode mode = Parse.ParseMode.LENIENT;
      if (tuple.lenient != null && tuple.lenient == 0) {
        mode = Parse.ParseMode.STRICT;
      }
      propertiesFromTuple(tuple, properties);
      actual =
          Parse.parse(
              tuple.parse,
              ppos,
              mode,
              (tuple.parseIntegerOnly != null && tuple.parseIntegerOnly == 1),
              (tuple.parseNoExponent != null && tuple.parseNoExponent == 1),
              properties,
              DecimalFormatSymbols.getInstance(tuple.locale));
    } catch (ParseException e) {
      e.printStackTrace();
      return e.getLocalizedMessage();
    }
    if (ppos.getIndex() == 0) {
      if (!tuple.output.equals("fail")) {
        return "Parse failed; got " + actual + ", but expected " + tuple.output;
      }
      return null;
    }
    if (tuple.output.equals("fail")) {
      return "Parse succeeded: " + actual + ", but was expected to fail.";
    }
    BigDecimal expected = new BigDecimal(tuple.output);
    if (expected.compareTo(new BigDecimal(actual.toString())) != 0) {
      return "Expected: " + expected + ", got: " + actual;
    }
    return null;
  }

  /**
   * Runs a single parse currency test. On success, returns null. On failure, returns the error.
   * This implementation just returns null. Subclasses should override.
   *
   * @param tuple contains the parameters of the format test.
   */
  @Override
  public String parseCurrency(NumberFormatTestData tuple) {
    return null;
  }

  /**
   * Runs a single select test. On success, returns null. On failure, returns the error. This
   * implementation just returns null. Subclasses should override.
   *
   * @param tuple contains the parameters of the format test.
   */
  @Override
  public String select(NumberFormatTestData tuple) {
    return null;
  }

  private static void propertiesFromTuple(NumberFormatTestData tuple, Properties properties) {
    if (tuple.minIntegerDigits != null) {
      properties.setMinimumIntegerDigits(tuple.minIntegerDigits);
    }
    if (tuple.maxIntegerDigits != null) {
      properties.setMaximumIntegerDigits(tuple.maxIntegerDigits);
    }
    if (tuple.minFractionDigits != null) {
      properties.setMinimumFractionDigits(tuple.minFractionDigits);
    }
    if (tuple.maxFractionDigits != null) {
      properties.setMaximumFractionDigits(tuple.maxFractionDigits);
    }
    if (tuple.currency != null) {
      properties.setCurrency(tuple.currency);
    }
    if (tuple.minGroupingDigits != null) {
      properties.setMinimumGroupingDigits(tuple.minGroupingDigits);
    }
    if (tuple.useSigDigits != null) {
      // TODO
    }
    if (tuple.minSigDigits != null) {
      properties.setMinimumSignificantDigits(tuple.minSigDigits);
    }
    if (tuple.maxSigDigits != null) {
      properties.setMaximumSignificantDigits(tuple.maxSigDigits);
    }
    if (tuple.useGrouping != null && tuple.useGrouping == 0) {
      properties.setGroupingSize(Integer.MAX_VALUE);
      properties.setSecondaryGroupingSize(Integer.MAX_VALUE);
    }
    if (tuple.multiplier != null) {
      properties.setMultiplier(new BigDecimal(tuple.multiplier));
    }
    if (tuple.roundingIncrement != null) {
      properties.setRoundingInterval(new BigDecimal(tuple.roundingIncrement.toString()));
    }
    if (tuple.formatWidth != null) {
      properties.setPaddingWidth(tuple.formatWidth);
    }
    if (tuple.padCharacter != null && tuple.padCharacter.length() > 0) {
      properties.setPaddingString(tuple.padCharacter.toString());
    }
    if (tuple.useScientific != null) {
      properties.setExponentDigits(tuple.useScientific);
    }
    if (tuple.grouping != null) {
      properties.setGroupingSize(tuple.grouping);
    }
    if (tuple.grouping2 != null) {
      properties.setSecondaryGroupingSize(tuple.grouping2);
    }
    if (tuple.roundingMode != null) {
      properties.setRoundingMode(RoundingMode.valueOf(tuple.roundingMode));
    }
    if (tuple.currencyUsage != null) {
      properties.setCurrencyUsage(tuple.currencyUsage);
    }
    if (tuple.minimumExponentDigits != null) {
      properties.setExponentDigits(tuple.minimumExponentDigits.byteValue());
    }
    if (tuple.exponentSignAlwaysShown != null) {
      properties.setExponentShowPlusSign(tuple.exponentSignAlwaysShown != 0);
    }
    if (tuple.decimalSeparatorAlwaysShown != null) {
      properties.setAlwaysShowDecimal(tuple.decimalSeparatorAlwaysShown != 0);
    }
    if (tuple.padPosition != null) {
      properties.setPaddingLocation(PaddingLocation.fromOld(tuple.padPosition));
    }
    if (tuple.positivePrefix != null) {
      properties.setPositivePrefix(tuple.positivePrefix);
    }
    if (tuple.positiveSuffix != null) {
      properties.setPositiveSuffix(tuple.positiveSuffix);
    }
    if (tuple.negativePrefix != null) {
      properties.setNegativePrefix(tuple.negativePrefix);
    }
    if (tuple.negativeSuffix != null) {
      properties.setNegativeSuffix(tuple.negativeSuffix);
    }
    if (tuple.localizedPattern != null) {
      // TODO
    }
    if (tuple.lenient != null) {
      // TODO
    }
    if (tuple.parseIntegerOnly != null) {
      // TODO
    }
    if (tuple.decimalPatternMatchRequired != null) {
      // TODO
    }
    if (tuple.parseNoExponent != null) {
      // TODO
    }
  }
}
