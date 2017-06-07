import java.io.*;
import java.net.*;
import java.util.*;

public class ServerAPP {

	ArrayList<ClientThread> clientConnections;
	ServerSocket serverSocket;
	ArrayList<String> listOfUsers;
	boolean serverOn = true;
	
	public ServerAPP() {
		
		try{
			InetAddress serverIP = InetAddress.getLocalHost();
			System.out.println(serverIP.getHostAddress());
		}catch(Exception e){
			//ToDo
		}
		
		clientConnections = new ArrayList<ClientThread>();
		
		//Establish the servers initial connection
		try{
			serverSocket = new ServerSocket(1201);
		}catch(Exception e){
			System.out.println("Couldn't use socket " + 1201);
			System.exit(-1);
		}
		
		//Accept incoming connections while serverOn is turned on
		while(serverOn){
			try{
				Socket clientSocket = serverSocket.accept();
				//clientConnections.add(new ClientThread(clientSocket));
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
							username = userInput[1];
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
				        	username = userInput[0];
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
			        		Broadcast(clientResponse);
			        	}
			        }
				}
		        
			}catch(Exception e){
				//ToDO
			}
		}
		
		public void Broadcast(String toSend) {
			for(int i = 0; i < clientConnections.size(); ++i) {
				ClientThread myThread = clientConnections.get(i);
				myThread.write.println(toSend);
				myThread.write.flush();
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
		
/*		public void UserDisconnect(String username){
			//chatGUI.DisplayMessage(username + " has disconnected");
			
			try {
				write.close();
				client = null;
				
			}catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

	
	
	/*//ChatWindow chatGUI;
	//ServerSocket serverSocket;
	Socket client;
	BufferedReader read;
	PrintWriter write;
	int numConnected = 0;
	int maxNumUsers = 10;
	String [] userInput;
	

	
	public void Start(){
		
		//Create the GUI window
		chatGUI = new ChatWindow("Chat Messenger (Server)", "SERVER_ADMIN");
		
		try{
			serverSocket = new ServerSocket(connection.getSocket());
			System.out.println("Connection active on socket " + connection.getSocket());
		}catch(IOException ioError){
			System.out.println("Couldn't connect to " + connection.getSocket());
			System.exit(-1);
		}

		while(serverOn) {
			try{
				client = serverSocket.accept();
			}
		}

		System.out.println("Awaiting incoming connections.. ");
		
		try{
			AcceptConnection();
		}catch(Exception e){
			System.out.println("SERVER ISNT LISTENING");
		}
	}
	
	public void AcceptConnection() throws Exception {		
		while(true) {
			try{
				
				//Reopen a socket if it was already closed
				if(client == null) {
					client = serverSocket.accept();
				}
				
		        //open read/write streams
		        read = new BufferedReader(new InputStreamReader(client.getInputStream()));
		        write = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
		        chatGUI.AddWriter(write); //sends writer info to the GUI
				
				if(numConnected < maxNumUsers) {	
					if(client == null)
					{
						System.out.println("Client is null");
						break;
					}
					
					if(write.checkError()) {
						UserDisconnect(userInput[0]);
					}
					
					System.out.println("Trying to establish a connection with the client..");
					
					String userInfo = read.readLine();
					userInput = userInfo.split(":");

			        if(userInput[0].equals(connection.getUsername()) && userInput[1].equals(connection.getPassword())) {
			            //User has connected
			        	chatGUI.DisplayMessage(userInput[0] + " has connected");
			        	write.println("Allowed");
			            write.flush();
			            ++numConnected;
			            System.out.println("There are now " + numConnected + " users connected.");
			        }
			        
			        //user login failed, present error.
			        else {
			            write.println("Login Failed");
			            write.flush();
			            client.close();
			        }
				}
				
				else {
					write = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
					write.println("Server is full, try again later.");
					write.flush();
					client.close();
				}
				
				String clientResponse = null;
				
				while(!write.checkError()) { //check error returns true if there's an error writing to it
					
						clientResponse = read.readLine();
						//System.out.println("message from client: " + clientResponse);
						
						if(clientResponse.compareTo("CloseUserConnection") == 0) {
							UserDisconnect(userInput[0]);
						}
						else {
							//System.out.println("Client has something to say.");
							chatGUI.DisplayMessage(clientResponse);
							clientResponse = null;
						}
				}

			}catch(Exception e){
				//ToDO
			}
		}
	}
	
	public void SendMessageToAll(String messageToSend){
		write.println(messageToSend);
		write.flush();
		messageToSend = null;
	}
	
	public void SendMessage(String messageToSend) {
		
		try{
			write.println(messageToSend);
			write.flush();
			
		}catch(Exception e){
			//
		}
	}
	
	public void UserDisconnect(String username) {
		
		chatGUI.DisplayMessage(username + " has disconnected");
		
		try {
			client.close();
			client = null;
			
		}catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		
		ServerAPP serverApp = new ServerAPP(); //Instantiates a ServerAPP

		try{
			serverApp.Start(); //launch GUI and obtain server connection.
			
		}catch(Exception e){
			//ToDO
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}*/