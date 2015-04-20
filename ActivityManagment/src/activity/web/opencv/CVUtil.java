package activity.web.opencv;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

public class CVUtil {

	public static boolean im2Double(Mat m, Mat dst)
	{
		if(m.depth() != CvType.CV_8U){
			return false;
		}
		int type_lf = CvType.CV_64FC3;

		if(m.channels() == 1)
			type_lf = CvType.CV_64FC1;

		m.convertTo(dst, type_lf);
		Scalar alpha = new Scalar(1.0/255); // the factor

		Core.multiply(dst , alpha,dst);
		
		return true;
	}
	
	public static Mat readInputStreamIntoMat(InputStream inputStream) throws IOException {
	    // Read into byte-array
	    byte[] temporaryImageInMemory = readStream(inputStream);

	    // Decode into mat. Use any IMREAD_ option that describes your image appropriately
	    Mat outputImage = Highgui.imdecode(new MatOfByte(temporaryImageInMemory), Highgui.IMREAD_GRAYSCALE);

	    return outputImage;
	}

	private static byte[] readStream(InputStream stream) throws IOException {
	    // Copy content of the image to byte-array
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    int nRead;
	    byte[] data = new byte[1024 * 1024];

	    while ((nRead = stream.read(data, 0, data.length)) != -1) {
	        buffer.write(data, 0, nRead);
	    }

	    buffer.flush();
	    byte[] temporaryImageInMemory = buffer.toByteArray();
	    buffer.close();
	    stream.close();
	    return temporaryImageInMemory;
	}
	
	public static BufferedImage toBufferedImageOfType(BufferedImage original, int type) {
	    if (original == null) {
	        throw new IllegalArgumentException("original == null");
	    }

	    // Don't convert if it already has correct type
	    if (original.getType() == type) {
	        return original;
	    }

	    // Create a buffered image
	    BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), type);

	    // Draw the image onto the new buffer
	    Graphics2D g = image.createGraphics();
	    try {
	        g.setComposite(AlphaComposite.Src);
	        g.drawImage(original, 0, 0, null);
	    }
	    finally {
	        g.dispose();
	    }

	    return image;
	}
	
	public static Mat img2Mat(BufferedImage im)
    {
		BufferedImage bi = toBufferedImageOfType(im, BufferedImage.TYPE_3BYTE_BGR);
		
		byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer())
	            .getData();

	    // Create a Matrix the same size of image
	    Mat image = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
	    // Fill Matrix with image values
	    image.put(0, 0, pixels);
        return image;
		
     } 
}
