/*
*******************************************************************************
* Copyright (C) 2012-2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationdatabuilder.h
*
* created on: 2012apr01
* created by: Markus W. Scherer
*/

#ifndef __COLLATIONDATABUILDER_H__
#define __COLLATIONDATABUILDER_H__

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/uniset.h"
#include "unicode/unistr.h"
#include "unicode/uversion.h"
#include "collation.h"
#include "collationdata.h"
#include "collationsettings.h"
#include "normalizer2impl.h"
#include "utrie2.h"
#include "uvectr32.h"
#include "uvectr64.h"
#include "uvector.h"

U_NAMESPACE_BEGIN

struct ConditionalCE32;

class CopyHelper;
class UCharsTrieBuilder;

/**
 * Low-level CollationData builder.
 * Takes (character, CE) pairs and builds them into runtime data structures.
 * Supports characters with context prefixes and contraction suffixes.
 */
class U_I18N_API CollationDataBuilder : public UObject {
public:
    /**
     * Collation element modifier. Interface class for a modifier
     * that changes a tailoring builder's temporary CEs to final CEs.
     * Called for every non-special CE32 and every expansion CE.
     */
    class CEModifier : public UObject {
    public:
        virtual ~CEModifier();
        /** Returns a new CE to replace the non-special input CE32, or else Collation::NO_CE. */
        virtual int64_t modifyCE32(uint32_t ce32) const = 0;
        /** Returns a new CE to replace the input CE, or else Collation::NO_CE. */
        virtual int64_t modifyCE(int64_t ce) const = 0;
    };

    CollationDataBuilder(UErrorCode &errorCode);

    virtual ~CollationDataBuilder();

    virtual UBool isCompressibleLeadByte(uint32_t b) const;

    inline UBool isCompressiblePrimary(uint32_t p) const {
        return isCompressibleLeadByte(p >> 24);
    }

    /**
     * @return TRUE if this builder has mappings (e.g., add() has been called)
     */
    UBool hasMappings() const { return modified; }

    /**
     * @return TRUE if c has CEs in this builder
     */
    UBool isAssigned(UChar32 c) const;

    /**
     * @return the three-byte primary if c maps to a single such CE and has no context data,
     * otherwise returns 0.
     */
    uint32_t getLongPrimaryIfSingleCE(UChar32 c) const;

    /**
     * @return the single CE for c.
     * Sets an error code if c does not have a single CE.
     */
    int64_t getSingleCE(UChar32 c, UErrorCode &errorCode) const;

    virtual void add(const UnicodeString &prefix, const UnicodeString &s,
                     const int64_t ces[], int32_t cesLength,
                     UErrorCode &errorCode);

    /**
     * Sets three-byte-primary CEs for a range of code points in code point order,
     * if it is worth doing; otherwise no change is made.
     * None of the code points in the range should have complex mappings so far
     * (expansions/contractions/prefixes).
     * @param start first code point
     * @param end last code point (inclusive)
     * @param primary primary weight for 'start'
     * @param step per-code point primary-weight increment
     * @param errorCode ICU in/out error code
     * @return TRUE if an OFFSET_TAG range was used for start..end
     */
    UBool maybeSetPrimaryRange(UChar32 start, UChar32 end,
                               uint32_t primary, int32_t step,
                               UErrorCode &errorCode);

    /**
     * Sets three-byte-primary CEs for a range of code points in code point order.
     * Sets range values if that is worth doing, or else individual values.
     * None of the code points in the range should have complex mappings so far
     * (expansions/contractions/prefixes).
     * @param start first code point
     * @param end last code point (inclusive)
     * @param primary primary weight for 'start'
     * @param step per-code point primary-weight increment
     * @param errorCode ICU in/out error code
     * @return the next primary after 'end': start primary incremented by ((end-start)+1)*step
     */
    uint32_t setPrimaryRangeAndReturnNext(UChar32 start, UChar32 end,
                                          uint32_t primary, int32_t step,
                                          UErrorCode &errorCode);

    /**
     * Copies all mappings from the src builder, with modifications.
     * This builder here must not be built yet, and should be empty.
     */
    void copyFrom(const CollationDataBuilder &src, const CEModifier &modifier,
                  UErrorCode &errorCode);

    virtual void build(CollationData &data, UErrorCode &errorCode) = 0;

    int32_t lengthOfCE32s() const { return ce32s.size(); }
    int32_t lengthOfCEs() const { return ce64s.size(); }
    int32_t lengthOfContexts() const { return contexts.length(); }

    int32_t serializeTrie(void *data, int32_t capacity, UErrorCode &errorCode) const;
    int32_t serializeUnsafeBackwardSet(uint16_t *data, int32_t capacity,
                                       UErrorCode &errorCode) const;
    UTrie2 *orphanTrie();

protected:
    friend class CopyHelper;

    UBool setJamoCEs(UErrorCode &errorCode);
    void setLeadSurrogates(UErrorCode &errorCode);

    static inline UBool isContractionCE32(uint32_t ce32) {
        return Collation::hasCE32Tag(ce32, Collation::CONTRACTION_TAG);
    }

    uint32_t getCE32FromOffsetCE32(UChar32 c, uint32_t ce32) const;

    int32_t addCE(int64_t ce, UErrorCode &errorCode);
    int32_t addConditionalCE32(const UnicodeString &context, uint32_t ce32, UErrorCode &errorCode);

    inline ConditionalCE32 *getConditionalCE32(int32_t index) const {
        return static_cast<ConditionalCE32 *>(conditionalCE32s[index]);
    }
    inline ConditionalCE32 *getConditionalCE32ForCE32(uint32_t ce32) const {
        return getConditionalCE32((int32_t)ce32 & 0xfffff);
    }

    void addCE32(const UnicodeString &prefix, const UnicodeString &s,
                 uint32_t ce32, UErrorCode &errorCode);

    static uint32_t encodeOneCEAsCE32(int64_t ce);
    uint32_t encodeOneCE(int64_t ce, UErrorCode &errorCode);
    uint32_t encodeCEs(const int64_t ces[], int32_t cesLength, UErrorCode &errorCode);
    uint32_t encodeExpansion(const int64_t ces[], int32_t length, UErrorCode &errorCode);
    uint32_t encodeExpansion32(const int32_t newCE32s[], int32_t length, UErrorCode &errorCode);

    void buildMappings(CollationData &data, UErrorCode &errorCode);

    void buildContexts(UErrorCode &errorCode);
    void buildContext(UChar32 c, UErrorCode &errorCode);
    int32_t addContextTrie(uint32_t defaultCE32, UCharsTrieBuilder &trieBuilder,
                           UErrorCode &errorCode);

    uint32_t getCE32FromContext(const UnicodeString &s, uint32_t ce32,
                                int32_t sIndex, UnicodeSet &consumed) const;
    uint32_t getCE32FromContraction(const UnicodeString &s,
                                    int32_t sIndex, UnicodeSet &consumed,
                                    ConditionalCE32 *firstCond,
                                    ConditionalCE32 *lastCond) const;

    const Normalizer2Impl &nfcImpl;
    const CollationData *base;
    const CollationSettings *baseSettings;
    UTrie2 *trie;
    UVector32 ce32s;
    UVector64 ce64s;
    UVector conditionalCE32s;  // vector of ConditionalCE32
    int64_t jamoCEs[19+21+27];
    // Characters that have context (prefixes or contraction suffixes).
    UnicodeSet contextChars;
    // Serialized UCharsTrie structures for finalized contexts.
    UnicodeString contexts;
    UnicodeSet unsafeBackwardSet;
    UBool modified;
};

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
#endif  // __COLLATIONDATABUILDER_H__
