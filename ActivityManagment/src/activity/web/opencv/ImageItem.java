package activity.web.opencv;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ImageItem {
	private Mat image;
	private Mat imageGray;
	
	public ImageItem()
	{
		image = new Mat();
		imageGray = new Mat();
	}
	
	public void load(String path)
	{
		image = Highgui.imread(path, 1);
		Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_BGR2GRAY);
	}
	
	public Mat getImage() {
		return image;
	}

	public void setImage(Mat image) {
		this.image = image;
		Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_BGR2GRAY);
	}

	public Mat getImageGray() {
		return imageGray;
	}

	public void setImageGray(Mat imageGray) {
		this.imageGray = imageGray;
	}
	
	
	
}
