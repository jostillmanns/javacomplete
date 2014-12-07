package me.tillmanns.javacomplete;

import java.util.ArrayList;

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

class JavaComplete {
    private static final String API_COMPLETE = "complete";
    private static final String API_CLEAN_IMPORTS = "cleanimports";
    private static final String API_ADD_IMPORT = "addimport";

    private static final String path = "JAVACOMPLETEPATH";

    private ImportAdder adder;

    public static void main(String[] args) {

	Configurator.defaultConfig()
	    .writer(new ConsoleWriter())
	    .level(Level.TRACE)
	    .formatPattern("{level}: {message}")
	    .activate();

	JavaComplete jc = new JavaComplete();
	jc.initCompleteLoop();
    }

    public void initCompleteLoop() {

	SocketHandler socketHandler = null;
	AFUNIXServerSocket server = null;
	adder = initImportAdder();

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
	ClassPool pool;

	pool = ClassPool.getDefault();
	String env = System.getenv(path);
	if (env != null)
	    pool.insertClassPath(System.getenv(path));

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
