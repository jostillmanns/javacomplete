package me.tillmanns.javacomplete;

public enum Regexp {
    ASSIGN ("(.*)[=<>!]+\\s*(new\\s*)?(\\w*)(\\s*\\(.*\\))?", 3),
    INSTANTIATE ("\\s*(new)\\s*(\\w*)\\s*(<\\s*\\w*\\s*>)*\\s*(\\(.*\\))", 2),
    METHOD ("\\s*(\\w*)\\s*(\\(.*\\))", 1),
    ARR_ELEM ("\\s*(\\w*)\\[\\d*\\]", 1),
    REDUNDANT_KEYWORD ("(return|case)\\s*(\\w*)", 2),
    LIST ("(\\w*,)+(\\w*)",2 ),
    NEW_STATEMENT ("\\s*(new)\\s*$", -1),
    THIS_STATEMENT ("^(this\\.)(.*)", 2);

    private final String regexp;
    private final int id;

    Regexp(String regexp, int id) {
	this.regexp = regexp;
	this.id = id;
    }

    public String getRegexp() {
	return regexp;
    }

    public int getId() {
	return id;
    }

    public static Regexp[] getValues() {
	return Regexp.values();
    }
}
