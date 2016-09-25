package knn;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;



/**
 * 
 * @author wang
 * ��׼KNN�㷨����
	1.  ׼�����ݣ������ݽ���Ԥ����
	2.  ѡ�ú��ʵ����ݽṹ�洢ѵ�����ݺͲ���Ԫ��
	3.  �趨��������k
	4.  ά��һ����СΪk�ĵİ������ɴ�С�����ȼ����У����ڴ洢�����ѵ��Ԫ�顣
	       �����ѵ��Ԫ����ѡȡk��Ԫ����Ϊ��ʼ�������Ԫ�飬�ֱ�������Ԫ�鵽��k��Ԫ��ľ��룬
	       ��ѵ��Ԫ���ź;���������ȼ�����
	5.  ����ѵ��Ԫ�鼯�����㵱ǰѵ��Ԫ�������Ԫ��ľ��룬
	       �����þ���L �����ȼ������е�������Lmax���бȽϡ�
	       ��L>=Lmax����������Ԫ�飬������һ��Ԫ�顣
	       ��L < Lmax��ɾ�����ȼ��������������Ԫ�飬����ǰѵ��Ԫ��������ȼ����С�
	6.  ������ϣ��������ȼ�������k ��Ԫ��Ķ����࣬��������Ϊ����Ԫ������
	7.  ����Ԫ�鼯������Ϻ��������ʣ������趨��ͬ��kֵ���½���ѵ�������ȡ�������С��k ֵ��
 *
 */
public class KNN {

	public static void main(String[] args) throws Exception {
		
		ConnectMysql ca = new ConnectMysql();
		ca.ConnectionMysqlFile();
		
		KNN KNNDemo = new KNN();
		KNNDemo.connectDB();

	}
	


	private void connectDB() throws Exception {
		
		Properties props = new Properties();
		//properties·�� ����blog��ϸ����
		props.load(new FileInputStream("src/jdbc.properties"));
		String driver = props.getProperty("jdbcDriverForName");
		String url = props.getProperty("databaseURL");
		String user = props.getProperty("user");
		String pass = props.getProperty("pass");
		
		
		Class.forName(driver);
		//jdbc�������ݿ� 
        Connection conn = DriverManager.getConnection(url , user, pass);  
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        try {
			KNNTraining(11,stmt1,stmt2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			stmt1.close();
			stmt2.close();  
		    conn.close();
		}
        
	}

	

	private void KNNTraining(int k,Statement stmt1,Statement stmt2) throws SQLException {
		//��ȡ������training��
		Map<String,String> trainingMap = getTrainingMap(stmt1,stmt2);
		
		
		for (int num=0; num<10; num++){
			//��ȡһ�����ֵ�test������HashMap��
			Map<String,String> testMap = getTestMap(num,stmt1,stmt2);
			//����training��
			Map<String,String> currentTrainingMap = trainingMap;
			//��ȡtraining���еļ�
			Set<String> trainingKeySets = currentTrainingMap.keySet();
			//��ȡtest��HashMap�еļ�
			Set<String> testKeySets = testMap.keySet();
			
			//�������ȶ���,�洢�ļ����;���
			//optimalMap����compare����returning 0 would merge keys��
			Map<String,Integer> optimalTrainingMap = new HashMap<>();

			System.out.println("----numΪ"+num+"ʱ�����ţ�");
			System.out.println("testMap�ļ�Ϊ "+testMap.keySet());
			System.out.println("currentTrainingMap.size(): "+currentTrainingMap.size());
			
			
			for(Iterator<String> trainingIterator = trainingKeySets.iterator();trainingIterator.hasNext();) {
				//����룺testIterator.next()��trainingIterator.next()����Stringֵ�ļ���Ӧ��value
				String trainingKeyString = trainingIterator.next();
				
				//�������϶�ȥ��ȡ��Ӧ��value�����Ƚ�
				String trainingValueString = currentTrainingMap.get(trainingKeyString);
				
				int distance=0;
				for(Iterator<String> testIterator = testKeySets.iterator();testIterator.hasNext();){
					String testKeyString = testIterator.next();
					String testValueString = testMap.get(testKeyString);
					
					distance += getHammingDistance(testValueString,trainingValueString);
					
				}
				//��training����filename��distance�ɶԴ���optimalTrainingMap
				optimalTrainingMap.put(trainingKeyString, distance);
				
			}
				
			
			
			
			// ���10�� ���ȶ��е�ǰk��filename���
			
			//��дcompare����
			ValueComparator bvc =  new ValueComparator(optimalTrainingMap);  
	        TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
	        
	        //hashmap�����ȶ��д����д����TreeMap��
	        sorted_map.putAll(optimalTrainingMap);
	        
	        //�Ѽ���ȡ��set�У�ֵҲһ��
	        Set<String> keySets = sorted_map.keySet();
	        Collection<Integer> values = sorted_map.values();
	        
	        //���ǰk��filename
	        Iterator<String> i = keySets.iterator();
	        int sum_i=0;
	        while(i.hasNext() && sum_i<k){
	        	
	        	System.out.print(i.next()+"  ");
	        	
	        	++sum_i;
	        }
	        
	        System.out.println(" --��"+k+"��");
	        //���ǰk��distance
	        Iterator<Integer> j = values.iterator();
	        int sum_j=0;
	        while(j.hasNext() && sum_j<k){
	        	System.out.print(j.next()+"        ");
	        	++sum_j;
	        }
	        System.out.println();
	        System.out.println("**********");
		}
		
		
		
	}

	private int getHammingDistance(String testValueString, String trainingValueString) {
		/**
		 * û�в��ñ�׼��ŷʽ������㷽��
		 * ��Ϊ�Ƚϵ����ַ�����Ϊchar[] ��ȫΪ��0���͡�1����
		 * ˼·ת��Ϊ���ַ����Ϊ0������Ϊ1��ֱ����͡���ȥ����ƽ���󿪷��ķ������衣
		 * ����Ӱ��������������
		 * ���ַ���������������
		 */
		byte[] testVSArr = testValueString.getBytes();
		byte[] trainingVSArr = trainingValueString.getBytes();
		int distance = 0;
		//�˴�����ϡ����������еĵ�����1024��0��1������д���
		for(int i=0; i<1024; i++){
			if(testVSArr[i]!=trainingVSArr[i]){
				distance++;
			}
		}
		return distance;
	}

	//�õ�һ��Training��
	//	1.�ഴ������stmt,һ��stmt��Ӧһ��rs;
	//	2.����һ��stmt��Ӧ���rs�Ļ�,��ֻ�ܵõ�һ��rs��Ͳ���,�������һ��rs���ٴ���������
	private Map<String, String> getTrainingMap(Statement stmt1,Statement stmt2) throws SQLException {
		ResultSet rs_column = stmt1.executeQuery("select count(*) from training");
		rs_column.next(); //�ܹؼ�
		int mapKeyNum = rs_column.getInt(1);
		System.out.println("class getTrainingMap:mapKeyNum: "+mapKeyNum+" ");
		Map<String,String> trainingMap = new HashMap<>();
		ResultSet rs_training_filename = stmt1.executeQuery("select file_name from training");
		ResultSet rs_training_content = stmt2.executeQuery("select txt_content from training");
//		�ᵼ��rs�Ĺر�		
//		for (int i=1; i<=mapKeyNum; i++) {
//			String filename_key = rs_training_filename.getString(mapKeyNum);
//			String content_value = rs_training_content.getString(mapKeyNum);
//			trainingMap.put(filename_key, content_value);
//		}
		
		
		//�Ƚ�����ֵ�ֱ��LinkedList
		LinkedList<String> filename_list = new LinkedList<>();
		LinkedList<String> content_list = new LinkedList<>();

		while(rs_training_filename.next()){
			String filename_key = rs_training_filename.getString(1);
			filename_list.add(filename_key);
		}
		while(rs_training_content.next()){
			String content_value = rs_training_content.getString(1);
			content_list.add(content_value);
		}
		
		//Ȼ��ѭ������map Collection��Ŵ� 0��ʼ
		for(int i=0; i<mapKeyNum; i++){
			String filename_key = filename_list.get(i);
			String content_value = content_list.get(i);
			trainingMap.put(filename_key, content_value);
		}

		rs_training_filename.close();
		rs_training_content.close();
		
		return trainingMap;
	}

	//�õ�ֻ��һ��ӳ���TestMap
	//ӳ����test����filename��num_��ͷ�ļ�¼�����ȡ�ã�
	private Map<String, String> getTestMap(int num,Statement stmt1, Statement stmt2) throws SQLException {
		String testnumsql = "select count(*) from test where file_name like '"+num+"_%';";
		ResultSet rs_column = stmt1.executeQuery(testnumsql);
		rs_column.next();
		int mapKeyNum = rs_column.getInt(1);
		System.out.println("class getTestMap:mapKeyNum: "+mapKeyNum+" ");
		Map<String,String> testMap = new HashMap<>();
		String selectFileNameSQL = "select file_name from test where file_name like'"+num+"_%';";
		String selectTXTContentSQL = "select txt_content from test where file_name like'"+num+"_%';";
		ResultSet rs_testSet_filename = stmt1.executeQuery(selectFileNameSQL);
		ResultSet rs_testSet_content = stmt2.executeQuery(selectTXTContentSQL);
		
		//�Ƚ�����ֵ�ֱ��LinkedList
		LinkedList<String> filename_list = new LinkedList<>();
		LinkedList<String> content_list = new LinkedList<>();

		while(rs_testSet_filename.next()){
			String filename_key = rs_testSet_filename.getString(1);
			filename_list.add(filename_key);
		}
		while(rs_testSet_content.next()){
			String content_value = rs_testSet_content.getString(1);
			content_list.add(content_value);
		}
		
		//�õ�[0~list����)�ڵ�һ�������
        Random randomIndex = new Random();
		int index = randomIndex.nextInt(filename_list.size());
		
		
		//���õ���һ�����ӳ�����map Collection
		testMap.put(filename_list.get(index), content_list.get(index));
		

		rs_testSet_filename.close();
		rs_testSet_content.close();

		return testMap;
	}

	

}
