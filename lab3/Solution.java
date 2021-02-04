import java.util.Date;
import java.util.concurrent.Semaphore;

public class Solution
{
	public static void main(String args[]) 
	{
		//instantiate (create) buffer shared by Producer & Consumer
		Buffer sharedBuffer = new BoundedBuffer();
		// create the producer and consumer threads
		Thread producerThread = new Thread(new Producer(sharedBuffer));
		Thread consumerThread = new Thread(new Consumer(sharedBuffer));
		//start() method allocates memory for a new thread in the JVM, 
		//and calls the run() method    		
		producerThread.start();
		consumerThread.start();               
	}
}

/**
 * An interface for buffers
 *
 */

interface Buffer
{
	/**
	 * insert an item into the Buffer.
	 * Note this may be either a blocking
	 * or non-blocking operation.
	 */
	public abstract void insert(Object item);

	/**
	 * remove an item from the Buffer.
	 * Note this may be either a blocking
	 * or non-blocking operation.
	 */
	public abstract Object remove();
}

/**
 * This program implements the bounded buffer using shared memory.
 */

class BoundedBuffer implements Buffer
{ 

	private static final int BUFFER_SIZE = 3; //max size of buffer array
	private int count; //number of items currently in the buffer
	private int in;   // points to the next free position in the buffer
	private int out;  // points to the first filled position in the buffer
	private Object[] buffer; //array of Objects
	private Semaphore mutex; //provides limited access to the buffer (mutual exclusion)
	private Semaphore empty; //keep track of the number of empty elements in the array
	private Semaphore full; //keep track of the number of filled elements in the array

	public BoundedBuffer()
	{
		// buffer is initially empty
		count = 0;
		in = 0;
		out = 0;
		buffer = new Object[BUFFER_SIZE];
		mutex = new Semaphore(1); //1 for mutual exclusion
		empty = new Semaphore(BUFFER_SIZE); //array begins with all empty elements
		full = new Semaphore(0); //array begins with no elements
	}

	// producer calls this method
	public void insert(Object item) 
	{
		/*
                 while (count == BUFFER_SIZE){
                  // do nothing, if the buffer array cannot be used (because full)
                 }
		 */
		try
		{
			empty.acquire(); //keep track of number of empty elements (value--)
			//This provides synchronization for the producer,
			//because this makes the producer stop running when buffer is full
			mutex.acquire(); //mutual exclusion
		}
		catch (InterruptedException e) 
		{ 
			System.out.println("ERROR in insert(): " + e);
		} 

		// add an item to the buffer
		++count;
		buffer[in] = item;
		//modulus (%) is the remainder of a division
		//for example, 0%3=0, 1%3=1, 2%3=2, 3%3=0, 4%3=1, 5%3=2
		in = (in + 1) % BUFFER_SIZE; 

		//buffer information feedback
		if (count == BUFFER_SIZE)
		{
			System.out.println("BUFFER FULL "
					+ "Producer inserted \"" + item 
					+ "\" count=" + count + ", "
					+ "in=" + in + ", out=" + out);
		}
		else
		{
			System.out.println("Producer inserted \"" + item 
					+ "\" count=" + count + ", "
					+ "in=" + in + ", out=" + out);
		}

		mutex.release(); //mutual exclusion
		full.release(); //keep track of number of elements (value++)      	
		//If buffer was empty, then this wakes up the Consumer
	}

	// consumer calls this method
	public Object remove()
	{
		Object item=null;
		/*
                 while (count == 0){ 
                 //if nothing in the buffer, then do nothing
                 //the buffer array cannot be used (because empty)
                 }
		 */

		try
		{
			full.acquire(); //keep track of number of elements (value--)
			//This provides synchronization for consumer, 
			//because this makes the Consumer stop running when buffer is empty

			mutex.acquire(); //mutual exclusion
		}
		catch (InterruptedException e) 
		{ 
			System.out.println("ERROR in try(): " + e);
		} 

		// remove an item from the buffer
		--count;
		item = buffer[out];
		//modulus (%) is the remainder of a division
		//for example, 0%3=0, 1%3=1, 2%3=2, 3%3=0, 4%3=1, 5%3=2   
		out = (out + 1) % BUFFER_SIZE;

		//buffer information feedback
		if (count == 0)
		{
			System.out.println("BUFFER EMPTY "
					+ "Consumer removed \"" + item 
					+ "\" count=" + count + ", "
					+ "in=" + in + ", out=" + out);
		}
		else
		{
			System.out.println("Consumer removed \"" + item 
					+ "\" count=" + count + ", "
					+ "in=" + in + ", out=" + out);
		}

		mutex.release(); //mutual exclusion
		empty.release(); //keep track of number of empty elements (value++)      	  
		//if buffer was full, then this wakes up the Producer 
		return item;
	}
}


/**
 * This is the producer thread for the bounded buffer problem.
 */

class Producer implements Runnable
{
	private  Buffer buffer;

	public Producer(Buffer b) 
	{
		buffer = b;
	}

	public void run()
	{
		Date message;
		while (true) 
		{
			System.out.println("Producer napping");
			SleepUtilities.nap();
			// produce an item & enter it into the buffer
			message = new Date();      
			System.out.println("Producer produced \"" + message + "\"");
			buffer.insert(message);
		}
	}
}

/**
 * This is the consumer thread for the bounded buffer problem.
 */

class Consumer implements Runnable
{
	private Buffer buffer;

	public Consumer(Buffer b) 
	{ 
		buffer = b;
	}

	public void run()
	{
		Date message = null;
		while (true){
			System.out.println("Consumer napping");
			SleepUtilities.nap(); 
			// consume an item from the buffer
			System.out.println("Consumer wants to consume");
			message = (Date)buffer.remove();
			System.out.println("Consumer consumed \"" + message + "\"");
		}
	}
}

/**
 * Utilities for causing a thread to sleep.
 * Note, we should be handling interrupted exceptions
 * but choose not to do so for code clarity.
 *
 */

class SleepUtilities
{
	private static final int NAP_TIME = 5; //max nap time in seconds

	/**
	 * Nap between zero and NAP_TIME seconds.
	 */
	public static void nap() 
	{
		nap(NAP_TIME);
	}

	/**
	 * Nap between zero and duration seconds.
	 */
	public static void nap(int duration) 
	{
		int sleeptime = (int) (NAP_TIME * Math.random() );
		System.out.println("Nap for " + sleeptime + " seconds"); 
		//Causes the currently executing thread to sleep (cease execution) 
		//for the specified number of milliseconds, 
		//subject to the precision and accuracy of system timers and schedulers.
		try { Thread.sleep(sleeptime*1000); }
		catch (InterruptedException e) 
		{
			//method sleep() throws InterruptedException - if any thread has interrupted the current thread. 
			System.out.println("ERROR in nap(): " + e);
		}
	}
}                   




