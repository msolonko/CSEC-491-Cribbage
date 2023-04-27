package cpsc474;

import java.io.PrintStream;

public class PrintStreamLogger implements Logger
{
    private PrintStream stream;

    public PrintStreamLogger(PrintStream stream)
    {
	this.stream = stream;
    }
    
    @Override
    public void log(String mess)
    {
	stream.println(mess);
    }	
}
