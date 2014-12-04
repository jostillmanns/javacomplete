package me.tillmanns.javacomplete;

import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ParseException;

import org.pmw.tinylog.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
import javassist.bytecode.AccessFlag;
import java.util.ArrayList;

import java.nio.charset.Charset;

import java.net.Socket;

public class Complete {
    Request request;
    ClassPool pool;

    public Complete(Request request, Socket socket) throws Exception {
	this.request = request;
	String completionList = "";
	OutputStream out = null;
	pool = ClassPool.getDefault();

	InputStream in = new ByteArrayInputStream(request.getBuffer().getBytes());

	try {
	    completionList = complete(in);

	    out = socket.getOutputStream();
	    out.write((lines(completionList)+"\n").getBytes());
	    out.write(completionList.getBytes());
	    out.flush();

	} catch (ParseException e) {
	    Logger.debug("parseexception");
	} catch (NullPointerException e) {
	    Logger.debug("nullpointerexcpetion");
	} finally {
	    out.write("".getBytes());
	    in.close();
	    out.close();
	}
    }

    private int lines(String s) {
	int counter = 0;
	for (int i =0; i < s.length(); i++) {
	    if (s.charAt(i) != '\n')
		continue;
	    counter++;
	}
	return counter;
    }

    private String complete(InputStream in) throws Exception {
	JavaCompleteCompilationUnit cu = new JavaCompleteCompilationUnit(in);
	TypePrinter tp = new TypePrinter(request);
	String expression;

	expression = ExpressionParser.removeParenBody(request.getExpression());
	expression = ExpressionParser.lastElement(expression);
	expression = ExpressionParser.parse(expression);
	if (expression.length() == 0) {
	    return tp.printLocalTypes(cu);
	}

	Logger.debug(expression);
	String[] parts = expression.split("\\.");
	Logger.debug(parts[0]);
	parts[0] = ExpressionParser.parse(parts[0]);

	ClassOrInterfaceType t = cu.getTypeOrNull(parts[0], request.getLine());
	String typeName;
	CtClass typeNode = null;

	if (t != null) {
	     typeName = t.getName();
	} else {
	    typeName = parts[0];
	}

	for (ImportDeclaration i:cu.getImports()) {
	    if (!i.getName().getName().equals(typeName))
		continue;
	    typeNode = pool.getOrNull(i.getName().toString());
	}

	if (typeNode == null) {
	    typeNode = pool.getOrNull("java.lang." + typeName);
	}

	if (typeNode == null && cu.getPackage() != null) {
	    typeNode = pool.getOrNull(String.format("%s.%s", cu.getPackage(), typeName));
	}

	if (typeNode == null) {
	    typeNode = pool.getOrNull(typeName);
	}

	if (typeNode == null) {
	    return "";
	}

	if (parts.length == 1)
	    return tp.printClassMembers(typeNode);

	CtClass nextNode = null;
	for (int i = 1; i < parts.length; i++) {
	    parts[i] = ExpressionParser.parse(parts[i]);

	    for (CtField f:typeNode.getFields()) {
		int accessFlags = f.getFieldInfo().getAccessFlags();

		if (!AccessFlag.isPublic(accessFlags))
		    continue;

		if (!f.getName().equals(parts[i]))
		    continue;

		nextNode = f.getType();
	    }

	    for (CtMethod m:typeNode.getMethods()) {
		int accessFlags = m.getMethodInfo().getAccessFlags();

		if (!AccessFlag.isPublic(accessFlags))
		    continue;

		if (!m.getName().equals(parts[i]))
		    continue;

		nextNode = m.getReturnType();
	    }

	    if (nextNode == null) {
		return "";
	    }

	    typeNode = nextNode;
	}
	return tp.printClassMembers(typeNode);
    }
}
