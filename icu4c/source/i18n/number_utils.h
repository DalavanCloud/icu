// © 2017 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html

#include "unicode/utypes.h"

#if !UCONFIG_NO_FORMATTING && !UPRV_INCOMPLETE_CPP11_SUPPORT
#ifndef __NUMBER_UTILS_H__
#define __NUMBER_UTILS_H__

#include "unicode/numberformatter.h"
#include "number_types.h"
#include "number_decimalquantity.h"
#include "number_scientific.h"
#include "number_patternstring.h"
#include "number_modifiers.h"
#include "number_multiplier.h"

U_NAMESPACE_BEGIN namespace number {
namespace impl {

class UnicodeStringCharSequence : public CharSequence {
  public:
    explicit UnicodeStringCharSequence(const UnicodeString& other) {
        fStr = other;
    }

    ~UnicodeStringCharSequence() U_OVERRIDE = default;

    int32_t length() const U_OVERRIDE {
        return fStr.length();
    }

    char16_t charAt(int32_t index) const U_OVERRIDE {
        return fStr.charAt(index);
    }

    UChar32 codePointAt(int32_t index) const U_OVERRIDE {
        return fStr.char32At(index);
    }

    UnicodeString toUnicodeString() const U_OVERRIDE {
        // Performs a copy:
        return fStr;
    }

    const UnicodeString toTempUnicodeString() const U_OVERRIDE {
        // Readonly alias:
        return UnicodeString().fastCopyFrom(fStr);
    }

  private:
    UnicodeString fStr;
};

struct MicroProps : public MicroPropsGenerator {

    // NOTE: All of these fields are properly initialized in NumberFormatterImpl.
    Rounder rounding;
    Grouper grouping;
    Padder padding;
    IntegerWidth integerWidth;
    UNumberSignDisplay sign;
    UNumberDecimalSeparatorDisplay decimal;
    bool useCurrency;

    // Note: This struct has no direct ownership of the following pointers.
    const DecimalFormatSymbols* symbols;
    const Modifier* modOuter;
    const Modifier* modMiddle;
    const Modifier* modInner;

    // The following "helper" fields may optionally be used during the MicroPropsGenerator.
    // They live here to retain memory.
    struct {
        ScientificModifier scientificModifier;
        EmptyModifier emptyWeakModifier{false};
        EmptyModifier emptyStrongModifier{true};
        MultiplierChain multiplier;
    } helpers;


    MicroProps() = default;

    MicroProps(const MicroProps& other) = default;

    MicroProps& operator=(const MicroProps& other) = default;

    void processQuantity(DecimalQuantity&, MicroProps& micros, UErrorCode& status) const U_OVERRIDE {
        (void) status;
        if (this == &micros) {
            // Unsafe path: no need to perform a copy.
            U_ASSERT(!exhausted);
            micros.exhausted = true;
            U_ASSERT(exhausted);
        } else {
            // Safe path: copy self into the output micros.
            micros = *this;
        }
    }

  private:
    // Internal fields:
    bool exhausted = false;
};

inline int32_t insertDigitFromSymbols(NumberStringBuilder& output, int32_t index, int8_t digit,
                                      const DecimalFormatSymbols& symbols, Field field,
                                      UErrorCode& status) {
    if (symbols.getCodePointZero() != -1) {
        return output.insertCodePoint(index, symbols.getCodePointZero() + digit, field, status);
    }
    return output.insert(index, symbols.getConstDigitSymbol(digit), field, status);
}

inline bool unitIsCurrency(const MeasureUnit& unit) {
    return uprv_strcmp("currency", unit.getType()) == 0;
}

inline bool unitIsNoUnit(const MeasureUnit& unit) {
    return uprv_strcmp("none", unit.getType()) == 0;
}

inline bool unitIsPercent(const MeasureUnit& unit) {
    return uprv_strcmp("percent", unit.getSubtype()) == 0;
}

inline bool unitIsPermille(const MeasureUnit& unit) {
    return uprv_strcmp("permille", unit.getSubtype()) == 0;
}

} // namespace impl
} // namespace number
U_NAMESPACE_END

#endif //__NUMBER_UTILS_H__

#endif /* #if !UCONFIG_NO_FORMATTING */
