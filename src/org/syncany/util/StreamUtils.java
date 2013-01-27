package org.syncany.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
	public static void read(byte[] inp, InputStream is) throws IOException{
		int l = inp.length;
		
		for(int i = 0; i < l; i++){
			int b = is.read();
			
			if(b == -1)
				throw new CipherEOFException("END OF STREAM REACHED");
			
			inp[i] = (byte)b;
		}
	}
	
	public static void read(byte[] inp, int s, int l, InputStream is) throws IOException{
		if(inp.length != l){
			throw new IOException("Buffer to small");
		}
		
		for(int i = 0; i < l; i++){
			int b = is.read();
			
			if(b == -1)
				throw new CipherEOFException("END OF STREAM REACHED");
			
			inp[s + i] = (byte)b;
		}
	}
	
	public static class CipherEOFException extends EOFException {		
		public CipherEOFException(String msg){
			super(msg);
		}

	}
}