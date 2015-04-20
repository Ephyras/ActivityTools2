package activity.web.opencv;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

public class IconItem {
	private String name;
	private Mat item;
	private Mat itemMask;
	
	private Rect item_tight;
	private Rect msk_rect_tight = new Rect();
	private Size dftSz;
	private Size dftTplSz;
	private double dftThr;
	private Mat fa2;
	private Mat fa2T;
	private double normAT;
	
	public IconItem(String name)
	{
		this.name = name;
	}
	
	public void load(String path)
	{
		item = Highgui.imread(path, 0);
	}
	
	public void loadMask(String path)
	{
		itemMask = Highgui.imread(path, 0);
	}
	
	public boolean tightenMask()
	{
		if( itemMask.empty() )
			return false;

		msk_rect_tight.x = itemMask.cols()-1;
		msk_rect_tight.y = itemMask.rows()-1;
		msk_rect_tight.width  = 0;
		msk_rect_tight.height = 0;

		int tmpx = 0; 
		int tmpy = 0;
		
		//byte buff[] = new byte[ (int) (itemMask.total() * itemMask.channels())];
		for(int i=0; i<itemMask.rows(); i++)
		{
			for(int j=0; j<itemMask.cols(); j++)
			{
				int v = (int)(itemMask.get(i, j)[0]);
				if(v != 0 )
				{
					if( msk_rect_tight.x > j ){ msk_rect_tight.x = j;}
					if( msk_rect_tight.y > i ){ msk_rect_tight.y = i;}
					if( tmpx    < j ){ tmpx    = j;}
					if( tmpy    < i ){ tmpy    = i;}
				}
			}
		}

		msk_rect_tight.width = tmpx - msk_rect_tight.x + 1;
		msk_rect_tight.height= tmpy - msk_rect_tight.y + 1;

		msk_rect_tight.width = msk_rect_tight.width>0 ? msk_rect_tight.width : 0;
		msk_rect_tight.height = msk_rect_tight.height>0 ? msk_rect_tight.height : 0;

		item_tight = msk_rect_tight;
		return true;
	}
	
	public void initDFTParameters(Size dft_sz_in, Size dft_tpl_sz_in, double dft_thr_in)
	{
		dftSz     = dft_sz_in;
		dftTplSz = dft_tpl_sz_in;
		dftThr    = dft_thr_in;

		Mat img_gray;
		Mat msk_gray;
		Mat M_a2 = new Mat();
		Mat M_a2T = new Mat();
		Mat M_aT = new Mat();

		img_gray = Mat.zeros(dftTplSz, item.type());
		Mat roiImg_gray = new Mat(img_gray, new Rect(0,0,msk_rect_tight.width, msk_rect_tight.height));
		item.submat( msk_rect_tight ).copyTo(roiImg_gray);
		
		//cvtColor(img_gray, img_gray, CV_BGR2GRAY);//Currently only deel with gray images

		msk_gray = Mat.zeros(dftTplSz, itemMask.type());
		Mat roiMsk_gray = new Mat(msk_gray, new Rect(0,0,msk_rect_tight.width, msk_rect_tight.height));
		itemMask.submat(msk_rect_tight ).copyTo(roiMsk_gray);//already gray img
		
		CVUtil.im2Double(img_gray, img_gray);
		CVUtil.im2Double(msk_gray, msk_gray);
		
		Mat img_fp = new Mat();
		Mat msk_fp = new Mat();
		Core.flip(img_gray, img_fp, -1);
		Core.flip(msk_gray, msk_fp, -1);
			
		M_a2  = msk_fp.mul(msk_fp);
		
		M_a2T = M_a2.mul(img_fp);
		M_aT  = msk_fp.mul(img_fp);

		normAT = Core.norm(M_aT, Core.NORM_L2);

		fa2 = Mat.zeros(dftSz, CvType.CV_64FC1);
		fa2T= Mat.zeros(dftSz, fa2.type());
		Mat roiF_a2 = new Mat (fa2,  new Rect(0,0,msk_fp.cols(),msk_fp.rows()));
		M_a2.copyTo(roiF_a2);
		Mat roiF_a2T = new Mat(fa2T, new Rect(0,0,msk_fp.cols(),msk_fp.rows()));
		M_a2T.copyTo(roiF_a2T);

		// now transform the padded Matrices in-place;
		// use "nonzeroRows" hint for faster processing
		Core.dft(fa2,  fa2,  0, msk_fp.rows());
		Core.dft(fa2T, fa2T, 0, msk_fp.rows());
	}
	
	List<Rect> match(Mat tar_img_gray, Mat F_I, Mat F_I2)
	{
		//-->debug
		boolean debuging = false;//true;//
		Mat map_show;
		Mat tpl_show;
		Mat box_show;
		//<--debug

		Mat rst = new Mat();

		//double* rst_i;
		int i,j;
		int a, b, a_opt, b_opt;
		double v, v_opt;
		double ep = 0.002;
		double current_thr = this.dftThr;
		boolean isPresent = false;


		getAlphaTplMatchingMap(new Size(tar_img_gray.cols(),tar_img_gray.rows()), dftTplSz, 
								new Size(msk_rect_tight.width, msk_rect_tight.height), 
								rst, fa2, fa2T, normAT, F_I, F_I2);
		
		MinMaxLocResult mmres = Core.minMaxLoc(rst);
		double minVal = mmres.minVal;
		double maxVal = mmres.maxVal;
		Point minLoc = mmres.minLoc;
		Point maxLoc = mmres.maxLoc;
		
		List<Rect> detects = new ArrayList<Rect>();
		if( maxVal>=current_thr )
		{
			if( maxVal-ep>=current_thr )
				current_thr = maxVal-ep;
			
			isPresent = true;
			
			for( i=0; i<rst.rows(); i++ )
			{
				//rst_i = (double*)rst.ptr<double>(i);//double type
				for( j=0; j<rst.cols(); j++ )
				{
					double vij = rst.get(i, j)[0];
					if( vij >= current_thr )
					{
						v_opt = vij;
						a_opt = i;
						b_opt = j;
						for( a=i; a<i+msk_rect_tight.height && a<rst.rows(); a++ )
						{
							for( b=j; b<j+msk_rect_tight.width && b<rst.cols(); b++ )
							{
								v = rst.get(a,b)[0];
								if( v>v_opt)
								{
									double[] rst_data = rst.get(a_opt, b_opt);
									rst_data[0] = 0;
									a_opt = a;
									b_opt = b;
									v_opt = v;
								}else
								{
									double[] rst_data = rst.get(a, b);
									rst_data[0] = 0;
								}
							}
						}
						detects.add(new Rect( b_opt-msk_rect_tight.x, a_opt-msk_rect_tight.y, item.cols(), item.rows()) );
					
					}
				}
			}
		}
		///reset used vector
		return detects;
	}
	
	public void getAlphaTplMatchingMap(Size tar_img_sz, Size com_tpl_sz, Size own_tpl_sz,
			Mat map, Mat F_a2, Mat F_a2T, double aT, Mat F_I, Mat F_I2)
	{
		//Convolution
		Mat tmpMat = new Mat();
		map.create((int)Math.abs(tar_img_sz.height - own_tpl_sz.height)+1, 
						(int)Math.abs(tar_img_sz.width - own_tpl_sz.width)+1, F_a2.type());

		Core.mulSpectrums(F_a2T, F_I, tmpMat, 0, false);
		Core.dft(tmpMat, tmpMat, Core.DFT_INVERSE+Core.DFT_SCALE, (int)(tar_img_sz.height+com_tpl_sz.height-own_tpl_sz.height));//
		Rect tmpRect = new Rect((int)com_tpl_sz.width-1, (int)com_tpl_sz.height-1, map.cols(), map.rows());
		tmpMat.submat(tmpRect).copyTo(map);
		

		//Calculate the normalization of the cost
		Mat M_norm_a2I2 = new Mat(map.rows(), map.cols(), F_a2.type());

		Core.mulSpectrums(F_a2, F_I2, tmpMat, 0, false);
		Core.dft(tmpMat, tmpMat, Core.DFT_INVERSE+Core.DFT_SCALE, (int)(tar_img_sz.height+com_tpl_sz.height-own_tpl_sz.height));//tar_img_sz.height
		tmpMat.submat(new Rect((int)com_tpl_sz.width-1, (int)com_tpl_sz.height-1, map.cols(), map.rows())).copyTo(M_norm_a2I2);

		Core.sqrt(M_norm_a2I2, M_norm_a2I2);
		
		Core.divide(map, M_norm_a2I2, map, 1/aT);
		
		//map = map / M_norm_a2I2 * (1/aT);
	}
	
	public static void printMat(Mat m)
	{
		for(int i=0; i<m.rows() && i<10; i++)
		{
			for(int j=0; j<m.cols() && j<10; j++)
			{
				double t = m.get(i, j)[0];
				
				System.out.print(t + " ");
			}
			System.out.println("");
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Mat getItem() {
		return item;
	}
	public void setItem(Mat item) {
		this.item = item;
	}
	public Mat getItemMask() {
		return itemMask;
	}
	public void setItemMask(Mat itemMask) {
		this.itemMask = itemMask;
	}
	public Size getDftSz() {
		return dftSz;
	}
	public void setDftSz(Size dftSz) {
		this.dftSz = dftSz;
	}
	public Size getDftTplSz() {
		return dftTplSz;
	}
	public void setDftTplSz(Size dftTplSz) {
		this.dftTplSz = dftTplSz;
	}
	public double getDftThr() {
		return dftThr;
	}
	public void setDftThr(double dftThr) {
		this.dftThr = dftThr;
	}
	public Mat getFa2() {
		return fa2;
	}
	public void setFa2(Mat fa2) {
		this.fa2 = fa2;
	}
	public Mat getFa2T() {
		return fa2T;
	}
	public void setFa2T(Mat fa2t) {
		fa2T = fa2t;
	}
	public double getNormAT() {
		return normAT;
	}
	public void setNormAT(double normAT) {
		this.normAT = normAT;
	}

	public Rect getItem_tight() {
		return item_tight;
	}

	public void setItem_tight(Rect item_tight) {
		this.item_tight = item_tight;
	}

	public Rect getMsk_rect_tight() {
		return msk_rect_tight;
	}

	public void setMsk_rect_tight(Rect msk_rect_tight) {
		this.msk_rect_tight = msk_rect_tight;
	}
	
	public static void main(String[] args)
	{
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		 
		IconItem item = new IconItem("bp");
		item.load("D:\\temp\\bp.png");
		item.loadMask("D:\\temp\\bp_msk4.png");
		
	}
	
}
