package me.tillmanns.javacomplete;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.net.Socket;

import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import javax.json.JsonReader;
import javax.json.JsonObject;
import javax.json.Json;

import org.pmw.tinylog.Logger;

public class SocketHandler {
    private static String KEY_FILE = "file";
    private static String KEY_EXPRESSION = "expression";
    private static String KEY_PREFIX = "prefix";
    private static String KEY_APICALL = "apicall";
    private static String KEY_LINE = "line";
    private static String KEY_BUFFER = "buffer";

    public SocketHandler() {}

    public AFUNIXServerSocket initServer() throws Exception{
	File socketFile = new File(new File(System.getProperty("java.io.tmpdir")), "javacomplete.sock");
	AFUNIXServerSocket server = AFUNIXServerSocket.newInstance();
	server.bind(new AFUNIXSocketAddress(socketFile));

	return server;
    }

    public void writeSocket(Socket socket, byte[] out) throws IOException {
	OutputStream os = socket.getOutputStream();

	try {
	    os.write(out);
	    os.flush();
	} catch(IOException e) {
	    throw e;
	} finally {
	    os.close();
	}
    }

    public Request readSocket(Socket socket) throws Exception {
	Request request = new Request();
	JsonReader reader = null;

	try {
	    reader = Json.createReader(socket.getInputStream());
	    JsonObject object = reader.readObject();
	    request.setFile(new File(object.getString(KEY_FILE)));
	    request.setExpression(object.getString(KEY_EXPRESSION));
	    request.setPrefix(object.getString(KEY_PREFIX));
	    request.setApicall(object.getString(KEY_APICALL));
	    request.setLine(object.getInt(KEY_LINE));
	    request.setBuffer(object.getString(KEY_BUFFER));	    

	    return request;
	} catch(Exception e) {
	    throw e;
	} finally {
	    if (reader != null) {
		reader.close();
	    }
	}
    }
}
