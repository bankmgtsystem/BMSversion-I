/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerTier;

import RemoteTier.Constants;
import RemoteTier.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.net.ssl.SSLSocket;

public class ServerThread extends Thread{
    private SSLSocket ssl_socket; 
    private DataInputStream reader;
    private PrintWriter writer;
    public ServerThread(SSLSocket socket){
        this.ssl_socket=socket;
    }
    public ServerThread(String username){
        
    }
    //this method is responsible for verifying the credentials
    private String verifyAccess(String username,byte[] password){
        return CentralServer.getRemoteDatabaseServer().login(username, password);
    }
    public void run(){
        try {
            this.ssl_socket.startHandshake();
            //this.input_stream=this.ssl_socket.getInputStream();
            this.reader=new DataInputStream(this.ssl_socket.getInputStream());
            this.writer=new PrintWriter(this.ssl_socket.getOutputStream()); 
            //the client sends its username with println ones handshaking is done...
            String username=reader.readUTF();
            //we prepare a buffer for the password...
            //i think we should do Buffer Overflow check here
            byte[] password=new byte[32];
            //then it reads the password into the byte array... 
            reader.read(password,0,password.length);
            //System.out.println("the length of password is "+password.length);
            //then we send to the client the response from the client;
            String reply=verifyAccess(username,password);
            this.writer.println(reply);
            this.writer.flush();
            while(!(reply.equalsIgnoreCase("manager") && reply.equalsIgnoreCase("clerk"))){
                username=reader.readUTF();
                password=new byte[32];
                reader.read(password,0,password.length);
                reply=verifyAccess(username,password);
                //if the reply is 'already signed in' the manager must be informed
                //and the connection terminated
                if(reply.equalsIgnoreCase(Constants.ALREADY_SIGNED_IN)){
                    String message="The account "+username+" was used to signin simultaneously";
                    ObjectOutputStream manager_socket=CentralServer.getSocketof("manager");
                    if(manager_socket!=null){
                        HashMap<String,Object> details=new HashMap<>();
                        details.put(Constants.ALREADY_SIGNED_IN, message);
                        manager_socket.writeObject(new Message(details,Constants.URGENT));
                        CentralServer.getRemoteDatabaseServer().saveTempObject("manager", message, 
                                username, password, Constants.STATUS_DELIVERED);
                    }else{
                        CentralServer.getRemoteDatabaseServer().saveTempObject("manager", message, username, 
                                password, Constants.STATUS_PENDING);
                    }
                    this.writer.println(reply);
                    this.writer.flush();
                    return;
                }
                this.writer.println(reply);
                this.writer.flush();
            }
            CentralServer.updateConnecteds(username, this.ssl_socket);
            //then the other side shoudl request the personalProfile and messages
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
