package com.eulerity.hackathon.imagefinder;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;


public class DisplayImage {
 
    private transient BufferedImage image;
    private transient boolean isPerson;
    private String url;
    private transient  String fileExtension;

    public DisplayImage(String url) throws MalformedURLException, IOException, URISyntaxException{
        if(url.startsWith("//")) 
            url = "http:" + url;
        this.url = Pattern.compile(url, Pattern.LITERAL).toString(); // prevents escape characters being read in url
        HttpURLConnection connection = (HttpURLConnection) new URL(this.url).openConnection();
        try {
            this.image =  ImageIO.read(connection.getInputStream());

        } catch (Exception e) {
            System.out.println(e);
        }
        // System.out.println(url + " " + fileExtension);
        this.fileExtension = findFileExtenString(this.url);


        try {
            this.isPerson = detectFace(this.url, this.fileExtension);

        } catch (Exception e) {
            this.isPerson = false;
        }

    }



    public static Mat BufferedImage2Mat(BufferedImage image, String fileExtension) throws IOException {
        Mat result = new Mat();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, fileExtension, byteArrayOutputStream);
        byteArrayOutputStream.flush();

        try {
            result = Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);

        } catch (Exception e) {
        }
        return result;
    }

    public static boolean detectFace(String url, String fileExtension) throws MalformedURLException, IOException {
        String[] allowedExtensions = {"png","jpeg", "jpg", "bmp", "svg"};
        boolean allowed = true;

        //Returns false if file type of image isnt in the array above. (List of supported formats for Buffered2ImageMat())
        for (int i = 0; i < allowedExtensions.length; i++) {
            if(allowedExtensions[i] != fileExtension) break;
            allowed = false;

        }
        if(!allowed) return false; 

        if(url.startsWith("data")) return false; // ImageIO.read(url) throws unknown protocol if url is a data url (ie: "date:image/svg....")

        
        nu.pattern.OpenCV.loadShared();

        CascadeClassifier faceDetector = new CascadeClassifier("src\\main\\java\\com\\eulerity\\hackathon\\imagefinder\\haarcascade_frontalface_alt.xml");
        if( faceDetector.empty()){
            faceDetector.load("haarcascade_fullbody.xml");
        }
        URL rUrl   = new URL(url);
        BufferedImage img = ImageIO.read(rUrl.openStream());

        // Reading the input image

        Mat image = BufferedImage2Mat(img, fileExtension);
        if (image == null){
            System.out.println("============================================");
            System.out.println("NO FACE NO FACE NO FACE");
            System.out.println("============================================");
            return false;

        }

        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2GRAY);
     
        MatOfRect faceDetections = new MatOfRect();

 
        faceDetector.detectMultiScale(image, faceDetections);
    
        if (faceDetections.empty()) {
            System.out.println("============================================");
            System.out.println("NO FACE NO FACE NO FACE");
            System.out.println("============================================");
            return false;

           
        }
        System.out.println("============================================");
        System.out.println("FACE FOUND FACE FOUND FACE FOUND FACE FOUND ");
        System.out.println("============================================");
        return true;

    }

    public String getUrl(){
        return this.url;
    }
    public BufferedImage getImage(){
        return this.image;
    }
    public boolean isPerson(){
        return this.isPerson;
    }
    public String getFileExtension(){
        
        return this.fileExtension;
    }
    public String findFileExtenString(String url) throws URISyntaxException{
        String result = "";
        try {
             result =  Paths.get(new URI(url).getPath()).getFileName().toString();

        } catch (java.net.URISyntaxException e) {
            //prevents spaces in uri causes errors 
            //ERROR in question 
            //java.io.IOException: Server returned HTTP response code: 500 for URL: http://images10.newegg.com/BizIntell/item/24/012/24-012-036/GIGABYTE Gaming Monitor-G32QC A-a8.jpg
            //java.net.URISyntaxException: Illegal character in path at index 68: http://images10.newegg.com/BizIntell/item/24/012/24-012-036/GIGABYTE Gaming Monitor-G32QC A-a8.jpg
            URI uri = new URI(url.replace(" ", "%20"));        
            result = Paths.get(uri.getPath()).getFileName().toString();
        }
        int dotIndex = result.indexOf(".");
        result = result.substring(dotIndex + 1, result.length());
        return result;
    }
}