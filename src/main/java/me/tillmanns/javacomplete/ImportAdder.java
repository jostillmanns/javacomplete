package me.tillmanns.javacomplete;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.Socket;
import java.net.URL;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.util.HashMap;

import org.pmw.tinylog.Logger;

class ImportAdder {
    private static String EXT_JAVA = "java";
    HashMap classmap;

    public ImportAdder() throws FileNotFoundException, IOException {
	String javasrc = System.getenv("JAVASRC");
	if (javasrc == null) {
	    return;
	}
	ZipFile zipFile = new ZipFile(javasrc);

	Enumeration<? extends ZipEntry> entries = zipFile.entries();
	classmap = new HashMap();

	String name;
	String path;
	while(entries.hasMoreElements()) {
	    ZipEntry entry = entries.nextElement();

	    if (!entry.getName().endsWith(".java"))
		continue;

	    name = ExpressionParser.lastElement(removeExt(entry.getName(), EXT_JAVA), "/");
	    path = entry.getName().replaceAll("/", ".");
	    path = removeExt(path, EXT_JAVA);
	    classmap.put(name, path);
	}
    }

    public void writePackage(Request request, Socket socket) throws Exception {
	String fullyqualified;
	String classname = request.getExpression();
	OutputStream out = socket.getOutputStream();
	out = socket.getOutputStream();
	try {
	    fullyqualified = classmap.get(request.getExpression()).toString();
	    out.write(("1\n"+fullyqualified).getBytes());
	} catch (NullPointerException e)  {
	    Logger.debug("unable to insert import");
	    out.write("0\n".getBytes());
	} finally {
	    out.flush();
	    out.close();
	}
    }

    private static String removeExt(String path, String ext) {
	return path.replaceAll(String.format("(\\.%s$)", ext), "");
    }
}
