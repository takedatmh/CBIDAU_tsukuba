package dataflow.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;



//import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PathFilterTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
 
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFileReader() {
		System.out.println("#testFileReader");
		
		PathFilter pathFilter = new PathFilter();
		
		String filePath = "Field" + Context.SEPARATOR + "sample.functionB.MethodB.txt";
		
		List<String> ListOfField = pathFilter.javapFieldReader(filePath);
		
		assertEquals("sample.functionB.CallerFromB caller",ListOfField.get(0));
		
	}
	
	@Test
	public void testCfgPathReader(){
		System.out.println("#testCfgPathReader");
		
		PathFilter pathFilter = new PathFilter();
		
		String filePath = "CFG_PathList" + Context.SEPARATOR + "sample.functionB.MethodsB_method01.txt";
		
		List<String> pathList = pathFilter.cfgPathReader(filePath);
		
		for(String p : pathList)
			System.out.println("path: " + pathList);
		
		//assertEquals("sample.functionB.CallerFromB caller", pathList.get(0));
	}
	
	@Test
	public void testFileReaderFromCSV(){
		System.out.println("#testFileReaderFromCSV");
		
		PathFilter pathFilter = new PathFilter();
		
		List<String> list = pathFilter.fileReaderFromCSV("ClassMethodCSV"+Context.SEPARATOR+"tomcat_class_method_arg.csv");
		//System.out.println("result: " + list);
		
	}
	
//	@Test
//	public void testMatchUnit() {
//		System.out.println("#testMatchUnit");
//
//		PathFilter pathFilter = new PathFilter();
//		
//		//CFGPath
//		String filePath = "CFG_PathList" + Context.SEPARATOR + "sample.functionB.MethodsB_method01.txt";		
//		List<String> cfgPathList = pathFilter.cfgPathReader(filePath);
//
//		//FieldList
//		List<String> fieldList = pathFilter.fileReaderFromCSV("ClassMethodCSV"+Context.SEPARATOR+"tomcat_class_method_arg.csv");		
//		
//		List<String> ret = pathFilter.matchUnit(cfgPathList, fieldList);
//		
//		//System.out.println("Filtered Paths: " + ret);
//
//	}
	
	@Test
	public void testExecuteJavapFieldReader() {
		System.out.println("#testExecuteJavapFieldReader");
		
		PathFilter pathFilter = new PathFilter();
		
		//pathFilter.executeJavapFieldReader("/Volume/storEdge/Labo/package_info");
		boolean result = pathFilter.executeJavapFieldReader("./JavapOutput");
		
		assertEquals(true, result);
		
	}
	
	@Test
	public void testReadClassFeildCSV() {
		System.out.println("#testReadClassFeildCSV");
		
		PathFilter pathFilter = new PathFilter();
		
		List<String> result = pathFilter.readClassFeildCSV("./FieldNohma/tom_class_feild_shusyoku.csv");
		
		for(String field : result)
			System.out.println("FieldList: " + field);
		
	}
	
	@Test
	public void testFtilter() {
		System.out.println("#testFilter : " + System.currentTimeMillis());		
		
		PathFilter pathFilter = new PathFilter();
		
		boolean result = pathFilter.filter();
		
		System.out.println("retuls: " + result + " : " + System.currentTimeMillis());
	}	
	

}
