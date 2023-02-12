package jackteng.util;

import java.util.*;

/**
 * <p>Description: This class contains various methods for manipulating
 *                 strings.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Institute of Information Science, Academia Sinica</p>
 * @author Jei-Wen Teng
 * @version 1.0
 */
public class Strings {
    public final static int ChineseLike = 0;
    public final static int EnglishLike = 1;

    private Strings() {
    }

    public static long count(String inputStr, String term) {
	long result = 0;

	for (int i = inputStr.indexOf(term); i >= 0;
	     i = inputStr.indexOf(term, i + 1)) {
	    result++;
	}

	return result;
    }

    /**
     * Gets the antecedents (the (n-1)-grams) of the n-gram <code>oldStr</code>.
     *
     * @param oldStr the target string.
     * @param langType the language type, either Chinese like or English like.
     * @return a string array of the two (n-1)-grams.
     */
    public static String[] getAnt(String oldStr, int langType) {
	String[] result = new String[2];

	if (langType == Strings.ChineseLike) {
	    result[0] = oldStr.substring(0, oldStr.length() - 1);
	    result[1] = oldStr.substring(1);
	} else if (langType == Strings.EnglishLike) {
	    result[0] = oldStr.substring(0, oldStr.lastIndexOf(' ')).trim();
	    result[1] = oldStr.substring(oldStr.indexOf(' ')).trim();
	}

	return result;
    }

    /**
     * Tests if <code>newStr</code> is a successor (a (n+1)-gram) of
     * n-gram <code>oldStr</code> such that <code>newStr</code> containes the
     * n-gram <code>oldStr</code> and an additional word before (on the left) or
     * after (on the right) <code>oldStr</code>.
     *
     * @param newStr the new string.
     * @param oldStr the old string.
     * @param langType the language type, either Chinese like or English like.
     * @return true if the new string is an successor of the old string.
     */
    public static boolean isSucc(String newStr, String oldStr, int langType) {
	boolean result = false;

	String[] antStrs = Strings.getAnt(newStr.trim(), langType);
	String trimOldStr = oldStr.trim();
	result = (trimOldStr.equals(antStrs[0]) || trimOldStr.equals(antStrs[1]));
	/*
	   int ni = newStr.indexOf(oldStr);
	   int nl = newStr.length();
	   int ol = oldStr.length();
	   if (langType == Strings.ChineseLike) {
		result = ((ni >= 0) && (nl == ol + 1));
	   } else if (langType == Strings.EnglishLike) {
		result = (((ni == 0) && (newStr.substring(ol).trim().indexOf(' ') < 0))
	 ||
							(((ni + ol) == nl) &&
							 (newStr.substring(0, ni).trim().indexOf(' ') < 0))
						 );
	   }
	 */

	return result;
    }

    /**
     * Calculates n of the n-gram.
     * @param ngram the target n-gram.
     * @param langType the language type, either Chinese like or English like.
     * @return n of the n-gram.
     */
    public static int getN(String ngram, int langType) {
	int result = 1;

	if (langType == Strings.ChineseLike) {
	    result = ngram.length();
	} else if (langType == Strings.EnglishLike) {
	    result = (new StringTokenizer(ngram, " ")).countTokens();
	}

	return result;
    }

    /**
     * Gets the sub-n-gram of the given n-gram.
     *
     * @param ngram the target n-gram.
     * @param from the beginning index, inclusive.
     * @param to the ending index, inclusive.
     * @param langType the language type, either Chinese like or English like.
     * @return the sub-n-gram.
     */
    public static String getSubNGram(String ngram, int from, int to,
				     int langType) {
	String result = "";

	if ((from <= 0) || (to > Strings.getN(ngram, langType)) || (from > to)) {
	    throw (new IndexOutOfBoundsException());
	} else {
	    if (langType == Strings.ChineseLike) {
		result = ngram.substring(from - 1, to);
	    } else if (langType == Strings.EnglishLike) {
		StringTokenizer st = new StringTokenizer(ngram, " ");
		while (st.hasMoreTokens() && (to > 0)) {
		    from--;
		    to--;
		    if (from == 0) {
			result = st.nextToken();
		    } else if (from < 0) {
			result = result + " " + st.nextToken();
		    } else {
			st.nextToken();
		    }
		}
	    }
	}

	return result;
    }

    /**
     * Removes redundant whitespaces in the string.
     * @param str the target string.
     * @return the condensed string.
     */
    public static String condenseSpace(String str) {
	return str.trim().replaceAll("\\s+", " ");
    }

    /**
     * Transforms a string array into a string of array elements delimited by
     * comma (',').
     *
     * @param target the string array to be transformed.
     * @return the string of the target string array elements delimited by
     *         comma.
     */
    public static String toString(String[] target) {
	return toString(target, ",");
    }

    /**
     * Transforms a string array into a string of array elements delimited by
     * the specified delimiter.
     *
     * @param target the string array to be transformed.
     * @param delim the delimiter.
     * @return the string of the target string array elements delimited by
     *         the specified delimiter.
     */
    public static String toString(String[] target, String delim) {
	StringBuffer result = new StringBuffer();

	if (target != null && target.length > 0) {
	    int total = target.length;

	    for (int i = 0; i < total; i++) {
		result.append(delim);
		result.append(target[i]);
	    }
	    if (result.length() > 0) {
		result.deleteCharAt(0);
	    }
	}

	return result.toString();
    }

    /**
     * Transforms a string delimited by default delimiter (which is comma ',')
     * into a string array.
     *
     * @param target the string to be transformed.
     * @return the string array transformed from the target string.
     */
    public static String[] toArray(String target) {
	return toArray(target, ",");
    }

    /**
     * Transforms a string delimited by specified delimiter into a string
     * array.
     *
     * @param target the string to be transformed.
     * @param delim the delimiter.
     * @return the string array transformed from the target string.
     */
    public static String[] toArray(String target, String delim) {
	String[] result = new String[0];

	if(target != null) {
	    StringTokenizer st = new StringTokenizer(target, delim);
	    result = new String[st.countTokens()];
	    int i = 0;
	    while(st.hasMoreTokens())       {
		result[i++] = st.nextToken();
	    }
	}

	return result;
    }

    /**
     * Inverses the given string.
     *
     * @param str the string to be inversed.
     * @return the inversed string.
     */
    public static String inverse(String str) {
	StringBuffer sb = new StringBuffer();

	for (int i = (str.length() - 1); i >= 0; i--) {
	    sb.append(str.charAt(i));
	}

	return sb.toString();
    }

    /**
     * Inverses the given string with special treatment for different language
     * types.
     *
     * @param str the string to be inversed.
     * @param langType the language type, either Chinese like or English like.
     * @return the inversed string.
     */
    public static String inverse(String str, int langType) {
	StringBuffer sb = new StringBuffer();

	if (langType == Strings.EnglishLike) {
	    String[] tmp = Strings.toArray(str, " ");
	    for (int i = (tmp.length - 1); i >= 0; i--) {
		sb.append(" ").append(tmp[i]);
	    }
	    if (sb.length() > 0) {
		sb.deleteCharAt(0);
	    }
	} else {
	    for (int i = (str.length() - 1); i >= 0; i--) {
		sb.append(str.charAt(i));
	    }
	}

	return sb.toString();
    }

    public static int indexOf(String target, String str, int fromIndex, int num) {
	int index = -1;

	if (num == 1) {
	    index = target.indexOf(str, fromIndex);
	} else if (num > 1) {
	    int newFromIndex = target.indexOf(str, fromIndex) + 1;
	    if (newFromIndex > 0) {
		index = Strings.indexOf(target, str, newFromIndex, --num);
	    }
	}

	return index;
    }

    /**
     * Returns a string resulting from replacing all occurrences of oldStr
     * in the target string with newStr.
     *
     * @param str the original string.
     * @param oldStr the string to be replaced.
     * @param newStr the new string to be replaced with.
     * @return a string derived from the original string by replacing all
     *         occurrences of oldStr in the original string with newStr.
     */
    public static String replaceStr(String str,String oldStr, String newStr) {
	int s = 0;
	int e = 0;
	int ol = oldStr.length();
	StringBuffer result = new StringBuffer();

	while ((e = str.indexOf(oldStr, s)) >= 0) {
	    result.append(str.substring(s, e));
	    result.append(newStr);
	    s = e + ol;
	}
	result.append(str.substring(s));
	return result.toString();
    }
}
