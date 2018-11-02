package util;

public class Sampler {
	
	public static int sampleMultinomial(double[] p) {
		for(int i=1; i<p.length; i++) p[i] += p[i-1];
		double prob = Math.random() * p[p.length-1];
		for(int i=0; i<p.length; i++)
			if(prob <= p[i]) return i;
		return -1;
	}

}
