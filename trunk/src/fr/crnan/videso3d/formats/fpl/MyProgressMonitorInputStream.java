package fr.crnan.videso3d.formats.fpl;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import javax.swing.ProgressMonitorInputStream;

public class MyProgressMonitorInputStream extends ProgressMonitorInputStream {

	private int nread = 0;
	
	public MyProgressMonitorInputStream(Component parent, Object message,
			InputStream in) {
		super(parent, message, in);
	}

	@Override
	public int read(byte[]b, int off, int len) throws IOException{
		   int nr = in.read(b, off, Math.min(len, 32));
	       if (nr > 0){
	    	   super.getProgressMonitor().setProgress(nread += nr);
	       }
	        if (super.getProgressMonitor().isCanceled()) {
	            InterruptedIOException exc =
	                                    new InterruptedIOException("progress");
	           exc.bytesTransferred = nread;
	           throw exc;
	       }
	        return nr;
	}
}
