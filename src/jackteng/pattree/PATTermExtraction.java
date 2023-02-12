package jackteng.pattree;

import java.io.*;
import java.util.*;
import jackteng.file.*;
import jackteng.util.*;

/**
 * <p>Description: PAT-tree based term extraction.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Institute of Information Science, Academia Sinica</p>
 * <p>Create Date: 2003/05/21</p>
 * <p>Last Modified Date: 2004/05/31</p>
 * @author Jei-Wen Teng
 * @version 1.0
 */
public class PATTermExtraction implements java.io.Serializable {

    public final static int SCP = 0;        // Symmetric Conditional Probability.
    public final static int SCPCD = 1;      // SCP with Context Dependency.
    public final static int GMSCP = 2;      // Geometric Mean of SCP.
    public final static int GMSCPCD = 3;    // Geometric Mean of SCP with Context Dependency.
    public final static int CD = 4;         // Context independency ratio.

    private int langType = Strings.ChineseLike;
    private PATTree pattree = null;
    private PATTree invpattree = null;

    public PATTermExtraction(String corpusDir, int langType) throws Exception {
	this.langType = langType;
	pattree = new PATTree(langType);
	invpattree = new PATTree(langType);
	File f = new File(corpusDir);

	if (f.isDirectory()) {
	    String[] files = f.list();
	    for (int i = 0; i < files.length; i++) {

		System.out.println("Start to add file: " + files[i]);
		long t = System.currentTimeMillis();

		this.addFile(corpusDir + "/" + files[i]);

		t = System.currentTimeMillis() - t;
		System.out.println("Finish adding file: " + files[i] + " in " +
				   ((double) t / 1000.0) + "sec.");
	    }
	} else {
	    //System.out.println("Corpus directory does not exist!");
	    throw new Exception("Corpus directory does not exist!");
	}
    }

    public void addFile(String fileName) {
	LineNumberReader lnr = null;
	try {
	//--------------------------------------------------
	//     lnr = new LineNumberReader(new FileReader(fileName));
	//-------------------------------------------------- 
	    lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(fileName), "big5"));
	    for (String str = lnr.readLine(); str != null; str = lnr.readLine()) {
		StringBuffer sb = new StringBuffer();
		str = Strings.condenseSpace(str);

		for (String temp = str; temp.length() > 0; temp = temp.substring(1)) {
		    String word = temp.substring(0, 1);
		//--------------------------------------------------
		//     System.err.println(word + " " + word.length());
		//-------------------------------------------------- 

		    boolean isLegal = ((langType == Strings.ChineseLike) ?
				       NGram.isChineseWord(word) :
				       (NGram.isEnglishAlphabet(word)
					|| word.equals(" ")));
		    if (isLegal) {
			sb.append(word);
		    } else {
			if (sb.length() > 0) {
			    String term = sb.toString().trim();
			    pattree.insert(term);
			    invpattree.insert(Strings.inverse(term, langType));
			}
			sb = new StringBuffer();
		    }
		}

		if (sb.length() > 0) {
		    String term = sb.toString().trim();
		    pattree.insert(term);
		    invpattree.insert(Strings.inverse(term, langType));
		}
	    }
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	} finally {
	    if (lnr != null) {
		try {
		    lnr.close();
		} catch (IOException ioe) {
		    ioe.printStackTrace();
		}
	    }
	}
    }

    /**
     * Calculates fair dispersion point normalization (FDPN) denominator value.
     *
     * @param ngram the target n-gram.
     * @param langType the language type, either Chinese like or English like.
     * @return the value.
     */
    private double Avp(String ngram, int langType) {
	double result = 0.0;

	int n = Strings.getN(ngram, langType);
	for (int i = 1; i <= n - 1; i++) {
	    double px = pattree.getFrequency(Strings.getSubNGram(ngram,
								 1, i, langType));
	    double py = pattree.getFrequency(Strings.getSubNGram(ngram,
								 i + 1, n, langType));
	    result += (px * py);
	}
	result /= ((double) (n - 1));

	return result;
    }

    /**
     * Calculates fair dispersion point normalization (FDPN) denominator value
     * excluding all uni-grams.
     *
     * @param ngram the target n-gram.
     * @param langType the language type, either Chinese like or English like.
     * @return the value.
     */
    private double partialAvp(String ngram, int langType) {
	double result = 0.0;

	int n = Strings.getN(ngram, langType);
	for (int i = 2; i <= n - 2; i++) {
	    double px = pattree.getFrequency(Strings.getSubNGram(ngram,
								 1, i, langType));
	    double py = pattree.getFrequency(Strings.getSubNGram(ngram,
								 i + 1, n, langType));
	    result += (px * py);
	}
	result /= ((double) (n - 3));

	return result;
    }

    /**
     * Calculates association between words of the n-gram using Symmetrical
     * Conditional Probability (SCP) measure with the fair dispersion concept.
     *
     * @param ngram the target n-gram.
     * @param langType the language type, either Chinese like or English like.
     * @return the value of the association measure.
     */
    public double SCP_f(String ngram, int langType) {
	double result = 0.0;

	double pxy = pattree.getFrequency(ngram);
	double avp = this.Avp(ngram, langType);
	result = pxy * pxy / avp;

	//System.out.println(ngram + ".pxy = " + pxy);
	//System.out.println(ngram + ".avp = " + avp);
	//System.out.println(ngram + ".result = " + result);

	return result;
    }

    /**
     * Calculates association between words of the n-gram using Symmetrical
     * Conditional Probability with Context Dependency (SCPCD) measure with the
     * fair dispersion concept.
     *
     * @param ngram the target n-gram.
     * @param langType the language type, either Chinese like or English like.
     * @return the value of the association measure.
     */
    public double SCPCD_f(String ngram, int langType) {
	double result = 0.0;

	double freq = pattree.getFrequency(ngram);
	ArrayList rc = pattree.getSuffixPerplexity(ngram, 1);
	ArrayList lc = invpattree.getSuffixPerplexity(
	    Strings.inverse(ngram, langType), 1);
	double RC = ((rc.size() == 0) ? freq : rc.size());
	double LC = ((lc.size() == 0) ? freq : lc.size());
	double pxy = RC * LC;
	double avp = this.Avp(ngram, langType);
	result = pxy * pxy / avp;

	//System.out.println(ngram + ".pxy = " + pxy);
	//System.out.println(ngram + ".avp = " + avp);
	//System.out.println(ngram + ".result = " + result);

	return result;
    }

    /**
     * Calculates context independency ratio.
     *
     * @param ngram the target n-gram.
     * @param langType the language type, either Chinese like or English like.
     * @return the value of the association measure.
     */
    public double CD(String ngram, int langType) {
	double result = 0.0;

	double freq = pattree.getFrequency(ngram);
	ArrayList rc = pattree.getSuffixPerplexity(ngram, 1);
	ArrayList lc = invpattree.getSuffixPerplexity(
	    Strings.inverse(ngram, langType), 1);
	double RC = ((rc.size() == 0) ? freq : rc.size());
	double LC = ((lc.size() == 0) ? freq : lc.size());
	result = (RC * LC) / (freq * freq);

	return result;
    }

    /**
     * Calculates association between words of the n-gram using Geometric Mean of
     * Symmetrical Conditional Probability (GMSCP) measure.
     *
     * @param ngram the target n-gram.
     * @param langType the language type, either Chinese like or English like.
     * @return the value of the association measure.
     */
    public double GMSCP(String ngram, int langType) {
	double result = 0.0;

	double pxy = pattree.getFrequency(ngram);
	double gm = 1.0;
	int n = Strings.getN(ngram, langType);
	for (int i = 1; i <= n - 1; i++) {
	    double px = pattree.getFrequency(Strings.getSubNGram(ngram,
								 1, i, langType));
	    double py = pattree.getFrequency(Strings.getSubNGram(ngram,
								 i + 1, n, langType));
	    gm *= (px * py);
	}
	gm = Math.pow(gm, 1.0 / (2.0 * ((double) (n - 1))));
	result = pxy / gm;

	//System.out.println(ngram + ".pxy = " + pxy);
	//System.out.println(ngram + ".gm = " + gm);
	//System.out.println(ngram + ".result = " + result);

	return result;
    }

    /**
     * Calculates association between words of the n-gram using Geometric Mean of
     * Symmetrical Conditional Probability (GMSCP) measure with Context
     * Dependency.
     *
     * @param ngram the target n-gram.
     * @param langType the language type, either Chinese like or English like.
     * @return the value of the association measure.
     */
    public double GMSCPCD(String ngram, int langType) {
	double result = 0.0;

	double freq = pattree.getFrequency(ngram);
	double gm = 1.0;
	int n = Strings.getN(ngram, langType);
	for (int i = 1; i <= n - 1; i++) {
	    double px = pattree.getFrequency(Strings.getSubNGram(ngram,
								 1, i, langType));
	    double py = pattree.getFrequency(Strings.getSubNGram(ngram,
								 i + 1, n, langType));
	    gm *= (px * py);
	}
	gm = Math.pow(gm, 1.0 / ((double) (n - 1)));
	ArrayList rc = pattree.getSuffixPerplexity(ngram, 1);
	ArrayList lc = invpattree.getSuffixPerplexity(
	    Strings.inverse(ngram, langType), 1);
	double RC = ((rc.size() == 0) ? freq : rc.size());
	double LC = ((lc.size() == 0) ? freq : lc.size());
	double pxy = RC * LC;
	result = pxy / gm;

	//System.out.println(ngram + ".pxy = " + pxy);
	//System.out.println(ngram + ".gm = " + gm);
	//System.out.println(ngram + ".result = " + result);

	return result;
    }

    /**
     * Calculates association between words of the n-gram using the specified
     * association measure.
     *
     * @param ngram the target n-gram.
     * @param associationMeasure the association measure to be used.
     * @param langType the language type, either Chinese like or English like.
     * @return the value of the association measure.
     */
    public double glue(String ngram, int associationMeasure, int langType) {
	double result = 0.0;

	if (associationMeasure == PATTermExtraction.SCP) {
	    result = this.SCP_f(ngram, langType);
	} else if (associationMeasure == PATTermExtraction.SCPCD) {
	    result = this.SCPCD_f(ngram, langType);
	} else if (associationMeasure == PATTermExtraction.GMSCP) {
	    result = this.GMSCP(ngram, langType);
	} else if (associationMeasure == PATTermExtraction.GMSCPCD) {
	    result = this.GMSCPCD(ngram, langType);
	} else if (associationMeasure == PATTermExtraction.CD) {
	    result = this.CD(ngram, langType);
	}

	return result;
    }

    public Map[] extract(int maxN, int minFreq, int kind, double freqRatio,
			 String fileName) {
	//--------------------------------------------------
	// Map[] result = new HashMap[maxN];
	//-------------------------------------------------- 
	FileHandler[] fh = null;

	if (fileName != null) {
	    fh = new FileHandler[maxN];
	    for (int j = 0; j < maxN; j++) {
		fh[j] = new FileHandler(fileName + "_" + (j + 1) + "-gram.txt");
		if (fh[j].exists()) fh[j].delete();
	    }
	}

	//--------------------------------------------------
	// for (int j = 0; j < maxN; j++) {
	//     result[j] = new HashMap<String, Integer>();
	// }
	//-------------------------------------------------- 
	@SuppressWarnings({"unchecked"})
	Map<String, Integer>[] result = (HashMap<String, Integer>[]) 
	    java.lang.reflect.Array.newInstance(new HashMap<String, Integer>().getClass(), maxN);

	for (int j = 0; j < maxN; ++j) result[j] = new HashMap<String, Integer>();

	String word = "";
	for (int i = 2; i <= maxN; i++) {
	    ArrayList sps = pattree.getSuffixPerplexity(word, i);
	    for (int j = (sps.size() - 1); j >= 0; j--) {
		SuffixPerplexity sp = (SuffixPerplexity) sps.get(j);
		String ngram = sp.suffix.trim();
		if (sp.freq >= minFreq) {
		    ArrayList rc = pattree.getSuffixPerplexity(ngram, 1);
		    ArrayList lc = invpattree.getSuffixPerplexity(
			Strings.inverse(ngram, this.langType), 1);
		    double rfreq = ((rc.size() > 0) ?
				    ((SuffixPerplexity) rc.get(rc.size() - 1)).freq :
				    0.0);
		    double lfreq = ((lc.size() > 0) ?
				    ((SuffixPerplexity) lc.get(lc.size() - 1)).freq :
				    0.0);
		    double rratio = rfreq / ((double) sp.freq);
		    double lratio = lfreq / ((double) sp.freq);
		    if (((rc.size() >= kind) || (rc.size() == 0)) &&
			((lc.size() >= kind) || (lc.size() == 0)) &&
			((rratio <= freqRatio) || (rratio == 0)) &&
			((lratio <= freqRatio) || (lratio == 0))) {
			result[i - 1].put(ngram, new Integer(sp.freq));
			if (fh != null) {
			    fh[i - 1].println(ngram + " " + sp.freq, true);
			}
		    }
		}
	    }
	}

	return result;
    }

    /**
     * Extracts multi-word units (MWUs) from the given ngrams using the specified
     * association measure and localmaxs algorithm.
     *
     * @param associationMeasure the association measure to be used.
     * @param minFreq the minimum frequency of a MWU.
     * @param maxN the maximum number of words of a MWU.
     * @param windowSize the comparison distance of localmaxs.
     * @param fileName the output file name; <code>null</code> if no output file
     *                 is required.
     * @return the map of the MWUs.
     */
    public Map[] extract(int associationMeasure, int minFreq, int maxN,
			 int windowSize, String fileName) {
	//--------------------------------------------------
	// Map[] result = new HashMap[maxN];
	//-------------------------------------------------- 
	FileHandler[] fh = null;

	if (fileName != null) {
	    fh = new FileHandler[maxN];
	    for (int j = 0; j < maxN; j++) {
		fh[j] = new FileHandler(fileName + "_localmaxs_" + associationMeasure +
					"_" + windowSize + "_" + (j + 1) +
					"-gram.txt");
		if (fh[j].exists()) {
		    fh[j].delete();
		}
	    }
	}

	//--------------------------------------------------
	// for (int j = 0; j < maxN; j++) {
	//     result[j] = new HashMap<String, Integer>();
	// }
	//-------------------------------------------------- 
	@SuppressWarnings({"unchecked"})
	Map<String, Integer>[] result = (Map<String, Integer>[]) 
	    java.lang.reflect.Array.newInstance(new HashMap<String, Integer>().getClass(), maxN);

	String word = "";
	for (int i = 2; i <= maxN; i++) {
	    ArrayList sps = pattree.getSuffixPerplexity(word, i);
	    for (int j = (sps.size() - 1); j >= 0; j--) {
		SuffixPerplexity sp = (SuffixPerplexity) sps.get(j);
		boolean isMWU = true;
		String ngram = sp.suffix;
		if (sp.freq >= minFreq) {
		    double g = this.glue(ngram, associationMeasure, this.langType);
		    if (i >= 3) {
			String[] antStrs = Strings.getAnt(ngram, langType);
			if ((g<this.glue(antStrs[0], associationMeasure, this.langType)) ||
			    (g<this.glue(antStrs[1], associationMeasure, this.langType))) {
			    isMWU = false;
			}
		    }
		    for (int k = 1; (k <= windowSize) && isMWU; k++) {
			ArrayList sufSuccStrs = pattree.getSuffixPerplexity(ngram, k);
			ArrayList preSuccStrs = invpattree.getSuffixPerplexity(
			    Strings.inverse(ngram, this.langType), k);
			for (int m = 0; (m < sufSuccStrs.size()) && isMWU; m++) {
			    String succStr = ngram +
					     ((SuffixPerplexity) sufSuccStrs.get(m)).suffix;
			    if (g <= this.glue(succStr, associationMeasure, this.langType)) {
				isMWU = false;
			    }
			}
			for (int m = 0; (m < preSuccStrs.size()) && isMWU; m++) {
			    String succStr = Strings.inverse(((SuffixPerplexity)
							      preSuccStrs.get(m)).suffix,
							     this.langType) +
					     ngram;
			    if (g <= this.glue(succStr, associationMeasure, this.langType)) {
				isMWU = false;
			    }
			}
		    }
		    if (isMWU) {
			result[i - 1].put(ngram, new Integer(sp.freq));
			if (fh != null) {
			    fh[i - 1].println(ngram + " " + sp.freq, true);
			}
		    }
		}
	    }
	}

	return result;
    }

    public void toFile(String fileName) {
	FileHandler fh = new FileHandler(fileName);
	fh.writeObject(this);
    }

    public static void main(String[] args) {

	/*
	   //String treeFile = "pattree_result/pattree";
	   //String treeFile = "pattree_english_result/english_corpus/pattree";
	   String treeFile = "Z:/CIRB010ALL200212/pattree_result/050/pattree";
	   //String corpusDir = "localmaxs_corpus/cirb010all200212/cte/ESY";
	   //String corpusDir = "stemmed_english_corpus";
	   String corpusDir = "Z:/CIRB010ALL200212/CIRB010DOCSBYTOPIC/050";
	   int langType = Strings.ChineseLike;
	   //int langType = Strings.EnglishLike;
	 */

	FileInputStream fis = null;

	// loads term extraction properties.
	File tePropf = new File("tengpatte.properties");
	if (tePropf.exists()) {
	    System.out.println("Processing tengpatte.properties......");
	    try {
		fis = new FileInputStream(tePropf);
		Properties prop = new Properties();
		prop.load(fis);
		String treeFile = prop.getProperty("treeFile",
						   "tengpatte_result/pattree");
		String corpusDir = prop.getProperty("corpusDir", "tengpatte_corpus");
		String lt = prop.getProperty("langType", "ChineseLike");
		int langType = Strings.ChineseLike;
		if (lt.equals("EnglishLike")) {
		    langType = Strings.EnglishLike;
		}
		String am = prop.getProperty("associationMeasure", "SCPCD");
		int associationMeasure = PATTermExtraction.SCPCD;
		if (am.equals("SCP")) {
		    associationMeasure = PATTermExtraction.SCP;
		} else if (am.equals("GMSCP")) {
		    associationMeasure = PATTermExtraction.GMSCP;
		} else if (am.equals("GMSCPCD")) {
		    associationMeasure = PATTermExtraction.GMSCPCD;
		} else if (am.equals("CD")) {
		    associationMeasure = PATTermExtraction.CD;
		}
		int minFreq = Integer.parseInt(prop.getProperty("minFreq", "2"));
		int maxN = Integer.parseInt(prop.getProperty("maxN", "12"));
		int windowSize = Integer.parseInt(prop.getProperty("windowSize", "1"));

		PATTermExtraction patte = null;
		FileHandler fh = new FileHandler(treeFile + ".pat");

		long t = System.currentTimeMillis();

		if (fh.exists()) {

		    t = System.currentTimeMillis();

		    patte = (PATTermExtraction) fh.readObject();

		    t = System.currentTimeMillis() - t;
		    System.out.println("Finish reading PAT-tree in "
				       + ((double) t / 1000.0) + "sec.");
		} else {

		    t = System.currentTimeMillis();

		    patte = new PATTermExtraction(corpusDir, langType);

		    t = System.currentTimeMillis() - t;
		    System.out.println("Finish constructing PAT-tree in "
				       + ((double) t / 1000.0) + "sec.");

		    t = System.currentTimeMillis();

		    patte.toFile(fh.getAbsolutePath());

		    t = System.currentTimeMillis() - t;
		    System.out.println("Finish writing PAT-tree in "
				       + ((double) t / 1000.0) + "sec.");
		}

		/*
		   t = System.currentTimeMillis();
		   patte.extract(12, 2, 3, 0.9, treeFile);
		   t = System.currentTimeMillis() - t;
		   System.out.println("Finish extracting MWUs in "
		 + ((double) t / 1000.0) + "sec.");
		 */

		t = System.currentTimeMillis();

		//patte.extract(PATTermExtraction.SCPCD, 2, 12, 1, treeFile);
		//patte.extract(PATTermExtraction.GMSCPCD, 2, 12, 1, treeFile);
		patte.extract(associationMeasure, minFreq, maxN, windowSize, treeFile);

		t = System.currentTimeMillis() - t;
		System.out.println("Finish extracting MWUs using PAT-localmaxs in "
				   + ((double) t / 1000.0) + "sec.");
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (fis != null) {
		    try {
			fis.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		    fis = null;
		}
	    }
	} else {
	    System.out.println("There is no tengpatte.properties file -> skip");
	}

	tePropf = new File("chienpatte.properties");
	if (tePropf.exists()) {
	    System.out.println("Processing chienpatte.properties......");
	    try {
		fis = new FileInputStream(tePropf);
		Properties prop = new Properties();
		prop.load(fis);
		String treeFile = prop.getProperty("treeFile",
						   "chienpatte_result/pattree");
		String corpusDir = prop.getProperty("corpusDir", "chienpatte_corpus");
		String lt = prop.getProperty("langType", "ChineseLike");
		int langType = Strings.ChineseLike;
		if (lt.equals("EnglishLike")) {
		    langType = Strings.EnglishLike;
		}
		int minFreq = Integer.parseInt(prop.getProperty("minFreq", "2"));
		int maxN = Integer.parseInt(prop.getProperty("maxN", "12"));
		int kind = Integer.parseInt(prop.getProperty("kind", "3"));
		double freqRatio = Double.parseDouble(prop.getProperty("freqRatio",
								       "0.9"));

		PATTermExtraction patte = null;
		FileHandler fh = new FileHandler(treeFile + ".pat");

		long t = System.currentTimeMillis();

		if (fh.exists()) {

		    t = System.currentTimeMillis();

		    patte = (PATTermExtraction) fh.readObject();

		    t = System.currentTimeMillis() - t;
		    System.out.println("Finish reading PAT-tree in "
				       + ((double) t / 1000.0) + "sec.");
		} else {

		    t = System.currentTimeMillis();

		    patte = new PATTermExtraction(corpusDir, langType);

		    t = System.currentTimeMillis() - t;
		    System.out.println("Finish constructing PAT-tree in "
				       + ((double) t / 1000.0) + "sec.");

		    t = System.currentTimeMillis();

		    patte.toFile(fh.getAbsolutePath());

		    t = System.currentTimeMillis() - t;
		    System.out.println("Finish writing PAT-tree in "
				       + ((double) t / 1000.0) + "sec.");
		}

		t = System.currentTimeMillis();

		patte.extract(maxN, minFreq, kind, freqRatio, treeFile);

		t = System.currentTimeMillis() - t;
		System.out.println("Finish extracting MWUs in "
				   + ((double) t / 1000.0) + "sec.");

	//--------------------------------------------------
	//        t = System.currentTimeMillis();
	//        //patte.extract(PATTermExtraction.SCPCD, 2, 12, 1, treeFile);
	//        //patte.extract(PATTermExtraction.GMSCPCD, 2, 12, 1, treeFile);
	//        patte.extract(associationMeasure, minFreq, maxN, windowSize, treeFile);
	//        t = System.currentTimeMillis() - t;
	//        System.out.println("Finish extracting MWUs using PAT-localmaxs in "
	// 	     + ((double) t / 1000.0) + "sec.");
	//-------------------------------------------------- 
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (fis != null) {
		    try {
			fis.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		    fis = null;
		}
	    }
	} else {
	    System.out.println("There is no chienpatte.properties file -> skip");
	}
    }
}
