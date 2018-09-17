package com.liveperson.websockettest;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;

import io.socket.client.Socket;

public class ChatServerUtil {
	
	public  static void sendMessage(Socket socket, String messageStr){
			JSONObject obj = new JSONObject();
			try {
				messageStr = messageStr.replaceAll("\\\\", "");
			//	System.out.println("Message :$" + messageStr+"$");
				obj = new JSONObject(messageStr);
	    	//	System.out.println(obj.toString());
				System.out.println(obj.getJSONArray("entry").get(0).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			socket.emit("usermessage", obj);
		
	}
	
	public static String[][] readFromFile(String filename) throws IOException {
		String[][] parampair = new String[countLines(filename)][3];
		File file = new File(filename);
		 
		  BufferedReader br = new BufferedReader(new FileReader(file));
		 
		  String line;
		  int count = 0;
		  while ((line = br.readLine()) != null) {
		     //  System.out.println("Read line is: " + line);
		        String[] input = line.split("##");
		        int index = 0;
		        for(String s : input) {
		        //	System.out.println("Each param is: " + s);
		        	parampair[count][index] = s;
		        	index += 1;
		        }
		        count += 1;
		  }
		  br.close();
		for(String[] s : parampair)
			System.out.println(s[0]);
		return parampair;

	}
	
	public static int countLines(String filename) throws IOException {
	    LineNumberReader reader  = new LineNumberReader(new FileReader(filename));
	int cnt = 0;
	String lineRead = "";
	while ((lineRead = reader.readLine()) != null) {}

	cnt = reader.getLineNumber(); 
	reader.close();
	return cnt;
	}


}
