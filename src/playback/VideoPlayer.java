
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import processing.core.PVector;

/**
 *
 * @author birk
 */
public class VideoPlayer implements runnableLedEffect {

    InputStream fis = null;
    BufferedInputStream bis = null;
    DataInputStream dis = null;
    PVector[] ledPositions;
    int streamSize = 0;
    String filename;
    RemoteControlledIntParameter remotePlayerPlay;
    RemoteControlledIntParameter remoteFramePlayerCount;
    RemoteControlledIntParameter remoteNextScene;
    RemoteControlledIntParameter remoteReloadFile;
    String name = "player";
    boolean checkFrameAgain = false;
    int streamFrame = 0;
    int sceneID = 0;
    int nLeds;
    LedColor[] ledColors;
    boolean newFrameAvailable=false;
    int frameCount = 0, lastFrameCount = 0;

    VideoPlayer(String filename, PVector[] ledPositions_) {
        ledPositions=ledPositions_;
        this.filename = filename;
        this.nLeds = ledPositions.length;
        ledColors = LedColor.createColorArray(nLeds);
        remotePlayerPlay = new RemoteControlledIntParameter("/Playback/Player/play", 0, 0, 1);
        remoteFramePlayerCount = new RemoteControlledIntParameter("/Playback/Player/frameCount", 0, 0, 18000);
        remoteNextScene = new RemoteControlledIntParameter("/Playback/Player/nextScene", 0, 0, 1);
        remoteReloadFile = new RemoteControlledIntParameter("/Playback/Player/reloadScene", 0, 0, 1);
        try {
            fis = new FileInputStream(filename + sceneID + ".vid");
            //set the buffer size to 500MB
            int bufferSize = 16 * 1024;
            bis = new BufferedInputStream(fis, bufferSize);
            dis = new DataInputStream(bis);
            System.out.println("Load Video: " + filename + sceneID + ".vid");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LedColor[] drawMe() {
        if(isPlaying()){
            checkFrame();
            if(getFrameAvailable()){
                ledColors = getFrame();
            }
        }
        else {
            if(getReloadFile()) reloadVideoFile();
        }
        return ledColors;
    }
    //check if the Playing is playing
    public boolean isPlaying() {
        if (remotePlayerPlay.getValue() > 0) {
            if (remotePlayerPlay.getChangedSinceReset()) {
            }
            return true;
        } else {
            return false;
        }
    }

    public void loadScene() {
        //System.out.println(remoteNextScene.getValue());
        try {
            if (remoteNextScene.getChangedSinceReset()) {
                remoteNextScene.resetChanged();

                sceneID++;

                fis = new FileInputStream(filename + sceneID + ".vid");
                //set the buffer size to 500MB
                int bufferSize = 16 * 1024;
                bis = new BufferedInputStream(fis, bufferSize);
                dis = new DataInputStream(bis);
                System.out.println("Load Video: " + filename + sceneID + ".vid");
                checkFrameAgain = false;
                lastFrameCount = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
/*
    //check if a new Frame is available
    public boolean newFrameAvailable() {
        if (remoteFramePlayerCount.getChangedSinceReset()) {
            remoteFramePlayerCount.resetChanged();
            int frameCount = remoteFramePlayerCount.getValue();
            //   System.out.println("play frame: "+frameCount);
            //  System.out.println("Frame to play: " + frameCount);
            //check if the right frame is available
            try {

                if (dis.available() < 1) {
                    //  System.out.println("Video File Ended");
                    return false;
                }
                boolean checkTheStream = true;
                while (checkTheStream) {
                    //if the last frame was in sync, the next frame from the stream should be picked
                    if (!checkFrameAgain) {
                        streamFrame = dis.readInt();
                        //  System.out.println("Next Frame in Stream: " + streamFrame);
                    }
                    //if the frame number from stream is equal to frame count, play the new frame
                    if (streamFrame == frameCount) {
                        checkFrameAgain = false;
                        //  System.out.println("Load the Frame");
                        return true;
                    }
                    //if the frame count is lower then the next available frame
                    if (frameCount < streamFrame) {
                        checkFrameAgain = true;
                        System.out.println("frameCount: " + frameCount + " ... NextStreamFrame: " + streamFrame);
                        return false;
                    } //if frame count is higher the next available frame, delete the frame from the stream and check for the next one
                    // ToDo: here should be a loop back to the beginning of the method to read the next frame from the stream. 
                    else {
                        System.out.println("frameCount: " + frameCount + " ... NextStreamFrame: " + streamFrame);
                        for (int i = 0; i < nLeds; i++) {
                            for (int j = 0; j < 3; j++) {
                                dis.readByte();
                            }
                        }
                        checkFrameAgain = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
*/
    
    public LedColor[] getFrame(){
        return ledColors;
    }
    
    public boolean getFrameAvailable(){
        if(newFrameAvailable){
            newFrameAvailable=false;
            return true;
        }
        else return false;
    }
    
    public boolean getReloadFile(){
        if(remoteReloadFile.getChangedSinceReset()){
            remoteReloadFile.resetChanged();
            return true;
        }
        else{
            return false;
        }
    }
    
    public void checkFrame() {
        frameCount = remoteFramePlayerCount.getValue();
        System.out.println(frameCount);
        
        if (frameCount>lastFrameCount){
            lastFrameCount = frameCount;
            try {
                if (dis.available() > 0) {
                    boolean checkTheStream = true;
                    newFrameAvailable = false;
                    while (checkTheStream) {
                        //if the last frame was in sync, the next frame from the stream should be picked
                        if (!checkFrameAgain) {
                            streamFrame = dis.readInt();
                        }
                        //if the frame number from stream is equal to frame count, play the new frame
                        if (streamFrame == frameCount) {
                            checkFrameAgain = false;
                            ledColors = readFrame();
                            checkTheStream=false;
                            newFrameAvailable=true;
                            System.out.println("Play Frame Nr.: " + frameCount);
                            newFrameAvailable = true;
                        } 
                        //if the frame count is lower then the next available frame
                        else if (frameCount < streamFrame) {
                            checkFrameAgain = true;
                            System.out.println("no frame available frameCount: " + frameCount + " ... NextStreamFrame: " + streamFrame);
                            checkTheStream=false;
                        } //if frame count is higher the next available frame, delete the frame from the stream and check for the next one
                        // ToDo: here should be a loop back to the beginning of the method to read the next frame from the stream. 
                        else{
                            System.out.println("!FRAMEDROP! frameCount: " + frameCount + " ... NextStreamFrame: " + streamFrame);
                            for (int i = 0; i < nLeds; i++) {
                                for (int j = 0; j < 3; j++) {
                                    dis.readByte();
                                }
                            }
                            checkFrameAgain = false;
                            checkTheStream=true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public LedColor[] readFrame() {
        byte[] theFrame = new byte[nLeds * 3];

        try {
            // read the stream

            for (int i = 0; i < nLeds; i++) {
                for (int j = 0; j < 3; j++) {
                    theFrame[(i * 3) + j] = dis.readByte();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return byteToLedColor(theFrame, nLeds);
    }

    public LedColor[] readFrameSW(int nLeds) {
        byte[] theFrame = new byte[nLeds];
        try {
            for (int i = 0; i < nLeds; i++) {
                theFrame[i] = dis.readByte();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteToLedSW(theFrame, nLeds);
    }

    public LedColor[] byteToLedSW(byte[] theFrame, int nLeds) {
        LedColor[] ledColors = LedColor.createColorArray(nLeds);
        for (int i = 0; i < nLeds; i++) {

            ledColors[i].x = (theFrame[i] + 128) / 255f;
            ledColors[i].y = (theFrame[i] + 128) / 255f;
            ledColors[i].z = (theFrame[i] + 128) / 255f;
        }
        return ledColors;

    }

    public LedColor[] byteToLedColor(byte[] theFrame, int nLeds) {
        LedColor[] ledColors = LedColor.createColorArray(nLeds);
        for (int i = 0; i < nLeds; i++) {
            int byteIndex = i * 3;

            ledColors[i].x = (theFrame[byteIndex] + 128) / 255f;
            ledColors[i].y = (theFrame[byteIndex + 1] + 128) / 255f;
            ledColors[i].z = (theFrame[byteIndex + 2] + 128) / 255f;
        }
        return ledColors;

    }

    public void reloadVideoFile() {
        try {
            fis = new FileInputStream(filename + sceneID + ".vid");
            //set the buffer size to 500MB
            int bufferSize = 16 * 1024;
            bis = new BufferedInputStream(fis, bufferSize);
            dis = new DataInputStream(bis);
            checkFrameAgain = false;
            lastFrameCount = 0;
            System.out.println("Load Video: " + filename + sceneID + ".vid");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    public String getName() {
            return name;
    }
}
