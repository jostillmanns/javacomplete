package me.tillmanns.javacomplete;

import org.pmw.tinylog.Logger;

public class ExpressionParser {
    private ExpressionParser() {}

    public static String removeParenBody(String s) {
	int open = 0;
	int close = 0;

	String result = "";
	for (int i = 0; i < s.length(); i++) {
	    if (open == close) {
		open = 0;
		close = 0;
	    }

	    if (s.charAt(i) == '(') {
		open++;
	    }

	    if (s.charAt(i) == ')') {
		close++;
	    }

	    if (close != open) {
		continue;
	    }

	    result = result + s.charAt(i);
	}
	
	return result.replaceAll("(\\))", "()");
    }

    public static String parse(String expression) {
	for(Regexp p:Regexp.getValues()) {

	    if (!expression.matches(p.getRegexp()))
	    	continue;

	    if (p.getId() == -1)
	    	return "";

	    Logger.debug("matches: {0}", p);

	    return expression.replaceAll(p.getRegexp(), String.format("$%s", p.getId()));
	}

	return expression;
    }

    public static String lastElement(String expression, String delimitor) {
	String[] elements = expression.split(delimitor);
	return elements[elements.length-1].trim();
    }

    public static String lastElement(String expression) {
	static String r = "\\*|/|\\%|\\+|-|<<|>>|<|>|<=|=>|instanceof|==|\\!=|&|\\^|\\||&&|\\|\\||\\?:";
	return lastElement(expression, r);
    }
}
