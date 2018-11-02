package models;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

import util.Document;
import util.Pair;
import util.Sampler;
import util.Sense;
import util.Term;
import util.TermIndexer;
import util.Topic;

public class WPSTM {
	
	private ArrayList<Document> docs;
	private ArrayList<Topic> topics;
	private ArrayList<Sense> senses;
	private double alpha, beta, gamma;
	private int numTopics, numSenses, numTerms;
	
	private TermIndexer indexer;
	private int targetIndex;
	
	public WPSTM(ArrayList<String> data, String target, int numSenses, int numTopics, double alpha, double beta, double gamma) {
		this.numSenses = numSenses;
		this.numTopics = numTopics;
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		
		indexer = new TermIndexer();
		targetIndex = indexer.getIndex(target);
		
		docs = new ArrayList<Document>();
		for(String instance : data) {
			String[] split = instance.split("\t", -1);
			String id = split[0];
			String[] localTermArray = split[1].split(" ", -1);
			String[] globalTermArray = split[2].split(" ", -1);
			ArrayList<Term> localTerms = new ArrayList<Term>();
			ArrayList<Term> globalTerms = new ArrayList<Term>();
			
			for(String localTerm : localTermArray) {
				int localTermIndex = indexer.getIndex(localTerm);
				localTerms.add(new Term(localTerm, localTermIndex));
			}
			
			for(String globalTerm : globalTermArray) {
				int globalTermIndex = indexer.getIndex(globalTerm);
				globalTerms.add(new Term(globalTerm, globalTermIndex));
			}
			
			docs.add(new Document(id, instance, localTerms, globalTerms, targetIndex, numSenses, numTopics, 2));
		}
		numTerms = indexer.getNumTerms();
		
		senses = new ArrayList<Sense>();
		for(int i=0; i<numSenses; i++)
			senses.add(new Sense(i, numTerms, numTopics));
		
		topics = new ArrayList<Topic>();
		for(int i=0; i<numTopics; i++)
			topics.add(new Topic(i, numTerms, numSenses));
		
		System.out.println("number of terms: " + numTerms);
		System.out.println("number of documents: " + docs.size());
	}
	
	public TermIndexer getTermIndexer() {
		return indexer;
	}
	
	public boolean initialize() {
		try {
			for(Document doc : docs) {
				ArrayList<Term> localTerms = doc.localTerms;
				
				for(Term localTerm : localTerms) {
					int termTopic = (int)(Math.random() * numTopics);
					int termSense = (int)(Math.random() * numSenses);
					
					localTerm.topic = termTopic;
					localTerm.sense = termSense;
					
					doc.nDT[termTopic]++;
					doc.nDS[termSense]++;
					doc.sDT++;
					doc.sDS++;

					topics.get(termTopic).nTW[localTerm.index]++;
					topics.get(termTopic).nTS[termSense]++;
					topics.get(termTopic).sTW++;
					topics.get(termTopic).sTS++;

					senses.get(termSense).nSW[localTerm.index]++;
					senses.get(termSense).nSW[targetIndex]++;
					senses.get(termSense).nST[termTopic]++;
					senses.get(termSense).jST[termTopic]++;
					senses.get(termSense).sSW+=2;
					senses.get(termSense).sST++;
				}
				
				ArrayList<Term> globalTerms = doc.globalTerms;
				
				for(Term globalTerm : globalTerms) {
					int termSwitch = (int)(Math.random() * 2);
					
					if(termSwitch == 0) {
						int termTopic = (int)(Math.random() * numTopics);
						
						globalTerm.topic = termTopic;
						globalTerm.sense = -1;
						
						doc.nDT[termTopic]++;
						doc.sDT++;
						
						topics.get(termTopic).nTW[globalTerm.index]++;
						topics.get(termTopic).sTW++;
					}
					else {
						int termSense = (int)(Math.random() * numSenses);
						
						globalTerm.topic = -1;
						globalTerm.sense = termSense;
						
						doc.nDS[termSense]++;
						doc.sDS++;
						
						senses.get(termSense).nSW[globalTerm.index]++;
						senses.get(termSense).sSW++;
					}
					
					doc.nDSw[termSwitch]++;
					doc.sDSw++;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean estimate(int numIters) {
		try {
			for(int iter=0; iter<numIters; iter++) {
				if(iter%100 == 0)
					System.out.println("iteration@" + iter);
				
				for(Document doc : docs) {
					ArrayList<Term> localTerms = doc.localTerms;
					
					for(Term localTerm : localTerms) {
						int oldTopic = localTerm.topic;
						int oldSense = localTerm.sense;
						
						doc.nDT[oldTopic]--;
						doc.nDS[oldSense]--;
						doc.sDT--;
						doc.sDS--;
						
						topics.get(oldTopic).nTW[localTerm.index]--;
						topics.get(oldTopic).nTS[oldSense]--;
						topics.get(oldTopic).sTW--;
						topics.get(oldTopic).sTS--;
						
						senses.get(oldSense).nSW[localTerm.index]--;
						senses.get(oldSense).nSW[targetIndex]--;
						senses.get(oldSense).nST[oldTopic]--;
						senses.get(oldSense).jST[oldTopic]--;
						senses.get(oldSense).sSW-=2;
						senses.get(oldSense).sST--;
						
						double[] p = new double[numSenses*numTopics];
						
						for(int sense=0; sense<numSenses; sense++) {
							for(int topic=0; topic<numTopics; topic++) {
								double pDT = (doc.nDT[topic] + alpha) / (doc.sDT + alpha*numTopics);
								double pTW = (topics.get(topic).nTW[localTerm.index] + beta) / (topics.get(topic).sTW + beta*numTerms);
								double pDS = (doc.nDS[sense] + alpha) / (doc.sDS + alpha*numSenses);
								double pSW = (senses.get(sense).nSW[localTerm.index] + beta) / (senses.get(sense).sSW + beta*numTerms) *
									         (senses.get(sense).nSW[targetIndex] + beta) / (senses.get(sense).sSW + beta*numTerms + 1);
								double pST = (senses.get(sense).nST[topic] + alpha) / (senses.get(sense).sST + alpha*numTopics);
								double pTS = (topics.get(topic).nTS[sense] + alpha) / (topics.get(topic).sTS + alpha*numSenses);
								double jST = (senses.get(sense).jST[topic] + alpha) / (numTopics*numSenses-1 + alpha*numTopics*numSenses);
								
//								System.out.println(sense + " " + topic + " " + (sense*numTopics+topic));
								p[sense*numTopics+topic] = pDT * pTW * pDS * pSW * pST * pTS * jST;
							}
						}
						
						int newPair = Sampler.sampleMultinomial(p);
						int newSense = newPair/numTopics;
						int newTopic = newPair%numTopics;
						
						if(newTopic == -1) {
							for(int i=0; i<p.length; i++)
								System.out.print(p[i] + " ");
							System.out.println();
							System.out.println(newPair + " " + numTopics);
							System.out.println(newSense + " " + newTopic);
						}
						
						doc.nDT[newTopic]++;
						doc.nDS[newSense]++;
						doc.sDT++;
						doc.sDS++;
						
						topics.get(newTopic).nTW[localTerm.index]++;
						topics.get(newTopic).nTS[newSense]++;
						topics.get(newTopic).sTW++;
						topics.get(newTopic).sTS++;
						
						senses.get(newSense).nSW[localTerm.index]++;
						senses.get(newSense).nSW[targetIndex]++;
						senses.get(newSense).nST[newTopic]++;
						senses.get(newSense).jST[newTopic]++;
						senses.get(newSense).sSW+=2;
						senses.get(newSense).sST++;
						
						localTerm.topic = newTopic;
						localTerm.sense = newSense;
					}
					
					ArrayList<Term> globalTerms = doc.globalTerms;
					
					for(Term globalTerm : globalTerms) {
						int oldTopic = globalTerm.topic;
						int oldSense = globalTerm.sense;
						
						if(oldTopic != -1) {
							doc.nDT[oldTopic]--;
							doc.nDSw[0]--;
							doc.sDT--;
							doc.sDSw--;
							
							topics.get(oldTopic).nTW[globalTerm.index]--;
							topics.get(oldTopic).sTW--;
						}
						else {
							doc.nDS[oldSense]--;
							doc.nDSw[1]--;
							doc.sDS--;
							doc.sDSw--;
							
							senses.get(oldSense).nSW[globalTerm.index]--;
							senses.get(oldSense).sSW--;
						}
						
						double[] p = new double[numSenses + numTopics];
						
						for(int topic=0; topic<numTopics; topic++) {
							double pDT = (doc.nDT[topic] + alpha) / (doc.sDT + alpha*numTopics);
							double pTW = (topics.get(topic).nTW[globalTerm.index] + beta) / (topics.get(topic).sTW + beta*numTerms);
							double pDSw = (doc.nDSw[0] + gamma) / (doc.sDSw + gamma*2);
						
							p[topic] = pDT * pTW * pDSw;
						}						
						for(int sense=0; sense<numSenses; sense++) {
							double pDS = (doc.nDS[sense] + alpha) / (doc.sDS + alpha*numSenses);
							double pSW = (senses.get(sense).nSW[globalTerm.index] + beta) / (senses.get(sense).sSW + beta*numTerms);
							double pDSw = (doc.nDSw[1] + gamma) / (doc.sDSw + gamma*2);
							
							p[numTopics + sense] = pDS * pSW * pDSw;
						}
						
						int newPair = Sampler.sampleMultinomial(p);
						int newTopic = -1, newSense = -1;
						if(newPair < numTopics) {
							newTopic = newPair;
							
							doc.nDT[newTopic]++;
							doc.nDSw[0]++;
							doc.sDT++;
							doc.sDSw++;

							topics.get(newTopic).nTW[globalTerm.index]++;
							topics.get(newTopic).sTW++;
						}
						else {
							newSense = newPair - numTopics;

							doc.nDS[newSense]++;
							doc.nDSw[1]++;
							doc.sDS++;
							doc.sDSw++;

							senses.get(newSense).nSW[globalTerm.index]++;
							senses.get(newSense).sSW++;
						}
						
						globalTerm.topic = newTopic;
						globalTerm.sense = newSense;
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * for WePS data
	 */
	public void printWePS(String filename, double threshold) throws Exception {
		ArrayList<ArrayList<String>> senseDocuments = new ArrayList<ArrayList<String>>();
		for(int sense=0; sense<numSenses; sense++)
			senseDocuments.add(new ArrayList<String>());
		
		for(Document doc : docs) {
			String id = doc.id;
			
			for(int sense=0; sense<numSenses; sense++) {
				double prob = (doc.nDS[sense] + alpha) / (doc.sDS + alpha*numSenses);
				if(prob > threshold)
					senseDocuments.get(sense).add(id);
			}
		}
		
		PrintWriter printer = new PrintWriter(new FileWriter(filename));
		printer.println("<clustering>");
		for(int sense=0; sense<numSenses; sense++) {
			ArrayList<String> documents = senseDocuments.get(sense);
			if(documents.size() == 0) continue;
			
			printer.println("<entity id=\"" + sense + "\">");
			for(String document : documents)
				printer.println("<doc rank=\"" + document + "\"/>");
			printer.println("</entity>");
		}
		printer.println("</clustering>");
		printer.close();
	}
	
	/**
	 * for SemEval data
	 */
	public void printSemEval(String filename, String target) throws Exception {
		PrintWriter printer = new PrintWriter(new FileWriter(filename, true));
		for(Document doc : docs) {
			String id = doc.id;
			printer.print(target + " " + id);
			
			TreeSet<Pair<Double, Integer>> set = new TreeSet<Pair<Double, Integer>>(Collections.reverseOrder());
			
			for(int sense=0; sense<numSenses; sense++) {
				double prob = (doc.nDS[sense] + 0.0) / (doc.sDS + 0.0);
				set.add(new Pair<Double, Integer>(prob, sense));
			}
			
			for(Pair<Double, Integer> pair : set) {
				if(pair.first < 1e-9) continue;
				printer.print(" " + target + "." + pair.second + "/" + pair.first);
			}
			
			printer.println();
		}
		printer.close();
	}

}
