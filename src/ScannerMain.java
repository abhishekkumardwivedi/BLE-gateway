import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ScannerMain {

	private static boolean checkNextLine;
	private final static Object lock = new Object();
	
	private static final int MAC_OFFSET_START = 14;
	private static final int MAC_OFFSET_END = MAC_OFFSET_START + 12;
	private static final int TEMP_OFFSET_START = 38;
	private static final int TEMP_OFFSET_END = TEMP_OFFSET_START + 10;
	private static String rawMsg = null;

	
	public static Map<String, String> data = new HashMap<String, String>();
	public static final Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
	
	public static void main(String[] args) {
		System.out.println("Hello World!");
		String [] raw = new String[2];
		checkNextLine = false;
		try {
			Process p = Runtime.getRuntime().exec("hcidump --raw");
			BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				synchronized (lock) {
					System.out.println("line  : " + line);
				    if(line.charAt(0) == '>') {
				    	if(rawMsg != null) {
				    		parseAndPutInMap(rawMsg);
				    		rawMsg = null;
				    	}
				    	rawMsg = line;
				    } else if(line.charAt(0) == ' ' && rawMsg != null) {
				    	rawMsg += line;
				    } else {
				    	System.out.println("thrown out: " + line);
				    }
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void parseAndPutInMap(String rawMsg) {
		String parsedMessage = rawMsg;
		String mac = null;
		String temp = null;
		parsedMessage = rawMsg.replace(" ", "").replace(">", "");
		if(parsedMessage.length() == 30) {
			System.out.println("Thrown off as doesn't have message --");
			System.out.println("parsedMessage: " +parsedMessage);
			return;
		}
		
		String invMac = parsedMessage.substring(MAC_OFFSET_START, MAC_OFFSET_END);
		char [] macArray = invMac.toCharArray();
		mac = new String(new char [] {macArray[10],macArray[11],
				macArray[8], macArray[9],
				macArray[6], macArray[7],
				macArray[4],macArray[5],
				macArray[2],macArray[3],
				macArray[0],macArray[1]});
		
		temp = parsedMessage.substring(TEMP_OFFSET_START, TEMP_OFFSET_END);
		
		System.out.println("temp:" + temp);
		System.out.println("mac: " + mac); // TODO: invert byte array.
		updateMap(mac, temp);
	}
	
	private static void updateMap(String mac, String temp) {
		if(map == null || temp == null)
			return;
		Map tempMap = map.get(mac);
		if(tempMap == null) {
			tempMap = new HashMap<String, String>();
		}
		tempMap.put(new java.util.Date(), temp);
		map.put(mac, tempMap);
		return;
	}
}