package me.tillmanns.javacomplete;

import japa.parser.ParseException;
import japa.parser.ParseException;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.pmw.tinylog.Logger;

class Definition {
    Request request;
    Socket socket;
    ClassPool pool;
    JavaCompleteCompilationUnit cu;

    public Definition(Request request, Socket socket, ClassPool pool) throws IOException {
	this.request = request;
	this.socket = socket;
	this.pool = pool;
	OutputStream out;
	out = socket.getOutputStream();
	InputStream in = new ByteArrayInputStream(request.getBuffer().getBytes());
	String signature;
	Complete complete = new Complete(request, pool);

	try {
	    signature = complete.complete(in);
	    if (signature.length() == 0)
		throw new NotFoundException("unable to find signature");
	    Logger.debug(signature);
	    out.write(("1\n"+signature).getBytes());
	} catch (ParseException e) {
	    Logger.debug("parseexception, unable to get definition");
	    out.write("0\n".getBytes());
	} catch (NotFoundException e){
	    Logger.debug("notfoundexception, unable to get definition");
	    out.write("0\n".getBytes());
	} finally {
	    out.flush();
	    out.close();
	}
    }
}
