import java.io.*;
import java.net.*;

public class ClientAPP {
	ChatWindow chatGUI;
	Socket socket;
	boolean connectionOn = false;
	BufferedReader read;
	PrintWriter write;
	String username = null;

	//Starts the client
	public void Start() {
		
		connectionOn = AttemptConnection();
		
		System.out.println("Username after attempt connection is: " + username);
		
		//Add GUI Window
		chatGUI = new ChatWindow("Chat Messenger (Client)", username, write);
		
        while(!write.checkError()) {
			try {
				String serverResponse = null;
				
				while((serverResponse = read.readLine()) != null){
					
					//If the client is being sent a userlist
					if(serverResponse.startsWith("USERLIST:")) {
						chatGUI.BuildUserList(serverResponse);
						serverResponse = null;
					}
					
					//If the client is being pinged with a disconnect message
					else if(serverResponse.startsWith("GracefulDisconnection")) {
						chatGUI.CloseClient();
						System.exit(0);
					}
					
					//If the client is being sent anything else
					else{
						LogResponse(serverResponse);
						chatGUI.DisplayMessage(serverResponse);
						serverResponse = null;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean AttemptConnection() {
		
		boolean connectionSuccess = false;
		StartWindow startGUI = new StartWindow("Login/Registration");
		
		connectionSuccess = startGUI.Launch();
		
		if(connectionSuccess) {	
			username = startGUI.username.toLowerCase();
			read = startGUI.read;
			write = startGUI.write;
			socket = startGUI.socket;
			startGUI.Destroy();
			startGUI = null;
			System.out.println("username 1AA is: " + username);
			return connectionSuccess;
		}
		
		else{
			return connectionSuccess;
		}
	}
	
	public void LogResponse(String toLog) {

		//Creates new log file
		File file = new File(username + "_ChatLog.txt");
		
		if(file.exists()) {
			try {
				FileWriter fileWriter = new FileWriter(file, true);
				fileWriter.write(toLog + System.getProperty("line.separator")); //because "\n" doesnt work with filewriter for some reason
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else{	
			try {
				FileWriter fileWriter = new FileWriter(file, true);
				file.createNewFile();
				fileWriter.write(toLog + System.getProperty("line.separator")); //because "\n" doesnt work with filewriter for some reason
				fileWriter.flush();				
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
	
	public static void main(String[] args) {
		
		ClientAPP clientApp = new ClientAPP();
		
		try{
			clientApp.Start();
			
		}catch(Exception e){
			//ToDO
		}
		
	}
}