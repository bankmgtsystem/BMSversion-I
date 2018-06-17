/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerTier;

import RemoteTier.Constants;
import RemoteTier.Message;
import RemoteTier.ManagerTask;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Windows
 */
public class Manager implements ManagerTask{
     
    @Override
    public Message displayAccount(String account, String type, String username, byte[] id) {
        switch(type){
            case Constants.MANAGER_OBJECT_KEY:
                return CentralServer.getRemoteDatabaseServer().getManagerAccount(username, id);
            case Constants.CLERK_OBJECT_KEY:
                return CentralServer.getRemoteDatabaseServer().getClerkAccount(account, username, id);
            case Constants.CLIENT_OBJECT_KEY:
                return CentralServer.getRemoteDatabaseServer().getClientAccount(account, username, id);
            default:
                return null;
        }
    }

    @Override
    public String createClerkAccount(HashMap<String, Object> profiles,String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().createClerk(profiles, username, id);
    }

    @Override
    public Message checkActiveConnection(String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().getActiveConnections(username, id);
    }

    @Override
    public String activate(ArrayList<String> accountNumbers, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().saveCreatedAccount(accountNumbers, username, id);
    }
    @Override
    public Message generateActivityReport(String clerk,Timestamp from, Timestamp upto, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().generateActivityReport(from, upto, clerk, username, id);
    }

    @Override
    public Message generateBankStatement(String accountNumber, Timestamp from, Timestamp upto, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().generateBankStatement(from, upto, accountNumber, username, id);
    }

    @Override
    public void logout(String username, byte[] id) {
        CentralServer.getRemoteDatabaseServer().logout(username, id);
    }

    @Override
    public String setUsername(String newUsername, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().setUsername(newUsername, username, id);
    }

    @Override
    public String setPassword(byte[] newPassword, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().setPassword(newPassword, username, id);
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
    public Message manageApprovedRequest(String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().saveAccountUpdate(username, id);
    }

    @Override
    public Message generateSystemReport(Timestamp from, Timestamp to, String username, byte[] id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Message generateIdsReport(Timestamp from, Timestamp to, String username, byte[] id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Message receiveMessage(String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().getMessages(username, id);
    }

    @Override
    public Message getLeaveApplications(String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().getLeaveApplication(username, id);
    }

    @Override
    public String approveRequests(ArrayList<String> applicants, String username, byte[] id) {
        ObjectOutputStream peer_socket;
        for(String applicant:applicants){
            peer_socket=CentralServer.getSocketof(applicant);
            if(peer_socket!=null){
                HashMap<String,Object> message=new HashMap();
                message.put(Constants.MANAGER_OBJECT_KEY, Constants.REQUEST_APPROVED);
                try{
                peer_socket.writeObject(new Message(message,Constants.LEAVE_APPLICATION));
                CentralServer.getRemoteDatabaseServer().approveRequest(applicant, username, id, Constants.STATUS_DELIVERED);
                }catch(IOException io){
                }
            }
            CentralServer.getRemoteDatabaseServer().approveRequest(applicant, username, id, Constants.STATUS_PENDING);
        }
        return Constants.SUCCESS_MESSAGE;
    }

    @Override
    public String disapproveRequests(ArrayList<String> applicants, String username, byte[] id) {
        ObjectOutputStream peer_socket;
        for(String applicant:applicants){
            peer_socket=CentralServer.getSocketof(applicant);
            if(peer_socket!=null){
                HashMap<String,Object> message=new HashMap();
                message.put("manager", Constants.REQUEST_DENIED);
                try{
                peer_socket.writeObject(new Message(message,Constants.LEAVE_APPLICATION));
                CentralServer.getRemoteDatabaseServer().disapproveRequest(applicant, username, id, Constants.STATUS_DELIVERED);
                }catch(IOException io){

                }
            }
            CentralServer.getRemoteDatabaseServer().disapproveRequest(applicant, username, id, Constants.STATUS_PENDING);
        }
        return Constants.SUCCESS_MESSAGE;
    }

    @Override
    public String blockClerkAccount(String clerkUsername, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().blockClerkAccount(clerkUsername, username, id);
    }

    @Override
    public String activateClerkAccount(String clerkUsername, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().activateClerkAccount(clerkUsername, username, id);
    }

    @Override
    public String deleteClerkAccount(String clerkUsername, String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().deleteClerkAccount(clerkUsername, username, id);
    }

    @Override
    public Message getUrgentMessages(String username, byte[] id) {
        return CentralServer.getRemoteDatabaseServer().getUrgentMessages(username, id);
    }
    
}
