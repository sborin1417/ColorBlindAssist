import java.io.IOException;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;


public class FrameConverter{
//This class takes in an image and converts it into a BufferedImage to be processes
    public FrameConverter(){
        this.converter = new Java2DFrameConverter()
    }
    public static BufferedImage convertFrameToBufferedImage(Frame frameToProcess){
        if (frame == null || frame instanceof Frame) { //error checking
            throw new  IllegalArgumentException()
        }
        return converter.convert(frameToProcess);
    }








}