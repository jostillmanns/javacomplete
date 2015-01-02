package me.tillmanns.javacomplete;

import java.util.ArrayList;

class CompletionCandidate {
    String name;
    String type;
    ArrayList<String> parameter;
    Integer scopeBegin;
    Integer scopeEnd;

    public Integer getScopeBegin() {
	return scopeBegin;
    }

    public Integer getScopeEnd() {
	return scopeEnd;
    }

    public CompletionCandidate(String name, String type, ArrayList<String> parameter) {
	this.name = name;
	this.type = type;
	this.parameter = parameter;
    }

    public CompletionCandidate(String name, String type, Integer begin, Integer end) {
	this.name = name;
	this.type = type;
	this.scopeBegin = begin;
	this.scopeEnd = end;
    }

    public String getName() {
	return this.name;
    }

    public String getType() {
	return this.type;
    }

    public ArrayList<String> getParameter() {
	return this.parameter;
    }

    public String print() {
	String result = String.format("%s!%s!", name, type);

	if (parameter == null)
	    return result;

	result = String.format("%s(", result);
	for (String s:parameter) {
	    result = String.format("%s%s,", result, s);
	}

	result = result.replaceAll(",+$", "");
	result = result+")";
	return result;
    }
}
