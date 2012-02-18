package edu.ucsc.cs.sil.bow;
import java.util.ArrayList;
import java.util.List;


public class BagOfWords extends BOWBase {

	public BagOfWords(String prefix) {
		super(prefix);
	}

	@Override
	public List<String> tokenize(String s) {
		List<String> wordsList = new ArrayList<String>();
		String splits[] = s.split("[\\W_]+");
		for (int i = 0; i < splits.length; i++) {
			if (splits[i].length() > 0) {
				wordsList.add(splits[i].toLowerCase());
			}
		}
		return wordsList;
	}

}
