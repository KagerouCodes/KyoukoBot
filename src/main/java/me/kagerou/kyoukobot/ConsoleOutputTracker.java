package main.java.me.kagerou.kyoukobot;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleOutputTracker
{	//a class for tracking console output, needed for manual reconnecting and grabbing latest logs
	private boolean newStuff; //a flag showing if anything was printed since the latest newOutput() check, if not, manual (re)connect is needed
	final static int MaxBufferSize = 50000;
	private char[] LastOutput = new char[MaxBufferSize]; //a buffer storing the latest output
	private PrintStream ps;
	int first_index, buffer_size;

    public ConsoleOutputTracker()
    {
    	newStuff = false;
    	first_index = buffer_size = 0;
    	ps = new PrintStream(new OutputStreamTracker(this));
    	System.setOut(ps);
    	System.setErr(ps);
    }
    
    public void setFlag(boolean flag)
    {
    	newStuff = flag;
    }
    
    public boolean newOutput()
    {
    	boolean result = newStuff;
    	newStuff = false;
    	return result;
    }
    
    synchronized void push(char ch) //memorises a char in LastOutput, cycles through the buffer if maximum size is reached
    {
    	LastOutput[(first_index + buffer_size) % MaxBufferSize] = ch;
    	if (buffer_size != MaxBufferSize)
    		buffer_size++;
    	else
    		first_index = (first_index + 1) % MaxBufferSize;
    }
    
    synchronized void pushCodePoint(int cp)
    {
    	try {
    		for (char ch: Character.toChars(cp))
    			push(ch);
    	}
    	catch (IllegalArgumentException e)
    	{
    		//e.printStackTrace(); //printing anything while failing to print would be a bad idea 
    	}
    }
    
    synchronized public String getLastOutput()
    {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < buffer_size; i++)
    		sb.append(LastOutput[(first_index + i) % MaxBufferSize]);
    	return sb.toString();
    }
    
    synchronized public void stop()
    {
    	ps.close();
    	ps = null;
    }

    class OutputStreamTracker extends OutputStream
    { //a redefined OutputStream incoprorating the tracker
    	ConsoleOutputTracker tracker;
    	PrintStream old;
    	PrintStream old_err;

        public OutputStreamTracker(ConsoleOutputTracker cot) {
            tracker = cot;
            old = System.out;
            old_err = System.err;
        }

        public void write(int b) throws IOException {
            old.write(b);
            tracker.setFlag(true);
            tracker.pushCodePoint(b);
        }

        public void flush() throws IOException {
            old.flush();
        }

        public void close() throws IOException {
            super.close();
            System.setOut(old);
            System.setErr(old_err);
        }
    }
}