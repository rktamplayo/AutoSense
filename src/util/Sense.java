package util;

public class Sense {
	
	public int sense;
	public double[] nSW, nST, jST;
	public double sSW, sST;
	
	public Sense(int sense, int numTerms, int numTopics) {
		this.sense = sense;
		if(numTerms != -1)
			nSW = new double[numTerms];
		nST = new double[numTopics];
		jST = new double[numTopics];
		sSW = 0; sST = 0;
	}

}
