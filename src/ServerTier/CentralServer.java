/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerTier;

import RemoteTier.Constants;
import RemoteTier.RemoteDatabaseServer;
import RemoteTier.SslConnection;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 *
 * @author Windows
 */
public class CentralServer {
    //this should reference the IP of the database server
    private static final String DATABASE_OBJECT_IP="127.0.0.1";
    private static  RemoteDatabaseServer databseStub;
    private static SslRMIServerSocketFactory server;
    private static ServerSocket SERVER_SOCKET;
    //private static final int server_port=12345;
    private static Registry clerkRegistry;
    private static Registry managerRegistry;
    private static HashMap<String,ObjectOutputStream> onlineEmployees;
    private static final String MANAGER_OBJECT_KEY="manager";
    private static  Registry registry;
    static{
        try{
            onlineEmployees=new HashMap(onlineEmployees);
        }catch(NullPointerException ex){
            onlineEmployees=new HashMap();
        }   
        getDatabaseStub();
    }
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException{
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                CentralServer.destroyRMIObject();
            }
        });
        server=SslConnection.getServerFactory();
        getDatabaseStub();
        createRMIObject();
        AcceptConnection();
    }
    protected static synchronized RemoteDatabaseServer getRemoteDatabaseServer(){
        return CentralServer.databseStub;
    }
    private static void getDatabaseStub(){
        try {
            registry=LocateRegistry.getRegistry(DATABASE_OBJECT_IP,
                    Constants.DATABASE_OBJECT_PORT, SslConnection.getClientFactory());
            databseStub=(RemoteDatabaseServer)(registry.lookup(Constants.DATABASE_OBJECT_KEY));
            System.out.println(CentralServer.databseStub.getClass());
        } catch (RemoteException ex) {
            Logger.getLogger(CentralServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(CentralServer.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    private static void AcceptConnection()throws NoSuchAlgorithmException, IOException{
        SERVER_SOCKET=SslConnection.getDefaultServerSocket();
        while(true){
                SSLSocket socket=(SSLSocket)SERVER_SOCKET.accept();
                new ServerThread(socket).start();
        }
    }
    protected static void updateConnecteds(String username,SSLSocket socket){
        try {
            CentralServer.onlineEmployees.put(username,new ObjectOutputStream(socket.getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(CentralServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    protected static ObjectOutputStream getSocketof(String username){
        return CentralServer.onlineEmployees.get(username);
    }
    protected static void removeConnecteds(String username){
        CentralServer.onlineEmployees.remove(username);
    }
    //this method will be respendible for creating the ManagerTask and ClientTask remote objects and exporting them...
    private static void createRMIObject(){
        try {
             clerkRegistry=LocateRegistry.createRegistry(Constants.CLERK_OBJECT_PORT, null, server);
            clerkRegistry.bind(Constants.CLERK_OBJECT_KEY,new Clerk());
             managerRegistry=LocateRegistry.createRegistry(Constants.MANAGER_OBJECT_PORT, null, server);
            managerRegistry.bind(CentralServer.MANAGER_OBJECT_KEY,new Manager());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (AlreadyBoundException ex) {
            ex.printStackTrace();
        }

        
    }
    //ones the server is done with all connections and before closing it unbinds the thw remote objects..
    private static void destroyRMIObject(){
        try {
            if(clerkRegistry!=null){
            clerkRegistry.unbind(Constants.CLERK_OBJECT_KEY);
            }
            if(managerRegistry!=null){
             managerRegistry.unbind(CentralServer.MANAGER_OBJECT_KEY);   
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (NotBoundException ex) {
             ex.printStackTrace();
        }
    }
    //this methiod will be responsible for doing a security check...
    private void doSecurityCheck(){
        
    }
}
