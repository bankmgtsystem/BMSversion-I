/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DatabaseTier;

import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 *
 * @author Windows
 */
public class SQLFileHandler {
    private static volatile ArrayList<String> stringSQLFiles;
    private static volatile ArrayList<PreparedStatement> preparedSQLFiles;
    static{
        stringSQLFiles=new ArrayList<>();
        preparedSQLFiles=new ArrayList();
    }
    protected synchronized static void setString(String stringFile){
        stringSQLFiles.add(stringFile);
    }
    protected synchronized static void setPrepared(PreparedStatement statement){
        preparedSQLFiles.add(statement);
    }
    protected synchronized static ArrayList<String> getStringSQLs(){
        return stringSQLFiles;
    }
    protected synchronized static ArrayList<PreparedStatement> getPreparedSQLs(){
        return preparedSQLFiles;
    }
}
