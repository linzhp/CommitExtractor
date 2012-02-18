package edu.ucsc.cs.sil.bow;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class BagOfWordsTest {

	@Test
	public void testTokenize() {
		BagOfWords bow = new BagOfWords("log");
		List<String> tokens = bow.tokenize("Externalization in progress 129_04_synchronize_with_stream");
		assertEquals("externalization", tokens.get(0));
		assertEquals("progress", tokens.get(2));
		assertEquals("129", tokens.get(3));
		assertEquals("synchronize", tokens.get(5));
	}

}
