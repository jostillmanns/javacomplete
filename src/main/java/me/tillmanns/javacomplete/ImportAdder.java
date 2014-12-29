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
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ArrayList;

import org.pmw.tinylog.Logger;

class ImportAdder {
    HashMap classmap;

    public static final String EXT_JAVA = "java";
    public static final String EXT_CLASS = "class";
    public static final String PATH_DELIM = "/";

    public ImportAdder() throws FileNotFoundException, IOException {
	classmap = new HashMap();
	initStdLib();
	initJars();
    }

    private void initJars() throws FileNotFoundException, IOException{
	String completePath = System.getenv(JavaComplete.ENV_CLASSPATH);
	if (completePath == null) {
	    return;
	}

	JarFile jarFile;
	Enumeration<? extends JarEntry> entries;
	String name;
	String path;


	for (String s:completePath.split(":")) {
	    if (!s.endsWith(".jar")) {
		continue;
	    }
	    jarFile = new JarFile(s);
	    entries = jarFile.entries();

	    while(entries.hasMoreElements()) {
		JarEntry entry = entries.nextElement();
		if (!entry.getName().endsWith(EXT_CLASS))
		    continue;

		name = ExpressionParser.lastElement(removeExt(entry.getName(), EXT_CLASS), PATH_DELIM);
		path = entry.getName().replaceAll(PATH_DELIM, ".");
		path = removeExt(path, EXT_CLASS);
		put(name, path);
	    }
	}
    }

    private void put(String name, String path) {
	ArrayList<String> vals;
	if (classmap.get(name) != null) {
	    vals = (ArrayList) classmap.get(name);
	    vals.add(path);
	    classmap.put(name, vals);
	    return;
	}
	vals = new ArrayList<String>();
	vals.add(path);
	classmap.put(name, vals);
    }

    private void initStdLib() throws FileNotFoundException, IOException{
	String javasrc = System.getenv(JavaComplete.ENV_JAVASRC);
	if (javasrc == null) {
	    return;
	}
	ZipFile zipFile = new ZipFile(javasrc);

	Enumeration<? extends ZipEntry> entries = zipFile.entries();

	String name;
	String path;
	while(entries.hasMoreElements()) {
	    ZipEntry entry = entries.nextElement();

	    if (!entry.getName().endsWith(EXT_JAVA))
		continue;

	    name = ExpressionParser.lastElement(removeExt(entry.getName(), EXT_JAVA), PATH_DELIM);
	    path = entry.getName().replaceAll(PATH_DELIM, ".");
	    path = removeExt(path, EXT_JAVA);
	    put(name, path);
	}
    }

    public void writePackage(Request request, Socket socket) throws Exception {
	ArrayList<String> fullyqualified;
	String classname = request.getExpression();
	OutputStream out = socket.getOutputStream();
	out = socket.getOutputStream();
	StringBuilder sb = new StringBuilder();
	try {
	    fullyqualified = (ArrayList) classmap.get(request.getExpression());
	    sb.append(String.format("%s\n", fullyqualified.size()));
	    for(String s:fullyqualified) {
		sb.append(s+"\n");
	    }
	    out.write(sb.toString().getBytes());
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
