package dataflow.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileReaderUtil {
	
	public List<String> readAllFilesInDir(String dirPath){
		//1.return object
		List<String> ret = new ArrayList<String>();
		
		//2. Read CFG Path
		File dir = new File(dirPath);
		//ディレクトリ配下のファイルをFileオブジェクトとして配列に取得。
	    File[] files = dir.listFiles();
	    //ファイルがなかったら終了。
	    if( files == null ) {
		      return ret;		   
	    } else {
	    	//Fileごとにループ。
		    for( File file : files ) {
		    	//存在チェック。
			      if( !file.exists() )
			        continue;//次のループに進む。
			      //ファイルだったら読み込む。
			      else if( file.isFile() ) {
			    	  //ファイルを読み込む。
			    	  List<String> tmp = readFile(file.getPath());
			    	  //Return のListに追記
			    	  for(String s : tmp){
			    		  ret.add(s);
			    	  }
			      }
		    }
	    }
		
		//return
		return ret;
	}

	/**
	 * Designate a file path and read that specific data.
	 * Then, return List<String> data.
	 * 
	 * @param filePath
	 * @return List<String> List data added file records data.
	 */
	public List<String> readFile(String filePath){
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

}
