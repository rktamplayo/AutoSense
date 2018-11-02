package util;

public class Term {
	
	public String term;
	public int index;
	public int topic, sense, table;
	
	public Term(String term, int index) {
		this.term = term;
		this.index = index;
		topic = -1;
		sense = -1;
		table = -1;
	}

}
