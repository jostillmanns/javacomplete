package me.tillmanns.javacomplete;

import java.util.ArrayList;

class CompletionCandidate {
    String name;
    String type;
    ArrayList<String> parameter;

    public CompletionCandidate(String name, String type, ArrayList<String> parameter) {
	this.name = name;
	this.type = type;
	this.parameter = parameter;
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
