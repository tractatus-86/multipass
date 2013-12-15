package leeloo.dallas.multipass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fire implements Element {
	 Logger log = LoggerFactory.getLogger(this.getClass());
	 private Map<String, String> sha2Map = new HashMap<String, String>();
	private static Fire instance = null;
	   protected Fire() {
	      // Exists only to defeat instantiation.
	   }
	   public static Element getStone() {
	      if(instance == null) {
	         instance = new Fire();
	      }
	      return instance;
	   }
	@Override
	public void process(File file) {
		try {
			String  key = generateHashKey(file.getAbsolutePath());
	    	if(!sha2Map.containsKey(key))
	    		sha2Map.put(key, file.getAbsolutePath());
	    	else
	    	{
	    		log.info("Collision Detected: \""+file.getAbsolutePath()+"is duplicate of \""+sha2Map.get(key)+"\"");
	    		move(file);
	    	}
		} catch (NoSuchAlgorithmException | IOException e) {
			
			e.printStackTrace();
		}

	}
	

	private void move(File file) throws IOException {
		String duplicateDir =file.getAbsolutePath().replace(FifthElement.startDirString, FifthElement.duplicateDir);
		log.info("Moving \""+file.getAbsolutePath()+"\" to \""+duplicateDir);
		FileUtils.moveFile(file, new File(duplicateDir));
		
	}
	private static String generateHashKey(String fileName) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
       FileInputStream fis = new FileInputStream(fileName);

       byte[] dataBytes = new byte[1024];

       int nread = 0; 
       while ((nread = fis.read(dataBytes)) != -1) {
         md.update(dataBytes, 0, nread);
       };
       byte[] mdbytes = md.digest();

       //convert the byte to hex format method 1
       StringBuffer sb = new StringBuffer();
       for (int i = 0; i < mdbytes.length; i++) {
         sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
       }

    //   System.out.println("Hex format : " + sb.toString());

      //convert the byte to hex format method 2
       StringBuffer hexString = new StringBuffer();
   	for (int i=0;i<mdbytes.length;i++) {
   	  hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
   	}
   	fis.close();
   //	System.out.println("Hex format : " + hexString.toString());
   	return hexString.toString();
	}

}
