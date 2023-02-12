package jackteng.pattree;

/**
 * <p>Description: The information of suffix perplexity.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Institute of Information Science, Academia Sinica</p>
 * <p>Create Date: 2003/05/19</p>
 * <p>Last Modified Date: 2003/05/19</p>
 * @author Jei-Wen Teng
 * @version 1.0
 */
public class SuffixPerplexity {
    public String suffix = "";
    public int freq = 0;

    public SuffixPerplexity(String suffix, int freq) {
	this.suffix = suffix;
	this.freq = freq;
    }
}
