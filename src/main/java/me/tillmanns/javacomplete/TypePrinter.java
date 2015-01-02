package me.tillmanns.javacomplete;

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

    public String print(CompletionCandidate v) {
	if(prefix.length() > 0 && !v.getName().startsWith(prefix))
	    return null;

	return v.print();
    }

    public String printLocalTypes(JavaCompleteCompilationUnit cu) {
	StringBuilder completionList = new StringBuilder();
	for(CompletionCandidate i:cu.getImports()) {
	    if (print(i) == null)
		continue;
	    completionList.append(print(i));
	    completionList.append("\n");
	}

	for(CompletionCandidate f:cu.getFields()) {
	    if (print(f) == null)
		continue;

	    completionList.append(print(f));
	    completionList.append("\n");
	}

	for(CompletionCandidate m:cu.getMethods()) {
	    if (print(m) == null)
		continue;
	    completionList.append(print(m));
	    completionList.append("\n");
	}

	for(CompletionCandidate v:cu.getVariables()) {
	    if (!(v.getScopeBegin() <= request.getLine() && request.getLine() <= v.getScopeEnd()))
		continue;

	    if (print(v) == null)
		continue;

	    completionList.append(print(v));
	    completionList.append("\n");
	}

	for (CompletionCandidate i:packageTypes()) {
	    if (print(i) == null)
		continue;

	    completionList.append(print(i));
	    completionList.append("\n");
	}

	return completionList.toString();
    }

    private ArrayList<CompletionCandidate> packageTypes() {
	File file = request.getFile().getParentFile();
	ArrayList<CompletionCandidate> types = new ArrayList<CompletionCandidate>();

	for(File f:file.listFiles()) {
	    if (f.isDirectory()) {
		continue;
	    }

	    if (!f.getName().endsWith(".java")) {
		continue;
	    }

	    if (f.getName().equals(String.format(".#%s", request.getFile().getName())))
		continue;

	    String name = f.getName().replaceAll("(\\.java)$", "");
	    types.add(new CompletionCandidate(name, "", null));
	}
	return types;
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
