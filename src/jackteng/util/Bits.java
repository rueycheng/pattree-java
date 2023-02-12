package jackteng.util;

import java.util.*;

/**
 * <p>Description: This class contains various methods for manipulating
 *                 bits.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Institute of Information Science, Academia Sinica</p>
 * @author Jei-Wen Teng
 * @version 1.0
 */
public class Bits {
    private Bits() { }

    /**
     * Returns a bitset of the given byte array. The byte-ordering of
     * bytes must be big-endian which means the most significant bit is in
     * element 0.
     *
     * @param bytes the target byte array.
     * @return the bitset of the given byte array.
     */
    public static BitSet toBitSet(byte[] bytes) {
	BitSet bits = new BitSet();

	for (int i = 0; i < bytes.length * 8; i++) {
	    if ((bytes[i / 8] & (128 >> (i % 8))) > 0) {
		bits.set(i);
	    }
	}

	return bits;
    }

    /**
     * Returns a byte array of at least length 1. The byte-ordering of the result
     * is big-endian which means the most significant bit is in element 0. The
     * bit at index 0 of the bit set is assumed to be the most significant bit.
     *
     * @param bits the target bitset.
     * @return the byte array of the bitset.
     */
    public static byte[] toByteArray(BitSet bits) {
	int bitLen = bits.length();
	int byteLen = (((bitLen % 8) == 0) ? (bitLen / 8) : (bitLen / 8 + 1));
	byte[] bytes = new byte[byteLen];

	for (int i = 0; i < bits.length(); i++) {
	    if (bits.get(i)) {
		bytes[i / 8] |= (128 >> (i % 8));
	    }
	}

	return bytes;
    }

    /**
     * Returns a conventional string representation of the given bitset which
     * means that we use a string of 1 and 0 to represent the bitset.
     *
     * @param bits the target bitset.
     * @return the conventional string representation of the given bitset.
     */
    public static String toStr(BitSet bits) {
	StringBuffer sb = new StringBuffer();

	for (int i = 0; i < bits.length(); i++) {
	    if (bits.get(i)) {
		sb.append("1");
	    } else {
		sb.append("0");
	    }
	}

	return sb.toString();
    }

    /**
     * Gets the comparison bit of the two bitsets. If the lengths of the two
     * bitsets are different and the prefix of the longer bitset is the shorter
     * bitset, then returns the first 1 bit after that prefix (i.e. the shorter
     * bitset is padded with 0 bits). If the two bitsets are equal, then returns
     * 0.
     *
     * @param bit1 the first bitset.
     * @param bit2 the second bitset.
     * @return the comparison bit of the two bitsets, 0 if the two bitsets are
     *         equal.
     */
    public static int getComparisonBit(BitSet bit1, BitSet bit2) {
	int bit1Len = bit1.length();
	int bit2Len = bit2.length();
	int maxLen = Math.max(bit1Len, bit2Len);
	int i = 0;
	boolean found = false;

	for (i = 0; (i < maxLen) && !found; i++) {
	    if ((i < bit1Len) && (i < bit2Len)) {
		if (bit1.get(i) != bit2.get(i)) {
		    found = true;
		}
	    } else {
		if (bit1Len > bit2Len) {
		    if (bit1.get(i)) {
			found = true;
		    }
		} else {
		    if (bit2.get(i)) {
			found = true;
		    }
		}
	    }
	}
	if (!found) {
	    i = 0;
	}

	return i;
    }
}
