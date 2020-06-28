package dataflow.summary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataflow.util.CollectionUtils;
import dataflow.util.Context;
import dataflow.util.FileInitialization;

/**
 * CGPathListフォルダ内のCGPathListのファイルを１つずつ読み込み、以下の処理をする。
 * 
 * 1.ディレクトリ内のファイルを読み込む。
 * 2.１つファイルを読み込む。
 * 3. 1レコード読み込む
 * 4. :::でsplitして配列に奇数番目と最後のノードを格納する。
 * 4. メッド名がset**, get**のいづれかで始まるメソッドを含むノードと、その一個前のノードを特定する。
 * 5.以下のカラム構成のデータをString のレコードとして作成。区切り文字は:::。
 * 　|setter/getter Method Name | CallGraphPathList File Name | Previous Caller Node Method Name |
 * 6.この情報を１行ずつ、CGPathListFilteredフォルダにCGPathListFiltered_FileName.txtとして保存する。
 * 
 * 後続の検証では、書き込んだファイルを全て読み込み１列目のsetter/getterが同一で、２列目のFileNameが異なれば、
 * 共有変数更新として抽出する。setterはUpdate, getterはRで自明なのでUnitのCRUDはみない。
 * また、そのパターンがあった場合には、一個前のsetterをコールしているCFGパスを解析して、Setter/Getterを含む
 * パスをカウントして総数と比較して削減効果を導出する。
 * 定性解析としてどれか一つぐらいグラフを出しておく。(graphviz)
 * 
 * @author takedatmh
 *
 */
public class CGPathAnalyzer {
	
	//Constructor
	public CGPathAnalyzer(){
	}
	
	/**
	 * CallGraphPathListのフォルダ内のファイルを全部読込、ファイル数分だけループする。
	 * 　１つのファイルを読込。
	 * 		1レコードずつ読込、レコード数分ループ
	 * 			setter/getterの探索
	 * 				IF setter/getterが存在したら
	 * 					存在数分ループ
	 * 						setter/getter Unit + ":::" + FileNmae + ":::" + Previous Unit をList<String>に追加
	 * 	1ファイル読込終わるごとに、結果ファイルに追記
	 * 	
	 * 　全ファイル処理したら結果ファイルclose
	 * 	
	 *    正常に終了したらtrueを返す。
	 */
	public boolean analyze(){
		//1. Return Object
		boolean ret = false;
		
		//2. Read CFG Path
		File dir = new File("./CallGraphPathList");
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
			    	  List<String> cgPathList = cgPathReader(file.getPath());
			    	  //マッチング処理を行う。set** get**で始まるメソッド名があるかチェツクし、存在するパスだけにフィルタリング。
			    	  List<String> filtered_cgPathList = retrieveStterGetter(cgPathList, file);
			    	  //Path情報を”CG_PathList_Filtered"ディレクトリに保存する。1000行超える場合はファイル分割。
			    	  ret = writeFilteredCGPathList("CG_PathList_Filtered", file.getName(), filtered_cgPathList);
			      }
		    }
	    }
		
		return ret;
	}
	
	
	/**
	 * 引数指定したパスのファイルからCGパス情報を読み取り、パス単位にリストに格納する。
	 * @param filePath
	 * @return pathList List<String>
	 */
	private List<String> cgPathReader(String filePath){
		List<String> ret = new ArrayList<String>();
		try {
			//ファイルパスからファイルを読込
			File file = new File(filePath);
			//ファイル存在チェック
			if(file.exists()){
				//存在していたら読込開始して、１行目をStringデータとして保持
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				
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
	 * 1ファイルに格納された複数のCGPathList情を格納したListから情報を１レコードずつ
	 * 取り出して、set***, get***のメソッド名が存在するのか正規表現で抽出する。
	 * 
	 * @param cgPathList
	 * @return
	 */
	public List<String> retrieveStterGetter(List<String> cgPathList, File file){
		//戻り値
		List<String> ret = new ArrayList<String>();
		
		//Loop by the number of cgPathList data.
		for(String path : cgPathList){
			//:::でCGPathListの１レコードをsplitで分割してノード数分ループして以下の処理を実施
			//path = path.substring(0, path.length() - 3);
			String[] nodeArray =  path.split(":::");
			//NodeArrayIndex
			int nodeArrayIndex = 0;
			for(String node : nodeArray){
				/*
				 * <で始まり、＞で終わる部分を正規表現で抽出
				 * */
				Pattern pattern = Pattern.compile("<.*?>");
				Matcher matcher = pattern.matcher(node);
				//マッチした文字列を発見した場合
				if(matcher.find()){
					//<>の中の文字列を空白区切りでスプリットして配列に格納。
					String[] array = matcher.group().split(" ");
					//この時点で配列要素がnullまたは３つなかったら、異なる<>を抽出しているのでcontinueして次のnodeループを回す。
					if(array == null || array.length != 3) continue;
					//1番目の配列要素の<を削除して再度格納
					String firstTmp = array[0].substring(1, array[0].length() - 1);
					array[0] = firstTmp;
					//3番目の配列要素の>を削除して再度格納
					String thirdTmp = array[2].substring(0, array[2].length() - 1);
					array[2] = thirdTmp;
					//配列の三番目がメソッド名なので三番目を取り出して
					String methodName = array[2];
					//Stringのcontainsメソッドで、setかgetが含まれるか判定
					if( methodName.contains("set") || methodName.contains("get") ){
						//結果一時格納変数
						String tmp;
						//レコード情報を作成する。
						if(nodeArrayIndex == 0){
							tmp = file.getName() + ":::" + array[0] + "." + array[2] + ":::" + node;
						} else {
							tmp = file.getName() + ":::" + array[0] + "." + array[2] + ":::" + nodeArray[nodeArrayIndex-1];							
						}
						//戻値の配列に格納: 第二要素のメソッド名が一致する要素が追加されている場合は追加をスキップする。含まれてなかったらtrueの条件。
						if( isContains(array[0] + "." + methodName, ret) )
							ret.add(tmp);
						
						//nodeIndexをカウントアップ
						nodeArrayIndex++;
					}
				}
			}
		}	
		//リターン
		return ret;
	}
	
	/**
	 * フィルター結果の格納List<String>の第二要素のメソッド名に、これから格納しようとしている情報と同じメソッド名が
	 * 含まれているかどうか確認するメソッド。同じメソッドが格納されていたらfalse, なかったらtrueの条件判定。
	 * 
	 * @param methodName
	 * @param CGPathList_filtered
	 * @return boolean 同じメソッドが格納されていたらfalse, なかったらtrue
	 */
	private boolean isContains(String methodName, List<String> CGPathList_filtered) {
		
		for(String record : CGPathList_filtered){
			String[] array = record.split(":::");
			String recordedMethodName = array[1];
			if(recordedMethodName != null && recordedMethodName.equals(methodName)){
				return false;
			} 
		}
		return true;
	}
	
	/**
	 * フィルタリングしたCGPath情報をフィアル出力する（追記）する。
	 * 書き込むPath情報が1000行以上の場合、OutOfMemoryを回避するために
	 * 1000行単位にファイル分割して出力する。
	 * 
	 * @param dirName
	 * @param fileName
	 * @param pathList
	 * @return true or false
	 */
	public boolean writeFilteredCGPathList(String dirName, String fileName, List<String> pathList){
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
//System.out.println("NumOfPath: " + pathList.size());			
			//Listを分割するUtilityクラスであるdevide()を利用して分割。
			dividedPathLists = CollectionUtils.devide(pathList, 1000);
//System.out.println("NumOfList/1000: " + dividedPathLists.size());			
			//Counter
			int count = 0;
			//分割リスト数分だけループして別ファイルで保存。
			for(List<String> pList : dividedPathLists){
				//よく無いが完全重複コードで、フィアルに書き出し。ファイル名だけインクリメント。
				/*Modified by takeda 20200613: fileName is fixed by "CGPathList_filtered.txt"*/
				//String filePath = dirName + Context.SEPARATOR + "Div_" + String.valueOf(count++) + "_" + fileName;
				String filePath = dirName + Context.SEPARATOR + "Div_" + String.valueOf(count++) + "_" + "CGPathList_filtered.txt";
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
		/*Modified by takeda 20200613: fileName is fixed by "CGPathList_filtered.txt"*/
		//String filePath = dirName + Context.SEPARATOR + fileName;
		String filePath = dirName + Context.SEPARATOR  + "CGPathList_filtered.txt";
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

}
