package util;

public class Topic {
	
	public int topic;
	public double[] nTW;
	public double sTW;
	public int numTables;
	
	public Topic(int topic, int numTerms) {
		this.topic = topic;
		nTW = new double[numTerms];
		sTW = 0;
		numTables = 0;
	}
	
	public double[] nTS;
	public double sTS;
	
	public Topic(int topic, int numTerms, int numSenses) {
		this.topic = topic;
		nTW = new double[numTerms];
		nTS = new double[numSenses];
		sTW = 0; sTS = 0;
	}

}
