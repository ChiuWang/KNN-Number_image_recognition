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
 * 标准KNN算法流程
	1.  准备数据，对数据进行预处理
	2.  选用合适的数据结构存储训练数据和测试元组
	3.  设定参数，如k
	4.  维护一个大小为k的的按距离由大到小的优先级队列，用于存储最近邻训练元组。
	       随机从训练元组中选取k个元组作为初始的最近邻元组，分别计算测试元组到这k个元组的距离，
	       将训练元组标号和距离存入优先级队列
	5.  遍历训练元组集，计算当前训练元组与测试元组的距离，
	       将所得距离L 与优先级队列中的最大距离Lmax进行比较。
	       若L>=Lmax，则舍弃该元组，遍历下一个元组。
	       若L < Lmax，删除优先级队列中最大距离的元组，将当前训练元组存入优先级队列。
	6.  遍历完毕，计算优先级队列中k 个元组的多数类，并将其作为测试元组的类别。
	7.  测试元组集测试完毕后计算误差率，继续设定不同的k值重新进行训练，最后取误差率最小的k 值。
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
		//properties路径 ？见blog详细分析
		props.load(new FileInputStream("src/jdbc.properties"));
		String driver = props.getProperty("jdbcDriverForName");
		String url = props.getProperty("databaseURL");
		String user = props.getProperty("user");
		String pass = props.getProperty("pass");
		
		
		Class.forName(driver);
		//jdbc连接数据库 
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
		//获取完整的training集
		Map<String,String> trainingMap = getTrainingMap(stmt1,stmt2);
		
		
		for (int num=0; num<10; num++){
			//读取一类数字的test集放在HashMap中
			Map<String,String> testMap = getTestMap(num,stmt1,stmt2);
			//复制training集
			Map<String,String> currentTrainingMap = trainingMap;
			//获取training集中的键
			Set<String> trainingKeySets = currentTrainingMap.keySet();
			//获取test集HashMap中的键
			Set<String> testKeySets = testMap.keySet();
			
			//建立优先队列,存储文件名和距离
			//optimalMap排序compare方法returning 0 would merge keys！
			Map<String,Integer> optimalTrainingMap = new HashMap<>();

			System.out.println("----num为"+num+"时的最优：");
			System.out.println("testMap的键为 "+testMap.keySet());
			System.out.println("currentTrainingMap.size(): "+currentTrainingMap.size());
			
			
			for(Iterator<String> trainingIterator = trainingKeySets.iterator();trainingIterator.hasNext();) {
				//算距离：testIterator.next()和trainingIterator.next()两个String值的键对应的value
				String trainingKeyString = trainingIterator.next();
				
				//两个集合都去获取对应的value用来比较
				String trainingValueString = currentTrainingMap.get(trainingKeyString);
				
				int distance=0;
				for(Iterator<String> testIterator = testKeySets.iterator();testIterator.hasNext();){
					String testKeyString = testIterator.next();
					String testValueString = testMap.get(testKeyString);
					
					distance += getHammingDistance(testValueString,trainingValueString);
					
				}
				//把training集的filename和distance成对存入optimalTrainingMap
				optimalTrainingMap.put(trainingKeyString, distance);
				
			}
				
			
			
			
			// 输出10次 优先队列的前k个filename结果
			
			//重写compare方法
			ValueComparator bvc =  new ValueComparator(optimalTrainingMap);  
	        TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
	        
	        //hashmap的优先队列存入改写过的TreeMap中
	        sorted_map.putAll(optimalTrainingMap);
	        
	        //把键抽取到set中，值也一样
	        Set<String> keySets = sorted_map.keySet();
	        Collection<Integer> values = sorted_map.values();
	        
	        //输出前k个filename
	        Iterator<String> i = keySets.iterator();
	        int sum_i=0;
	        while(i.hasNext() && sum_i<k){
	        	
	        	System.out.print(i.next()+"  ");
	        	
	        	++sum_i;
	        }
	        
	        System.out.println(" --共"+k+"个");
	        //输出前k个distance
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
		 * 没有采用标准的欧式距离计算方法
		 * 因为比较的是字符串化为char[] 后全为‘0’和‘1’，
		 * 思路转化为两字符相等为0，不等为1，直接求和。免去了先平方后开方的繁琐步骤。
		 * 而不影响距离的排序结果。
		 * 这种方法叫做汉明距离
		 */
		byte[] testVSArr = testValueString.getBytes();
		byte[] trainingVSArr = trainingValueString.getBytes();
		int distance = 0;
		//此处不耦合。。但是所有的点阵都是1024个0或1。这样写清楚
		for(int i=0; i<1024; i++){
			if(testVSArr[i]!=trainingVSArr[i]){
				distance++;
			}
		}
		return distance;
	}

	//得到一个Training集
	//	1.多创建几个stmt,一个stmt对应一个rs;
	//	2.若用一个stmt对应多个rs的话,那只能得到一个rs后就操作,处理完第一个rs后再处理其他的
	private Map<String, String> getTrainingMap(Statement stmt1,Statement stmt2) throws SQLException {
		ResultSet rs_column = stmt1.executeQuery("select count(*) from training");
		rs_column.next(); //很关键
		int mapKeyNum = rs_column.getInt(1);
		System.out.println("class getTrainingMap:mapKeyNum: "+mapKeyNum+" ");
		Map<String,String> trainingMap = new HashMap<>();
		ResultSet rs_training_filename = stmt1.executeQuery("select file_name from training");
		ResultSet rs_training_content = stmt2.executeQuery("select txt_content from training");
//		会导致rs的关闭		
//		for (int i=1; i<=mapKeyNum; i++) {
//			String filename_key = rs_training_filename.getString(mapKeyNum);
//			String content_value = rs_training_content.getString(mapKeyNum);
//			trainingMap.put(filename_key, content_value);
//		}
		
		
		//先将键和值分别存LinkedList
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
		
		//然后循环插入map Collection编号从 0开始
		for(int i=0; i<mapKeyNum; i++){
			String filename_key = filename_list.get(i);
			String content_value = content_list.get(i);
			trainingMap.put(filename_key, content_value);
		}

		rs_training_filename.close();
		rs_training_content.close();
		
		return trainingMap;
	}

	//得到只含一个映射的TestMap
	//映射由test表中filename以num_开头的记录中随机取得！
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
		
		//先将键和值分别存LinkedList
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
		
		//得到[0~list长度)内的一个随机数
        Random randomIndex = new Random();
		int index = randomIndex.nextInt(filename_list.size());
		
		
		//将得到的一个随机映射插入map Collection
		testMap.put(filename_list.get(index), content_list.get(index));
		

		rs_testSet_filename.close();
		rs_testSet_content.close();

		return testMap;
	}

	

}
