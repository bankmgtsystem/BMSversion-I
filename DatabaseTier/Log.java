/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DatabaseTier;

import RemoteTier.Constants;
import java.sql.SQLException;

/**
 *
 * @author Windows
 */
class Log {
    private Log(){
        
    }
    protected static void saveLog(String employee,String action,String type){
        try{
        Constants.prepared=Constants.connector.prepareStatement("insert into Logger values(?,now(),?,?);");
        Constants.prepared.setString(1,employee);
        Constants.prepared.setString(2, action);
        Constants.prepared.setString(3,type);
        SQLFileHandler.setPrepared(Constants.prepared);
        synchronized(Constants.state){
            Constants.prepared.executeUpdate();
        }
        }
        catch(SQLException ex){
            
        }
    }
    protected static void logTransactions(String clerk,String account,
            String type,String subject,Double amount){
        try {
            Constants.prepared=Constants.connector.prepareStatement("insert into Transactions values("
                    + "?,?,?,?,round(?,2),now());");
            Constants.prepared.setString(1,account);
            Constants.prepared.setString(2,clerk);
            Constants.prepared.setString(3,type);
            Constants.prepared.setString(4,subject);
            Constants.prepared.setDouble(5,amount);
            SQLFileHandler.setPrepared(Constants.prepared);
            synchronized(Constants.state){
            Constants.prepared.executeUpdate();
            }
        } catch (SQLException ex) {
            
        }
    } 
}
