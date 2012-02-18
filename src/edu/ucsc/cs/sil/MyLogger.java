package edu.ucsc.cs.sil;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class MyLogger {
	private static Logger logger;
	
	public static Logger getLogger(){
		if(logger == null){
			logger = Logger.getLogger("CommitExtractor");
			FileHandler fh;
			try {
				fh = new FileHandler("extractor.log", false);
				fh.setFormatter(new SimpleFormatter());
				logger.addHandler(fh);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.setLevel(Level.WARNING);
		}
		return logger;
	}
}
