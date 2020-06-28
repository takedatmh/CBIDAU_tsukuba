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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soot.Unit;

public class PathFilter_bk01 {

	/**
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
System.out.println(file.getPath());
			    	  List<String> cfgPathList = cfgPathReader(file.getPath());
			    	  //マッチング処理を行う。
			    	  List<String> filtered_cfgPathList = matchUnit(cfgPathList, fieldList);
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
	private boolean writeFilteredCFGPathList(String dirName, String fileName, List<String> pathList){
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
		String filePath = dirName + Context.SEPARATOR + fileName;
		try {
			//postscript version 追記形式
			in = new FileWriter(filePath, true);
			out = new PrintWriter(in);
			out.println(pathList.toString());
			out.flush();
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
					for(String e: tmp){
						//ClassName MethodNameの形に加工。”が前後についているのでそれを削除。
						//String fqcn = tmp.get(0).substring(1).substring(tmp.get(0).length()) + " " + tmp.get(1).substring(0).substring(tmp.get(1).length());
						String fqcn = tmp.get(0).replaceAll("\"", "") + " " + tmp.get(1).replaceAll("\"", "");
//System.out.println("fqcn: " + fqcn);
						ret.add(fqcn);
					}
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
					for(String e: tmp){
						//ClassName MethodNameの形に加工。”が前後についているのでそれを削除。
						//String fqcn = tmp.get(0).substring(1).substring(tmp.get(0).length()) + " " + tmp.get(1).substring(0).substring(tmp.get(1).length());
						String fqcn = tmp.get(0).replaceAll("\"", "") + " " + tmp.get(1).replaceAll("\"", "");
//System.out.println("fqcn: " + fqcn);
						ret.add(fqcn);
					}
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
				line = line.replace("[[","[").replace("]]", "]");
//System.out.println("line: " + line);
				/*
				 * 途中に入ってくる[?= return**],の[]を削除して、全体として[[*,*],[*,*],[*,*]]に変更する。
				 * */
				Pattern pattern0 = Pattern.compile("\\[\\?=\\s{1}return\\s{1}[a-zA-Z_0-9]+\\]");
				Matcher matcher0 = pattern0.matcher(line);
				while(matcher0.find()){
					//matcher0.replaceAll(matcher0.group().replace("[", "").replace("]", ""));
//System.out.println("m0: " + matcher0.group());
					String modified = matcher0.replaceAll(matcher0.group().replace("[", "").replace("]", ""));
					line = modified;
				}
//System.out.println("newline: " + line);
//				//Pattern pattern = Pattern.compile("^(\\[(.+,)+\\v],)+$");
//				Pattern pattern = Pattern.compile("\\[([a-zA-Z_0-9]+,\\s{1})[a-zA-Z_0-9]+\\]");
//				Matcher matcher = pattern.matcher(line);
//				while(matcher.find()){
//					System.out.println("m1 " + matcher.group());
//					ret.add(matcher.group());
//				}
/*
 * ],でsplitしてあげて、且つ、[ ],を削除することで、***,***,***のパス情報を取り出しListに格納。
 * */
				String[] listOfLine = line.split("],");				
				//先頭の[を削除して戻値のListに格納
				for(String path : listOfLine) {
//System.out.println("before: " + path);
					path = path.substring(1);
//System.out.println("after: " + path);					
					ret.add(path);
				}
//for(String p: ret)
//System.out.println("ret: " + p);
				br.close();
			}
		}catch (IOException e){
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * CFGのパスリストと、フィールドのリストから、CFGパスリストの内、フィールドリストにあるフィールドを含むパスだけ
	 * 選別したリストを返す。
	 * @param cfgPathList
	 * @param fieldList
	 * @return cfgPathList List<String> 
	 */
	public List<String> matchUnit(List<String> cfgPathList, List<String> fieldList) {
		//Return Object to store CFG Path.
		List<String> ret = new ArrayList<String>();
		
		//Retrieve path contained public or static field.
		for(String path : cfgPathList) {
			for(String field : fieldList){
				if( path.contains(field) ){
					ret.add(path);
				}
			}
		}
System.out.println("Extracted path: " + ret);		
		
		return ret;
	}

}
