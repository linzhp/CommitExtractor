import java.io.BufferedReader;
import java.io.FileReader;


public class DeleteComment {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws Exception {
		FileReader fr = new FileReader(args[0]);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		while(line != null){
			System.out.println(line.split("#")[0]);
			line = br.readLine();
		}
	}

}
