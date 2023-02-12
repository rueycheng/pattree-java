package jackteng.pattree;

/**
 * <p>Description: The node of PAT-tree.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Institute of Information Science, Academia Sinica</p>
 * <p>Create Date: 2003/05/14</p>
 * <p>Last Modified Date: 2003/05/15</p>
 * @author Jei-Wen Teng
 * @version 1.0
 */
public class PATNode implements java.io.Serializable {
    public int dataPos = 0;          // the real string.
    public int CB = 0;          // the comparison bit.
    public int freqOfExternalNodes = 1;          // total frequency of all external nodes.
    public int freq = 1;          // frequency.
    public PATNode left = this;
    public PATNode right = null;

    public PATNode() {
    }

    public PATNode(int dataPos, int CB, int numExternalNodes, int freq,
		   PATNode left, PATNode right) {
	this.dataPos = dataPos;
	this.CB = CB;
	this.freqOfExternalNodes = numExternalNodes;
	this.freq = freq;
	this.left = left;
	this.right = right;
    }
}
