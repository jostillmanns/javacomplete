package me.tillmanns.javacomplete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.pmw.tinylog.Logger;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.writers.FileWriter;
import org.pmw.tinylog.writers.ConsoleWriter;

import java.lang.StringBuilder;

import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.newsclub.net.unix.AFUNIXServerSocket;
import java.net.Socket;

import javassist.ClassPool;
import javassist.NotFoundException;

class JavaComplete {
    private static final String API_COMPLETE = "complete";
    private static final String API_CLEAN_IMPORTS = "cleanimports";
    private static final String API_ADD_IMPORT = "addimport";
    private static final String API_DEFINITION = "definition";

    public static final String ENV_CLASSPATH = "JAVACOMPLETEPATH";
    public static final String ENV_JAVASRC = "JAVASRC";

    private ImportAdder adder;
    private ClassPool pool;
    private HashMap classMap;

    public static void main(String[] args) {
	Configurator.defaultConfig()
	    .writer(new ConsoleWriter())
	    .level(Level.TRACE)
	    .formatPattern("{level}: {message}")
	    .activate();

	JavaComplete jc = new JavaComplete();
	jc.initCompleteLoop();
    }

    private void initClassPool() throws NotFoundException {
	String reference = System.getenv(ENV_CLASSPATH);
	pool = ClassPool.getDefault();
	classMap = new HashMap();
	for(String s:reference.split(":")) {
	    File f = new File(s);
	    classMap.put(s, f.lastModified());
	    pool.appendClassPath(s);
	}
    }

    private void updateClassPool() throws NotFoundException {
	Iterator i = classMap.entrySet().iterator();
	while (i.hasNext()) {
	    Entry pairs = (Entry)i.next();

	    String name = (String)pairs.getKey();
	    Long time = (Long)pairs.getValue();

	    File f = new File(name);
	    if (f.lastModified() == time)
		continue;

	    initClassPool();
	    return;
	}
    }
    
    public void initCompleteLoop() {

	SocketHandler socketHandler = null;
	AFUNIXServerSocket server = null;
	adder = initImportAdder();
	try {
	    initClassPool();
	} catch (NotFoundException e) {
	    Logger.debug("unable to load classpool");
	    return;
	}

	try {
	    socketHandler = new SocketHandler();
	    server = socketHandler.initServer();

	    while (true) {
		try {
		    acceptRequest(socketHandler, server);
		} catch (Exception e) {
		    Logger.debug("could not complete at point: {0}", e);
		}
	    }

	} catch (Exception e) {
	    Logger.trace(e);
	} finally {
	    close(server);
	}
    }

    private static ImportAdder initImportAdder() {
	ImportAdder adder = null;
	try {
	    adder = new ImportAdder();
	} catch (FileNotFoundException e) {
	    Logger.debug("unable to find java src file");
	} catch (IOException e) {
	    Logger.debug("unable to read java src file");
	}

	return adder;
    }

    private void acceptRequest(SocketHandler socketHandler, AFUNIXServerSocket server) throws Exception {
	Socket socket;
	Request request;
	// ClassPool pool;

	// pool = ClassPool.getDefault();
	updateClassPool();
	String env = System.getenv(ENV_CLASSPATH);
	if (env != null)
	    pool.insertClassPath(env);

	socket = server.accept();
	Logger.info("acceppted new client");

	request = socketHandler.readSocket(socket);
	Logger.debug(request.getApicall());

	switch(request.getApicall()) {
	case API_COMPLETE:
	    new Complete(request, socket, pool);
	    break;

	case API_CLEAN_IMPORTS:
	    new ImportCleaner(request, socket);
	    break;

	case API_ADD_IMPORT:
	    if (adder == null)
		break;

	    adder.writePackage(request, socket);

	case API_DEFINITION:
	    new Definition(request, socket, pool);

	default:
	    break;
	}

	socket.close();
    }

    private static void close(Closeable c) {
	try {
	    if(c != null) {
		c.close();
	    }
	} catch(Exception e) {
	    Logger.trace(e);
	}
    }
}
