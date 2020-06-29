# CBIDAU_tsukuba
tsukuba Univa version 1.0.0

# What to Use CB-IDAU

Use CB-IDAU to automatically generate test code candidates from source codes so you can focus on work that matters most. CB-IDAU is commonly used for:

- Call Graph Analysis for Java
- Control Flow Graph Analyis for Java
- Genrating Call Graph as graphviz format
- Generating Control Flow Graph as graphviz and R igraph format
- Generate Test Case as binary java method combination based on CRUD access

Regarding Overview of CB-IDAU, please refer to our [wiki] page.

# Downloads
Non-source downloads such as WAR files and several Linux packages can be found on our [GitHub].

# Source
Our latest and greatest source of CB-IDAU can be found on [GitHub].

[GitHub]: https://github.com/takedatmh/CBIDAU_tsukuba
[wiki]: https://github.com/takedatmh/CBIDAU_tsukuba/wiki

# How to execute CB-IDAU?
- Check out the whole of project from [GitHub].
- Build sample source code built in this project by JDK 1.7.
- If you have any compile error when you check out this project, please add jar files in lib folder as this project build library. 
- Run config settings for CGCreator.java and CFG_DF_20200524.java as follows:
- Program arguments: -whole-program -xml-attributes -keep-line-number -f jimple -p cg.cha enabled:true -p cg verbose:true,all-reachable:true,safe-forname:true,safe-newinstance:true
- VM arguments: -Xss1000m -Xmx5000M -Dmethod=method01 -Dmain=sample.functionA.MethodsA.MainB -Dtarget=sample.functionB.MethodsB
- Finally, execute the CGPathAnalyzer.java.
- As a result, you can get any result files in CallGraph, CallGraphPathList, GFG_PathList, CFG_PathList_Filtered, CG_PathList_Filtered, ContraolFlowGraphPDF and igraph.
