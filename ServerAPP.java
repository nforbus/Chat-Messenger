import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class ServerAPP {

	ArrayList<ClientThread> clientConnections;
	ServerSocket serverSocket;
	Socket clientSocket;
	boolean serverOn = true;
	
	public ServerAPP() {
		
		String serverString = null;
		
		try{
			InetAddress serverIP = InetAddress.getLocalHost();
			serverString = serverIP.getHostAddress();
			System.out.println(serverIP.getHostAddress());
		}catch(Exception e){
			//ToDo
		}
		
		clientConnections = new ArrayList<ClientThread>();
		
		//Termination button for the server
		JFrame keepOn = new JFrame();
		keepOn.setVisible(true);
		keepOn.setMinimumSize(new Dimension(500,100));
		keepOn.addWindowListener(new WindowAdapter() {
			public void actionPerformed(ActionEvent e)
			{
				EndConnections();
			}
		});
		JButton killServer = new JButton("PRESS TO TERMINATE CONNECTIONS TO: " + serverString);
		killServer.setVisible(true);
		killServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				EndConnections();
			}
		});
		keepOn.add(killServer);
		
		//Establish the servers initial connection
		try{
			serverSocket = new ServerSocket(1201);
		}catch(Exception e){
			System.out.println("Couldn't use socket " + 1201);
			System.exit(-1);
		}
		
		//Accept incoming connections while serverOn is turned on
		while(serverOn)	{				
			try{
				clientSocket = serverSocket.accept();
				ClientThread clientThread = new ClientThread(clientSocket);
				clientThread.start();
				clientConnections.add(clientThread);
				
			}catch(Exception e){
				System.out.println("Couldn't accept connection.");
			}
		}
		
		//Close connection while serverOn is turned off
		try{
			serverSocket.close();
			System.out.println("Server closed.");
		}catch(Exception e){
			System.out.println("Server experienced error when shutting off.");
		}
	}
	
	public void EndConnections() {
		
		serverOn = false;
		
		//Ping all connected clients, tell them to gracefully disconnect
		for(int i = 0; i < clientConnections.size(); ++i) {
			clientConnections.get(i).GracefulDisconnect();
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
	
	public static void main(String [] args) {
		new ServerAPP();
	}
		
	
	//Thread which handles a server/client communications connection
	class ClientThread extends Thread {
		
		Socket socket;
		boolean threadOn = true;
		BufferedReader read = null;
		PrintWriter write = null;
		String username = null;
		String allOfTheUsers = null;
		
		//Default constructor
		public ClientThread() {
			super();
		}
		
		//Other constructor
		public ClientThread(Socket clientSocket){
			socket = clientSocket;
		}
		
		public void run() {
			String [] userInput;
			
			try{
		        read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		        write = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		        
		        while(threadOn){			
		        	String userInfo = read.readLine();
					userInput = userInfo.split(":");

					//Check if user is trying to log in or register
					if(userInput[0].equals("Register")){
						if(RegisterUser(userInput[1], userInput[2])) {
							write.println("Allowed");
							write.flush();
							username = userInput[1].toLowerCase();
							AnnounceStatus("Allowed");
							BuildUserList();
						}
						
						else{
							System.out.println("Registration failed.");
							write.println("Registration Failed");
							write.flush();
							socket.close();
						}
					}
					
					else{
						//If user information is correct
						if(CheckUser(userInput[0], userInput[1])) {
				        	write.println("Allowed");
				            write.flush();
				        	username = userInput[0].toLowerCase();
				        	AnnounceStatus("Allowed");
				        	BuildUserList();
				        }
				        
				        //user login failed, present error.
				        else {
				        	System.out.println("Login failed.");
				            write.println("Login Failed");
				            write.flush();
				            socket.close();
				        }
					}
					
			        String clientResponse = null;
			        
			        while(!write.checkError()) {
			        	clientResponse = read.readLine();
			        	
			        	//User has disconnected, close thread down
			        	if(clientResponse.compareTo("CloseUserConnection") == 0) {
			        		AnnounceStatus("Disconnect");
			        		write.close();
			        		read.close();
			        		socket.close();
			        		clientConnections.remove(this);
			        	}
			        	else{
			        		
			        		if(clientResponse.startsWith("@")) {
			        			DirectMessage(clientResponse);
			        		}
			        		else{
				        		Broadcast(clientResponse);
			        		}
			        	}
			        }
				}
		        
			}catch(Exception e){
				//ToDO
			}
		}
		
		public void GracefulDisconnect() {
			
			write.println("GracefulDisconnection");
			write.flush();
			
		}
		
		public void Broadcast(String toSend) {
			for(int i = 0; i < clientConnections.size(); ++i) {
				ClientThread myThread = clientConnections.get(i);
				myThread.write.println(toSend);
				myThread.write.flush();
			}
		}
		
		public void DirectMessage(String toSend) {
			
			ArrayList<String> dissectedMessage = new ArrayList<String>(Arrays.asList(toSend.split(":")));
			String toUser = new String(dissectedMessage.get(0)).substring(1);
			String toMessage = new String(dissectedMessage.get(1));
			String fromUser = new String(dissectedMessage.get(2));
			
			String newMessage = new String(fromUser + " says to " + toUser + " : " +  toMessage);
			
			toUser = toUser.toLowerCase();
			fromUser = fromUser.toLowerCase();
			
			if(VerifyUserExists(toUser)) {
				write.println(newMessage); //Send the message back to yourself since you should also see it in log
				write.flush();
				
				//Somehow figure out which client has toUser connected..
				for(int i = 0; i < clientConnections.size(); ++i) {
					
					ClientThread myThread = clientConnections.get(i);
					
					if(myThread.username.equals(toUser)) {
						myThread.write.println(newMessage);
						myThread.write.flush();
					}
				}
			}
			
			else{
				write.println("That user doesn't exist.");
				write.flush();
			}
			
		}
		
		public void AnnounceStatus(String statusUpdate) {
			
			String statusMessage = null;
			
			if(statusUpdate.compareTo("Disconnect") == 0) {
				statusMessage = new String(username + " has disconnected.");
			}
			
			else {
				statusMessage = new String(username + " has connected.");
			}
			
			
			for(int i = 0; i < clientConnections.size(); ++i) {
				ClientThread myThread = clientConnections.get(i);
				myThread.write.println(statusMessage);
				myThread.write.flush();
			}
		}
		
		public boolean VerifyUserExists(String toVerify) {
			
			Scanner file_in = null;
			String myUser = null;
			
			try{
				file_in = new Scanner(new File("RegisteredUsers.txt"));
				
				while(file_in.hasNextLine()) {
					myUser = file_in.next();
					String [] userInput = myUser.split(":");
					if(userInput[0].equals(toVerify)){
						file_in.close();
						return true;
					}
				}
				
				file_in.close();
				return false;
			}catch(Exception e){
				System.out.println("Could not verify if user existed or not.");
			}
			
			return false;
		}
		
		public boolean CheckUser(String usernameInfo, String passwordInfo) {
			
			Scanner file_in = null;
			String myUser = null;
			
			String newUsernameInfo = usernameInfo.toLowerCase();
			String userInfo = new String(newUsernameInfo + ":" + passwordInfo);
			
			try{
				file_in = new Scanner(new File("RegisteredUsers.txt"));
				
				while(file_in.hasNextLine()) {
					myUser = file_in.next();
					if(myUser.equals(userInfo)){
						file_in.close();
						return true;
					}
				}
			}catch(Exception e){
				//ToDO
			}
			
			return false;
		}
		
		public boolean RegisterUser(String usernameInfo, String passwordInfo)
		{
			Scanner file_in = null;
			String myUser = null;
			
			String newUsernameInfo = usernameInfo.toLowerCase();
			String userInfo = new String(newUsernameInfo + ":" + passwordInfo);
			
			try{
				file_in = new Scanner(new File("RegisteredUsers.txt"));
				
				while(file_in.hasNextLine()) {
					myUser = file_in.next();
					String [] userInput = myUser.split(":");
					if(userInput[0].equals(newUsernameInfo)){
						file_in.close();
						return false;
					}
				}
				
				file_in.close();
				AddUser(userInfo);
				return true;
				
			}catch(Exception e){
				//ToDO
			}
			
			return false;
		}
		
		public void AddUser(String userInfo) {
			File file = new File("RegisteredUsers.txt");
			
			if(file.exists()) {
				try {
					FileWriter fileWriter = new FileWriter(file, true);
					fileWriter.write(System.getProperty("line.separator") + userInfo);
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			else{	
				System.out.println("COULDNT ACCESS REGISTERED USERS!");
			}
		}
		
		public void BuildUserList() {
			Scanner file_in = null;
			String myUser = null;
			
			try{
				file_in = new Scanner(new File("RegisteredUsers.txt"));
				
				while(file_in.hasNextLine()) {
					myUser = file_in.next();
					String [] userInput = myUser.split(":");
					if(allOfTheUsers == null) {
						allOfTheUsers = new String("USERLIST:" + userInput[0]);
					}
					else{
						allOfTheUsers = new String(allOfTheUsers + ":" + userInput[0].toString());
					}
				}
				
				file_in.close();
								
				for(int i = 0; i < clientConnections.size(); ++i) {
					ClientThread myThread = clientConnections.get(i);
					myThread.write.println(allOfTheUsers);
					myThread.write.flush();
				}

			}catch(Exception e){
				//ToDO
			}
		}
	}	
}