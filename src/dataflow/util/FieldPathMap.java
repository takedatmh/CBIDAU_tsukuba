package dataflow.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 変数名と、それに依存したパス上のノード（UNIT)情報をファイルに文字列として格納するクラス。
 * key		: 変数FQCN (package名.クラス名 Field名)
 * 区切文字	: ;
 * Value	: クラスFQCN_メソッド名 パス文字列 CRUD_Unit文字列
 * のレコード情報をリストに文字列として格納する。
 * 取り出す時はsplit(";")として変数とPathのUnit情報をCRUD種別で取り出す。
 * ただのリストなので変数名は重複して格納されるが、この重複情報の組み合わせがCBIDAUの抽出した情報である。
 * 出力はファイル、FieldPathMapフォルダ内に、固定のファイル名でFieldPathMapingList.txt
 * として格納する。別オープンソースを解析する際には上書きされるが退避するオペレーション前提。
 * 
 * @author takedatmh
 */
public class FieldPathMap {
	
	/**
	 * フィールドとそこにアクセスパス上のノード（Unit)の情報をマッピングした情報を作成してファイルに書き込む。
	 * @param fieldPathMapList
	 * @return boolean ファイル出力に成功している場合true.
	 */
	public static boolean writeFieldPathMapList(List<String> fieldPathMapList) {
		//return value.
		boolean ret = false;
		
		//Write the list of path.
		FileWriter in = null;
		PrintWriter out = null;
		
		try {
			//postscript version
			in = new FileWriter("./FieldPathMap" + Context.SEPARATOR + "FieldPathMapingList.txt", true);
			out = new PrintWriter(in);
			for(String data : fieldPathMapList)
				out.println(data);
			out.flush();
			//戻り値の更新
			ret = true;
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
	 * Fieldに参照するUnit単位の文字列情報をPath文字列から抽出するための正規表現。
	 * R_＊＊＊フィールド文字＊＊＊,R_
	 * のようにCRUD情報の頭文字で挟まれた最短マッチを返却する。
	 * 最短マッチを実現するために、左側はR_が出現してR_が出現しない。右側は？を使った最短マッチ。
	 * @param field
	 * @return Unitの文字列
	 */
	public static String detectUnitString(String field, String path) {
		String ret = null;
		
		/*EOF Char is Modified by takeda from , to >. in 20200526.*/
		/*Top Char , is added by takeda in 20200525.*/
		//Pattern pattern = Pattern.compile("(C_|R_|U_|D_)[^(C_|R_|U_|D_)].*" + field + ".*?,");\\
		//Pattern pattern = Pattern.compile("(C_|R_|U_|D_)[^(C_|R_|U_|D_)].*" + field + ".*?>");
		Pattern pattern = Pattern.compile("\\,(C_|R_|U_|D_)([^(C_|R_|U_|D_)]*?)" + field + ">\\,");
		Matcher matcher = pattern.matcher(path);
		
		//マッチした文字列があれば
		if(matcher.find())
			//matcherからマッチして抜き出した文字列を戻り値に設定。
			ret = matcher.group().substring(1,matcher.group().length());
		else
			//matcherに何もなければ”Not match"を返却する。
			System.out.println("Not match.");
		
		return ret.substring(0, ret.length()-1);
	}

}
