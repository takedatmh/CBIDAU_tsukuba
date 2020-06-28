package dataflow.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathFilter {

	/**
	 * 任意のメソッドのすべてのCFGパスのリストのうち、そのオープンソース全体の中のPublic/Staticな変数
	 * のいづれか一つを参照しているパスを抽出する。
	 * 
	 * 1.引数したCSVファイル（FieldList）情報を読み込む。
	 * 2.Loop: メソッド単位に出力したCFG_Pathのtxtファイルがあれば読み込み以下の処理を繰返す。
	 * 		2-1.引数指定したCSVファイルからCFG_Path情報を読み込む。 
	 * 		2-2.FieldListに格納されているFieldと依存関係のあるPathのみを抽出。
	 * 		2-3.抽出したPathをファイルにCSVファイルとして出力する。(igraph dataframe形式）
	 * 	3.END Loop: 読み込むファイルがなくなれば終了
	 * @return boolean フィルタリングしたPath情報を全てcsvフィアルに書き込む。
	 */
	public boolean filter() {
		// Return Variable
		boolean ret = false;
		
		//1. Read Field data.
		List<String> fieldList = readClassFeildCSV("./FieldNohma/tom_class_feild_shusyoku.csv");

		//2. Read CFG Path
		File dir = new File("./CFG_PathList");
		//ディレクトリ配下のファイルをFileオブジェクトとして配列に取得。
	    File[] files = dir.listFiles();
	    //ファイルがなかったら終了。
	    if( files == null ) {
		      return ret = false;		   
	    } else {
	    	//Fileごとにループ。
		    for( File file : files ) {
		    	//存在チェック。
			      if( !file.exists() )
			        continue;//次のループに進む。
			      //ファイルだったら読み込む。
			      else if( file.isFile() ) {
			    	  //ファイルを読み込む。
//System.out.println("Read CFG_PathList: from: " + file.getPath());
			    	  List<String> cfgPathList = cfgPathReader(file.getPath());
			    	  //マッチング処理を行う。
			    	  List<String> filtered_cfgPathList = matchUnit(cfgPathList, fieldList, file);
			    	  /*
			    	   * 取得したパス情報をファイルに記録する。Dirは、CFG_PathList_Filtered.
			    	   *CFG_DF_***.javaでは、igraph形式の２つのcsvファイルと、
			    	   *"CFG_PathList"ディレクトリ配下に"ClassNameFQCN_MethodName.txt"のUnit単位のエッジ情報のリストを出力している。
			    	   *ここでは、igraphのデータは対象外として、"CFG_PathList_Filtered"ディレクトリ配下に.txt形式で同様のパス情報を出力する。
			    	   */
			    	  ret = writeFilteredCFGPathList("CFG_PathList_Filtered", file.getName(), filtered_cfgPathList);
			      }
		    }
	    }
		// return
		return ret;
	}
	//writer for CFG_PathList_filtered
	public boolean writeFilteredCFGPathList(String dirName, String fileName, List<String> pathList){
		//Return Object
		boolean ret = false;
		
		/*
		 * Delete previous result files under the output directories as follows:
		 */
	     File dir = new File("dirName" + Context.SEPARATOR);
	     FileInitialization.deleteFile(dir);
		
		//Write all paths into CFG_PathList directory as '.txt' format file.
		FileWriter in = null;
		PrintWriter out = null;
		
		//Added by takeda in 20200518. Split PathList when the list have more that 1,000 row data.
		List<List<String>> dividedPathLists = null;
		
//Case1: 1000行以上の場合
		if( pathList.size() > 1000 ) {
System.out.println("NumOfPath: " + pathList.size());			
			//Listを分割するUtilityクラスであるdevide()を利用して分割。
			dividedPathLists = CollectionUtils.devide(pathList, 1000);
System.out.println("NumOfList/1000: " + dividedPathLists.size());			
			//Counter
			int count = 0;
			//分割リスト数分だけループして別ファイルで保存。
			for(List<String> pList : dividedPathLists){
				//よく無いが完全重複コードで、フィアルに書き出し。ファイル名だけインクリメント。
				String filePath = dirName + Context.SEPARATOR + "Div_" + String.valueOf(count++) + "_" + fileName;
				try {
					//postscript version 追記形式
					in = new FileWriter(filePath, true);
					out = new PrintWriter(in);
					//pathごとに改行して書き込み。
					for(String p : pList)
						//Modidied by takeda in 20200523.
						//out.println(pList.toString()+"\\n");
						out.println(p);
					out.flush();
					//return flag = true.
					ret = true;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						//close
						in.close();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return ret;
		}
		//Add END./////
//Case2: 1000行より少ない場合		
		String filePath = dirName + Context.SEPARATOR + fileName;
		try {
			//postscript version 追記形式
			in = new FileWriter(filePath, true);
			out = new PrintWriter(in);
			//Modidied by takeda in 20200525.
			//pathごとに改行して書き込み。
			//out.println(pathList.toString());
			for(String p : pathList)
				out.println(p);
			out.flush();
			//return flag = true.
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				//close
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	
	
	/**
	 * javapの結果ファイルを読み込むjavapFileReaderを連続実行させるメソッド。
	 * 指定したファイル以下の**.txtファルを読み込み結果をcsvファイルで上書きしていく。
	 * FieldList.txt
	 * **.**.** **
	 * **.**.** **
	 */
	public boolean executeJavapFieldReader(String dirPathForReadFile) {
		
		//Return Object
		boolean ret = false;
		File dir = new File(dirPathForReadFile);
		/*
		 * Delete previous result files under the output directories as follows:
		 * :: Field/FieldList.csv
		 */
		//Modidied by takeda in 20200518
		 //FileInitialization.deleteFile( new File("./Field"+ Context.SEPARATOR +"FieldList.csv") );
		FileInitialization.deleteFile( new File("./Field") );
		//ディレクトリ配下のファイルをFileオブジェクトとして配列に取得
	    File[] files = dir.listFiles();

	    //ファイルがなかったら終了
	    if( files == null ) {
		      return ret;		   
	    } else {
	    	//Fileごとにループ
		    for( File file : files ) {
		    	//存在チェック
			      if( !file.exists() )
			        continue;//次のループに進む
			      //ディレクトリがさらにあったら深堀
			      else if( file.isDirectory() )
			    	  executeJavapFieldReader(dirPathForReadFile);
			      //ファイルだったら javaFieldReaderでjavapファイルを読み込んで、FieldのFQCNリストを作成する。
			      else if( file.isFile() ) {
//System.out.println("javapFileName: " + dirPathForReadFile + Context.SEPARATOR + file.getName());
			        List<String> fieldFQCNList = javapFieldReader( dirPathForReadFile + Context.SEPARATOR + file.getName() );
			        ret = writer("Field" + Context.SEPARATOR + "FielList.csv", fieldFQCNList);
			      }
			}
		}		
	    
		//Return
		return ret;
	}
	//writer
	private static boolean writer(String filePath, List<String> fieldFQCNList) {
		boolean ret = false;
		
		//Write the list of path.
		FileWriter in = null;
		PrintWriter out = null;
		
		try {
			//postscript version
			in = new FileWriter(filePath, true);
			out = new PrintWriter(in);
			for(String path : fieldFQCNList)
				out.println(path.toString());
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				//close
				in.close();
				out.close();
				//Change flag from false to true.
				ret = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	/**
	 * javapコマンドの出力結果をパースしてpublicフィールドのFQCNをリストで格納する。
	 * 
	 * @param filePath
	 * @return
	 */
	public List<String> javapFieldReader(String filePath) {

		List<String> ret = new ArrayList<String>();

		//Read javap output file.
		try {
			File file = new File(filePath);

			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str = br.readLine();
				while (str != null) {
					//*** class FQCNクラス名 **** { で、クラス名行の記述を検出。１行しかないので発見次第classTxtに格納。
					//Pattern classPattern = Pattern.compile("^[a-zA-Z0-9_]+\\s{1}[a-zA-Z0-9_]+\\{$");

					//public****フィールド名で、フィールド行の記述を検出
					Pattern patternField = Pattern
							//modidied by takeda 20200518
							//.compile("^public\\s([^(,)]+\\.)[^(,)]+\\s[^(,)]+;$");
							.compile("^public\\s([^(),]+\\.)+[^(,)]+\\s[^(,)]+;$");
					Matcher matcher = patternField.matcher(str.trim());
					while (matcher.find()) {
						String fieldTxt = matcher.group();
//System.out.println("FieldTxt: "+fieldTxt);
						//final modifierが含まれている場合、CUDのオペレーションが無いため除外
						if( fieldTxt.contains("final") ) {
							continue;
						}
						//public , static ,を文字列から削除 
						fieldTxt.replaceAll(";", "").replace("public ", "").replace("static ", "");
						//型名FQCN Field名になっているところを、スペースでsplitして、後ろのField名だけにする。
						String tmpFieldTxt = Arrays.asList( fieldTxt.split(" ") ).get(2);
						//ClassFQAC Field名　の形にするためにクラス名を頭につけてる。
						ret.add( file.getName().replace(".txt", "") + " " + tmpFieldTxt.replace(";", "") );					
					}
					str = br.readLine();
				}

				br.close();
			} else {
//System.out.println("File is not existing.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//System.out.println(ret);
		return ret;
	}
	
	/**
	 * CSVファイルとして
	 * className, methodName, argName
	 * の３列で構成されるcsvファイルを読み込む。
	 * className¥¥s{1}methodName
	 * の文字列に編集してList<String>方として全行格納する。
	 * @param filePath
	 * @return List<String> className¥¥s{1}methodName
	 */
	public List<String> fileReaderFromCSV(String filePath){
		//return object
		List<String> ret = new ArrayList<String>();
		//BufferedReader
		BufferedReader bf = null;
		
		try {
			//Read File
			File file = new File(filePath);
			if(file.exists()){
				bf = new BufferedReader(new FileReader(file));
				String line = bf.readLine();
				while(line != null){
					List<String> tmp = Arrays.asList(line.split(","));
//Modified by takeda in 20200523. Delete For Loop.
//					for(String e: tmp){
//						//ClassName MethodNameの形に加工。”が前後についているのでそれを削除。
//						//String fqcn = tmp.get(0).substring(1).substring(tmp.get(0).length()) + " " + tmp.get(1).substring(0).substring(tmp.get(1).length());
//						//Modified by takeda in 20200523
//						String fqcn = tmp.get(0).replaceAll("\"", "") + " " + tmp.get(1).replaceAll("\"", "");
////System.out.println("fqcn: " + fqcn);
//						ret.add(fqcn);
//					}
					String fqcn = tmp.get(0).replaceAll("\"", "") + " " + tmp.get(1).replaceAll("\"", "");
					ret.add(fqcn);
					line = bf.readLine();
				}
			}
		//catch処理
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try{
				bf.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		//return ClassName¥¥s{1}methodName
		return ret;
	}
	
	/**
	 * CSVファイルとして
	 * className, FieldName, modifier
	 * の３列で構成されるcsvファイル(tom_class_feild_shusyoku.csv)を読み込む。
	 * className¥¥s{1}FieldName
	 * の文字列に編集してList<String>方として全行格納する。
	 * @param filePath
	 * @return List<String> className¥¥s{1}FieldName
	 */
	public List<String> readClassFeildCSV (String filePath){
		//return object
		List<String> ret = new ArrayList<String>();
		//BufferedReader
		BufferedReader bf = null;
		
		try {
			//Read File
			File file = new File(filePath);
			if(file.exists()){
				bf = new BufferedReader(new FileReader(file));
				String line = bf.readLine();
				while(line != null){
					List<String> tmp = Arrays.asList(line.split(","));
//Modified by takeda in 20200523. Delete for loop.
//					for(String e: tmp){
//						//ClassName MethodNameの形に加工。”が前後についているのでそれを削除。
//						//String fqcn = tmp.get(0).substring(1).substring(tmp.get(0).length()) + " " + tmp.get(1).substring(0).substring(tmp.get(1).length());
//						String fqcn = tmp.get(0).replaceAll("\"", "") + " " + tmp.get(1).replaceAll("\"", "");
////System.out.println("fqcn: " + fqcn);
//						ret.add(fqcn);
//					}
					//ダブルコーテーションを文字列から削除。
					String fqcn = tmp.get(0).replaceAll("\"", "") + " " + tmp.get(1).replaceAll("\"", "");
					//ClassFQCNとフィールド名を空白で区切った文字列を１レコードとして戻り値のListに追加。
					ret.add(fqcn);
					
//ADD by takeda in 202005023. 'classFQCN: Type FieldName' の文字列を作る。メソッドが所属するクラスのフィールドはこの形式で表現されるため。
					//Modifierと型が一緒になっているので区切り文字のスペースで要素を分離
					String[] tmp2Array = tmp.get(2).replaceAll("\"", "").split(" ");
					//最後の要素が型の情報なので、それだけ取り出して文字列として保持
					String type = tmp2Array[tmp2Array.length - 1];
					//最終的な ”Package名 型 フィールド名”　の文字列を作成　
					String thisClassField = tmp.get(0).replaceAll("\"", "") + ": " 
							+ type + " " 
							+ tmp.get(1).replaceAll("\"", "");
//System.out.println("Class Field: " + thisClassField);
					ret.add(thisClassField);
					
					//次の行を読み込み
					line = bf.readLine();
				}
			}
		//catch処理
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try{
				bf.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		//return ClassName¥¥s{1}methodName
//for(String f : ret)
//System.out.println("Field List: " + f);
		return ret;
	}
	
	/**
	 * 引数指定したパスのファイルからCFGパス情報を読み取り、パス単位にリストに格納する。
	 * @param filePath
	 * @return pathList List<String>
	 */
	public List<String> cfgPathReader(String filePath){
		List<String> ret = new ArrayList<String>();
		try {
			File file = new File(filePath);
			
			if(file.exists()){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				//Modified by takeda in 20200518. Read. Line Feed version.
//				line = line.replace("[[","[").replace("]]", "]");
////System.out.println("line: " + line);
//				/*
//				 * 途中に入ってくる[?= return**],の[]を削除して、全体として[[*,*],[*,*],[*,*]]に変更する。
//				 * */
//				Pattern pattern0 = Pattern.compile("\\[\\?=\\s{1}return\\s{1}[a-zA-Z_0-9]+\\]");
//				Matcher matcher0 = pattern0.matcher(line);
//				while(matcher0.find()){
//					//matcher0.replaceAll(matcher0.group().replace("[", "").replace("]", ""));
////System.out.println("m0: " + matcher0.group());
//					String modified = matcher0.replaceAll(matcher0.group().replace("[", "").replace("]", ""));
//					line = modified;
//				}
////System.out.println("newline: " + line);
////				//Pattern pattern = Pattern.compile("^(\\[(.+,)+\\v],)+$");
////				Pattern pattern = Pattern.compile("\\[([a-zA-Z_0-9]+,\\s{1})[a-zA-Z_0-9]+\\]");
////				Matcher matcher = pattern.matcher(line);
////				while(matcher.find()){
////					System.out.println("m1 " + matcher.group());
////					ret.add(matcher.group());
////				}
///*
// * ],でsplitしてあげて、且つ、[ ],を削除することで、***,***,***のパス情報を取り出しListに格納。
// * */
//				String[] listOfLine = line.split("],");				
//				//先頭の[を削除して戻値のListに格納
//				for(String path : listOfLine) {
////System.out.println("before: " + path);
//					path = path.substring(1);
////System.out.println("after: " + path);					
//					ret.add(path);
//				}
////for(String p: ret)
////System.out.println("ret: " + p);
				
				/* Line Break Data Version. */
				//ファイルを１行ずつ読み込んでリストに格納する。
				while(line != null) {
//System.out.println("After replace: " + line);
					//戻値のリストに格納
					ret.add(line);	
					//ファイルから次の行を読み込む
					line = br.readLine();
				}
				//バッファーリーダーのクローズ
				br.close();
			}
		}catch (IOException e){
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * CFGのパスリストと、フィールドのリストから、CFGパスリストの内、フィールドリストにあるフィールドを含むパスだけ
	 * 選別したリストを返す。File型の引数は、該当するノードがどのクラスのどのメッドのものかの情報をファイル名から抽出するためのもの。
	 * 20200523 ADD
	 * Static/Publicのフィールドとの関係が検出された場合、どのノードがどの変数と依存しているかのマッピング情報を保持
	 * 
	 *  変数FQCN (package名.クラス名 Field名);クラスFQCN_メソッド名;パス文字列;CRUD_Unit文字列
	 * 
	 *  のMap情報を作成してファイルに保存する。区切り文字は”；”
	 *  変数FQCNはFieldListから参照している文字列そのまま。
	 *  keyは重複を許してファイルの記述する。そのため、プログラム上はList<String>に保持する。
	 *  最後にファイルに書き込む ./FieldPathMap/fieldPathMap.txt
	 *　
	 * @param cfgPathList
	 * @param fieldList
	 * @return cfgPathList List<String> 
	 */
	public List<String> matchUnit(List<String> cfgPathList, List<String> fieldList, File file) {
		//Return Object to store CFG Path.
		List<String> ret = new ArrayList<String>();
		
		//Field<->Pathマッピング情報を格納するList
		List<String> fieldPathMapList = new ArrayList<String>();
		
		//Retrieve path contained public or static field.
		for(String path : cfgPathList) {
			for(String field : fieldList){
				if( path.contains(field) ){
					//変数依存パスのみを保持するリストに当該パス情報を格納
					ret.add(path);
					
					/* ADD by takeda in 20200524. */
					//Fieldに依存しているUnit文字列を抽出するために正規表現を利用（,R_文字文字Field名文字,R_)
					String unitString = FieldPathMap.detectUnitString(field, path);
					//Field <-> Path のマッピング情報をリストとして保持
					fieldPathMapList.add(field + ";" + file.getName() + ";" + path + ";" + unitString);					
				}
			}
		}
//System.out.println("Pub/Staticを含むパス: " + ret);		
		//Field-Path Map情報をファイルに書き込む
		FieldPathMap.writeFieldPathMapList(fieldPathMapList);
		
		//return
		return ret;
	}
	
	/**
	 * FieldがPathに含まれた場合には、どのFieldがどのパス上に含まれていたかのマッピング情報を
	 * ファイルに保存して置いて、後続の解析時に即時に使えるようにしておく。
	 * 
	 * クラスFQCN　Field名：：パス名、パス名、。。。パス名
	 * 
	 * の形式
	 * 
	 * @param fieldPathMap
	 */
	public void writeFieldPathMapFile(Map<String, String> fieldPathMap) {
		Set<String> keys = fieldPathMap.keySet();
		for(String key : keys) {
			//create string data.
			String fieldMapData = key + "::" + fieldPathMap.get(key);
System.out.println("Data: " + fieldMapData);			
			//writerメソッドで書き込み
			writerFieldPath("./FieldPathMap/FieldPathMap.txt",fieldMapData);
			
		}
	}
	
	//writer
	private static boolean writerFieldPath(String filePath, String data) {
		boolean ret = false;
		
		//Write the list of path.
		FileWriter in = null;
		PrintWriter out = null;
		
		try {
			//postscript version
			in = new FileWriter(filePath, true);
			out = new PrintWriter(in);
			out.println(data);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				//close
				in.close();
				out.close();
				//Change flag from false to true.
				ret = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

}
