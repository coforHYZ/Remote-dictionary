
import java.net.*;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class Client implements ActionListener{
	static JTextArea showArea;
    static JTextField msgText;
    JFrame mainJframe;
    JButton sentBtn;
    JScrollPane JSPane;
    JPanel pane;
    Container con;
    static DataInputStream dis;
    static DataOutputStream dos;
    public  Client()  {
    	mainJframe=new JFrame("Dictionary Client");
        con=mainJframe.getContentPane();
        showArea=new JTextArea();
        showArea.setEditable(false);
        showArea.setLineWrap(true);
        JSPane=new JScrollPane(showArea);
        msgText=new JTextField();
        msgText.setColumns(30);
        msgText.addActionListener(this);
        sentBtn=new JButton("Send");
        sentBtn.addActionListener(this);

        pane=new JPanel();
        pane.setLayout(new FlowLayout());
        pane.add(msgText);
        pane.add(sentBtn);

        con.add(JSPane, BorderLayout.CENTER);
        con.add(pane, BorderLayout.SOUTH);
        mainJframe.setSize (500 ,400);
        mainJframe.setVisible (true);
        mainJframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
	public void actionPerformed(ActionEvent e){
        String s=msgText.getText();
        if (s.length()>0){
            try{
                dos.writeUTF(s);
                dos.flush();
            } catch (IOException e1){
                showArea.append("Sorry...Message error!\n");
            }
        }

    }
	
    public static void main(String args[]) {
    	String addr=null;
    	int port=0;
	   try {
		if(args[0]!=null)
		      addr=args[0];
		else
		      addr="169.254.159.141";
		if(args[1]!=null)
		      port=Integer.parseInt(args[1]);
		else
		      port=12306;
		new Client();
	    Socket client = new Socket("169.254.159.141",12306);
	    // Get an input file handle from the socket and read the input
	    InputStream cin = client.getInputStream();
	    OutputStream cout = client.getOutputStream();
	    dis = new DataInputStream(cin);
	    dos = new DataOutputStream(cout);
	    while(true) {
	    	String st = new String (dis.readUTF());
	    	showArea.append(st);
	    	// When done, just close the connection and exit
	    	String choice = msgText.getText();
	    	if (choice.equals("A")) {
	    		showArea.append(dis.readUTF()+"\r\n");
	    		msgText.getText();//type the word
	    		showArea.append(dis.readUTF()+"\r\n");//get the meaning
	    	}
	    	else if(choice.equals("B")) {
	    		showArea.append(dis.readUTF()+"\r\n");
	    		msgText.getText();
	    		showArea.append(dis.readUTF()+"\r\n");
	    		msgText.getText();
	    		showArea.append(dis.readUTF()+"\r\n");
	    	}
	    	else if(choice.equals("C")) {
	    		showArea.append(dis.readUTF()+"\r\n");
	    		msgText.getText();
	    		showArea.append(dis.readUTF()+"\r\n");
	    	}
	    	else if(choice.equals("D")){
	    		showArea.append(dis.readUTF()+"\r\n");
	    		dis.close();
	        	dos.close();
	        	cin.close();
	        	cout.close();
	        	client.close();
	        	System.exit(0);
	    	}
	    	else
	    		showArea.append(new String (dis.readUTF()));
	    	showArea.append(new String (dis.readUTF()+"\r\n"));
	    	msgText.getText();;
	      }
	   }
	   catch(ConnectException e) {
		   showArea.append("Sorry...cannot find the server.Please check address\r\n");
	   }
	   catch(SocketException e) {
		   showArea.append("Sorry...The service is closed.Please try connection again\r\n");
	   }
	   catch(IOException e) {
	    	 e.printStackTrace();
	   }
	   catch(Exception e) {
	    	 e.printStackTrace();
	   }
  	}
}

