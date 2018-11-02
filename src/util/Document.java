package util;

import java.util.ArrayList;

public class Document {
	
	public String id, document;
	public ArrayList<Term> terms;
	public double[] nDT;
	public double sDT;
	
	public int sense;
	
	/**
	 * LDA & HC
	 */
	public Document(String id, String document, ArrayList<Term> terms, int numTopics) {
		this.id = id;
		this.document = document;
		this.terms = new ArrayList<Term>(terms);
		nDT = new double[numTopics];
		sDT = 0;
		
		sense = -1;
	}
	
	public ArrayList<Term> localTerms, globalTerms;
	public double[] nDS;
	public double sDS;
	
	/**
	 * STM
	 */
	public Document(String id, String document, ArrayList<Term> localTerms, ArrayList<Term> globalTerms, int numSenses, int numTopics) {
		this.id = id;
		this.document = document;
		this.localTerms = new ArrayList<Term>(localTerms);
		this.globalTerms = new ArrayList<Term>(globalTerms);
		nDT = new double[numTopics];
		nDS = new double[numSenses];
		sDT = 0; sDS = 0;
	}
	
	public double[] nDSw;
	public double sDSw;
	
	/**
	 * SPTM
	 */
	public Document(String id, String document, ArrayList<Term> localTerms, ArrayList<Term> globalTerms, int targetIndex, int numSenses, int numTopics, int numSwitches) {
		this(id, document, localTerms, globalTerms, numSenses, numTopics);
		nDSw = new double[numSwitches];
		sDSw = 0;
	}
	
	public int numTables;
	public int[] tableToTopic;
	public int[] nDTa;
	
	/**
	 * HDP
	 */
	public Document(String id, String document, ArrayList<Term> terms) {
		this.id = id;
		this.document = document;
		this.terms = new ArrayList<Term>(terms);
		numTables = 0;
		nDTa = new int[2];
		tableToTopic = new int[2];
	}
	
	public void defragment(int[] kOldToKNew) {
		System.out.println("tables: " + numTables);
		
		int[] tOldToTNew = new int[numTables];
		int newNumTables = 0;
		for(int table=0; table<numTables; table++) {
			if(nDTa[table] > 0) {
				tOldToTNew[table] = newNumTables;
				tableToTopic[newNumTables] = kOldToKNew[tableToTopic[table]];
				int temp = nDTa[newNumTables];
				nDTa[newNumTables] = nDTa[table];
				nDTa[table] = temp;
				newNumTables++;
			}
			else
				tableToTopic[table] = -1;
		}
		numTables = newNumTables;
		for(Term term : terms) {
			System.out.println("term table: " + term.table);
			term.table = tOldToTNew[term.table];
		}
	}

}
