package jackteng.util;

import java.util.*;
import java.io.*;

/**
 * <p>Description: This class performs n-gram related operations.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Institute of Information Science, Academia Sinica</p>
 * @author Jei-Wen Teng
 * @version 1.0
 */
public class NGram {
    private int N = 2;
    //private NGram.NGramInfo ngramInfo = new NGram.NGramInfo();
    private NGram.NGramInfo[] ngramInfos = null;

    public NGram(int N) {
	this.N = N;
    }

    public static boolean isChineseWord(String word) {
	boolean result = true;
	byte[] b = null;

	try {
	    b = word.getBytes("big5");
	} catch (UnsupportedEncodingException uee) {
	    uee.printStackTrace();
	}
	if ( (b == null) || (b.length != 2)) {
	    result = false;
	} else {
	    int hi = (int) ( (b[0] + 256) % 256);
	    int lo = (int) ( (b[1] + 256) % 256);

	    //System.out.println("hi=" + hi);
	    //System.out.println("lo=" + lo);

	    if (hi < 0xA4) {
		result = false;
	    } else if ( (hi > 0xC6) && (hi < 0xC9)) {
		result = false;
	    } else if (hi > 0xF9) {
		result = false;
	    } else {
		if (lo < 0x40) {
		    result = false;
		} else if ( (lo > 0x7E) && (lo < 0xA1)) {
		    result = false;
		}
	    }
	}

	return result;
    }

    public static boolean isEnglishAlphabet(String word) {
	boolean result = false;
	byte[] b = null;

	try {
	    b = word.getBytes("big5");
	} catch (UnsupportedEncodingException uee) {
	    uee.printStackTrace();
	}
	if ( (b != null) && (b.length == 1)) {
	    if ( ( (b[0] >= 'a') && (b[0] <= 'z')) ||
		 ( (b[0] >= 'A') && (b[0] <= 'Z'))) {
		result = true;
	    }
	}

	return result;
    }

    public class NGramInfo {
	public Map<String, Long> englishTermMap = new HashMap<String, Long>();
	public Map<String, Long> chineseTermMap = new HashMap<String, Long>();
    }

    public NGram.NGramInfo getNGram(String str) {
	return getNGram(str, false);
    }

    public NGram.NGramInfo getNGram(String str, boolean includeEnglish) {
	return getNGram(str, includeEnglish, false);
    }

    public NGram.NGramInfo getNGram(String str, boolean includeEnglish,
				    boolean keep) {
	NGram.NGramInfo result = null;
	StringBuffer sb = new StringBuffer();
	StringBuffer esb = new StringBuffer();
	boolean isAlphabet = true;

	if (ngramInfos == null) {
	    ngramInfos = new NGram.NGramInfo[this.N];
	    for (int i = 0; i < this.N; i++) {
		ngramInfos[i] = new NGram.NGramInfo();
	    }
	}
	if (!keep) {
	    result = new NGram.NGramInfo();
	} else {
	    result = ngramInfos[this.N - 1];
	}
	for (String temp = str; temp.length() > 0; temp = temp.substring(1)) {
	    String word = temp.substring(0, 1);
	    if (isChineseWord(word)) {
		sb.append(word);
		isAlphabet = false;
	    } else {
		sb = new StringBuffer();
		if (includeEnglish) {
		    if (isEnglishAlphabet(word) || word.equals(" ")) {
			esb.append(word);
			if (word.equals(" ")) {
			    isAlphabet = false;
			} else {
			    isAlphabet = true;
			}
		    } else {
			isAlphabet = false;
		    }
		}
	    }
	    if (includeEnglish) {
		if (!isAlphabet) {
		    if (Strings.getN(esb.toString(), Strings.EnglishLike) == this.N) {
			//if (esb.length() > 1) { // obviates single-alphabet English terms.
			String term = esb.toString().trim();
			Long tf = (Long) result.englishTermMap.get(term);
			if (tf != null) {
			    result.englishTermMap.put(term, new Long(tf.longValue() + 1));

			    //System.out.println("term = " + term + "; tf = " +
			    //									 ( (Long) result.englishTermMap.get(term)));
			} else {
			    result.englishTermMap.put(term, new Long(1));

			    //System.out.println("term = " + term + "; tf = 1 (first time)");
			}
			//}
			if (!word.equals(" ") || (this.N == 1)) {
			    esb = new StringBuffer();
			} else {
			    for (int esbi=esb.indexOf(" "); esbi==0; esbi=esb.indexOf(" ")) {
				esb.delete(0, 1);
			    }
			    esb.delete(0, esb.indexOf(" ") + 1);
			}
		    } else {
			if (!word.equals(" ")) {
			    esb = new StringBuffer();
			}
		    }
		}
	    }

	    if (sb.length() == this.N) {
		String term = sb.toString();
		Long tf = (Long) result.chineseTermMap.get(term);
		if (tf != null) {
		    result.chineseTermMap.put(term, new Long(tf.longValue() + 1));

		    //System.out.println("term = " + term + "; tf = " +
		    //									 ( (Long) result.chineseTermMap.get(term)));
		} else {
		    result.chineseTermMap.put(term, new Long(1));

		    //System.out.println("term = " + term + "; tf = 1 (first time)");
		}
		sb = sb.delete(0, 1);
	    }
	}

	// saves the remaining English term in the buffer.
	if (includeEnglish) {
	    if (Strings.getN(esb.toString(), Strings.EnglishLike) == this.N) {
		//if (esb.length() > 1) { // obviates single-alphabet English terms.
		String term = esb.toString().trim();
		Long tf = (Long) result.englishTermMap.get(term);
		if (tf != null) {
		    result.englishTermMap.put(term, new Long(tf.longValue() + 1));

		    //System.out.println("term = " + term + "; tf = " +
		    //									 ( (Long) result.englishTermMap.get(term)));
		} else {
		    result.englishTermMap.put(term, new Long(1));

		    //System.out.println("term = " + term + "; tf = 1 (first time)");
		}
		//}
	    }
	}

	return result;
    }

    public NGram.NGramInfo[] get1ToNGrams(String str, boolean includeEnglish) {
	return get1ToNGrams(str, includeEnglish, false);
    }

    public NGram.NGramInfo[] get1ToNGrams(String str, boolean includeEnglish,
					  boolean keep) {
	int n = this.N;

	if (!keep || (ngramInfos == null)) {
	    ngramInfos = new NGram.NGramInfo[n];
	    for (int i = 0; i < n; i++) {
		ngramInfos[i] = new NGram.NGramInfo();
	    }
	}
	/*
	   for (int i = n; i >= 1; i--) {
		this.N = i;
		getNGram(str, includeEnglish, true);
	   }
	   this.N = n;
	 */
	for (int i = 1; i <= n; i++) {
	    this.N = i;
	    getNGram(str, includeEnglish, true);
	}

	return ngramInfos;
    }
}
