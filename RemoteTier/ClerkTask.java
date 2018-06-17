/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RemoteTier;
import java.io.Serializable;
import java.rmi.Remote;
import java.util.HashMap;
/**
 *
 * @author Windows
 */
public interface ClerkTask extends Remote,Serializable{
    //the remote mothods will be defined here...
    //The (....String username,byte[] id) at the end of every method argument list will help identify the clerk...
    //this method will be responsible for viewing the clients' accounts...
    public abstract Message displayClientAccount(String account,String username,byte[] id);
    //this method will be responsible for withdrawing from clients' accounts
    public abstract String withdraw(String account,Double amount,String username,byte[] id);
    //this method will be responsible for depositing  to clients' accounts
    public abstract String deposit(String account,String subject,Double amount,String username,byte[] id);
    //this method will be responsible for creating client's accounts...
    public abstract String createClientAccount(HashMap<String,Object> profile,String username,byte[] id);
    //this method will be responsible viewing their personal profile...
    public abstract Message personalProfile(String username,byte[] id);
    //ones they have logged into the system this method will be called to fill attendance
    public abstract String fillAttendance(String username,byte[] id);
    //ones they are done filling thier form this method will be called to complete their submit.
    public abstract String fillLeavePermit(HashMap<String,Object> details,String username,byte[] id);
    //to log out...
    public abstract void logout(String username,byte[] id);
    //change password..
    public abstract void setPassword(byte[] newPassword,String username,byte[] id);
    //the following method will allow the clerk to send message to a specified employee
    public abstract String sendMessage(String to,String payload, String username,byte[] id);
    //the following method will be used to get a message that was sent when the client was offline
    public abstract Message receiveMessage( String username,byte[] id);
}
