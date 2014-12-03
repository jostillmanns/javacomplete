package me.tillmanns.javacomplete;

import org.pmw.tinylog.Logger;

import java.net.Socket;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.util.ArrayList;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.FieldDeclaration;

import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;

public class ImportCleaner {
    Request request;

    public ImportCleaner(Request request, Socket socket) throws Exception {
	String importList = "";
	request.setExpression("");
	request.setPrefix("");

	InputStream in = new ByteArrayInputStream(request.getBuffer().getBytes());
	importList = importsToClean(in);

	Logger.debug("clean!");
    }

    private String importsToClean(InputStream in) throws Exception {
	StringBuilder typeList = new StringBuilder();
	JavaCompleteCompilationUnit cu = new JavaCompleteCompilationUnit(in);	
	
	return "";
    }
}
