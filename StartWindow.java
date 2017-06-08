import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

public class StartWindow implements ActionListener {
	
	String windowName = null;
	JFrame myWindow = null;
	JTextArea usernameField = null;
	JTextArea passwordField = null;
	JTextArea ipAddressField = null;
	String username = null;
	String password = null;
	
	PrintWriter write = null;
	BufferedReader read = null;
	Socket socket = null;
	boolean successfulLogin = false;
	
	//default constructor
	public StartWindow() {}
	
	//normal constructor
	public StartWindow(String windowType) {
		
		windowName = windowType;
	}
	
	//executes the window
	public boolean Launch() {
		
		AddFrame(windowName);
		myWindow.revalidate();
		myWindow.setVisible(true);
		
		while(!successfulLogin) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return successfulLogin;
	}
	
	//Creates a JFrame object to serve as the base window
	public void AddFrame(String windowName) {
		myWindow = new JFrame(windowName);
		myWindow.setMinimumSize(new Dimension(400,125));
		myWindow.setLocation(0,0);
		myWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent){
				if(write != null) {
					CloseConnection();
					System.exit(0);
				}
				System.exit(0);
			}        
		});
		
		AddPanels(myWindow);
	}
	
	public void AddPanels(JFrame toAddTo) {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setVisible(true);
		
		JPanel middlePanel = new JPanel();
		middlePanel.setVisible(true);
		
		JPanel topPanel = new JPanel();
		topPanel.setVisible(true);
		
		//Adds the text field for user to input the desired server IP address.
		ipAddressField = new JTextArea(1,20);
		ipAddressField.setLineWrap(true);
		ipAddressField.setVisible(true);
		ipAddressField.setText("IP Address");
		JScrollPane ipAddressPane = new JScrollPane(ipAddressField);
		
		//Adds the text field for user to input their username/desired username.
		usernameField = new JTextArea(1,20);
		usernameField.setLineWrap(true);
		usernameField.setVisible(true);
		usernameField.setText("Username");
		JScrollPane usernamePane = new JScrollPane(usernameField);
		
		//Adds the text field for user to input their password/desired password.
		passwordField = new JTextArea(1,20);
		passwordField.setLineWrap(true);
		passwordField.setVisible(true);
		passwordField.setText("Password");
		JScrollPane passwordPane = new JScrollPane(passwordField);
		
		JButton loginButton = new JButton("Login");
		loginButton.setPreferredSize(new Dimension(90,20));
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				String usernameInfo = usernameField.getText();
				String passwordInfo = passwordField.getText();
				String ipAddressInfo = ipAddressField.getText();
				ValidateUser(usernameInfo, passwordInfo, ipAddressInfo);
			}
		});
		
		JButton registerButton = new JButton("Register");
		registerButton.setPreferredSize(new Dimension(90,20));
		registerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				String usernameInfo = usernameField.getText();
				String passwordInfo = passwordField.getText();
				String ipAddressInfo = ipAddressField.getText();
				CreateUser(usernameInfo, passwordInfo, ipAddressInfo);
			}
		});
		
		topPanel.add(usernamePane);
		topPanel.add(loginButton);
		middlePanel.add(passwordPane);
		middlePanel.add(registerButton);
		bottomPanel.add(ipAddressPane);
		
		toAddTo.getContentPane().add(topPanel, BorderLayout.NORTH);
		toAddTo.getContentPane().add(middlePanel, BorderLayout.CENTER);
		toAddTo.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
	}
	
	public void ValidateUser(String usernameInfo, String passwordInfo, String ipInfo) {
		
		String userInfo = new String(usernameInfo + ":" + passwordInfo);
		String serverResponse = null;
		
		try {
			socket = new Socket(ipInfo, 1201);
			write = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));	
			read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			write.println(userInfo);
			write.flush();
			serverResponse = read.readLine();
			
			if(serverResponse.compareTo("Allowed") != 0) {
				JOptionPane.showMessageDialog(null, "Incorrect login information", "Error", JOptionPane.ERROR_MESSAGE);
				successfulLogin = false;
			}
			else {
				System.out.println("login success!");
				username = usernameInfo.toLowerCase(); //Stores the username for later
				System.out.println("username is: " + username);
				successfulLogin = true;
			}
			
		}catch(Exception e){
			System.out.println("Failure to connect");
		}
	}
	
	public void CreateUser(String usernameInfo, String passwordInfo, String ipInfo) {
		
		String userInfo = new String("Register" + ":" + usernameInfo + ":" + passwordInfo);
		String serverResponse = null;
		
		try {
			socket = new Socket(ipInfo, 1201);
			write = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));	
			read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			write.println(userInfo);
			write.flush();
			serverResponse = read.readLine();
			
		}catch(Exception e){
			System.out.println("Failure to connect");
		}
		
		if(serverResponse.compareTo("Allowed") != 0) {
			JOptionPane.showMessageDialog(null, "Couldn't register", "Error", JOptionPane.ERROR_MESSAGE);
			successfulLogin = false;
		}
		else {
			System.out.println("registration success!");
			username = usernameInfo; //Stores the username for later
			successfulLogin = true;
		}
		
	}
	
	public void CloseConnection() {
		
		if(socket != null) {
			String closingMessage = "CloseUserConnection";
			write.println(closingMessage);
			write.flush();
		}
	}
	
	public void Destroy() {
		myWindow.dispose();

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
