package knn;

import java.util.Comparator;
import java.util.Map;
/**
 * 
 * @author wang
 * http://blog.csdn.net/u011734144/article/details/52384284
 * ��TreeMap����value��������
 */
public class ValueComparator implements Comparator<String> {
	Map<String, Integer> base;  
    //������Ҫ��Ҫ�Ƚϵ�map���ϴ�����
    public ValueComparator(Map<String, Integer> base) {  
        this.base = base;  
    }  
  
    // Note: this comparator imposes orderings that are inconsistent with equals.    
    //�Ƚϵ�ʱ�򣬴������������Ӧ����map������key���������洫���Ҫ�Ƚϵļ���base�����Ի�ȡ��key��Ӧ��value��Ȼ����value���бȽ�   
    public int compare(String a, String b) {  
        if (base.get(a) >= base.get(b)) {  
            return 1;  
        } else {  
            return -1;  
        } // returning 0 would merge keys  
    }  
}

