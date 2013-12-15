package leeloo.dallas.multipass;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Badaboom
 *
 */
public class FifthElement 
{
	 static Logger log = LoggerFactory.getLogger(FifthElement.class);
	 
	public static final String startDirString = "C:\\Users\\Phobos\\Downloads\\The National Discography";
	public static final String duplicateDir = "C:\\Duplicates";
	public static final String[] extensions = { "mp3" };
	private static Collection<File> files;

	public static void main(String[] args) {
		files = FileUtils.listFiles(new File(startDirString),
				extensions, true);
		log.warn("passing fire stone");
		//pass(Fire.getStone());
		log.warn("passing wind stone");
		pass(Wind.getStone());
		pass(Water.getStone());
		pass(Earth.getStone());
	}

	private static void pass(Element element) {
		for (File file : files) {
			element.process(file);
		}
	}
}
