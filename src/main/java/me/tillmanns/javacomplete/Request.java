package me.tillmanns.javacomplete;

import java.io.File;

public class Request {
    private File file;
    private String expression;
    private String prefix;
    private String apicall;
    private Integer line;
    private String buffer;

    public void setFile(File in) {
	this.file = in;
    }

    public void setExpression(String in) {
	this.expression = in;
    }

    public void setPrefix(String in) {
	this.prefix = in;
    }

    public void setApicall(String in) {
	this.apicall = in;
    }

    public void setLine(Integer in) {
	this.line = in;
    }

    public void setBuffer(String in) {
	this.buffer = in;
    }

    public File getFile() {
	return this.file; 
    }

    public String getExpression() {
	return this.expression;
    }

    public String getPrefix() {
	return this.prefix;
    }

    public String getApicall() {
	return this.apicall;
    }

    public Integer getLine() {
	return this.line;
    }

    public String getBuffer() {
	return this.buffer;
    }

    public String toString() {
	return String.format("\nfile: %s\nexpression: %s\nprefix: %s\napicall: %s\nline: %s",
			     file, expression, prefix, apicall, line);
    }
}
