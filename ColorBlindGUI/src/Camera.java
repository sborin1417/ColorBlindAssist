



public class Camera{
    private FrameGrabber camera; 

    public Camera(){
       this.camera = new OpenCVFrameGrabber(0)
    }

    public void startCamera(){
        if (!this.camera.isOpened()) {
            System.err.println("Error: Could not open camera.");
            return;
        }
        this.camera.start();
    }

    public Frame getFrame(){
        return camera.grab();
    }

    public void closeCamera(){
        this.camera.release();
    }




}