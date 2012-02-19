package edu.ucsc.cs.sil.bow;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class BagOfWordsPlusPlusTest {

	@Test
	public void testTokenize() {
		BagOfWordsPlusPlus bow = new BagOfWordsPlusPlus("filePath");
		List<String> token = bow.tokenize("BagOfWordsPlusPlusTest");
		assertEquals("words", token.get(2));
		token = bow.tokenize("CommitExtractor/test/edu/ucsc/cs/sil/bow/BagOfWordsPlusPlusTest.java");
		assertEquals("extractor", token.get(1));
		assertEquals("test", token.get(2));
		assertEquals("test", token.get(13));
		
		token = bow.tokenize(".settings/.api_filters");
		assertEquals("settings", token.get(0));
		assertEquals("api", token.get(1));
		assertEquals("filters", token.get(2));
	}

}
