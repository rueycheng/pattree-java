package jackteng.pattree;

import java.util.*;
import jackteng.util.*;
import jackteng.file.*;

/**
 * <p>Description: An implementation of PAT-tree.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Institute of Information Science, Academia Sinica</p>
 * <p>Create Date: 2003/05/15</p>
 * <p>Last Modified Date: 2003/05/21</p>
 * @author Jei-Wen Teng
 * @version 1.0
 */
public class PATTree implements java.io.Serializable {

    public PATNode tree = null;
    protected int nextPos = 0;
    protected String dataStrs = "";
    private int langType = Strings.ChineseLike;
    private int bitLen = 16;

    public PATTree() { }

    public PATTree(int langType) {
	this.langType = langType;
	if (langType == Strings.ChineseLike) {
	    bitLen = 16;
	} else if (langType == Strings.EnglishLike) {
	    bitLen = 8;
	}
    }

    public PATNode search(String str) {
	PATNode result = null;
	BitSet bits = Bits.toBitSet(str.getBytes());

	if (tree != null) {
	    result = tree.left;
	    int lastCB = 0;
	    int currentCB = result.CB;

	    /*
	     * traverses nodes until we find the external node which is the node whose
	     * comparison bit is less than or equal to the parent (previous searched)
	     * node.
	     */
	    while (currentCB > lastCB) {
		if (bits.get(currentCB - 1)) {
		    result = result.right;
		} else {
		    result = result.left;
		}
		lastCB = currentCB;
		currentCB = result.CB;
	    }
	    int ei = dataStrs.indexOf(",", result.dataPos);
	    if (ei == -1) {
		ei = dataStrs.length();
	    }
	    if (!dataStrs.substring(result.dataPos, ei).equals(str)) {
		result = null;
	    }
	}

	return result;
    }

    private void updateDataStrs(String data) {
	if (this.search(data) == null) {
	    if (dataStrs.equals("")) {
		dataStrs = data;
	    } else {
		dataStrs = dataStrs + "," + data;
	    }
	    nextPos = nextPos + 1 + data.length();
	}
    }

    /**
     * Inserts the data string and its suffix strings into the PAT-tree.
     *
     * @param data the data string to be inserted.
     * @return the node generated for the data string after the insertion
     *         operation.
     */
    public PATNode insert(String data) {
	PATNode result = null;
	int dataPos = this.nextPos;

	this.updateDataStrs(data);
	if (tree == null) {
	    tree = new PATNode();
	    result = tree;
	} else {
	    result = this.insertSuffixStr(data, dataPos);
	}
	int n = Strings.getN(data, this.langType);
	for (int i = 2; i <= n; i++) {
	    this.insertSuffixStr(Strings.getSubNGram(data, i, n, this.langType),
				 dataPos + this.getDataPosOffset(data, i - 1, this.langType));
	}

	return result;
    }

    private int getDataPosOffset(String data, int offset, int langType) {
	int result = offset;

	if (langType == Strings.EnglishLike) {
	    result = Strings.indexOf(data, " ", 0, offset) + 1;
	}

	return result;
    }

    /**
     * Inserts the suffix string into the PAT-tree.
     *
     * @param data the suffix string to be inserted.
     * @param dataPos the data position of the suffix string.
     * @return the node generated for the suffix string after the insertion
     *         operation.
     */
    private PATNode insertSuffixStr(String data, int dataPos) {
	PATNode result = null;
	BitSet bits = Bits.toBitSet(data.getBytes());

	result = tree.left;
	int lastCB = 0;
	int currentCB = result.CB;
	//--------------------------------------------------
	// ArrayList searchList = new ArrayList();
	//-------------------------------------------------- 
	ArrayList<PATNode> searchList = new ArrayList<PATNode>();
	searchList.add(tree);
	searchList.add(result);

	/*
	 * traverses nodes until we find the external node which is the node whose
	 * comparison bit is less than or equal to the parent (previous searched)
	 * node and adds these nodes into a search list.
	 */
	while (currentCB > lastCB) {
	    if (bits.get(currentCB - 1)) {
		result = result.right;
	    } else {
		result = result.left;
	    }
	    lastCB = currentCB;
	    currentCB = result.CB;
	    searchList.add(result);
	}
	int ei = dataStrs.indexOf(",", result.dataPos);
	if (ei == -1) {
	    ei = dataStrs.length();
	}
	String nodeData = dataStrs.substring(result.dataPos, ei);
	if (nodeData.equals(data)) {
	    result.freq++;

	    // increments the frequency of external nodes of the searched internal
	    // nodes in the search list (i.e. all nodes in the search list except the
	    // last one which is the external node found).
	    for (int i = 0; i < searchList.size() - 1; i++) {
		((PATNode) searchList.get(i)).freqOfExternalNodes++;
	    }
	} else {
	    boolean found = false;
	    PATNode childNode = null;                 // child of the new node.
	    PATNode parentNode = null;                  // parent of the new node.
	    currentCB = Bits.getComparisonBit(bits,
					      Bits.toBitSet(nodeData.getBytes()));
	    for (int i = 0; (i < searchList.size()) && !found; i++) {
		parentNode = childNode;
		childNode = (PATNode) searchList.get(i);
		if (childNode.CB >= currentCB) {                      // the position to insert the new node
		    // is found, which is before the node
		    // whose comparison bit is larger than
		    // or equal to the new node.
		    found = true;
		} else {                      // the internal node whose comparison bit is less than the new
		    // node.

		    // increments the frequency of external nodes of the searched internal
		    // nodes in the search list (i.e. all nodes in the search list except
		    // the last one which is the external node found).
		    if (i != (searchList.size() - 1)) {
			childNode.freqOfExternalNodes++;
		    }
		}
	    }
	    result = new PATNode(dataPos, currentCB, 0, 1, null, null);
	    if (bits.get(currentCB - 1)) {
		result.left = childNode;
		result.right = result;
	    } else {
		result.left = result;
		result.right = childNode;
	    }
	    if (currentCB < result.left.CB) {                 // the left child node is an internal
		// node.
		result.freqOfExternalNodes += result.left.freqOfExternalNodes;
	    } else {                 // the left child node is an external node.
		result.freqOfExternalNodes += result.left.freq;
	    }
	    if (currentCB < result.right.CB) {                 // the right child node is an internal
		// node.
		result.freqOfExternalNodes += result.right.freqOfExternalNodes;
	    } else {                 // the right child node is an external node.
		result.freqOfExternalNodes += result.right.freq;
	    }
	    if ((parentNode.CB == 0) || (!bits.get(parentNode.CB - 1))) {
		parentNode.left = result;
	    } else {
		parentNode.right = result;
	    }
	}

	return result;
    }

    public int getFrequency(String word) {
	int result = 0;
	BitSet bits = Bits.toBitSet(word.getBytes());
	int wordBitLen = word.getBytes().length * 8;

	PATNode node = tree.left;
	int lastCB = 0;
	int currentCB = node.CB;

	/*
	 * traverses nodes until we find the external node, which is the node whose
	 * comparison bit is less than or equal to the parent (previous searched)
	 * node, or the node whose comparison bit is larger than the number
	 * of bits in the word.
	 */
	while ((currentCB > lastCB) && (currentCB <= wordBitLen)) {
	    if (bits.get(currentCB - 1)) {
		node = node.right;
	    } else {
		node = node.left;
	    }
	    lastCB = currentCB;
	    currentCB = node.CB;
	}
	int ei = node.dataPos + word.length();
	if ((ei <= dataStrs.length()) &&
	    dataStrs.substring(node.dataPos, ei).equals(word)) {                          // word is found.
	    if (currentCB > wordBitLen) {                 // the node matching the word is an internal
		// node.
		result = node.freqOfExternalNodes;
	    } else {                  // the node matching the word is an external node.
		result = node.freq;
	    }
	}

	return result;
    }

    private int getSuffixLen(String data, String word, int suffixLen,
			     int langType) {
	int result = suffixLen;

	if (langType == Strings.EnglishLike) {
	    if (data.length() <= word.length()) {
		result = -1;
	    } else {
		if (data.charAt(word.length()) != ' ') {
		    result = suffixLen - 1;
		}
		int prevIndex = ((result == 0) ? word.length() :
				 Strings.indexOf(data, " ", word.length(), result));
		int nextIndex = Strings.indexOf(data, " ", word.length(), result + 1);
		if (nextIndex == -1) {
		    if (prevIndex == -1) {
			result = -1;
		    } else {
			result = data.length() - word.length();
		    }
		} else {
		    result = nextIndex - word.length();
		}
	    }
	}

	return result;
    }

    public ArrayList getSuffixPerplexity(String word, int suffixLen) {
	//--------------------------------------------------
	// ArrayList result = new ArrayList();
	//-------------------------------------------------- 
	ArrayList<SuffixPerplexity> result = new ArrayList<SuffixPerplexity>();
	BitSet bits = Bits.toBitSet(word.getBytes());
	int wordBitLen = word.getBytes().length * 8;
	//int maxCB = wordBitLen + suffixLen * this.bitLen;

	if (tree != null) {
	    PATNode node = tree.left;
	    int lastCB = 0;
	    int currentCB = node.CB;

	    /*
	     * traverses nodes until we find the external node, which is the node
	     * whose comparison bit is less than or equal to the parent (previous
	     * searched) node, or the node whose comparison bit is larger than the
	     * number of bits in the word.
	     */
	    while ( (currentCB > lastCB) && (currentCB <= wordBitLen)) {
		if (bits.get(currentCB - 1)) {
		    node = node.right;
		}
		else {
		    node = node.left;
		}
		lastCB = currentCB;
		currentCB = node.CB;
	    }
	    int ei = node.dataPos + word.length();
	    if ( (ei <= dataStrs.length()) &&
		 dataStrs.substring(node.dataPos, ei).equals(word)) {                            // word is found.
		ei = dataStrs.indexOf(",", node.dataPos);
		if (ei == -1) {
		    ei = dataStrs.length();
		}
		String data = dataStrs.substring(node.dataPos, ei);
		if (currentCB <= lastCB) {                     // the word is found in an external node.
		    int realSuffixLen = this.getSuffixLen(data, word, suffixLen,
							  this.langType);
		    if ( (data.length() >= (word.length() + realSuffixLen)) &&
			 (realSuffixLen != -1)) {
			String suffix = data.substring(word.length(),
						       word.length() + realSuffixLen);
			result.add(new SuffixPerplexity(suffix, node.freq));
		    }
		}
		else {                     // the word is found in an internal node.
		//--------------------------------------------------
		//     Stack s = new Stack();
		//-------------------------------------------------- 
		    Stack<PATNode> s = new Stack<PATNode>();
		    s.push(node);
		    while (!s.empty()) {
			node = (PATNode) s.pop();
			ei = dataStrs.indexOf(",", node.dataPos);
			if (ei == -1) {
			    ei = dataStrs.length();
			}
			data = dataStrs.substring(node.dataPos, ei);
			int realSuffixLen = this.getSuffixLen(data, word, suffixLen,
							      this.langType);
			int maxCB = wordBitLen + realSuffixLen * this.bitLen;
			if ( (node.CB > maxCB) && (realSuffixLen != -1)) {
			    String suffix = data.substring(word.length(),
							   word.length() + realSuffixLen);
			    result.add(new SuffixPerplexity(suffix,node.freqOfExternalNodes));
			}
			else {
			    ei = dataStrs.indexOf(",", node.left.dataPos);
			    if (ei == -1) {
				ei = dataStrs.length();
			    }
			    data = dataStrs.substring(node.left.dataPos, ei);
			    realSuffixLen = this.getSuffixLen(data, word, suffixLen,
							      this.langType);
			    if (node.left.CB > node.CB) {
				s.push(node.left);
			    }
			    else {
				if ( (data.length() >= (word.length() + realSuffixLen)) &&
				     (realSuffixLen != -1)) {
				    String suffix = data.substring(word.length(),
								   word.length() + realSuffixLen);
				    result.add(new SuffixPerplexity(suffix, node.left.freq));
				}
			    }
			    ei = dataStrs.indexOf(",", node.right.dataPos);
			    if (ei == -1) {
				ei = dataStrs.length();
			    }
			    data = dataStrs.substring(node.right.dataPos, ei);
			    realSuffixLen = this.getSuffixLen(data, word, suffixLen,
							      this.langType);
			    if (node.right.CB > node.CB) {
				s.push(node.right);
			    }
			    else {
				if ( (data.length() >= (word.length() + realSuffixLen)) &&
				     (realSuffixLen != -1)) {
				    String suffix = data.substring(word.length(),
								   word.length() + realSuffixLen);
				    result.add(new SuffixPerplexity(suffix, node.right.freq));
				}
			    }
			}
		    }
		}
	    }

	    Collections.sort(result, new Comparator<SuffixPerplexity>() {
		public int compare(SuffixPerplexity lhs, SuffixPerplexity rhs) {
		    return lhs.freq - rhs.freq;
		}
	    });
	}

	return result;
    }

    public String getDataStrs() {
	return this.dataStrs;
    }

    public void print() {
	if (tree != null) {
	    System.out.println("Data Stream: " + dataStrs);
	    System.out.println(tree.dataPos + "(" + tree.CB + ", " +
			       tree.freqOfExternalNodes + ", " + tree.freq + ")");
	    System.out.println("left: " + tree.left.dataPos);
	    if (tree.left.CB > tree.CB) {
		this.printTree(tree.left);
	    }
	}
    }

    private void printTree(PATNode t) {
	System.out.println(t.dataPos + "(" + t.CB + ", " +
			   t.freqOfExternalNodes + ", " + t.freq + ")");
	System.out.println("left: " + t.left.dataPos);
	System.out.println("right: " + t.right.dataPos);
	if (t.left.CB > t.CB) {
	    this.printTree(t.left);
	}
	if (t.right.CB > t.CB) {
	    this.printTree(t.right);
	}
    }

    public void toFile(String fileName) {
	FileHandler fh = new FileHandler(fileName);
	fh.writeObject(this);
    }

}
