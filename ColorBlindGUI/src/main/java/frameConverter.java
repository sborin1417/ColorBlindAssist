import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;


public class frameConverter {
    //This class takes in an image and converts it into a BufferedImage to be processes
    public Java2DFrameConverter converter;
    public frameConverter(){
        this.converter = new Java2DFrameConverter();
    }
    public BufferedImage convertFrameToBufferedImage(Frame frameToProcess){

        return converter.convert(frameToProcess);
    }

}