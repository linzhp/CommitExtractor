import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;


public class BagOfWords {
	private static String javaCOperators[] = { "+", "-", "*", "/", "%", ">", ">=", "<",
		"<=", "==", "!=", "<<", ">>", ">>>", "+=", "-=", "*=", "/=", "%=",
		"&=", "|=", "^=", "<<=", ">>=", ">>>=", "&&", "||", "!", "&", "|",
		"^", "++", "--", "~", "->" };
	private Map<String, Integer> termFreq;
	private String prefix;
	
	public BagOfWords(String prefix){
		termFreq = new TreeMap<String, Integer>();
		this.prefix = prefix;
	}
	
	public Map<String, Integer> getTermFreq() {
		return termFreq;
	}

	public void add(String s){
		List<String> tokens = tokenize(s);
		for(String t : tokens){
			String key = prefix+"__"+t;
			Integer freq = termFreq.get(key);
			if(freq == null){
				freq = 0;
			}
			termFreq.put(key, freq+1);
		}
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
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select content from content limit 1");
		rs.next();
		System.out.printf(rs.getString("content"));
	}

}
