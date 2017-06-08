import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

public class ChatWindow implements ActionListener, MouseListener {	
	
	JFrame myWindow = null;
	JTextArea chatLog = null;
	JTextArea userList = null;
	JTextArea chatBar = null;
	String username = "blank";
	PrintWriter write = null;
		
	public ChatWindow(String windowName, String usernameToAdd, PrintWriter myWriter) {
		username = usernameToAdd.toLowerCase();
		write = myWriter;
		AddFrame(windowName);
		myWindow.revalidate();
		myWindow.setVisible(true);
	}
		
	//Creates a JFrame object to serve as the base window
	public void AddFrame(String windowName) {
		myWindow = new JFrame(windowName);
		myWindow.setMinimumSize(new Dimension(800,500));
		myWindow.setLocation(0,0);
		myWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent){
				if(write != null) {
					CloseClient();
					System.exit(0);
				}
				System.exit(0);
			}        
		});
		
		AddPanels(myWindow);
	}
	
    //Creates the JPanels that will be added to the window JFrame
	public void AddPanels(JFrame toAddTo) {
	
		//Creates the bottom panel that includes the Enter and History buttons, as well as the text input field
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setVisible(true);
		bottomPanel.setLocation(0,0);
		
		//Creates the bar where users will type in messages
		chatBar = new JTextArea(2,50);
		chatBar.setLineWrap(true);
		chatBar.setVisible(true);
		chatBar.addKeyListener(new KeyListener() { //Recognizes enter button being pressed, processes it as a message send
			@Override
			public void keyPressed(KeyEvent pressed) {
				if(pressed.getKeyCode() == KeyEvent.VK_ENTER)
				{
					pressed.consume();
					String message = chatBar.getText();
					
					if(message.startsWith("@")){
						String directMessage = new String(message + ":" + username);
						write.println(directMessage);
						write.flush();
						chatBar.setText(null);
					}
					else{
						String generalMessage = new String(username + ": " + message);
						write.println(generalMessage);
						write.flush();
						chatBar.setText(null);
					}
				}
			}
	
			@Override
			public void keyReleased(KeyEvent arg0) {}
	
			@Override
			public void keyTyped(KeyEvent arg0) {}
		});
		
		JScrollPane chatBarPane = new JScrollPane(chatBar);
		bottomPanel.add(chatBarPane);
		
		//Creates the button users will hit in order to send a message
		JButton enterButton = new JButton("Enter");
		enterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{

				String message = chatBar.getText();
				
				if(message.startsWith("@")){
					String directMessage = new String(message + ":" + username);
					write.println(directMessage);
					write.flush();
					chatBar.setText(null);
				}
				else{
					String generalMessage = new String(username + ": " + message);
					write.println(generalMessage);
					write.flush();
					chatBar.setText(null);
				}
			}
		});
		enterButton.setSize(90,20);
		enterButton.setVisible(true);
		bottomPanel.add(enterButton);
		
		//Creates the button that opens up the chat history
		JButton historyButton = new JButton("History");
		historyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				String fileToOpen = new String(username + "_ChatLog.txt");
				ProcessBuilder pb = new ProcessBuilder("Notepad.exe", fileToOpen);
				try {
					pb.start();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "ERROR", "Cannot find that log file!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		historyButton.setSize(120,10);
		historyButton.setVisible(true);
		historyButton.addActionListener(this);
		bottomPanel.add(historyButton);
		
		//Creates the button that logs out
		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CloseClient();
				System.exit(0);
			}
		});
		logoutButton.setSize(120,10);
		logoutButton.setVisible(true);
		logoutButton.addActionListener(this);
		bottomPanel.add(logoutButton);
		
		toAddTo.getRootPane().setDefaultButton(enterButton); //Assigns the enterButton as the default function when the enter button is pressed in frame
		toAddTo.getContentPane().add(bottomPanel, BorderLayout.SOUTH); //adds bottomPane (which includes bottomPanel) to the south position of the frame
		
		//Creates the middle panel which includes the text area of the chat room, as well as the user list
		JPanel middlePanel = new JPanel();
		middlePanel.setVisible(true);
		//middlePanel.addMouseListener(this);
		
		//Creates the text area where messages will be displayed
		chatLog = new JTextArea(25, 54);
		chatLog.setVisible(true);
		chatLog.setEditable(false);
		JScrollPane chatLogPanel = new JScrollPane(chatLog);
		middlePanel.add(chatLogPanel);
		
		//Creates the text area where the list of users will be displayed
		userList = new JTextArea(25,15);
		userList.setVisible(true);
		userList.setEditable(false);
		JScrollPane userListPane = new JScrollPane(userList);
		//userListPane.addMouseListener(this);
		middlePanel.add(userListPane);
		
		toAddTo.getContentPane().add(middlePanel, BorderLayout.CENTER); //adds middlePanel to the center position of the frame
	}
	
	public void DisplayMessage(String messageToDisplay) {

		chatLog.append(messageToDisplay + "\n");
		chatLog.revalidate();
	}
	
	public void AddWriter(PrintWriter toAdd) {
		write = toAdd;
	}
	
	public void CloseClient() {
		String closingMessage = "CloseUserConnection";
		write.println(closingMessage);
		write.flush();
		myWindow.dispose();
		write.close();
	}
	
	public void BuildUserList(String userListToBuild) {
		
		ArrayList<String> listOfUsers = new ArrayList<String>(Arrays.asList(userListToBuild.split(":")));
		listOfUsers.remove(0);
		
		//Collections.sort(listOfUsers, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(listOfUsers); //sorts items in listOfUsers alphabetically
		userList.setText("");
		
		for(int i = 0; i < listOfUsers.size(); ++i) {
			userList.append(listOfUsers.get(i) + "\n");
		}
		userList.revalidate();
	}

	void OpenDirectPrompt() {
		System.out.println("TEST!");
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		OpenDirectPrompt();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}
}