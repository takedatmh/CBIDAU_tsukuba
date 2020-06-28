package dataflow.summary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * FieldPathMap.txtからフィールドをキーとして、そのフィールドにアクセスしているノードの情報をvalueとして格納したTreeMap情報に整理する。
 * TreeMapにしているのは、Mapに格納する際にFieldの情報でソート処理を自動的にしておくため。
 * TreeMapには以下の形式でデータが格納される。
 * 
 * key	:	Field名
 * value:	クラス名_メソッド名;path情報;Unit情報:::他のPath情報:::...
 * 
 * valueに格納されるアクセスしているUnitやパス情報は、一つのフィールドに対して複数のアクセスの可能性があるため、区切り文字を”:::”として
 * 複数記載する。加工するときは:::を区切り文字としてsplitして利用する。
 * @author takedatmh
 *
 */
public class CBIDAU_TestCaseCreator {

	/**
	 * splitFieldPathMapDataを実行するためのメインメソッド。 
	 * @param args メイン引数
	 */
	public static void main(String[] args) {

		//Execute split method.
		//TreeMap<String, String> treeMap = splitFieldPathMapData("./FieldPathMap/FieldPathMapingList.txt");
		TreeMap<String, String> treeMap = splitFieldPathMapData("./FieldPathMap/tmp.txt");
		
		//Expression of Field and Unit
		Iterator<String> key_Itr = treeMap.keySet().iterator();
		while(key_Itr.hasNext()){
			String key  = (String) key_Itr.next();
			System.out.println("DataSore: " + key);
			String tmp = treeMap.get(key);
			String[] records = tmp.split(":::");
			for(String record : records){
				//System.out.println("record: " + record);
				
				String[] e = record.split(";");
				System.out.println("AccessNode: " + e[0].replace(".txt", "") + " " + e[2]);
			}
		}

	}

	/**
	 * FieldPathMap.txtを読み込んで、Field名をキーとし、Path & Unit情報をvalueとして
	 * TreeMapに格納するメソッド。TreeMapに格納することでkeyを自動的にソートする効果がある。
	 * Valueには同一フィールドにアクセスする複数Unitが存在しうるため、区切り文字として:::を利用
	 * して複数の情報を一つのString情報としとして保持する。このMapの中でネストの配列構造を持たせない。
	 * 
	 * @param filePath
	 * @return TreeMap<String, String> key(FieldName), value(path&Unit情報)
	 */
	public static TreeMap<String, String> splitFieldPathMapData(String filePath) {
		//return object
		TreeMap<String, String> ret = null;
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		try {		
			//Read File from filePath info.
			File file = new File(filePath);
			
			if(file.exists() && file.isFile()){
				@SuppressWarnings("resource")
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				
				while(line != null){
					//FieldPathMap.txtのファイルは;を区切り文字としているため分割する。
					String[] dividedArray = line.split(";");
					//最初の配列はField情報
					String keyField = dividedArray[0];
					//1: クラス・メソッド名、2: path文字列、3:Unit文字列
					String valueData = dividedArray[1] + ";" + dividedArray[2] + ";" + dividedArray[3];
					//同一key名の場合は、valueに区切り文字として:::設定して複数情報を記載する。
					if( !map.containsKey(keyField) ){
						map.put(keyField, valueData);
					} else {
						String tmp = map.get(keyField);
						map.put(keyField, tmp + ":::" + valueData);
					}
					
					//次の行を読み込み
					line = br.readLine();
				}//END 
				
				//LinkedHashMapをTreeMapにオブジェクト変換してkeyの自動ソート
				ret = new TreeMap<String, String>(map);
								
			}//END IF
			
		} catch (IOException e) {
				e.printStackTrace();
		}
		//TreeMapのデータを返却する。
		return ret;
	}

}
