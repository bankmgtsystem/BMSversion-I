/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerTier;

import RemoteTier.Constants;
import RemoteTier.Message;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 *
 * @author Windows
 */
public class Clerk implements RemoteTier.ClerkTask{

    @Override
    public Message displayClientAccount(String account, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().getClientAccount(account, username, id);
    }

    @Override
    public String withdraw(String account, Double amount, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().withdraw(account, amount, username, id);
    }

    @Override
    public String deposit(String account,String subject,Double amount, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().deposit(account, subject, amount, username, id);
    }

    @Override
    public String createClientAccount(HashMap<String,Object> details, String username, byte[] id) {
        String reply=CentralServer.getRemoteDatabaseServer().createClient(details, username, id);
        //the followig is called to append to the profiles HashMap the account number of the new client.
        details.put(Constants.CLIENTS_TABLE_ACCOUNTS, reply);
        sendApprovalRequest(details);
        return reply;
    }

    @Override
    public Message personalProfile(String username, byte[] id) {
        System.out.println("personalProfile methid called");
         return CentralServer.getRemoteDatabaseServer().getClerkAccount(username, username, id);
    }

    @Override
    public String fillAttendance(String username, byte[] id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String fillLeavePermit(HashMap<String,Object> details, String username, byte[] id) {
        ObjectOutputStream peer_socket=CentralServer.getSocketof("manager");
        if(peer_socket!=null){
            try{
            peer_socket.writeObject(new Message(details,Constants.LEAVE_APPLICATION));
            CentralServer.getRemoteDatabaseServer().fillLeavePermit(details, username, id, Constants.STATUS_DELIVERED);
            return Constants.MESSAGE_DELIVERED;
            }catch(IOException io){
                
            }
        }
        CentralServer.getRemoteDatabaseServer().fillLeavePermit(details, username, id, Constants.STATUS_PENDING);
        return Constants.DESTINATION_OFFLINE;
    }

    @Override
    public void logout(String username, byte[] id) {
        CentralServer.removeConnecteds(username);
        CentralServer.getRemoteDatabaseServer().logout(username, id);
    }

    @Override
    public void setPassword(byte[] newPassword, String username, byte[] id) {
        CentralServer.getRemoteDatabaseServer().setPassword(newPassword, username, id);
    }
    //this method checks to see if the manager is online and..
    //if it is, sends to it direcrly the approval request without storing in the database
    private static void sendApprovalRequest(HashMap<String,Object> details){
        ObjectOutputStream manager_socket=CentralServer.getSocketof("manager");
        if(manager_socket!=null){
            try {
                Message request=new Message(details, Constants.APPROVE_CLIENT);
                manager_socket.writeObject(request);
            } catch (IOException ex) {
              
            }
        }
    }

    @Override
    public String sendMessage(String to, String payload, String username, byte[] id) {
        ObjectOutputStream peer_socket=CentralServer.getSocketof(to);
        if(peer_socket!=null){
            HashMap<String,Object> message=new HashMap();
            message.put(to, payload);
            try{
            peer_socket.writeObject(new Message(message,Constants.PEER_MESSAGE));
            CentralServer.getRemoteDatabaseServer().saveTempObject(to, payload, username, id, Constants.STATUS_DELIVERED);
            return Constants.MESSAGE_DELIVERED;
            }catch(IOException io){
                
            }
        }
        CentralServer.getRemoteDatabaseServer().saveTempObject(to, payload, username, id, Constants.STATUS_PENDING);
        return Constants.DESTINATION_OFFLINE;
    }

    @Override
    public Message receiveMessage(String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().getMessages(username, id);
    }

   
}
