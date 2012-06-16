package ganglia;

import ganglia.gmetric.GMetric;
import ganglia.gmetric.GMetric.UDPAddressingMode;
import ganglia.gmetric.GMetricPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GMonitor {

	private static Logger log =
			Logger.getLogger(GMonitor.class.getName());
	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
	private List<GSampler> samplers = new ArrayList<GSampler>();
	private boolean daemon = true ;
	private GMetric gmetric = null ;
	private ThreadFactory daemonThreadGroup = new ThreadFactory() {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("Ganglia Sampling Thread");
			t.setDaemon(daemon);
			return t;
		}
	};
	/**
	 * Starts the sampling
	 */    
	public void start() {
		executor.setThreadFactory(daemonThreadGroup);

		for (GSampler s : samplers) {
			executor.scheduleAtFixedRate(s, s.getInitialDelay(), s.getDelay(), TimeUnit.SECONDS);
		}
	}
	/**
	 * Stops the sampling of MBeans
	 */
	public void stop() {
		executor.shutdown();
	}
	/**
	 * Adds a new MBeanSampler to be sampled
	 * @param s the MBeanSampler
	 */
	public void addSampler(GSampler s) {
		samplers.add(s);
		s.setPublisher( new GMetricPublisher(gmetric));
	}
	/**
	 * Returns the daemon status of the scheduler thread
	 * @return true if the scheduler thread is a daemon
	 */
	public boolean isDaemon() {
		return daemon;
	}
	/**
	 * Sets the scheduler daemon thread to be true/false.  This only has an 
	 * effect before the start method is called.
	 * @param daemon the requested scheduler daemon status
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}
	public GMetric getGmetric() {
		return gmetric;
	}
	public void setGmetric(GMetric gmetric) {
		this.gmetric = gmetric;
	}
	/**
	 * A log running, trivial main method for test purposes
	 * premain method
	 * @param args Not used
	 */
	public static void main(String[] args) throws Exception {
		
		GMonitor a = null ;
        try {
            a = new GMonitor();
            a.setGmetric(new GMetric("239.2.11.71", 8649, UDPAddressingMode.MULTICAST));
            a.addSampler(new CoreSampler());
            a.start();
        } catch ( Exception ex ) {
            log.severe("Exception starting GMonitor");
            ex.printStackTrace();
        }
        
		while( true ) {
			Thread.sleep(1000*60*5);
			System.out.println("Test wakeup");
		}
	}

}
