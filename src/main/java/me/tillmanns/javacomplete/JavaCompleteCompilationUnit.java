package me.tillmanns.javacomplete;

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;

import org.pmw.tinylog.Logger;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.SimpleType;

public class JavaCompleteCompilationUnit {
    CompilationUnit cu;
    private ArrayList<CompletionCandidate> fields = null;
    private ArrayList<CompletionCandidate> methods = null;
    private ArrayList<CompletionCandidate> variables = null;
    private ArrayList<CompletionCandidate> imports = null;

    public JavaCompleteCompilationUnit(String in) {
	fields = new ArrayList<CompletionCandidate>();
	methods = new ArrayList<CompletionCandidate>();
	variables = new ArrayList<CompletionCandidate>();
	imports = new ArrayList<CompletionCandidate>();

	ASTParser parser = ASTParser.newParser(AST.JLS3);
	parser.setSource(in.toCharArray());

	cu = (CompilationUnit) parser.createAST(null);
	for (Object i:cu.imports())
	    init((ImportDeclaration)i);

	for (Object o:cu.types()) {
	    if (!(o instanceof TypeDeclaration))
		continue;

	    TypeDeclaration typeDeclaration = (TypeDeclaration) o;
	    init(typeDeclaration.getFields());
	    init(typeDeclaration.getMethods());
	}
    }

    public String getPackage() {
	if (cu.getPackage() == null) {
	    return "";
	}
	return cu.getPackage().getName().toString();
    }

    public String getTypeOrNull(String name, Integer line) {
	for (CompletionCandidate i:getImports()) {
	    if (!i.getName().equals(name))
		continue;

	    return i.getType();
	}

	for (CompletionCandidate i:getFields()) {
	    if (!i.getName().equals(name))
		continue;

	    return i.getType();
	}

	for (CompletionCandidate m:getMethods()) {
	    if (!m.getName().equals(name))
		continue;

	    return m.getType();
	}

	for (CompletionCandidate v:getVariables()) {
	    if (!(v.getScopeBegin() <= line && line <= v.getScopeEnd()))
		continue;

	    if (!v.getName().equals(name))
		continue;

	    return v.getType();
	}

	return null;
    }

    public ArrayList<CompletionCandidate> getMethods() {
	return methods;
    }

    public ArrayList<CompletionCandidate> getFields() {
	return fields;
    }

    public ArrayList<CompletionCandidate> getImports() {
	return imports;
    }

    public ArrayList<CompletionCandidate> getVariables() {
	return variables;
    }

    private void init(ImportDeclaration o) {
	String type = o.getName().toString();
	String name = ((QualifiedName)o.getName()).getName().toString();
	imports.add(new CompletionCandidate(name, type, null));
    }

    private String getType(Type type) {
	if (!(type instanceof ParameterizedType))
	    return type.toString();

	ParameterizedType ptype = (ParameterizedType) type;
	return ptype.getType().toString();
    }

    private String[] getTypeArguments(Type type) {
	if (!(type instanceof ParameterizedType))
	    return null;

	ParameterizedType ptype = (ParameterizedType) type;
	String[] types = new String[ptype.typeArguments().size()];
	for (int i = 0; i < types.length; i++) {
	    types[i] = ((SimpleType)(ptype.typeArguments().get(i))).getName().toString();
	}

	return types;
    }

    private void init(FieldDeclaration[] arr) {
	String name;
	String type;
	String[] typearguments;
	CompletionCandidate c;
	for(FieldDeclaration f:arr) {
	    type = getType(f.getType());
	    for (Object o:f.fragments()) {
		name = ((VariableDeclarationFragment) o).getName().toString();
		c = new CompletionCandidate(name, type, null);
		c.setTypeArguments(getTypeArguments(f.getType()));
		fields.add(c);
	    }
	}
    }

    private void init(SingleVariableDeclaration o) {
	String name = o.getName().toString();
	String type = getType(o.getType());
	String[] typeArguments = getTypeArguments(o.getType());
	Integer begin = cu.getLineNumber(o.getParent().getStartPosition());
	Integer end = cu.getLineNumber(o.getParent().getStartPosition() + o.getParent().getLength());

	CompletionCandidate c = new CompletionCandidate(name, type, begin, end);
	c.setTypeArguments(typeArguments);
	variables.add(c);
    }

    private void init(MethodDeclaration[] arr) {
	String name;
	String type;
	String[] typeArguments = null;

	for(MethodDeclaration m:arr) {
	    type = "";
	    if (!m.isConstructor()) {
		type = getType(m.getReturnType2());
		typeArguments = getTypeArguments(m.getReturnType2());
	    }

	    name = m.getName().toString();

	    ArrayList<String> parameters = new ArrayList<String>();

	    if (m.parameters() != null) {
		for(Object p:m.parameters()) {
		    SingleVariableDeclaration s = (SingleVariableDeclaration)p;
		    init(s);
		    parameters.add(s.getType().toString());
		}
	    }

	    CompletionCandidate c = new CompletionCandidate(name,type,parameters);
	    c.setTypeArguments(typeArguments);
	    methods.add(c);
	    Block body = m.getBody();
	    for(Object s:body.statements())
		initStatement((Statement)s);
	}
    }

    private void initStatement(Statement o) {
	if (o instanceof DoStatement)
	    init((DoStatement)o);

	if (o instanceof ExpressionStatement)
	    init((ExpressionStatement)o);

	if (o instanceof EnhancedForStatement)
	    init((EnhancedForStatement)o);

	if (o instanceof ForStatement)
	    init((ForStatement)o);

	if (o instanceof IfStatement)
	    init((IfStatement)o);

	if (o instanceof LabeledStatement)
	    init((LabeledStatement)o);

	if (o instanceof ReturnStatement)
	    init((ReturnStatement)o);

	if (o instanceof SuperConstructorInvocation)
	    init((SuperConstructorInvocation)o);

	if (o instanceof SwitchCase)
	    init((SwitchCase)o);

	if (o instanceof SwitchStatement)
	    init((SwitchStatement)o);

	if (o instanceof VariableDeclarationStatement)
	    init((VariableDeclarationStatement)o);

	if (o instanceof WhileStatement)
	    init((WhileStatement)o);

	if (o instanceof Block)
	    init((Block)o);
    }

    private void init(Expression o) {
	if (o instanceof VariableDeclarationExpression)
	    init((VariableDeclarationExpression)o);

	if (o instanceof Assignment)
	    init((Assignment)o);

    }

    private void init(Assignment o) {
	init(o.getLeftHandSide());
	init(o.getRightHandSide());
    }

    private void init(Block o) {
	for (Object s:o.statements())
	    initStatement((Statement)s);
    }

    private void init(VariableDeclarationStatement v) {
	String name;
	String type = getType(v.getType());
	String[] typeArguments = getTypeArguments(v.getType());
	Integer begin = cu.getLineNumber(v.getParent().getStartPosition());
	Integer end = cu.getLineNumber(v.getParent().getStartPosition() + v.getParent().getLength());
	CompletionCandidate c;

	for(Object f:v.fragments()) {
	    VariableDeclarationFragment fragment = (VariableDeclarationFragment)f;
	    name = fragment.getName().toString();
	    c = new CompletionCandidate(name, type, begin, end);
	    c.setTypeArguments(typeArguments);
	    variables.add(c);
	}
    }

    private void init(DoStatement o) {
	initStatement(o.getBody());
	init(o.getExpression());
    }

    private void init(ExpressionStatement o) {
	init(o.getExpression());
    }

    private void init(EnhancedForStatement o) {
	init(o.getParameter());
	init(o.getExpression());
	initStatement(o.getBody());
    }

    private void init(ForStatement o) {
	for(Object v:o.initializers())
	    init((VariableDeclarationExpression)v);

	for(Object v:o.updaters())
	    init((Expression)v);

	init(o.getExpression());

	initStatement(o.getBody());
    }

    private void init(IfStatement o) {
	initStatement(o.getThenStatement());
	initStatement(o.getElseStatement());
	init(o.getExpression());
    }

    private void init(LabeledStatement o) {
	initStatement(o.getBody());
    }

    private void init(ReturnStatement o) {
	init(o.getExpression());
    }

    private void init(SuperConstructorInvocation o) {
	init(o.getExpression());
    }

    private void init(SwitchCase o) {
	init(o.getExpression());
    }

    private void init(SwitchStatement o) {
	for(Object s:o.statements())
	    initStatement((Statement) o);

	init(o.getExpression());
    }

    private void init(SynchronizedStatement o) {
	initStatement(o.getBody());
	init(o.getExpression());
    }

    private void init(ThrowStatement o) {
	init(o.getExpression());
    }

    private void init(TryStatement o) {
	init(o.getBody());
	init(o.getFinally());
    }

    private void init(WhileStatement o) {
	initStatement(o.getBody());
	init(o.getExpression());
    }

    private void init(VariableDeclarationExpression v) {
	String name;
	String type = getType(v.getType());
	String[] typeArguments = getTypeArguments(v.getType());
	Integer begin = cu.getLineNumber(v.getParent().getStartPosition());
	Integer end = cu.getLineNumber(v.getParent().getStartPosition() + v.getParent().getLength());

	for(Object f:v.fragments()) {
	    VariableDeclarationFragment fragment = (VariableDeclarationFragment)f;
	    name = fragment.getName().toString();
	    CompletionCandidate c = new CompletionCandidate(name,type,begin,end);
	    c.setTypeArguments(typeArguments);
	    variables.add(c);
	}
    }
}
