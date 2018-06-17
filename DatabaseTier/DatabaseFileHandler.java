/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DatabaseTier;
import RemoteTier.Constants;
import RemoteTier.Message;
import RemoteTier.RemoteDatabaseServer;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.serial.SerialBlob;
/**
 *
 * @author Windows
 */
public class DatabaseFileHandler implements RemoteDatabaseServer{
    private Message getAccountProfile(String subject,String type) {
        String table="";
        String appropriate_column="";
        String identifier="";
        String image="";
        switch(type){
            case "client":
                identifier=Constants.ACCOUNT_BALANCE;
                table="clients";
                appropriate_column=Constants.CLIENTS_TABLE_ACCOUNTS;
                image=",image";
                break;
            case "employee":
                System.out.println("it is an employeeee");
                identifier=Constants.EMPLOYEE_USERNAME;
                table="employees";
                appropriate_column=Constants.EMPLOYEE_USERNAME;
                image=",image";
                break;
            case "employees":
                identifier=Constants.EMPLOYEE_USERNAME;
                table="employees";
                appropriate_column=Constants.EMPLOYEE_USERNAME;
                break;
            case "clients":
                identifier=Constants.ACCOUNT_BALANCE;
                table="clients";
                appropriate_column=Constants.CLIENTS_TABLE_ACCOUNTS;
                break;
        }
        try {
            String query="select "+identifier+",firstName,middleName,lastName,sex,nationality,age,fixedPhoneAddress,"
                    + "mobilePhoneAddress,kebele,woreda,houseNumber,"
                    + "subcity,region"+image+" from "+table+" where "+appropriate_column+" like '"+subject+"';";
            synchronized(Constants.state){
                Constants.result=Constants.state.executeQuery(query);
                System.out.println("result set obtained");
            }
            return new Message(Constants.result);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
//i dont like how this method operates.
    private ResultSet getRequest(String username, byte[] id,String type) {
        if(checkAccess(username, id)){
            String update_time;
            String update_status;
            String query;
            try{
            if(type==null){
                query="select sender,message from messages where receiver='"+username+"' "
                    + "and status='Pending' and messageType is null";
                update_time="update messages set deliveredTime=now() where receiver='"+username+
                        "' and status='Pending' and messageType is null;";
                update_status="update messages set status='Delivered' where "
                        + "receiver='"+username+"' and messageType is null;";
            }
            else{
                query="select sender,message from messages where receiver='"+username+"' "
                    + "and status='Pending' and messageType='"+type+"';";
                update_time="update messages set deliveredTime=now() where receiver='"+username+
                        "' and status='Pending' and messageType='"+type+"';";
                update_status="update messages set status='Delivered' where "
                        + "receiver='"+username+"' and messageType='"+type+"';";
            }
            synchronized(Constants.state){
                Constants.result=Constants.state.executeQuery(query);
                if(Constants.result.next()){
                    Constants.state.executeUpdate(update_time);
                    Constants.state.executeUpdate(update_status);
                    SQLFileHandler.setString(update_time);
                    SQLFileHandler.setString(update_status);
                    Log.saveLog(username, Constants.OBTAINED_MESSAGES, Constants.ACTION_ROUTINE);
                    Constants.result.beforeFirst();
                }
            }
            
            return Constants.result;
            } catch (SQLException ex) {
                
            }
        }
        return null;
    }

    @Override
    public Message getNmapScanReportOn(String filter,String username, byte[] id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Message getClientAccount(String account_number,String username, byte[] id) {
        if(checkAccess(username, id)){
            if(isClient(account_number)){
                Log.saveLog(username,Constants.PERSONAL_INFO+account_number,Constants.ACTION_ROUTINE);
                return this.getAccountProfile(account_number, "client");
            }
            else if(account_number.equals(Constants.WILD_CARD)){
                Log.saveLog(username,Constants.ALL_EMPLOYEES,Constants.ACTION_ROUTINE);
                return this.getAccountProfile(account_number, "clients");
            }
            return new Message(null, Constants.NO_SUCH_ACTIVE_ACCOUNT);
        }
        return null;
    }

    @Override
    public Message getManagerAccount(String username, byte[] id) {
        if(checkAccess(username, id)){
            Log.saveLog(username,Constants.PERSONAL_INFO,Constants.ACTION_ROUTINE);
            return this.getAccountProfile(username, "employee");
        }
        return null;
    }

    @Override
    public Message getClerkAccount(String clerkUsername,String username, byte[] id) {
        if(checkAccess(username, id)){
            if(isEmployee(clerkUsername)){
                Log.saveLog(username,Constants.PERSONAL_INFO+clerkUsername,Constants.ACTION_ROUTINE);
                return this.getAccountProfile(clerkUsername, "employee");
            }
            else if(clerkUsername.equals(Constants.WILD_CARD)){
                Log.saveLog(username,Constants.ALL_EMPLOYEES,Constants.ACTION_ROUTINE);
                return this.getAccountProfile(clerkUsername, "employees");
            }    
            return new Message(null, Constants.NO_SUCH_EMPLOYEE);
        }
        return null;
    }

    @Override
    public Message generatetSecurityReport(Timestamp from, Timestamp to, String about,String username, byte[] id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Message generatetSystemReport(Timestamp from, Timestamp to, String about,String username, byte[] id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Message generatetIdsReport(Timestamp from, Timestamp to, String about,String username, byte[] id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Message generateActivityReport(Timestamp from, Timestamp to, String employee,String username, byte[] id) {
        if(checkAccess(username, id)){
            if(isEmployee(employee)|| employee.equals(Constants.WILD_CARD)){
                try {
                    Constants.prepared=Constants.connector.prepareStatement("select * from logger where timeOfAction>=? "
                            + "and timeOfAction<=? and employee like ?");
                    Constants.prepared.setTimestamp(1, from);
                    Constants.prepared.setTimestamp(2, to);
                    Constants.prepared.setString(3, employee);
                    synchronized(Constants.state){
                        Constants.result=Constants.prepared.executeQuery();
                    }
                    if(employee.equals("%")){
                        Log.saveLog(username, Constants.OBTAINED_ACTIVITY_ABOUT_ALL, Constants.ACTION_ROUTINE);
                    }
                    else{
                        Log.saveLog(username, Constants.OBTAINED_ACTIVITY_ABOUT+employee, Constants.ACTION_ROUTINE);
                    }
                    Constants.result.beforeFirst();
                    return new Message(Constants.result);
                } catch (SQLException ex) {

                }
            }
            return new Message(null,Constants.NO_SUCH_EMPLOYEE);
        }
        return null;
    }

    @Override
    public Message generateBankStatement(Timestamp from, Timestamp to, String client_account_number,String username, byte[] id) {
        if(checkAccess(username, id)){
            try {
                Constants.prepared=Constants.connector.prepareStatement("select client,type,subject,amount,timeOfAction"
                        + "from transactions where timeOfAction>=? and timeOfAction<=? and client=?;");
                Constants.prepared.setTimestamp(1,from);
                Constants.prepared.setTimestamp(2,to);
                Constants.prepared.setString(1,client_account_number);
                synchronized(Constants.state){
                    Constants.result=Constants.prepared.executeQuery();
                }
                if(isClient(client_account_number)){
                    Log.saveLog(username, Constants.GENERATED_BANK_STATEMENT+
                            client_account_number, Constants.ACTION_ROUTINE);
                }
                return new Message(Constants.result);
            } catch (SQLException ex) {
                
            }
        }
        return null;
    }

    @Override
    public Message getActiveConnections(String username, byte[] id) {
        if(checkAccess(username, id)){
            try {
                synchronized(Constants.state){
                    Constants.result=Constants.state.executeQuery("select * from active_connections;");
                }    
                Log.saveLog(username, Constants.ACTIVE_CONNECTIONS, Constants.ACTION_ROUTINE);
                return new Message(Constants.result);
            } catch (SQLException ex) {
                
            }
        }
        return null;
    }

    @Override
    public Message saveAccountUpdate(String username, byte[] id) {
        if(checkAccess(username, id)){
            try{
                synchronized(Constants.state){
                    Constants.result=Constants.state.executeQuery("select * from clients "
                        + "where accountStatus='Pending';");
                }
                return new Message(Constants.result);
            }
            catch(SQLException e){
                //we should do something with them...
            }
        }
        return null;
    }

    @Override
    public String saveCreatedAccount(ArrayList<String> verified,String username, byte[] id) {
        if(checkAccess(username, id)){
            try {
                for(String client:verified){
                    String update="update clients set accountStatus='Active' "
                            + "where accountNumber='"+client+"';";
                    synchronized(Constants.state){
                        Constants.state.executeUpdate(update);
                    }
                    SQLFileHandler.setString(update);
                }
                Log.saveLog(username, Constants.VERIFIED_ACCOUNTS, Constants.ACTION_ROUTINE);
                return "Success";
            } catch (SQLException ex) {
                return "Unable to update";
            }
        }
        return null;
    }

    @Override
    public String saveTempObject(String to,String message,String username, byte[] id,String status) {
        if(checkAccess(username, id)){
            if(isEmployee(to)){
                try{
                    Constants.prepared=Constants.connector.prepareStatement("insert into messages"
                            + "(sender,receiver,message,status) values(?,?,?,?);");
                    Constants.prepared.setString(1, username);
                    Constants.prepared.setString(2, to);
                    Constants.prepared.setString(3, message);
                    Constants.prepared.setString(4, status);
                    SQLFileHandler.setPrepared(Constants.prepared);
                    synchronized(Constants.state){
                        Constants.prepared.executeUpdate();
                    }
                    Log.saveLog(username, Constants.SENT_MESSAGE+to, Constants.ACTION_ROUTINE);
                    return Constants.SUCCESS_MESSAGE;
                }catch(SQLException e){
                    return Constants.UNABLE_TO_PERFORM;
                }
            }
            return Constants.NO_SUCH_EMPLOYEE;
        }
        return null;
    }

    @Override
    public void logout(String username, byte[] id) {
        if(checkAccess(username, id)){
            try {
                String deletion="delete from active_connections where username='"+username+"';";
                synchronized(Constants.state){
                    Constants.state.executeUpdate(deletion);
                }
                SQLFileHandler.setString(deletion);
                Log.saveLog(username, Constants.LOG_OUT, Constants.ACTION_ROUTINE);
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseFileHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String fillAttendance(String username, byte[] id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String fillLeavePermit(HashMap<String,Object> details, String username, byte[] id,String status) {
        if(checkAccess(username, id)){
            String message=details.get(Constants.FULLNAME)+"\n"+details.get(Constants.REASON_FOR_APPLICATION)+"\n"+
                    details.get(Constants.FROM_DAY)+"\n"+details.get(Constants.TO_DAY);
            try {
                Constants.prepared=Constants.connector.prepareStatement("insert into messages (sender,receiver,"
                        + "message,status,messageType)values(?,?,?,?,?);");
                Constants.prepared.setString(1, username);
                Constants.prepared.setString(2, "manager");
                Constants.prepared.setString(3, message);
                Constants.prepared.setString(4, status);
                Constants.prepared.setString(5, Constants.LEAVE_APPLICATION);
                SQLFileHandler.setPrepared(Constants.prepared);
                synchronized(Constants.state){
                    Constants.prepared.executeUpdate();
                }
                Log.saveLog(username, Constants.LEAVE_PERMIT, Constants.ACTION_ROUTINE);
                return Constants.SUCCESS_MESSAGE;
            } catch (SQLException ex) {
                return Constants.UNABLE_TO_PERFORM;
            }
        
        }
        return null;
    }

    @Override
    public String login(String username, byte[] password) {
        //return Constants.MANAGER_OBJECT_KEY;
        if(checkAccess(username, password)){
            try{
                synchronized(Constants.state){
                    Constants.state.execute("use bank_management;");
                    Constants.result=Constants.state.executeQuery("select username from "
                                + "active_connections where username='"+username+"';");
                    if(Constants.result.next()){
                        return Constants.ALREADY_SIGNED_IN;
                    }
                    else{
                       Constants.result=Constants.state.executeQuery("select status from "
                               + "employees where username='"+username+"';");
                       if(Constants.result.next()){
                            if(Constants.result.getString(1).equalsIgnoreCase(Constants.ACCOUNT_BLOCKED)){
                                return Constants.BLOCKED_MESSAGE;
                            }
                       } 
                        Constants.result=Constants.state.executeQuery("select type from "
                                + "employees where username='"+username+"';");
                        Constants.result.next();
                        return Constants.result.getString(1);
                    }
                }
            }catch(SQLException ex){
                ex.printStackTrace();
                return Constants.UNABLE_TO_PERFORM;
            }
        }
        return Constants.INCORRECT_CREDENTIALS;
    }

    @Override
    public String withdraw(String account, Double amount, String username, byte[] id) {
        if(checkAccess(username, id)){
            if(isClient(account)){
                Double current_balance=getBalance(account);
                if(current_balance<amount){
                    try{
                        Constants.prepared=Constants.connector.prepareStatement("update Clients set accountBalance="
                        + "round(accountBalance-?,2) where accountNumber=?;");
                        Constants.prepared.setDouble(1, amount);
                        Constants.prepared.setString(2, account);
                        SQLFileHandler.setPrepared(Constants.prepared);
                        synchronized(Constants.state){
                            Constants.prepared.executeUpdate();
                        }
                        Log.logTransactions(username, account, "withdrow", "self", amount);
                        Log.saveLog(username,"withdrew "+amount+" from "+account, Constants.ACTION_ROUTINE);
                        return Constants.SUCCESS_MESSAGE;
                    }catch(SQLException ex){
                        return Constants.UNABLE_TO_PERFORM;
                    }
                }
                return "The account contains only "+current_balance;
            }
            return Constants.NO_SUCH_ACTIVE_ACCOUNT;
        }
        return null;
    }

    @Override
    public String deposit(String account, String subject,Double amount, String username, byte[] id) {
        if(checkAccess(username, id)){
            if(isClient(account)){
                try {
                    Constants.prepared=Constants.connector.prepareStatement("update Clients set accountBalance="
                            + "round(accountBalance+?,2) where accountNumber=? ;");
                    Constants.prepared.setDouble(1, amount);
                    Constants.prepared.setString(2, account);
                    SQLFileHandler.setPrepared(Constants.prepared);
                    synchronized(Constants.state){
                        Constants.prepared.executeUpdate();
                    }
                    Log.logTransactions(username, account, "deposit", subject, amount);
                    Log.saveLog(username, "deposited "+amount+" to "+account, Constants.ACTION_ROUTINE);
                    return Constants.SUCCESS_MESSAGE;
                } catch (SQLException ex) {
                    return Constants.UNABLE_TO_PERFORM;
                }
            }
            return Constants.NO_SUCH_ACTIVE_ACCOUNT;
        }
        return null;
    }
    private static Double getBalance(String account_number){
        try {
            synchronized(Constants.state){
                Constants.result=Constants.state.executeQuery("select accountBalance "
                        + "from clients where='"+account_number+"';");
            }
            Constants.result.next();
            return Constants.result.getDouble(1);
        } catch (SQLException ex) {
            return 0.0;
        }
    }
    private static boolean checkAccess(String username,byte[] password){
        try {
            synchronized(Constants.state){
                Constants.state.executeUpdate("use bank_management;");
                Constants.result=Constants.state.executeQuery("select password from "
                        + "employees where username='"+username+"';");
            }
            if(Constants.result.next()){
                Blob password_blob=Constants.result.getBlob(1);
                byte[] correct_password=password_blob.getBytes(1, Long.valueOf(password_blob.length()).intValue());
                ByteBuffer input_buffer=ByteBuffer.wrap(password);
                ByteBuffer correct_buffer=ByteBuffer.wrap(correct_password);
                return input_buffer.equals(correct_buffer);
            }
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    private static boolean isClient(String account_number){
        try {
            synchronized(Constants.state){
                Constants.result=Constants.state.executeQuery("select accountNumber from clients "
                        + "where accountNumber='"+account_number+"' and accountStatus='Active';");
            }
            return Constants.result.next();
        } catch (SQLException ex) {
            return false;
        }
    }
    private static boolean isEmployee(String username){
        try {
            synchronized(Constants.state){
                Constants.result=Constants.state.executeQuery("select username from "
                        + "employees where username='"+username+"';");
            }
            return Constants.result.next();
        } catch (SQLException ex) {
            return false;
        }
    }

    @Override
    public String createClient(HashMap<String, Object> details, String username, byte[] id) {
        String[] columns={Constants.CLIENTS_TABLE_ACCOUNTS,Constants.FIRST_NAME,Constants.MIDDLE_NAME,
        Constants.LAST_NAME,Constants.SEX,Constants.NATIONALITY,Constants.AGE,Constants.FIXED_PHONE,
        Constants.MOBILE_PHONE,Constants.KEBELE,Constants.WOREDA, Constants.HOUSE_NUMBER,Constants.SUB_CITY,
        Constants.REGION,Constants.ACCOUNT_BALANCE,Constants.INTEREST_RATE,Constants.IMAGE};
        try {
            Constants.prepared=Constants.connector.prepareStatement("insert into clients values("
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),'Pending',?,?,?;");
            //if there are no accounts yet created this will probabaly return 
            String new_account=getAccountNumber();
            Constants.prepared.setString(1,new_account);
            int column;
            for(column=1;column<columns.length-1;column++){
                Constants.prepared.setString(column+1,(String)details.get(columns[column]));
            }
            Constants.prepared.setBlob(column+1,(Blob)details.get(columns[column]));
            SQLFileHandler.setPrepared(Constants.prepared);
            synchronized(Constants.state){
                Constants.prepared.executeUpdate();
            }
            return new_account;
        } catch (SQLException ex) {
            return Constants.UNABLE_TO_PERFORM;
        }
    }

    @Override
    public String createClerk(HashMap<String, Object> details, String username, byte[] id) {
        String[] columns={Constants.EMPLOYEE_USERNAME,Constants.FIRST_NAME,Constants.MIDDLE_NAME,
        Constants.LAST_NAME,Constants.SEX,Constants.NATIONALITY,Constants.AGE,Constants.FIXED_PHONE,
        Constants.MOBILE_PHONE,Constants.KEBELE,Constants.WOREDA,Constants.HOUSE_NUMBER,Constants.SUB_CITY,
        Constants.REGION,Constants.PASSWORD,Constants.IMAGE};
        try {
            MessageDigest digester=MessageDigest.getInstance("SHA-256");
            digester.update("Admin123".getBytes());
            Constants.prepared=Constants.connector.prepareStatement("insert into employees values("
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),?,'Clerk',?,'Active');");
            String defaultUser="Admin";
            int step=0;
            while(isEmployee(defaultUser)){
                defaultUser="Admin"+step;
                if(defaultUser.length()>10){
                    defaultUser=anotherRoute(defaultUser);
                }
                step++;
            }
            Constants.prepared.setString(1,defaultUser);
            int column;
            for(column=1;column<columns.length-1;column++){
                if(column!=14){
                    Constants.prepared.setString(column+1,(String)details.get(columns[column]));
                }else{
                    Constants.prepared.setBlob(column+1,new SerialBlob(digester.digest()));
                }
            }
            Constants.prepared.setBlob(column+1,(Blob)details.get(columns[column]));
            SQLFileHandler.setPrepared(Constants.prepared);
            synchronized(Constants.state){
                Constants.prepared.executeUpdate();
            }
            return defaultUser;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Constants.UNABLE_TO_PERFORM;
        }
        catch (NoSuchAlgorithmException ex) {
            return Constants.UNABLE_TO_PERFORM;
        }
    }
    private String anotherRoute(String deadend){
        int admin_part=deadend.indexOf("n");
        Character int_begin=deadend.charAt(admin_part+1);
        int new_value=Integer.parseInt(int_begin.toString());
        return "Admin"+new_value;
    }
    private String getAccountNumber() throws SQLException{
        Constants.result=Constants.state.executeQuery("select max(accountNumber) from Clients;");
        if(Constants.result.next()){
            Long value=(Constants.result.getLong("max(accountNumber)"))+7;
            String new_account=Long.toString(value);
            if(new_account.length()==13){
                return new_account;
            }
            else{
                int zero_position=new_account.indexOf("0");
                String new_sequence="";
                for(int i=1;i<=zero_position;i++) new_sequence+=i;
                new_sequence+=0;
                int j=1;
                while(new_sequence.length()<14){
                    new_sequence+=j;
                    j++;
                }
                return new_sequence;
            }
        }
        else{
            return "1234567890123";
        }
    }

    @Override
    public String setPassword(byte[] newPassword, String username, byte[] id) {
        if(checkAccess(username, id)){
            try {
                Constants.prepared=Constants.connector.prepareStatement("update employees set password=? where username=?;");
                Constants.prepared.setBlob(1, new SerialBlob(newPassword));
                Constants.prepared.setString(2,username);
                SQLFileHandler.setPrepared(Constants.prepared);
                synchronized(Constants.state){
                    Constants.prepared.executeUpdate();
                }
                return Constants.SUCCESS_MESSAGE;
            } catch (SQLException ex) {
                return Constants.UNABLE_TO_PERFORM;
            }
        }
        return null;
    }

    @Override
    public Message getLeaveApplication(String username, byte[] id) {
        if(checkAccess(username, id)){
            Constants.result=getRequest(username, id, Constants.LEAVE_APPLICATION);
            HashMap<String,Object> details=new HashMap();
            try {
                String flag=Constants.NO_NEW_MESSAGE;
                while(Constants.result.next()){
                    flag=Constants.LEAVE_APPLICATION;
                    details.put(Constants.result.getString(1), Constants.result.getString(2));
                }
                return new Message(details,flag);
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseFileHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    //when the manager logins this method will be called to retrieve urgent messges immediately 
    @Override
    public Message getUrgentMessages(String username, byte[] id) {
        if(checkAccess(username, id)){
            ResultSet applications=getRequest(username, id, Constants.URGENT);
            HashMap<String,Object> details=new HashMap();
            try {
                String flag=Constants.NO_NEW_MESSAGE;
                while(applications.next()){
                    flag=Constants.URGENT;
                    details.put(applications.getString(2), applications.getString(1));
                }
                return new Message(details,flag);
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseFileHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public Message getMessages(String username, byte[] id) {
        if(checkAccess(username, id)){
            ResultSet applications=getRequest(username, id, null);
            HashMap<String,Object> details=new HashMap();
            try {
                String flag=Constants.NO_NEW_MESSAGE;
                while(applications.next()){
                    flag=Constants.ROUTINE_MESSAGE;
                    details.put(applications.getString(2), applications.getString(1));
                }
                return new Message(details,flag);
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseFileHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public String setUsername(String newUsername, String username, byte[] id) {
        if(checkAccess(username, id)){
            if(!isEmployee(newUsername)){
                try {
                    String update="update employees set username='"+newUsername+
                            "' where username='"+username+"';";
                    synchronized(Constants.state){
                        Constants.state.executeUpdate(update);
                    }
                    SQLFileHandler.setString(update);
                    return Constants.SUCCESS_MESSAGE;
                } catch (SQLException ex) {
                    return Constants.UNABLE_TO_PERFORM;
                }
                
            }
            return Constants.USERNAME_EXISTS;
        }
        return null;
    }

    @Override
    public void approveRequest(String applicant, String username, byte[] id,String status) {
        if(checkAccess(username, id)){
            String update;
            try {
                update="insert into messages (sender,receiver,message,status) "
                        + "values('manager','"+applicant+"','"+Constants.REQUEST_APPROVED+"','"+status+"');";
                SQLFileHandler.setString(update);
                synchronized(Constants.state){
                Constants.state.executeUpdate(update);
                }
                Log.saveLog(applicant, Constants.APPROVED_REQUEST+applicant, Constants.ACTION_ROUTINE);
                } catch (SQLException ex) {
                }
        }
    }

    @Override
    public void disapproveRequest(String applicant, String username, byte[] id,String status) {
        if(checkAccess(username, id)){
            String update;
                try {
                    update="insert into messages (sender,receiver,message,status) "
                            + "values('manager','"+applicant+"','"+Constants.REQUEST_DENIED+"','"+status+"');";
                    SQLFileHandler.setString(update);
                    synchronized(Constants.state){
                    Constants.state.executeUpdate(update);
                    }
                    Log.saveLog(applicant, Constants.DENIED_REQUEST+applicant, Constants.ACTION_ROUTINE);
                } catch (SQLException ex) {
                }
        }
    }

    @Override
    public String blockClerkAccount(String clerkUsername, String username, byte[] id) {
        if(checkAccess(username, id)){
            if(isEmployee(clerkUsername)){
                synchronized(Constants.state){
                    try {
                        Constants.state.executeUpdate("update employees set status='Blocked' "
                                + "where username='"+clerkUsername+"';");
                        return Constants.SUCCESS_MESSAGE;
                    } catch (SQLException ex) {
                        return Constants.UNABLE_TO_PERFORM;
                    }
                }
            }
            return Constants.NO_SUCH_EMPLOYEE;
        }
        return null;
    }

    @Override
    public String activateClerkAccount(String clerkUsername, String username, byte[] id) {
        if(checkAccess(username, id)){
            if(isEmployee(clerkUsername)){
                synchronized(Constants.state){
                    try {
                        Constants.state.executeUpdate("update employees set status='Active' "
                                + "where username='"+clerkUsername+"';");
                        return Constants.SUCCESS_MESSAGE;
                    } catch (SQLException ex) {
                        return Constants.UNABLE_TO_PERFORM;
                    }
                }
            }
            return Constants.NO_SUCH_EMPLOYEE;
        }
        return null;
    }

    @Override
    public String deleteClerkAccount(String clerkUsername, String username, byte[] id) {
        if(checkAccess(username, id)){
            if(isEmployee(clerkUsername)){
                synchronized(Constants.state){
                    try {
                        Constants.state.executeUpdate("delete from employees where username='"+clerkUsername+"';");
                        return Constants.SUCCESS_MESSAGE;
                    } catch (SQLException ex) {
                        return Constants.UNABLE_TO_PERFORM;
                    }
                }
            }
            return Constants.NO_SUCH_EMPLOYEE;
        }
        return null;
    }
}
