package activity.web.job;

import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.slf4j.LoggerFactory;

import activity.web.manager.InteractionSummaryProcesser;
import activity.web.manager.ActionMatcher;

public class InteractionSummaryJob implements StatefulJob{
	
	static Logger logger = Logger.getLogger(InteractionSummaryJob.class.getName());
	
	static
	{
		//System.loadLibrary( "opencv_java249" );
	}
	
	public InteractionSummaryJob()
	{
		//System.loadLibrary( "opencv_java249" );
	}
	
	@Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
    	//Thread.sleep(1000 * 10);
    	
    	logger.info("summary background job begin....");
    	//InteractionSummaryProcesser isp = new InteractionSummaryProcesser();
    	//isp.process();
    	
    	//ActionMatcher am = new ActionMatcher();
    	//am.process();
//    	
    } catch (Exception ex) {
    	ex.printStackTrace();
    	logger.info(ex.getStackTrace());
    }
  }
	
  public static void main(String[] args)
  {
	InteractionSummaryProcesser isp = new InteractionSummaryProcesser();
  	isp.process();
  	
  	ActionMatcher am = new ActionMatcher();
  	am.process();
  }
}
