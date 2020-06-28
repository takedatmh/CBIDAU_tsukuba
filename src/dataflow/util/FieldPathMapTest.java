package dataflow.util;

import org.junit.Test;

public class FieldPathMapTest {
	
	//String field = "FIELD";
	String field = "org.apache.catalina.Host host";
	
	//String path = "R_if r4 == null FIELD goto $r18 = new java.lang.StringBuilder,C_$r12 ";
	//String path = "R_r1 := @parameter0: java.io.PrintWriter,R_r1 := @parameter0: java.io.PrintWriter,R_r2 := @parameter1: java.lang.String,R_r3 := @parameter2: org.apache.catalina.util.ContextName,R_r4 := @parameter3: java.lang.String,R_z0 := @parameter4: boolean,R_r5 := @parameter5: org.apache.tomcat.util.res.StringManager,R_if r2 == null goto (branch),U_$i0 = virtualinvoke r2.<java.lang.String: int length()>(),R_if $i0 != 0 goto (branch),U_r2 = null,R_if r4 == null goto $i2 = r0.<org.apache.catalina.manager.ManagerServlet: int debug>,U_$i1 = virtualinvoke r4.<java.lang.String: int length()>(),R_if $i1 != 0 goto $i2 = r0.<org.apache.catalina.manager.ManagerServlet: int debug>,U_r4 = null,U_$i2 = r0.<org.apache.catalina.manager.ManagerServlet: int debug>,R_if $i2 < 1 goto $z1 = staticinvoke <org.apache.catalina.manager.ManagerServlet: boolean validateContextName(org.apache.catalina.util.ContextName,java.io.PrintWriter,org.apache.tomcat.util.res.StringManager)>(r3, r1, r5),R_if r2 == null goto (branch),U_$i3 = virtualinvoke r2.<java.lang.String: int length()>(),R_if $i3 <= 0 goto (branch),R_if r4 == null goto $r18 = new java.lang.StringBuilder,C_$r12 = new java.lang.StringBuilder,R_specialinvoke $r12.<java.lang.StringBuilder: void <init>(java.lang.String)>("install: Installing context configuration at \'"),U_$r13 = virtualinvoke $r12.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(r2),U_$r14 = virtualinvoke $r13.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("\' from \'"),U_$r15 = virtualinvoke $r14.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(r4),U_$r16 = virtualinvoke $r15.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("\'"),U_$r17 = virtualinvoke $r16.<java.lang.StringBuilder: java.lang.String toString()>(),R_virtualinvoke r0.<org.apache.catalina.manager.ManagerServlet: void log(java.lang.String)>($r17),null_goto [?= $z1 = staticinvoke <org.apache.catalina.manager.ManagerServlet: boolean validateContextName(org.apache.catalina.util.ContextName,java.io.PrintWriter,org.apache.tomcat.util.res.StringManager)>(r3, r1, r5)],U_$z1 = staticinvoke <org.apache.catalina.manager.ManagerServlet: boolean validateContextName(org.apache.catalina.util.ContextName,java.io.PrintWriter,org.apache.tomcat.util.res.StringManager)>(r3, r1, r5),R_if $z1 != 0 goto r6 = virtualinvoke r3.<org.apache.catalina.util.ContextName: java.lang.String getName()>(),U_r6 = virtualinvoke r3.<org.apache.catalina.util.ContextName: java.lang.String getName()>(),U_r7 = virtualinvoke r3.<org.apache.catalina.util.ContextName: java.lang.String getBaseName()>(),U_r8 = virtualinvoke r3.<org.apache.catalina.util.ContextName: java.lang.String getDisplayName()>(),U_$r32 = r0.<org.apache.catalina.manager.ManagerServlet: org.apache.catalina.Host host>";
	//String path = "U_r8 = virtualinvoke r3.<org.apache.catalina.util.ContextName: java.lang.String getDisplayName()>(),U_$r32 = r0.<org.apache.catalina.manager.ManagerServlet: org.apache.catalina.Host host>, R_abc,";	
	String path = "U_r7 = virtualinvoke r3.<org.apache.catalina.util.ContextName: java.lang.String getBaseName()>(),U_r8 = virtualinvoke r3.<org.apache.catalina.util.ContextName: java.lang.String getDisplayName()>(),U_$r32 = r0.<org.apache.catalina.manager.ManagerServlet: org.apache.catalina.Host host>,U_$r33 = interfaceinvoke";
	
	@Test
	public void testDetectUnitString() {
		String result = FieldPathMap.detectUnitString(field, path);
		System.out.println(result);
	}

}
