package edu.ucsc.cs.sil.bow;

import java.util.*;

public class BagOfWordsPlus extends BOWBase{
	private static String javaCOperators[] = { "+", "-", "*", "/", "%", ">", ">=", "<",
		"<=", "==", "!=", "<<", ">>", ">>>", "+=", "-=", "*=", "/=", "%=",
		"&=", "|=", "^=", "<<=", ">>=", ">>>=", "&&", "||", "!", "&", "|",
		"^", "++", "--", "~", "->" };
	
	public BagOfWordsPlus(String prefix){
		super(prefix);
	}
	
	public List<String> tokenize(String s) {
		Set<String> javaCOperatorSet = new TreeSet<String>(Arrays.asList(javaCOperators));
		List<String> wordsList = new ArrayList<String>();
		String splits[] = s.split("[\\W_]+");
		for (int i = 0; i < splits.length; i++) {
			if (splits[i].length() > 0) {
				wordsList.add(splits[i].toLowerCase());
			}
		}

		// Looking for op
		String splits2[] = s.split("\\s+");
		for (int i = 0; i < splits2.length; i++) {
			if (javaCOperatorSet.contains(splits2[i])) {
				wordsList.add(splits2[i]);
			}
		}
		return wordsList;
	}


}
