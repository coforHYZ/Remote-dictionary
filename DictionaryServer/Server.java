import java.net.*;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;


public class Server {
	static String name=null;
	static int port=0;
	static String hostname=null;
	static DataOutputStream pdos=null;
	static DataInputStream pdis=null;
	  public static void main(String args[]) throws IOException {
		  int size =2;
		  int maxsize=4;
		  long livetime=10;
		  BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(2);//waiting queue
		  ThreadFactory tfactory = new CreateThread();
		  RejectedExecutionHandler handler = new ToReject();//how to reject thread
	      ThreadPoolExecutor executor = new ThreadPoolExecutor(size, maxsize,livetime,	TimeUnit.MILLISECONDS,queue,tfactory,handler);
	      if(args[0]!=null)
	    	  port=Integer.parseInt(args[0]);
	      else
	    	  port=12306;
	      if(args[1]!=null)
	    	  name=args[1];
	      else
	    	  name="dictionary.txt";
	      name=args[1];
	      ServerSocket s = new ServerSocket(port);
	      s.setSoTimeout(300000);
	      System.out.println("Server is launching...");
	      while (true)
	      {
	    	  try {
	    		  Socket s1=s.accept(); 
	    		  serverthread sth=new serverthread(s1);
	    		  executor.execute(sth);
	    	  }catch(SocketTimeoutException e) {
	    		  System.out.println("No client connecting for long time...Server will close.");
	    		  break;
	    	  }
	      }
	      s.close();
	      System.exit(0);
	  }
}
 class ToReject implements RejectedExecutionHandler {
	 Date date=new Date();
	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		File f = new File("log.txt");
		try(BufferedWriter out = new BufferedWriter(new FileWriter(f,true))){
	    	out.write("Thread "+r.toString()+"for host:"+Server.hostname + " is rejected at"+date+"\r\n");
	    	Server.pdos.writeUTF("Sorry,You are in line. Please wait for 2 people.");
	    	out.close();
	    }
	    catch(IOException ex) {
            ex.printStackTrace();
        }
	}
}
 class CreateThread implements ThreadFactory{
	final AtomicInteger tnum = new AtomicInteger(1);
	Date date=new Date();
	File f = new File("log.txt");
	public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "Thread " + tnum.getAndIncrement());
        try(BufferedWriter out = new BufferedWriter(new FileWriter(f,true))){
	    	out.write("Thread " +r.toString()+"for Host:"+Server.hostname+ " create successfully at "+date+"\r\n");
	    	out.close();
	    }
	    catch(IOException e) {
            e.printStackTrace();
        }
        return t;
    }
}

class serverthread implements Runnable{
	Socket s1 =null;
	String host=null;
	public serverthread(Socket s) throws IOException {
		s1=s;
	}
	public synchronized void run(){
		
	     try {
	    	 host=s1.getInetAddress().getLocalHost().toString();
	    	 Server.hostname=host;
	    	 System.out.println("Client:"+host+" connected");
		     OutputStream s1out = s1.getOutputStream();
		     InputStream s1in = s1.getInputStream();
		     DataOutputStream dos = new DataOutputStream (s1out);
		     Server.pdos=dos;
		     DataInputStream dis = new DataInputStream (s1in);
		     Server.pdis=dis;
	    	 while(true) {
	    		 dos.writeUTF("Welcome! what do you want to do?\n"
	    		    + "Input:\n"
	    			+ "A)Query meaning of one word\n"
	    			+ "B)Add one word into dictionary\n"
	    			+ "C)Remove an existing word\n"
	    			+ "D)Quit\r\n");
	    		 dos.flush();
	    		 String choice = new String (dis.readUTF());
	    		 if (choice.equals("A")) {
	    			 dos.writeUTF("Please type the word...\r\n");
	    			 String s =new String(dis.readUTF());
	    			 if(read(s,dos)) continue;
	    			 query(s,dos);
	    		 }
	    		 else if (choice.equals("B")) {
	    			 dos.writeUTF("Please type the word added...\r\n");
	    			 String s =new String(dis.readUTF());
	    			 if(read(s,dos)) continue;
	    			 dos.writeUTF("Please type the meaning of the word...\r\n");
	    			 String m = new String (dis.readUTF());
	    			 if(read(m,dos)) continue;
	    			 add(s,m,dos);
	    		
	    		 }
	    		 else if (choice.equals("C")) {
	    			 dos.writeUTF("Please type the word to remove...\r\n");
	    			 String st = new String (dis.readUTF());
	    			 if(read(st,dos)) continue;
	    			 remove(st,dos);
	    		 }
	    		 else if (choice.equals("D")) {
	    			 System.out.println("Client:"+host+" disconnected");
	    			 dos.close();
	    			 dis.close();
	    			 s1out.close();
	    			 s1in.close();
	    			 s1.close();
	    			 break;
	    		 }
	    		 else {
	    			 dos.writeUTF("Sorry...That is illegal input.\r\n");
	    		 }
	    		 dos.writeUTF("Tap to continue...\r\n");
	    		 dis.readUTF();
	    	 }
	     }
	     catch(SocketException e) {
	    	 System.out.println("The client "+host+" shutdown.\r\n");
	     }
	     catch(IOException e) {
	    	 e.printStackTrace();
	     }
	     catch(Exception e) {
	    	 System.out.println(e.toString());  
	     }
	     
	}
	
	static Boolean read(String s,DataOutputStream dos) throws IOException{
		Boolean mless =false;
		for(int i = 0; i < s.length(); i++) {
			if(!(s.charAt(i)>='A'&&s.charAt(i)<='Z')&&!(s.charAt(i)>='a'&&s.charAt(i)<='z')&&s.charAt(i)!=' '&&s.charAt(i)!=','&&s.charAt(i)!='.') {
				dos.writeUTF("Sorry...You input illegal words.Please try again.");
				return true;
			}
		}
		return false;
		
		
	}
	
	static void remove(String st,DataOutputStream dos) throws IOException{
		try {
		    File f = new File(Server.name);
		    FileReader in = new FileReader(f);
		    LineNumberReader reader = new LineNumberReader(in);
		    StringBuffer s = new StringBuffer();
		    String word=null;
		    int number=-1;
		    while((word=reader.readLine())!=null)
		    {
		    	if (word.equals(st)) {
		    		reader.readLine();
		    		number++;
		    		continue;
		    	}
		    	s.append(word).append("\r\n");
		    }
		    if(number == -1) {
		    	dos.writeUTF("This word is not existing.No need to remove it.\r\n");
		    }
		    else {
		    	
		    	BufferedWriter out = new BufferedWriter(new FileWriter(f));
		    	out.write(s.toString());
		    	out.close();
		    	dos.writeUTF("Successfully remove it!\r\n");
		    }
		}catch(FileNotFoundException e) {
			dos.writeUTF("Sorry...The dictionary is not found\n");

	     }catch(IOException e) {
	    	 dos.writeUTF("The operation could not be done right...\n");

		 }catch (Exception e) {
			 dos.writeUTF("Unknown Error happen...\n");
		 } 
	 }
	static void add (String st,String m,DataOutputStream dos) throws IOException{
		 try {
			 File f = new File(Server.name);
		 
		    FileReader in = new FileReader(f);
		    LineNumberReader reader = new LineNumberReader(in);
		    int number =-1;
		    String word=null;
		    while((word=reader.readLine())!=null)
		    {
		    	if (word.equals(st))
		    	{
		    		number = reader.getLineNumber();
		    		dos.writeUTF("Duplicate:same word existing\r\n");
		    		return;
		    	}	
		     }
		     BufferedWriter out = new BufferedWriter(new FileWriter(f,true));
		     out.write(st+"\r\n");
		     out.write(m+"\r\n");
		     dos.writeUTF("Successfully add word!\r\n");
		     out.close();
	      }catch(FileNotFoundException e) {
	    	  dos.writeUTF("Sorry...The dictionary is not found\n");
		  }catch(IOException e) {
			  dos.writeUTF("The operation could not be done right...\n");
		  }catch (Exception e) {
			  dos.writeUTF("Unknown Error happen...\n");
		  } 
		    		
		    
		    
	  }
	static Boolean query (String st,DataOutputStream dos) throws IOException{
		try {
		    FileReader in = new FileReader(new File(Server.name));
		    LineNumberReader reader = new LineNumberReader(in);
		    int number =-1;
		    String content =null,word=null;
		    while((word=reader.readLine())!=null)
		    {
		    	if (word.equals(st))
		    	{
		    		number = reader.getLineNumber();
		    		content = reader.readLine();
		    		break;
		    	}	
		    }
		    if(number == -1) {
		    	dos.writeUTF("Sorry...This word is not found\r\n");
		    	return false;
		    }
		    else {
		    	dos.writeUTF(content);
		    	return true;
		    }
		}catch(FileNotFoundException e) {
			dos.writeUTF("Sorry...The dictionary is not found\n");
	    }catch(IOException e) {
	    	dos.writeUTF("The operation could not be done right...\n");
	    }catch (Exception e) {
	    	dos.writeUTF("Unknown Error happen...\n");
	    }
		return false;
	}
}
