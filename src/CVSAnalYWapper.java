import java.io.IOException;


public class CVSAnalYWapper {
	public static void run(){
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("export PYTHONPATH=$PYTHONPATH:/home/linzhp/python/repositoryhandler:/home/linzhp/python/guilty/");
			Process ps = rt.exec("/home/linzhp/python/cvsanaly/cvsanaly2 -u root -d cvsanaly /home/linzhp/data/voldemort");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
