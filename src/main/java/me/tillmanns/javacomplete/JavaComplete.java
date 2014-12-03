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

import org.newsclub.net.unix.AFUNIXServerSocket;
import java.net.Socket;

import java.net.Socket;

class JavaComplete {
    private static final String API_COMPLETE = "complete";
    private static final String API_CLEAN_IMPORTS = "cleanimports";

    public static void main(String[] args) {

	Configurator.defaultConfig()
	    .writer(new ConsoleWriter())
	    .level(Level.TRACE)
	    .formatPattern("{level}: {message}")
	    .activate();

	SocketHandler socketHandler = null;
	AFUNIXServerSocket server = null;

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

    private static void acceptRequest(SocketHandler socketHandler, AFUNIXServerSocket server) throws Exception {
	Socket socket;
	Request request;
	
	socket = server.accept();
	Logger.info("acceppted new client");

	request = socketHandler.readSocket(socket);

	switch(request.getApicall()) {
	case API_COMPLETE:
	    new Complete(request, socket);	    
	    break;

	case API_CLEAN_IMPORTS:
	    
	    new ImportCleaner(request, socket);
	    
	    break;

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
