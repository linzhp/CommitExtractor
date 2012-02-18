package edu.ucsc.cs.sil;
import static org.junit.Assert.*;

import org.junit.Test;


public class ExtractorTest {
	private String patch = "diff --git a/lib/mysql-connector-java-3.1.14-bin.jar b/lib/mysql-connector-java-3.1.14-bin.jar\n"+
	"new file mode 100644\n"+
	"index 0000000..cd27e23\n"+
	"Binary files /dev/null and b/lib/mysql-connector-java-3.1.14-bin.jar differ\n"+
	"diff --git a/src/Extractor.java b/src/Extractor.java\n"+
	"index 24469d3..9edcae4 100644\n"+
	"--- a/src/Extractor.java\n"+
	"+++ b/src/Extractor.java\n"+
	"@@ -10,10 +10,11 @@ import weka.core.Attribute;\n"+
	" import weka.core.FastVector;\n"+
	" import weka.core.Instance;\n"+
	" import weka.core.Instances;\n"+
	"+import weka.filters.Filter;\n"+
	"+import weka.filters.unsupervised.attribute.NumericToNominal;\n"+
	"\n"+
	"public class Extractor {\n"+
	"\n"+
	"-       private static int numOfAttrs;\n"+
	"\n"+
	"/**\n"+
	"* @param args\n";
	
	@Test
	public void testAddedDelta() {
		String delta=Extractor.extractDelta(patch, '+');
		assertEquals("import weka.filters.Filter;\nimport weka.filters.unsupervised.attribute.NumericToNominal;\n", delta);
		
		delta = Extractor.extractDelta(patch, '-');
		assertEquals("       private static int numOfAttrs;\n", delta);
	}

}
