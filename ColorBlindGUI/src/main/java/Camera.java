import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;




public class Camera{
    private FrameGrabber camera;

    public Camera(){
        this.camera = new OpenCVFrameGrabber(0);
    }

    public void startCamera() throws FrameGrabber.Exception {
        System.setProperty("OPENCV_AVFOUNDATION_SKIP_AUTH", "1"); //macOS authentication problems
        this.camera.start();
    }

    public Frame getFrame() throws FrameGrabber.Exception {
        return camera.grab();
    }

    public void closeCamera() throws FrameGrabber.Exception {
        this.camera.release();
    }




}