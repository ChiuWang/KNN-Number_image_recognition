package knn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectMysql {
//	public static void main(String[] args) throws Exception {
//		ConnectMysql ca = new ConnectMysql();
//  	ca.ConnectionMysqlFile();
//	}

	public void ConnectionMysqlFile() throws Exception   
	    {  
	        String s1=new String("C:\\KNN\\testDigits\\");
	        String testtable = "test";
	        insertIntoTable(s1,testtable);
			
			
			String s2=new String("C:\\KNN\\trainingDigits\\");
			String trainingtable = "training";
			insertIntoTable(s2,trainingtable);   
	    }

	private void insertIntoTable(String s,String table) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		/** 
		 * jdbc连接数据库 
         */  
        String dbur1 = "jdbc:mysql://localhost:3306/testknn";  
        Connection conn = DriverManager.getConnection(dbur1, "root", "123456");  
        
        
		//if it doesnot exist -->create table
        boolean flag=validateTableExist(table,conn);
		if(flag){
			System.out.println("表"+table+"已经存在！");
		}else{
			Statement stmt = conn.createStatement();
			//then create table
			String sql_createtable="create table "+table+"(file_name VARCHAR(255) primary key,txt_content VARCHAR(2048));";
			try {
				stmt.executeUpdate(sql_createtable);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				String[] testDir = getDir(s);
				for (int i=0; i< testDir.length; i++) {
		            String filename = testDir[i];
		            String testFullStr=s+filename;
		            File file = new File(testFullStr);
		            String content = txtToString(file);
		            String sql = "insert into "+table+"(file_name,txt_content)values('"+filename+"','"+content+"');";
		            stmt.executeUpdate(sql);
		        }
				System.out.println("表"+table+"已经建立好！");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				//rs.close();  
			    stmt.close();  
			    conn.close();
			} 
		}
		  
       
	}


	private boolean validateTableExist(String table,Connection conn) {
    		boolean flag = false;  
            try {   
                DatabaseMetaData meta = conn.getMetaData();  
                String type [] = {"TABLE"};  
                ResultSet rs = meta.getTables(null, null, table, type);  
                flag = rs.next();  
            } catch (SQLException e) {  
                e.printStackTrace();  
            }  
            return flag;  
    	}

	/**
	 *  获取一个txt文件中的字符串内容
	 */
	private static String txtToString(File file) {
		StringBuffer result = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = null;
			while((s=br.readLine())!=null){
				result.append(s);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	
	/**
	 * 获取一个目录下的所有文件名 
	 */
	private String[] getDir(String string) {
		File dir = new File(string);
		String[] children = dir.list();
		if(children.length==0){
			System.out.println("不是一个目录！");
			return null;
		} else {
			return children;
		}
	} 

}
