package graphtester;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


/*
 * Using Jsoup, According to the javadocs, 
 * the default timeout for an org.jsoup.Connection is 30 seconds.
 *Hence we have to manually set a timeout for it to run,
 *however it will still continue to poll
 *
 *We will use concurrent linked queue
 *https://www.javacodex.com/Concurrency/ConcurrentLinkedQueue-Example
 *based on FIFO, head of queue is the elem that has been in 
 *the queue for the longest time
 *tail of queue shortest
 *we want to put and take quote data off the queue using 2 spawned threads
 */
public class tester6{
	
	public static void main (String args[]) {
	    System.out.println("Starting quote pull...");
	    ConcurrentLinkedQueue<Double> queue = new ConcurrentLinkedQueue<Double>();
	    //instantiate runnable object
	    Runnable runnable = new AddToQueue(queue);
	    //instantiate new thread
	    Thread thread = new Thread(runnable);
	    //start thread
	    thread.start();
	}
}

class AddToQueue implements Runnable{
	private static String quoteName = "ripple";
	private ExecutorService executor;
	private ArrayList<Double> quotePriceArray;
	//declare concurrentlinkedqueue
	ConcurrentLinkedQueue<Double> queue;
	AddToQueue(ConcurrentLinkedQueue<Double> queue){
		this.queue=queue;
	}

	@Override
	public void run() {
		double sum=0;
		try {
			queue.add(retrieveCoinInfo(quoteName));
			System.out.println(retrieveCoinInfo(quoteName));
			
			Thread.sleep(5000);
		    executor = Executors.newCachedThreadPool();
            executor.execute(this);
		}catch (InterruptedException ex) {

            Logger.getLogger(tester6.class.getName()).log(Level.SEVERE, null, ex);
        }
		for(double x:queue) {
			sum=sum+x;
		}
		System.out.println("The sum is " + sum);
		System.out.println("The array is " + queue);
		
		
	}
	
	public double retrieveCoinInfo(String Quote) {
		double quotePrice;
//		System.out.print(new SimpleDateFormat("dd-MMMM-YYYY HH:mm:ss").format(Calendar.getInstance().getTime()));
//		System.out.println(" SGT TIME");
		
		try {
			Document doc = Jsoup.connect("https://coinmarketcap.com/currencies/" + Quote).timeout(0).userAgent("Mozilla").get();
			if(doc==null) {
				System.out.println(Quote + " is invalid. Removed from List");
			}
			Elements x = doc.select("span#quote_price");
			//get the root element
				if(x==null) {
					quotePrice = -1;
					return quotePrice;
				}else {
					String line = x.text();
					String currency;
					int start = line.indexOf("U")-9;
					int end = line.indexOf("U")-1;
//					System.out.println("The Price of " + Quote.toUpperCase() + " is " + line.substring(start,end));
					quotePrice = Double.parseDouble((line.substring(start,end)));
					
				}
			}catch(Exception e) {
				e.printStackTrace();
				quotePrice = -1;
			}
		return quotePrice;
	}
}