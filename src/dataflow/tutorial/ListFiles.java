package dataflow.tutorial;

import java.io.File;

public class ListFiles {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        //Fileクラスのオブジェクトを生成する
        File dir = new File("./JavapOutput");
        
        //listFilesメソッドを使用して一覧を取得する
        File[] list = dir.listFiles();
        
        if(list != null) {
            
            for(int i=0; i<list.length; i++) {
                
                //ファイルの場合
                if(list[i].isFile()) {
                    System.out.println("ファイルです : " + list[i].toString());
                }
                //ディレクトリの場合
                else if(list[i].isDirectory()) {
                    System.out.println("ディレクトリです : " + list[i].toString());
                }
            }
        } else {
            System.out.println("null");
        }

	}

}
