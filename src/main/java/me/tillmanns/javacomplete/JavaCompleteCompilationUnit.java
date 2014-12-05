package me.tillmanns.javacomplete;

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;

// external packages
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.visitor.VoidVisitorAdapter;

// Ast body elements
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.TypeDeclaration;

// Ast types
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.type.PrimitiveType;

// Ast statements
import japa.parser.ast.stmt.BlockStmt;

import org.pmw.tinylog.Logger;

public class JavaCompleteCompilationUnit {
    CompilationUnit c;

    public JavaCompleteCompilationUnit(InputStream in) throws Exception {
	    c = JavaParser.parse(in);
    }

    public String getPackage() {
	if (c.getPackage() == null) {
	    return null;
	}
	return c.getPackage().getName().toString();
    }

    public ArrayList<ImportDeclaration> getImports() {
	if (c.getImports() == null) {
	    return new ArrayList<ImportDeclaration>();
	}
	ArrayList<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
	imports.addAll(c.getImports());
	return imports;
    }

    public ArrayList<MethodDeclaration> getMethods() {
	final ArrayList<MethodDeclaration> arr = new ArrayList<MethodDeclaration>();

	class MethodVisitor extends VoidVisitorAdapter {
	    @Override
	    public void visit(MethodDeclaration n, Object arg) {
		arr.add(n);

		super.visit(n, arg);
	    }
	}

	new MethodVisitor().visit(c, null);
	return arr;
    }

    final ArrayList<ConstructorDeclaration> getConstructors() {
	final ArrayList<ConstructorDeclaration> arr = new ArrayList<ConstructorDeclaration>();

	class ConstructorVisitor extends VoidVisitorAdapter {
	    @Override
	    public void visit(ConstructorDeclaration n, Object arg) {
		arr.add(n);

		super.visit(n, arg);
	    }
	}

	new ConstructorVisitor().visit(c, null);
	return arr;
    }

    public ConstructorDeclaration getConstructorOrNull(final Integer line) {
	class ConstructorVisitor extends VoidVisitorAdapter {
	    public ConstructorDeclaration m = null;

	    @Override
	    public void visit(ConstructorDeclaration n, Object arg) {
		if(n.getBeginLine() <= line && line <= n.getEndLine()) {
		    m = n;
		}

		if (m == null) {
		    super.visit(n, arg);
		}
	    }
	}

	ConstructorVisitor mv = new ConstructorVisitor();
	mv.visit(c, null);
	return mv.m;
    }

    public MethodDeclaration getMethodOrNull(final Integer line) {
	class MethodVisitor extends VoidVisitorAdapter {
	    public MethodDeclaration m = null;

	    @Override
	    public void visit(MethodDeclaration n, Object arg) {
		if(n.getBeginLine() <= line && line <= n.getEndLine()) {
		    m = n;
		}

		if (m == null) {
		    super.visit(n, arg);
		}
	    }
	}

	MethodVisitor mv = new MethodVisitor();
	mv.visit(c, null);
	return mv.m;
    }

    public ArrayList<BlockStmt> getBlockStatements(MethodDeclaration m) {
	final ArrayList<BlockStmt> arr = new ArrayList<BlockStmt>();

	class BlockStmtVisitor extends VoidVisitorAdapter {
	    @Override
	    public void visit(BlockStmt n, Object arg) {
		arr.add(n);
		super.visit(n, arg);
	    }
	}

	new BlockStmtVisitor().visit(m, null);
	return arr;
    }

    public ArrayList<BlockStmt> getBlockStatements(ConstructorDeclaration m) {
	final ArrayList<BlockStmt> arr = new ArrayList<BlockStmt>();

	class BlockStmtVisitor extends VoidVisitorAdapter {
	    @Override
	    public void visit(BlockStmt n, Object arg) {
		arr.add(n);
		super.visit(n, arg);
	    }
	}

	new BlockStmtVisitor().visit(m, null);
	return arr;
    }

    public ArrayList<VariableDeclarationExpr> getVariables(final BodyDeclaration m, final Integer l) {
	final ArrayList<VariableDeclarationExpr> arr = new ArrayList<VariableDeclarationExpr>();
	final ArrayList<BlockStmt> blocks = new ArrayList<BlockStmt>();

	if (m instanceof MethodDeclaration) {
	    blocks.addAll(getBlockStatements((MethodDeclaration)m));
	}

	if (m instanceof ConstructorDeclaration) {

	    blocks.addAll(getBlockStatements((ConstructorDeclaration)m));
	}


	class VariableDeclarationExprVisitor extends VoidVisitorAdapter {
	    @Override
	    public void visit(VariableDeclarationExpr n, Object arg) {
		// --- check the scope of the variable
		// find the block, the variable is directly nested in
		BlockStmt blockstmt = null;
		for(BlockStmt b:blocks) {
		    if(b.getBeginLine() <= n.getBeginLine() && n.getEndLine() <= b.getEndLine()) {
			blockstmt = b;
		    }
		}

		// check if i am inside that block
		if (blockstmt.getBeginLine() <= l && l <= blockstmt.getEndLine()) {
		    arr.add(n);
		}
		super.visit(n, arg);
	    }
	}	

	if (m instanceof MethodDeclaration) {
	    new VariableDeclarationExprVisitor().visit((MethodDeclaration) m, null);
	}

	if (m instanceof ConstructorDeclaration) {
	    new VariableDeclarationExprVisitor().visit((ConstructorDeclaration) m, null);
	}
	
	return arr;
    }

    public ArrayList<Parameter> getParameters(BodyDeclaration m) {
	final ArrayList<Parameter> arr = new ArrayList<Parameter>();

	class ParameterVisitor extends VoidVisitorAdapter {
	    @Override
	    public void visit(Parameter n, Object arg) {
		arr.add(n);
		super.visit(n, arg);
	    }
	}

	if (m instanceof MethodDeclaration) {
	    new ParameterVisitor().visit((MethodDeclaration) m, null);
	}

	if (m instanceof ConstructorDeclaration) {
	    new ParameterVisitor().visit((ConstructorDeclaration) m, null);
	}

	return arr;
    }

    private ClassOrInterfaceType castType(Type t) {
	if (t instanceof ReferenceType) {
	    return (ClassOrInterfaceType) ((ReferenceType) t).getType();
	}

	if (t instanceof ClassOrInterfaceType) {
	    return (ClassOrInterfaceType) ((ReferenceType) t).getType();
	}

	return null;
    }

    public ClassOrInterfaceType getTypeOrNull(String name, Integer line) {
	for(MethodDeclaration m:getMethods()) {
	    if (!m.getName().equals(name)) {
		continue;
	    }
	    return castType(m.getType());
	}
	MethodDeclaration m = getMethodOrNull(line);
	ConstructorDeclaration c = getConstructorOrNull(line);

	// Field Type
	for(FieldDeclaration f:getFields()) {
	    VariableDeclarator v = f.getVariables().get(0);
	    VariableDeclaratorId id = v.getId();

	    if (!id.getName().equals(name))
		continue;

	    return castType(f.getType());
	}

	// if we are not inside a method we only return fields
	if (m == null && c == null) {
	    return null;
	}

	// Method Variable
	for(VariableDeclarationExpr vexpr:getVariables(m, line)) {
	    VariableDeclarator v = vexpr.getVars().get(0);
	    VariableDeclaratorId id = v.getId();

	    if (!id.getName().equals(name))
		continue;
	    return castType(vexpr.getType());
	}

	for(VariableDeclarationExpr vexpr:getVariables(c, line)) {
	    VariableDeclarator v = vexpr.getVars().get(0);
	    VariableDeclaratorId id = v.getId();

	    if (!id.getName().equals(name))
		continue;
	    return castType(vexpr.getType());
	}

	for(Parameter p:getParameters(m)) {
	    VariableDeclaratorId id = p.getId();

	    if (!id.getName().equals(name))
		continue;

	    return castType(p.getType());
	}

	for(Parameter p:getParameters(c)) {
	    VariableDeclaratorId id = p.getId();

	    if (!id.getName().equals(name))
		continue;

	    return castType(p.getType());
	}

	return null;
    }

    public ArrayList<FieldDeclaration> getFields() {
    	final ArrayList<FieldDeclaration> arr = new ArrayList<FieldDeclaration>();

    	class FieldDeclarationVisitor extends VoidVisitorAdapter {
    	    @Override
    	    public void visit(FieldDeclaration n, Object arg) {
    	    	arr.add(n);
    	    	super.visit(n, arg);
    	    }
    	}

    	new FieldDeclarationVisitor().visit(c, null);
    	return arr;
    }

    public TypeDeclaration getType() {
	return c.getTypes().get(0);
    }
}
