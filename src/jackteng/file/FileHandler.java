package jackteng.file;

import java.io.*;

/**
 * <p>Description: A file handler extending <code>java.io.File</code>.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Jei-Wen Teng
 * @version 1.0
 */
public class FileHandler extends File {

    public FileHandler(File parent, String child) {
	super(parent, child);
    }

    public FileHandler(String pathname) {
	super(pathname);
    }

    public FileHandler(String parent, String child) {
	super(parent, child);
    }

    public void showFileContent() {
	FileInputStream fis = null;
	byte[] b = new byte[1];

	try {
	    fis = new FileInputStream(this);
	    for (int i = fis.read(b); i != -1; i = fis.read(b)) {
		if (i > 0) {
		    System.out.write(b);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (fis != null) {
		try {
		    fis.close();
		} catch (IOException ioe) {
		    ioe.printStackTrace();
		}
	    }
	}
    }

    public boolean copyFile(String pathname) {
	return copyFile(pathname, false);
    }

    public boolean copyFile(String pathname, boolean append) {
	boolean result = false;

	if (this.isFile()) {
	    File destFile = new File(pathname);
	    FileInputStream fis = null;
	    FileOutputStream fos = null;
	    byte[] b = new byte[1];

	    try {
		fis = new FileInputStream(this);
		if (destFile.isDirectory()) {
		    destFile = new File(destFile, this.getName());
		}
		fos = new FileOutputStream(destFile, append);
		for (int i = fis.read(b); i != -1; i = fis.read(b)) {
		    if (i > 0) {
			fos.write(b);
		    }
		}
		result = true;
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (fis != null) {
		    try {
			fis.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		}
		if (fos != null) {
		    try {
			fos.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		}
	    }
	}

	return result;
    }

    public boolean write(String str) {
	return write(str, null, false);
    }

    public boolean write(String str, String enc) {
	return write(str, enc, false);
    }

    public boolean write(String str, boolean append) {
	return write(str, null, append);
    }

    public boolean write(String str, String enc, boolean append) {
	boolean result = false;

	if (!this.isDirectory()) {
	    FileOutputStream fos = null;

	    try {
		fos = new FileOutputStream(this.getPath(), append);
		byte[] strb = null;
		if (enc != null) {
		    strb = str.getBytes(enc);
		} else {
		    strb = str.getBytes();
		}
		fos.write(strb);
		result = true;
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (fos != null) {
		    try {
			fos.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		}
	    }
	}

	return result;
    }

    public boolean println(String str) {
	return println(str, false);
    }

    public boolean println(String str, boolean append) {
	boolean result = false;

	if (!this.isDirectory()) {
	    FileOutputStream fos = null;
	    PrintWriter pw = null;

	    try {
		fos = new FileOutputStream(this.getPath(), append);
		pw = new PrintWriter(fos, true);
		pw.println(str);
		result = true;
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (fos != null) {
		    try {
			fos.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		}
		if (pw != null) {
		    pw.close();
		}
	    }
	}

	return result;
    }

    public boolean print(char c) {
	return print(c, false);
    }

    public boolean print(char c, boolean append) {
	boolean result = false;

	if (!this.isDirectory()) {
	    FileOutputStream fos = null;
	    PrintWriter pw = null;

	    try {
		fos = new FileOutputStream(this.getPath(), append);
		pw = new PrintWriter(fos, true);
		pw.print(c);
		pw.flush();
		result = true;
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (fos != null) {
		    try {
			fos.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		}
		if (pw != null) {
		    pw.close();
		}
	    }
	}

	return result;
    }

    public Object readObject() {
	Object result = null;

	if (this.isFile()) {
	    FileInputStream fis = null;
	    ObjectInputStream ois = null;

	    try {
		fis = new FileInputStream(this);
		ois = new ObjectInputStream(fis);
		result = ois.readObject();
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (fis != null) {
		    try {
			fis.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		}
		if (ois != null) {
		    try {
			ois.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		}
	    }
	}

	return result;
    }

    public boolean writeObject(Object obj) {
	return writeObject(obj, false);
    }

    public boolean writeObject(Object obj, boolean append) {
	boolean result = false;

	if (!this.isDirectory()) {
	    FileOutputStream fos = null;
	    ObjectOutputStream oos = null;

	    try {
		fos = new FileOutputStream(this.getPath(), append);
		oos = new ObjectOutputStream(fos);
		oos.writeObject(obj);
		oos.flush();
		result = true;
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (fos != null) {
		    try {
			fos.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		}
		if (oos != null) {
		    try {
			oos.close();
		    } catch (IOException ioe) {
			ioe.printStackTrace();
		    }
		}
	    }
	}

	return result;
    }
}
