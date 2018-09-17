package test.com.liveperson.websockettest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.liveperson.websockettest.ChatServerUtil;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isOneOf;

import org.hamcrest.Matchers;
import org.json.*;


public class ChatServerConnectionTest {
	
	private String url;
	private Socket socket;
	int messagecounter = 0;
	private static String filename;
	private String request;
	private String response;
	private String[][] data;
	private int dataindex;
	private int numberofresponses;
	private String BotId;
	
	private String beginReqStr = "{\"object\":\"page\",\"entry\":[{\"id\":\"d1ed798b-e687-46bf-af08-810299deb5c4\",\"time\":1534885606385,\"messaging\":[{\"sender\":{\"id\":\"cb1a62a1-4b78-489e-9cc1-966fa2707eef\"},\"recipient\":{\"id\":\"";
    private String endReqStr = "\\\"},\\\"timestamp\\\":1534885606385,\\\"message\\\":{\\\"mid\\\":\\\"mid.1534885606385:d1ed798b-e687-46bf-af08-810299deb5c4\\\",";
    private String endReqStr2 = "\\\"},\\\"timestamp\\\":1534885606385,";
    private String beginResStr = "{\"agentEscalation\":false,\"recipient\":{\"id\":\"cb1a62a1-4b78-489e-9cc1-966fa2707eef\"},\"message\":{\"delay\":0,";
    private String endResStr = ",\"seq\":0}}";
	String actualResponse = "actualresponse";
	
	
	@BeforeSuite
	@Parameters({"socketurl", "filename", "botid"})
	public void setUp(String url, String filename, String BotId) throws URISyntaxException, IOException {
		this.url = url;
		this.BotId = BotId;
		IO.Options opts = new IO.Options();
		opts.transports = new String[] {"websocket"};
		socket = IO.socket(url, opts);
		this.filename = filename;
		System.out.println("File name for request and responses : " + this.filename );
		data = ChatServerUtil.readFromFile(filename);
		numberofresponses = 0;
		request = new String();
		response = new String();
	}
	
	
	@Test
	public void testConnection() throws InterruptedException, URISyntaxException {
			
			IO.Options opts = new IO.Options();
			opts.transports = new String[] {"websocket"};
			socket = IO.socket(url, opts);
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

				  public void call(Object... args) {
					System.out.println("connected " + socket.connected());
					Assert.assertEquals( socket.connected(), true, "connected?");
				  }

				}).on("botresponse", new Emitter.Listener() {

				  public void call(Object... args) {
					  System.out.println("received message" + args[0].toString());
				  }

				}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

				  public void call(Object... args) {
					  System.out.println("disconnected : " + args[0].toString());
					  Assert.assertEquals( socket.connected(), false, "connected?");
				  }

				});
				socket.connect();
				Thread.sleep(10000);
				socket.disconnect();
				
	}
	
	
	
	@Test(dataProvider = "dp", timeOut = 90000)
	public void testSendMessage(int i) throws InterruptedException, URISyntaxException {
		//request = data[i][0];
		//response = data[i][1];
		List<String> responses = new ArrayList<String>();
		Boolean multi = false;
		//request =  beginReqStr + BotId + endReqStr + data[i][0] + "}}]}]}";
		if(data[i][0].contains("postback"))
			request =  beginReqStr + BotId + endReqStr2 + data[i][0] + "}}]}]}";
		else
			request =  beginReqStr + BotId + endReqStr + data[i][0] + "}}]}]}";
		if(data[i][1].contains("&&")) {
			multi = true;
			String[] res = data[i][1].split("&&");
			for(String s : res) {
				responses.add(s);
			}
		}
		else
			//response = beginResStr + data[i][1] + endResStr;
			response = data[i][1];
		System.out.println("Request is : " + request);
		messagecounter = 0;
		numberofresponses = Integer.valueOf(data[i][2]);
	
		System.out.println("number of responses: " + numberofresponses);
			IO.Options opts = new IO.Options();
			opts.transports = new String[] {"websocket"};
			socket = IO.socket(url, opts);
			
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
				  public void call(Object... args) {
					System.out.println("connected " + socket.connected());
					Assert.assertEquals( socket.connected(), true, "connected?");
					
				  }

				}).on("botresponse", new Emitter.Listener() {

				  public void call(Object... args) {
					  System.out.println("received message" + args[0].toString());
					  
					  
					  messagecounter += 1;
					 if(!args[0].toString().contains("\"sender_action\"")) {
						 
						 actualResponse = args[0].toString();
					  
				     }
				  }

				}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

				  public void call(Object... args) {
					  System.out.println("disconnected : " + args[0].toString());
					  Assert.assertEquals( socket.connected(), false, "connected?");
				  }

				});
				socket.connect();
				ChatServerUtil.sendMessage(socket, request);
				Thread.sleep(10000);
				socket.disconnect();
				Assert.assertEquals(numberofresponses, messagecounter);
				if(!multi) 
					//Assert.assertEquals(actualResponse, response);
				assertThat(actualResponse, containsString(response));
				else {
					System.out.println(responses.toString());
					Assert.assertTrue(AnyIn(actualResponse,responses));
					multi = false;
				}

	}
	
	public static boolean AnyIn(String s, List<String> values)
    {
        for(String str : values) {
        	if(s.contains(str))
        		return true;
        }
        return false;
    }
	
	@DataProvider(name = "requestresponse")
    public Integer[][] dp() {
		Integer[][] obj = new Integer[data.length][1];
		for(int i = 0; i < data.length; i++) {
			
			obj[i][0] = i;
			
		}
		return obj;
    }
	
}
