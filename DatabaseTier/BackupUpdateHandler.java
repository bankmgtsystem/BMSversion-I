/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DatabaseTier;

import RemoteTier.Constants;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Windows
 */
public class BackupUpdateHandler {
    protected static void update(){
        synchronized(Constants.state){
            try {
                    Constants.state.executeUpdate("use backup_bank_management;");
                    for(String sql:SQLFileHandler.getStringSQLs()){
                        Constants.state.executeUpdate(sql);
                    }
                    for(PreparedStatement sql:SQLFileHandler.getPreparedSQLs()){
                        sql.executeUpdate();
                    }
                    SQLFileHandler.getStringSQLs().clear();
                    SQLFileHandler.getPreparedSQLs().clear();
                    Constants.state.executeUpdate("use bank_management;");
                } catch (SQLException ex) {
                    Logger.getLogger(DatabaseServer.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    } 
}
