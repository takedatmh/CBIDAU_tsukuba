package dataflow.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ThrowStmt;

/**
 * Unitを引数として受け取り、オペランドを解析して、各ノードで行っている処理のCRUD情報を解析する。
 * 解析したCRUD情報をUnitの文字列情報の接頭語としてR_のように付与した文字列を返す。
 * 基本的にCFG_DF_****.classのメイン処理で利用されることを想定している。
 * CFGの再帰探索処理により実行パスを全パターン取得してファイルに書き込む際に、各ノードに相当する
 * Unitの文字列情報の頭文字にCRUD情報のアルファベットを加えておくことにより、のちの解析において、
 * static, public データアクセスしているノードがCRDUのなんの処理をしているのか後から判別しやすくする。
 * IDAU法を最終的に適用するために必要な情報。取り出す際はString#split("_")を利用すれば容易に取り出せる。
 * @author takedatmh
 */
public class CRUD_Judgement {

	/**
	 * Unitを受け取り、そのオペランドに相当するvalueのExpressionからCRUDのどの操作をしているのか判定し、
	 * Unitの文字列情報の接頭語としてC_,R_,U_,D_をつけた文字列を戻り値として返す。
	 * @param unit
	 * @param crudMap
	 */
	public static String judgeCRUD(Unit u){
		//return value.
		String ret = null;
		
		//crud information 
		String crudInfo = null;
		
		//crudMap
		//Map<Unit, String> crudMap = new HashMap<Unit, String>();
		
		//Get soot value object from Unit.
		Iterator<ValueBox> iValueBox = u.getUseBoxes().iterator();
//Loop2 Unit->iValueBox
		while (iValueBox.hasNext()) {
			ValueBox valueBox = iValueBox.next();
			Value v = valueBox.getValue();

			/* 
			 * Check Expressions of each soot value and detect CRUD information from each soot value.
			 */
			if (v instanceof NewExpr || v instanceof NewArrayExpr
					|| v instanceof NewMultiArrayExpr || v instanceof AnyNewExpr) {
				crudInfo = "C";
			} else if (u instanceof NullConstant
					|| u.toString().contains("nullConstant ")
					|| u.toString().contains("delete ")
					|| u.toString().contains("Delete ")) {
				crudInfo = "D";
			// Treat each Statement.
			} else if (u instanceof AssignStmt) {
				crudInfo = "U";
			} else if (u instanceof IdentityStmt) {
				crudInfo = "R";
			} else if (u instanceof GotoStmt) {
				crudInfo = "R";
			} else if (u instanceof IfStmt) {
				crudInfo = "R";
			} else if (u instanceof InvokeStmt) {
				crudInfo = "R";
			} else if (u instanceof MonitorStmt) {
				crudInfo = "R";
			} else if (u instanceof ReturnStmt) {
				crudInfo = "R";
			} else if (u instanceof ThrowStmt) {
				crudInfo = "R";
			} else if (u instanceof BreakpointStmt) {
				crudInfo = "R";
			} else if (u instanceof NopStmt) {
				crudInfo = "R";
				// Any other cases are fallen into R.
			} else {
				crudInfo = "R";
			}
		}
		
		//return value creation.
		ret = crudInfo + "_" + u.toString();
				
		//return.
		return ret;
	}

}
