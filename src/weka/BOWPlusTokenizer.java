package weka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import weka.core.tokenizers.Tokenizer;

public class BOWPlusTokenizer extends Tokenizer {

	private static final long serialVersionUID = -8480868123745598496L;
	static String javaCOperators[] = { "+", "-", "*", "/", "%", ">", ">=", "<",
		"<=", "==", "!=", "<<", ">>", ">>>", "+=", "-=", "*=", "/=", "%=",
		"&=", "|=", "^=", "<<=", ">>=", ">>>=", "&&", "||", "!", "&", "|",
		"^", "++", "--", "~", "->" };
	private Iterator<String> iterator;
	@Override
	public String getRevision() {
		return "0.1";
	}

	@Override
	public String globalInfo() {
		return "Bag of words tokenizer, assuming each operator is surrounded by spaces";
	}

	@Override
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

	@Override
	public Object nextElement() {
		return iterator.next();
	}

	@Override
	public void tokenize(String s) {
		HashSet<String> javaCOperatorSet = new HashSet<String>(Arrays.asList(javaCOperators));
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
		iterator = wordsList.iterator();
	}

}
