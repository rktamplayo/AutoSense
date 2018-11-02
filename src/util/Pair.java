package util;

public class Pair<L extends Comparable<? super L>, R extends Comparable<? super R>> implements Comparable<Pair<L, R>> {
	
	public L first;
	public R second;
	
	public Pair(L first, R second) {
		this.first = first;
		this.second = second;
	}
	
	public int compareTo(Pair<L, R> p) {
		if(!first.equals(p.first))
			return first.compareTo(p.first);
		else
			return second.compareTo(p.second);
	}
	
	public String toString() {
		return "(" + first.toString() + "," + second.toString() + ")";
	}
}
