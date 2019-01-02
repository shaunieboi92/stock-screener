package graphtester;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

public class grapher2 extends Application{
	private Timeline timeline2;
	private NumberAxis xAxis;
	private static final int MAX_DATA_POINTS = 100;
	private ExecutorService executor;
	private Series series;
	private int xSeriesData = 0;
	private AddToQueue addToQueue;
	private ConcurrentLinkedQueue<Double> queue = new ConcurrentLinkedQueue<Double>();
	private static String quoteName = "ripple";

	
	//create initialization of axis
	private void initialization(Stage primaryStage) throws Exception{
			//NumberAxis(lower limit, upper limit, interval)
		  xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 100);
		  xAxis.setForceZeroInRange(false);
		  xAxis.setAutoRanging(false);
		  
		  
		  NumberAxis yAxis = new NumberAxis(0.3,0.5, 20);
		  yAxis.setAutoRanging(false);
		
		  final LineChart<Number, Number> sc = new LineChart<Number, Number>(xAxis, yAxis) {
		        // Override to remove symbols on each data point            
		        @Override
		        protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {

		        }
		    };
		    sc.setAnimated(false);
		    sc.setId("liveAreaChart");
		    sc.setTitle("RealTime Stock Chart");
		    series = new LineChart.Series<Number, Number>();
		    series.setName("Area Chart Series");
		    sc.getData().add(series);
		    primaryStage.setScene(new Scene(sc));

	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		initialization(primaryStage);
		primaryStage.show();
	    executor = Executors.newCachedThreadPool();
	   
	    addToQueue = new AddToQueue();
	    executor.execute(addToQueue);
	    prepareTimeline();
		
	}
	public static void main(String []args) {
		launch (args);
	}
	
	private class AddToQueue implements Runnable{

		@Override
		public void run() {
			double sum=0;
			try {
				queue.add(retrieveCoinInfo(quoteName));
				System.out.println(retrieveCoinInfo(quoteName));
				System.out.println(queue);

				Thread.sleep(5000);//if the fetch is slower than sleep, 
			    executor = Executors.newCachedThreadPool();
	            executor.execute(this);
	            
			}catch (InterruptedException ex) {

	            Logger.getLogger(grapher2.class.getName()).log(Level.SEVERE, null, ex);
	        }
			
			
			
		}
		
		public double retrieveCoinInfo(String Quote) {
			double quotePrice;
//			System.out.print(new SimpleDateFormat("dd-MMMM-YYYY HH:mm:ss").format(Calendar.getInstance().getTime()));
//			System.out.println(" SGT TIME");
			
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
//						System.out.println("The Price of " + Quote.toUpperCase() + " is " + line.substring(start,end));
						quotePrice = Double.parseDouble((line.substring(start,end)));
						
					}
				}catch(Exception e) {
					e.printStackTrace();
					quotePrice = -1;
				}
			return quotePrice;
		}
	}
	
	private void prepareTimeline() {
	    // Every frame to take any data from queue and add to chart

	    new AnimationTimer() {

	        @Override
	        public void handle(long now) {

	            addDataToSeries();

	        }
	    }.start();

	}
	
	private void addDataToSeries() {

	    for (int i = 0; i < 20; i++) {

	        if (queue.isEmpty()) {

	            break;
	        }
	       
	        series.getData().add(new LineChart.Data(xSeriesData++, queue.remove()));

	    }
	    // remove points to keep us at no more than MAX_DATA_POINTS
	    if (series.getData().size() > MAX_DATA_POINTS) {
	        series.getData().remove(0, series.getData().size() - MAX_DATA_POINTS);
	    }

	    // update 
	  
	    
	    
	}

}
