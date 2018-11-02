package util;

import java.util.TreeMap;

public class TermIndexer {
	
	private int numTerms;
	private TreeMap<String, Integer> termToIndex;
	private TreeMap<Integer, String> indexToTerm;
	
	public TermIndexer() {
		numTerms = 0;
		termToIndex = new TreeMap<String, Integer>();
		indexToTerm = new TreeMap<Integer, String>();
	}
	
	public int getIndex(String term) {
		if(termToIndex.containsKey(term))
			return termToIndex.get(term);
		else {
			termToIndex.put(term, numTerms);
			indexToTerm.put(numTerms, term);
			numTerms++;
			return termToIndex.get(term);
		}
	}
	
	public String getTerm(int index) {
		if(indexToTerm.containsKey(index))
			return indexToTerm.get(index);
		else
			return null;
	}
	
	public int getNumTerms() {
		return numTerms;
	}

}
