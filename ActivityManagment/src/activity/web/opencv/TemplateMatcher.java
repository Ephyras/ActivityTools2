package activity.web.opencv;

import java.util.List;

import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

public class TemplateMatcher {
	static Logger logger = Logger.getLogger(TemplateMatcher.class.getName());
	
	Size			dft_tpl_sz_com = new Size();
	Size			dft_sz_com = new Size();
	double			dft_thr_com;
	
	public List<Rect> match(ImageItem image, IconItem icon)
	{
		setAlphaTplMatchingParameters(image.getImage().size(), icon, 0.9);
		Mat F_I = new Mat();
		Mat F_I2 = new Mat();
		
		calcFrameDFTs(image.getImageGray(), F_I, F_I2);
		
		return icon.match(image.getImageGray(), F_I, F_I2);
	}
	
	void setAlphaTplMatchingParameters(Size image_sz, IconItem icon_item, double dft_thr_com_in)
	{
		if(!icon_item.getItemMask().empty())
		{
			icon_item.tightenMask();
		}

		dft_tpl_sz_com.width  = icon_item.getMsk_rect_tight().width;
		dft_tpl_sz_com.height = icon_item.getMsk_rect_tight().height;

		dft_sz_com.width  = Core.getOptimalDFTSize((int)(image_sz.width  + dft_tpl_sz_com.width  - 1));
		dft_sz_com.height = Core.getOptimalDFTSize((int)(image_sz.height + dft_tpl_sz_com.height - 1));

		dft_thr_com = dft_thr_com_in;

		icon_item.initDFTParameters(dft_sz_com, dft_tpl_sz_com, dft_thr_com);
	}
	
	void calcFrameDFTs(Mat frame_gray_in, Mat fi_out, Mat fi2_out)
	{
		Mat frame_gray = new Mat();
		CVUtil.im2Double(frame_gray_in, frame_gray);
		
		Mat F_I  = Mat.zeros(dft_sz_com, frame_gray.type());
	    Mat F_I2 = Mat.zeros(dft_sz_com, frame_gray.type());

		Mat M_I2 = frame_gray.mul(frame_gray);
		Mat roiF_I = new Mat(F_I,  new Rect(0,0,frame_gray.cols(),frame_gray.rows()));
	    frame_gray.copyTo(roiF_I);
	    Mat roiF_I2 = new Mat(F_I2, new Rect(0,0,frame_gray.cols(),frame_gray.rows()));
	    M_I2.copyTo(roiF_I2);

		Core.dft(F_I,  F_I,  0, frame_gray.rows());
	    Core.dft(F_I2, F_I2, 0, frame_gray.rows());
	    
	    F_I.copyTo(fi_out);
	    F_I2.copyTo(fi2_out);
	}
	
	public static void main(String[] args)
	{
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		 
		IconItem icon = new IconItem("bp");
		icon.load("D:\\temp\\bp.png");
		icon.loadMask("D:\\temp\\bp_msk4.png");
		
		ImageItem image = new ImageItem();
		image.load("D:/temp/screenshot_bp.png");
		
		TemplateMatcher m = new TemplateMatcher();
		List<Rect> detects = m.match(image, icon);
		
		for(int i=0; i<detects.size(); i++)
		{
			Core.rectangle(image.getImage(), detects.get(i).tl(), detects.get(i).br(), new Scalar(0,0,255));
		}
		
		Highgui.imwrite("D:/temp/out.png", image.getImage());
		//printMat(icon.getItemMask());
		
	}
}
