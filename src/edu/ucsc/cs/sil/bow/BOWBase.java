package edu.ucsc.cs.sil.bow;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public abstract class BOWBase {
	protected Map<String, Integer> termFreq = new TreeMap<String, Integer>();
	protected String prefix;

	public BOWBase(String prefix){
		this.prefix = prefix;		
	}

	public Map<String, Integer> getTermFreq() {
		return termFreq;
	}

	public void add(String s){
		List<String> tokens = tokenize(s);
		for(String t : tokens){
			if(t.equals(""))
				continue;
			String key = prefix+"__"+t;
			Integer freq = termFreq.get(key);
			if(freq == null){
				freq = 0;
			}
			termFreq.put(key, freq+1);
		}
	}
	
	public abstract List<String> tokenize(String s);
	
}
