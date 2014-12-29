package me.tillmanns.javacomplete;

import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
import javassist.bytecode.AccessFlag;
import java.util.ArrayList;

import java.io.File;
import java.util.ArrayList;

import org.pmw.tinylog.Logger;

class TypePrinter {
    private String name;
    private String type;
    private String parameters;
    private String prefix;

    private Request request;

    public TypePrinter(Request request) {
	this.request = request;
	this.prefix = request.getPrefix();
    }

    private StringBuilder append(String candidate, StringBuilder sb) {
	if (sb.indexOf(candidate) != -1)
	    return sb;

	sb.append(candidate);
	sb.append("\n");
	return sb;
    }

    public String printLocalTypes(JavaCompleteCompilationUnit cu) {
	StringBuilder completionList = new StringBuilder();
	MethodDeclaration method = cu.getMethodOrNull(request.getLine());
	ConstructorDeclaration constructor = cu.getConstructorOrNull(request.getLine());
	String candidate;

	if (method != null) {
	    for (VariableDeclarationExpr vexpr:cu.getVariables(method, request.getLine())) {
		candidate = print(vexpr);
		if (candidate == null)
		    continue;
		completionList = append(candidate, completionList);
	    }

	    for (Parameter p:cu.getParameters(method)) {
		candidate = print(p);
		if (candidate == null)
		    continue;
		completionList = append(candidate, completionList);
	    }
	}

	if (constructor != null) {
	    for (VariableDeclarationExpr vexpr:cu.getVariables(constructor, request.getLine())) {
		candidate = print(vexpr);
		if (candidate == null)
		    continue;
		completionList = append(candidate, completionList);
	    }

	    for (Parameter p:cu.getParameters(constructor)) {
		candidate = print(p);
		if (candidate == null)
		    continue;
		completionList = append(candidate, completionList);
	    }

	    candidate = print(constructor);
	    if (candidate != null) {
		completionList = append(candidate, completionList);
	    }
	}

	for(ImportDeclaration i:cu.getImports()) {
	    candidate = print(i);
	    if (candidate == null)
		continue;
	    completionList = append(candidate, completionList);
	}

	for (MethodDeclaration m:cu.getMethods()) {
	    candidate = print(m);
	    if (candidate == null)
		continue;
	    completionList = append(candidate, completionList);
	}

	for (FieldDeclaration f:cu.getFields()) {
	    candidate = print(f);
	    if (candidate == null)
		continue;
	    completionList = append(candidate, completionList);
	}

	for (String i:packageTypes()) {
	    if (!i.startsWith(request.getPrefix()))
		continue;
	    completionList = append(String.format("%s!!", i), completionList);
	}

	return completionList.toString();
    }

    private ArrayList<String> packageTypes() {
	File file = request.getFile().getParentFile();
	ArrayList<String> types = new ArrayList<String>();

	for(File f:file.listFiles()) {
	    if (f.isDirectory()) {
		continue;
	    }

	    if (!f.getName().endsWith(".java")) {
		continue;
	    }

	    if (f.getName().equals(String.format(".#%s", request.getFile().getName())))
		continue;

	    types.add(f.getName().replaceAll("(\\.java)$", ""));
	}
	return types;
    }

    private String typeToString (Type n) {
	if (n instanceof ReferenceType)
	    return typeToString((ReferenceType) n);

	if (n instanceof PrimitiveType)
	    return typeToString((PrimitiveType) n);

	return "";
    }

    private String typeToString (ReferenceType n) {
	return ((ClassOrInterfaceType) n.getType()).getName();
    }

    private String typeToString (PrimitiveType n) {
	return String.format("%s", n.getType());
    }

    public CompletionCandidate getCandidate(VariableDeclarationExpr n) {
	name = n.getVars().get(0).getId().getName();

	if (prefix.length() > 0 && !name.startsWith(prefix))
	    return null;

	type = typeToString(n.getType());

	return new CompletionCandidate(name, type, null);
    }

    public String print(VariableDeclarationExpr n) {
	CompletionCandidate c = getCandidate(n);
	if (c == null)
	    return null;
	return c.print();
    }

    public String print(Parameter n) {
	name = ((Parameter) n).getId().getName();

	if (prefix.length() > 0 && !name.startsWith(prefix))
	    return null;

	type = typeToString(n.getType());
	return String.format("%s!%s!", name, type);
    }

    public String print(ImportDeclaration n) {
	name = n.getName().getName();

	if (prefix.length() > 0 && !name.startsWith(prefix))
	    return null;

	return String.format("%s!!", name);
    }

    public CompletionCandidate getCandidate(FieldDeclaration n) {
	name = (n.getVariables().get(0).getId().getName());

	if (prefix.length() > 0 && !name.startsWith(prefix))
	    return null;

	type = typeToString(n.getType());
	return new CompletionCandidate(name, type, null);
    }

    public String print(FieldDeclaration n) {
	CompletionCandidate c = getCandidate(n);
	if (c == null)
	    return null;
	return c.print();
    }

    public CompletionCandidate getCandidate(ConstructorDeclaration n) {
	name = n.getName();

	if (prefix.length() > 0 && !name.startsWith(prefix))
	    return null;

	type = "";

	ArrayList<String> parameters = new ArrayList<String>();

	if (n.getParameters() != null) {
	    for(Parameter p:n.getParameters()) {
		parameters.add(p.toString().split(" ")[0]);
	    }
	}

	return new CompletionCandidate(name, type, parameters);
    }

    public String print(ConstructorDeclaration n) {
	CompletionCandidate c = getCandidate(n);

	if (c == null)
	    return null;
	return c.print();
    }

    public CompletionCandidate getCandidate(MethodDeclaration n) {
	name = n.getName();

	if (prefix.length() > 0 && !name.startsWith(prefix))
	    return null;

	type = typeToString(n.getType());

	ArrayList<String> parameters = new ArrayList<String>();

	if (n.getParameters() != null) {
	    for(Parameter p:n.getParameters()) {
		parameters.add(p.toString().split(" ")[0]);
	    }
	}

	return new CompletionCandidate(name, type, parameters);
    }

    public String print(MethodDeclaration n) {
	CompletionCandidate c = getCandidate(n);
	if (c == null)
	    return null;
	return c.print();
    }

    public String printClassMembers(CtClass clazz) {
	StringBuilder completionList = new StringBuilder();
	String name = "";
	String type = "";
	String parameters = "";
	for (CtMethod m:clazz.getMethods()) {
	    int accessFlags = m.getMethodInfo().getAccessFlags();

	    if (!AccessFlag.isPublic(accessFlags))
		continue;

	    name = m.getName();

	    if (!name.startsWith(request.getPrefix()))
		continue;

	    try {
		type = m.getReturnType().getSimpleName();
	    } catch(Exception e) {;;}

	    parameters = "(";

	    try {
		for(CtClass p:m.getParameterTypes()){
		    parameters = String.format("%s%s,", parameters, p.getSimpleName());
		}
	    } catch (Exception e) {;;}

	    parameters = String.format("%s)", parameters.replaceAll(",+$", ""));
	    completionList.append(String.format("%s!%s!%s", name, type, parameters));
	    completionList.append("\n");
	}

	for (CtField f:clazz.getFields()) {
	    int accessFlags = f.getFieldInfo().getAccessFlags();

	    if (!AccessFlag.isPublic(accessFlags))
		continue;

	    name = f.getName();

	    if (!name.startsWith(request.getPrefix()))
		continue;

	    try {
		type = f.getType().getName();
	    } catch (Exception e) {;;}

	    completionList.append(String.format("%s!%s!", name, type));
	    completionList.append("\n");
	}

	return completionList.toString();
    }

}
