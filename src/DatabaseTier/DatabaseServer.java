/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DatabaseTier;

import RemoteTier.Constants;
import RemoteTier.SslConnection;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 *
 * @author Windows
 */
public class DatabaseServer {
    private static Registry registry;
    private static boolean shut_down=false;
    public static void main(String[] args){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                BackupUpdateHandler.update();
            }
        });
        try {
            Constants.state.executeUpdate("use bank_management;");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseServer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        export_stub();
        setBackupScheduler();
        while(!DatabaseServer.shut_down){
            //simply be online 
        }
        remove_stub();
    }
    protected static void shut_down(){
        DatabaseServer.shut_down=true;
    }
    private static void export_stub(){
        SslRMIServerSocketFactory server_factory=SslConnection.getServerFactory();
        try {
            DatabaseServer.registry=LocateRegistry.createRegistry(Constants.DATABASE_OBJECT_PORT, null, server_factory);
            DatabaseServer.registry.rebind(Constants.DATABASE_OBJECT_KEY, new DatabaseFileHandler());
        } catch (RemoteException ex) {
            Logger.getLogger(DatabaseServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static void remove_stub(){
        try {
            DatabaseServer.registry.unbind(Constants.DATABASE_OBJECT_KEY);
        } catch (RemoteException ex) {
            Logger.getLogger(DatabaseServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(DatabaseServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static void  setBackupScheduler(){
       Long delay=(new Double(Math.random()*10000000)).longValue();
       Long period=(new Double(Math.random()*1000000)).longValue();
       new Timer().schedule(new TimerTask(){
           public void run(){
               BackupUpdateHandler.update();
           }
       }, delay, period);
    }
}
