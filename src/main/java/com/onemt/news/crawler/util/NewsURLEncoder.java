package com.onemt.news.crawler.util;

import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException ;
import java.util.BitSet;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.security.action.GetPropertyAction;

public class NewsURLEncoder {
    static BitSet dontNeedEncoding;
    static final int caseDiff = ('a' - 'A');
    static String dfltEncName = null;

    static {

	dontNeedEncoding = new BitSet(256);
	int i;
	for (i = 'a'; i <= 'z'; i++) {
	    dontNeedEncoding.set(i);
	}
	for (i = 'A'; i <= 'Z'; i++) {
	    dontNeedEncoding.set(i);
	}
	for (i = '0'; i <= '9'; i++) {
	    dontNeedEncoding.set(i);
	}
	dontNeedEncoding.set(' '); /* encoding a space to a + is done
				    * in the encode() method */
	dontNeedEncoding.set('-');
	dontNeedEncoding.set('_');
	dontNeedEncoding.set('.');
	dontNeedEncoding.set('*');
	dontNeedEncoding.set(':');
	dontNeedEncoding.set('/');
	//新增% 2017/1/12
	dontNeedEncoding.set('%');
	dontNeedEncoding.set('?');
	dontNeedEncoding.set('=');
	dontNeedEncoding.set('&');
    	dfltEncName = (String)AccessController.doPrivileged (
	    new GetPropertyAction("file.encoding")
    	);
    }

    /**
     * You can't call the constructor.
     */
    private NewsURLEncoder() { }

    @Deprecated
    public static String encode(String s) {

	String str = null;

	try {
	    str = encode(s, dfltEncName);
	} catch (UnsupportedEncodingException e) {
	    // The system should always have the platform default
	}

	return str;
    }

    public static String encode(String s, String enc) 
	throws UnsupportedEncodingException {

	boolean needToChange = false;
        StringBuffer out = new StringBuffer(s.length());
	Charset charset;
	CharArrayWriter charArrayWriter = new CharArrayWriter();

	if (enc == null)
	    throw new NullPointerException("charsetName");

	try {
	    charset = Charset.forName(enc);
	} catch (IllegalCharsetNameException e) {
            throw new UnsupportedEncodingException(enc);
        } catch (UnsupportedCharsetException e) {
	    throw new UnsupportedEncodingException(enc);
	}

	for (int i = 0; i < s.length();) {
	    int c = (int) s.charAt(i);
	    //System.out.println("Examining character: " + c);
	    if (dontNeedEncoding.get(c)) {
		if (c == ' ') {
		    c = '+';
		    needToChange = true;
		}
		//System.out.println("Storing: " + c);
		out.append((char)c);
		i++;
	    } else {
		// convert to external encoding before hex conversion
		do {
		    charArrayWriter.write(c);
		    /*
		     * If this character represents the start of a Unicode
		     * surrogate pair, then pass in two characters. It's not
		     * clear what should be done if a bytes reserved in the 
		     * surrogate pairs range occurs outside of a legal
		     * surrogate pair. For now, just treat it as if it were 
		     * any other character.
		     */
		    if (c >= 0xD800 && c <= 0xDBFF) {
			/*
			  System.out.println(Integer.toHexString(c) 
			  + " is high surrogate");
			*/
			if ( (i+1) < s.length()) {
			    int d = (int) s.charAt(i+1);
			    /*
			      System.out.println("\tExamining " 
			      + Integer.toHexString(d));
			    */
			    if (d >= 0xDC00 && d <= 0xDFFF) {
				/*
				  System.out.println("\t" 
				  + Integer.toHexString(d) 
				  + " is low surrogate");
				*/
			        charArrayWriter.write(d);
				i++;
			    }
			}
		    } 
		    i++;
		} while (i < s.length() && !dontNeedEncoding.get((c = (int) s.charAt(i))));

		charArrayWriter.flush();
		String str = new String(charArrayWriter.toCharArray());
		byte[] ba = str.getBytes(charset);
		for (int j = 0; j < ba.length; j++) {
		    out.append('%');
		    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
		    // converting to use uppercase letter as part of
		    // the hex value if ch is a letter.
		    if (Character.isLetter(ch)) {
			ch -= caseDiff;
		    }
		    out.append(ch);
		    ch = Character.forDigit(ba[j] & 0xF, 16);
		    if (Character.isLetter(ch)) {
			ch -= caseDiff;
		    }
		    out.append(ch);
		}
		charArrayWriter.reset();
		needToChange = true;
	    }
	}

	return (needToChange? out.toString() : s);
    }
}
