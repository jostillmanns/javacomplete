package me.tillmanns.javacomplete;

import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import org.pmw.tinylog.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
import javassist.bytecode.AccessFlag;
import javassist.NotFoundException;

import java.nio.charset.Charset;

import java.net.Socket;


public class Complete {
    Request request;
    ClassPool pool;

    public Complete(Request request, ClassPool pool) {
	this.request = request;
	this.pool = pool;
    }

    public Complete(Request request, Socket socket, ClassPool pool) throws Exception {
	this.request = request;
	String completionList = "";
	OutputStream out = null;
	this.pool = pool;

	try {
	    completionList = complete(request.getBuffer());

	    out = socket.getOutputStream();
	    out.write((lines(completionList)+"\n").getBytes());
	    out.write(completionList.getBytes());
	    out.flush();

	} catch (NullPointerException e) {
	    Logger.trace(e);
	} finally {
	    if (out != null) {
		out.write("0\n".getBytes());
		out.close();
	    }
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

    public CtClass getInitialTypeOrNull(JavaCompleteCompilationUnit cu, String typeName) {
	CtClass typeNode = null;

	for (CompletionCandidate i:cu.getImports()) {
	    String name = i.getName();

	    if (!name.equals(typeName))
		continue;
	    typeNode = pool.getOrNull(i.getType());
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

	return typeNode;
    }


    public String complete(String in) throws NotFoundException {
	JavaCompleteCompilationUnit cu = new JavaCompleteCompilationUnit(in);
	TypePrinter tp = new TypePrinter(request);
	String expression;

	expression = ExpressionParser.removeParenBody(request.getExpression());
	expression = ExpressionParser.lastElement(expression);
	expression = ExpressionParser.parse(expression);
	if (expression.length() == 0) {
	    return tp.printLocalTypes(cu);
	}


	String[] parts = expression.split("\\.");
	parts[0] = ExpressionParser.parse(parts[0]);

	Logger.debug("completing upon: {}", parts[0]);

	String t = cu.getTypeOrNull(parts[0], request.getLine());
	String typeName;

	if (t != null) {
	    typeName = t;
	} else {
	    typeName = parts[0];
	}

	CtClass typeNode = getInitialTypeOrNull(cu, typeName);

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
