package com.example.demo;

import net.sf.image4j.util.ConvertUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;


public class image {

    private String name;
    private int length;
    private BufferedImage img;
   // private FileWriter writer;
    private int height;
    private int width;
    private int middleBlockSize=3;
    public image(String name) {
        this.name=name;
    }
    public image() {

    }
    public void rotateCw( )
    {
        int newWidth  = img.getWidth();
        int newHeight = img.getHeight();
        BufferedImage   newImage = new BufferedImage( newHeight, newWidth, img.getType() );
        for( int i=0 ; i < newWidth ; i++ )
            for( int j=0 ; j < newHeight ; j++ )
                newImage.setRGB( newHeight-1-j, i, img.getRGB(i,j) );

        img=newImage;
        height=img.getHeight();
        width=img.getWidth();
    }
    public static byte[] decodeImage(String imageDataString) {
        return Base64.getDecoder().decode(imageDataString);
    }
    public void setString(String imageString) throws IOException {
        byte[] imageByteArray = decodeImage(imageString);

        InputStream is = new ByteArrayInputStream(imageByteArray);
        img = ImageIO.read(is);
        height=img.getHeight();
        width=img.getWidth();
    }
    public void readImage() throws IOException {
      //  writer = new FileWriter("C:\\Users\\mcrossley\\eclipse-workspace\\StegoProj\\src\\pixel_values.txt");
        //Reading the image
        File file= new File(name);
        //https://www.tutorialspoint.com/how-to-get-pixels-rgb-values-of-an-image-using-java-opencv-library#:~:text=Retrieving%20the%20pixel%20contents%20(ARGB%20values)%20of%20an%20image%20%E2%88%92&text=Get%20the%20pixel%20value%20at,and%20getBlue()%20methods%20respectively.
        img = ImageIO.read(file);
        img = ConvertUtil.convert24(img);

        height=img.getHeight();
        width=img.getWidth();


    }


    public void readPixels() throws IOException {
        int max=0;
        int pixel,red,green,blue;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //Retrieving contents of a pixel
                pixel = img.getRGB(x,y);
                //Creating a Color object from pixel value
                Color color = new Color(pixel, true);
                //Retrieving the R G B values
                red = color.getRed();
                green = color.getGreen();
                blue = color.getBlue();
                // alpha=color.getAlpha();

                if(red>max) {
                    max=red;
                }
             //  writer.append(red+":");
              //  writer.append(green+":");
              //  writer.append(blue+"");
                // writer.append(alpha+"");
               // writer.append("\n");
               // writer.flush();
                pixel = 0;
                //red+=150;
                if(red>255) {
                    red=255;
                }
                pixel =  ((red)<<16) | (green<<8) | blue;
                img.setRGB(x, y, pixel);

            }
        }

       // writer.close();
        System.out.println("RGB values at each pixel are stored in the specified file" + max+"MAX");
    }
    public void hidemsgLength(String binary) {
         int pixel;
        String asBinary;
       // System.out.println("TOP LEFT IS "+topLeft[0]+"   "+topLeft[1]);
        int[] topLeft = new int[]{(width/2)-1,(height/2)-1};
        int[] currPos = new int[]{topLeft[0]-1,topLeft[1]};
        imagePos imagePos = new imagePos(currPos,'S',0,3);
        Color color;
        StringBuilder sb = new StringBuilder(binary);
        for(int z=0;z<15-binary.length();z++){
            sb.insert(0,'0');
        }
        System.out.println(sb);
        binary=sb.toString();
        for(int x = 0;x<5;x++){
            asBinary=binary.substring(x*3,x*3+3);

            currPos = imagePos.getCurrPos();
            System.out.println("HIDING At "+ currPos[0] + "    "+currPos[1]+" going "+imagePos.getDirection());

            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);
            int newPixel = getNewColour(color,asBinary);
            img.setRGB(currPos[0], currPos[1], newPixel);
            color = new Color(newPixel, true);
            //  System.out.println(asBinary+"    "+ asBinary.substring(y*3,y*3+3)+"   " +Integer.toString(color.getRed(),2) + "   " + Integer.toString(color.getGreen(),2)+ "    "+Integer.toString(color.getBlue(),2)+  "  "+color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());
            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);
            System.out.println(color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());
            //currDistance++;
            imagePos.incCurrDist();
            getNextPos(imagePos);
        }
        System.out.println("ENCODED WITH LENGTH  "+Integer.parseInt(binary.toString(), 2));
        pixel = img.getRGB(598, 299);
        color = new Color(pixel, true);
        System.out.println("WHY NO WORK "+color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());

    }
    public int getMsgLength(imageCentre centre) {
        int pixel,red,green,blue;
        StringBuilder str  = new StringBuilder();
        String asBinary;
        char direction='S';
        int maxLength=3;
        int currDistance=0;
        Color color;
        int[] topLeft = new int[]{(width/2)-1,(height/2)-1};
        topLeft[0]=centre.getX();
        topLeft[1]=centre.getY();
        int[] currPos = new int[]{topLeft[0]-1,topLeft[1]};

        imagePos imagePos = new imagePos(currPos,'S',0,3);

        for(int x = 0;x<5;x++){
            currPos = imagePos.getCurrPos();
            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);
            red = color.getRed();
            asBinary = Integer.toString(red,2);
            str.append(asBinary.charAt(asBinary.length()-1));
            green = color.getGreen();
            asBinary = Integer.toString(green,2);
            str.append(asBinary.charAt(asBinary.length()-1));
            blue = color.getBlue();
            asBinary = Integer.toString(blue,2);
            str.append(asBinary.charAt(asBinary.length()-1));
            imagePos.incCurrDist();
            getNextPos(imagePos);
         //   System.out.println("NOTHIDING At "+ currPos[0] + "    "+currPos[1]+" going "+direction);
           // System.out.println(red+"   "+green+"   "+blue+" ++" + str);

        }

        length = Integer.parseInt(str.toString(), 2);
        // System.out.println("STRING IS " + str + " LENGTH " + length);
        System.out.println("DECODED LENGTH IS "+length);

        return length;

    }

    public int getEmbeddedValue(int colour,char bit) {
        String asBinary = Integer.toString(colour,2);
        String newBinary = asBinary.substring(0, asBinary.length()-1)+bit;

        return Integer.parseInt(newBinary, 2);
    }

    public void saveNewImage(String fileName) throws IOException {
        File outPutImage = new File(fileName);
        ImageIO.write(img, "png", outPutImage);
        System.out.println("NEW IMAGE SAVED AT DESTINATION " +fileName);
    }
    public imageCentre isCentre(){
        imageCentre imgCentre;
        imgCentre = findCentre();
        if(imgCentre!=null){
            return imgCentre;
        }
        rotateCw();
        imgCentre = findCentre();
        if(imgCentre!=null){
            return imgCentre;
        }
        rotateCw();
        imgCentre = findCentre();
        if(imgCentre!=null){
            return imgCentre;
        }
        rotateCw();
        imgCentre = findCentre();
        if(imgCentre!=null){
            return imgCentre;
        }
        return null;
    }
    public imageCentre findCentre(){
        int pixel,red,green,blue;
        int countx,county;
        boolean valid;
        boolean isCentre=false;
        String redAsBinary,greenAsBinary,blueAsBinary;

        for(int x=0;x<width-middleBlockSize;x++) {
            for(int y=0;y<height-middleBlockSize;y++) {
                countx=0;
                county=0;
                valid = true;
                do{
                    pixel = img.getRGB(x+countx, y+county);
                    //pixel = 0;
                    Color color = new Color(pixel, true);
                    red = color.getRed();
                    redAsBinary = Integer.toString(red,2);
                    green = color.getGreen();
                    greenAsBinary = Integer.toString(green,2);
                    blue = color.getBlue();
                    blueAsBinary = Integer.toString(blue,2);
                    if(countx==0 && county==0) {
                        if(redAsBinary.charAt(redAsBinary.length()-1)!='0' || greenAsBinary.charAt(greenAsBinary.length()-1)!='0' ||blueAsBinary.charAt(blueAsBinary.length()-1)!='1'){
                            valid = false;
                        }
                    }else if((countx+county)%2==0){
                            if(redAsBinary.charAt(redAsBinary.length()-1)!='1' || greenAsBinary.charAt(greenAsBinary.length()-1)!='0' ||blueAsBinary.charAt(blueAsBinary.length()-1)!='1'){
                                valid = false;
                            }
                    }else{
                        if(redAsBinary.charAt(redAsBinary.length()-1)!='0' || greenAsBinary.charAt(greenAsBinary.length()-1)!='1' ||blueAsBinary.charAt(blueAsBinary.length()-1)!='0'){
                            valid = false;
                        }
                    }
                    if(county>=middleBlockSize){
                        valid = false;
                    }
                     if(countx==middleBlockSize-1 &&  county==middleBlockSize-1 && valid){
                        System.out.println("FOUND CENTER AT " + x+"   "+y);
                        System.out.println(x + "  " +y + "  "+countx +"  " + county +"  "+ redAsBinary+ "  "+greenAsBinary+"   "+blueAsBinary );
                        county++;
                        return new imageCentre(x,y,0);
                    }else if(countx<middleBlockSize-1){
                        countx++;
                    }else{
                        countx=0;
                        county++;
                    }

                }while(valid);

            }

        }
        System.out.println("no center found");
        return null;
    }
    public int[] getXY(int position){
        int[] xy = new int[2];
        xy[0] = (int)position%width;
        xy[1] = (int)Math.floor((double)position/(double)width);

        //System.out.println(position + "  x " + xy[0] + "  y" + xy[1]);
        return xy;
    }
public int getNewColour(Color color, String binary){
        int blue,green,red,newBlue,newGreen,newRed;
    red = color.getRed();
    //asBinary = Integer.toString(red,2);
    newRed = getEmbeddedValue(red,binary.charAt(0));
    //System.out.println(Integer.toString(red,2) + "   " + Integer.toString(newRed,2) + "   "+binary.charAt(0));
    green = color.getGreen();
    //asBinary = Integer.toString(green,2);
    newGreen = getEmbeddedValue(green,binary.charAt(1));
  //  System.out.println(Integer.toString(green,2) + "   " + Integer.toString(newGreen,2) + "   "+binary.charAt(1));
    blue = color.getBlue();
    //colBinary = Integer.toString(blue,2);
    // System.out.println("RED IS " + red + " BINARY " + asBinary + " EMBEDDING " + binary.charAt(x));
    newBlue = getEmbeddedValue(blue,binary.charAt(2));
   // System.out.println(Integer.toString(blue,2) + "   " + Integer.toString(newBlue,2) + "   "+binary.charAt(2));
    int pixel = (newRed<<16) | (newGreen<<8) | newBlue;

return pixel;

}
    private imagePos getNextPos(imagePos imagePos){

        switch (imagePos.getDirection()){
            case 'S':
                imagePos.incY();
                break;
            case 'E':
                imagePos.incX();
                break;
            case 'N':
                imagePos.decY();
                break;
            case 'W':
                imagePos.decX();
                break;
        }
        if(imagePos.getCurrDistance()==imagePos.getMaxLength()){
            imagePos.setCurrDistance(0);
            System.out.println("CHANGED DIRECTION");
            switch (imagePos.getDirection()){
                case 'S':
                    imagePos.setDirection('E');
                    imagePos.incMaxLength();
                    break;
                case 'E':
                    imagePos.setDirection('N');
                    break;
                case 'N':
                    imagePos.setDirection('W');
                    imagePos.incMaxLength();
                    break;
                case 'W':
                    imagePos.setDirection('S');
                    break;
            }
        }

    return imagePos;
    }
    public void hideData(String msg) {

        int pixels;
        Color colors;
        int pixel,red,green,blue;
        char character;
        int newRed, newGreen,newBlue;
        int ascii;
        String asBinary;
        String newBinary;
        String colBinary;
        int count;
        char direction;
        direction='S';
        Color color;
        int[] topLeft = new int[]{(width/2)-1,(height/2)-1};
        int[] currPos = new int[]{topLeft[0]-1,topLeft[1]};
        imagePos imagePos = new imagePos(currPos,'S',0,3);

        int newPixel;
        pixel = img.getRGB(topLeft[0], topLeft[1]);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,"001");
        img.setRGB( topLeft[0],topLeft[1],newPixel);

        pixel = img.getRGB(topLeft[0]+1, topLeft[1]);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,"010");
        img.setRGB(topLeft[0]+1,topLeft[1], newPixel);

        pixel = img.getRGB(topLeft[0]+2, topLeft[1]);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,"101");
        img.setRGB( topLeft[0]+2,topLeft[1], newPixel);

        pixel = img.getRGB(topLeft[0], topLeft[1]+1);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,"010");
        img.setRGB( topLeft[0],topLeft[1]+1, newPixel);

        pixel = img.getRGB(topLeft[0]+1,topLeft[1]+1);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,"101");
        img.setRGB( topLeft[0]+1,topLeft[1]+1, newPixel);

        pixel = img.getRGB(topLeft[0]+2,topLeft[1]+1);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,"010");
        img.setRGB( topLeft[0]+2,topLeft[1]+1, newPixel);

        pixel = img.getRGB(topLeft[0],topLeft[1]+2);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,"101");
        img.setRGB( topLeft[0],topLeft[1]+2, newPixel);

        pixel = img.getRGB(topLeft[0]+1,topLeft[1]+2);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,"010");
        img.setRGB( topLeft[0]+1,topLeft[1]+2, newPixel);

        pixel = img.getRGB(topLeft[0]+2,topLeft[1]+2);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,"101");
        img.setRGB( topLeft[0]+2,topLeft[1]+2, newPixel);

        for(int x = -5;x<msg.length();x++){
            if(x>=0){
                asBinary= Integer.toString(msg.charAt(x),2);
                //System.out.println("INITIAL"+asBinary);
                StringBuilder sb = new StringBuilder(asBinary);
                for(int z=0;z<9-asBinary.length();z++){
                    sb.insert(0,'0');
                }
                asBinary=sb.toString();
                // asBinary="000";
                //  System.out.println("FINAL  "+asBinary);
                for(int y =0;y<3;y++){
                    currPos = imagePos.getCurrPos();
                    // int pixels = img.getRGB(602, 302);
                    //  Color colors = new Color(pixels, true);
                    //System.out.println("COLOR PIXEL 602 302 IS "+ colors.getRed()+"   "+colors.getGreen()+"     "+colors.getBlue());
                    System.out.println("USEFUL "+ currPos[0] + "    "+currPos[1]+" going "+imagePos.getDirection()+ "  "+imagePos.getCurrDistance()+"    "+imagePos.getMaxLength());

                    pixel = img.getRGB(currPos[0], currPos[1]);
                    color = new Color(pixel, true);
                    newPixel = getNewColour(color,asBinary.substring(y*3,y*3+3));
                    img.setRGB(currPos[0], currPos[1], newPixel);
                    color = new Color(newPixel, true);
                    //System.out.println(asBinary+"    "+ asBinary.substring(y*3,y*3+3)+"   " +Integer.toString(color.getRed(),2) + "   " + Integer.toString(color.getGreen(),2)+ "    "+Integer.toString(color.getBlue(),2)+  "  "+color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());
                    pixel = img.getRGB(currPos[0], currPos[1]);
                    color = new Color(pixel, true);
                    System.out.println(color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());
                    imagePos.incCurrDist();
                    getNextPos(imagePos);
                }

            }else{
                System.out.println("USELESS "+ currPos[0] + "    "+currPos[1]+" going "+imagePos.getDirection());
                imagePos.incCurrDist();
                getNextPos(imagePos);
            }

                // System.out.println(asBinary.substring(y*3,y*3+3)+"----"+Integer.toString(color.getRed(),2) + "   " + Integer.toString(color.getGreen(),2)+"    "+Integer.toString(color.getBlue(),2));


            //System.out.println(msg.charAt(x));
        }


        //int pixels = img.getRGB(602, 302);
        //Color colors = new Color(pixels, true);
        // System.out.println("COLOR AT 602 302 IS "+ colors.getRed()+"   "+colors.getGreen()+"     "+colors.getBlue());
        System.out.println("Height" + height+ "  width"+width);
        System.out.println("Height2    " + height/2+ "  width"+width/2);
    }

    public String extractData(imageCentre centre) {
        int[] topLeft = new int[]{(width/2)-1,(height/2)-1};
        topLeft[0]=centre.getX();
        topLeft[1]=centre.getY();
        System.out.println("ENCODIGN WITH TOP LEFT AT " + topLeft[0] + "  y "+topLeft[1]);
        int pixels = img.getRGB(111, 111);
        Color colors = new Color(pixels, true);
        StringBuilder binStr  = new StringBuilder();
        StringBuilder msgStr  = new StringBuilder();

        int pixel,red,green,blue,asciiVal;
        char character;
        int newRed, newGreen,newBlue;
        int ascii;
        String asBinary;
        String newBinary;
        String colBinary;
        int count;

        Color color;
        imageCentre imageCentre = findCentre();
        char direction ='S';
        int[] currPos = new int[]{imageCentre.getX()-1,imageCentre.getY()};
        System.out.println("STARTING AT "+currPos[0]+"   "+currPos[1]);
        int maxLength=3;
        int currDistance=0;
        System.out.println("EXTRACTING MSG LEGNTH "+length);
        for(int x = -5;x<length;x++){
            if(x>=0){
                binStr.setLength(0);

                for(int y =0;y<3;y++){

                    pixel = img.getRGB(currPos[0], currPos[1]);

                    color = new Color(pixel, true);
                    red = color.getRed();
                    asBinary = Integer.toString(red,2);
                    binStr.append(asBinary.charAt(asBinary.length()-1));
                    green = color.getGreen();
                    asBinary = Integer.toString(green,2);
                    binStr.append(asBinary.charAt(asBinary.length()-1));
                    blue = color.getBlue();
                    asBinary = Integer.toString(blue,2);
                    binStr.append(asBinary.charAt(asBinary.length()-1));
                   // System.out.println("At "+ currPos[0] + "    "+currPos[1]);

                    currDistance++;
                    switch (direction){
                        case 'S':
                            currPos[1]++;
                            break;
                        case 'E':
                            currPos[0]++;
                            break;
                        case 'N':
                            currPos[1]--;
                            break;
                        case 'W':
                            currPos[0]--;
                            break;
                    }
                    if(currDistance==maxLength){
                        currDistance=0;
                        switch (direction){
                            case 'S':
                                direction='E';
                                maxLength++;
                                break;
                            case 'E':
                                direction='N';
                                break;
                            case 'N':
                                maxLength++;
                                direction='W';
                                break;
                            case 'W':
                                direction='S';
                                break;
                        }
                    }
                }
                asciiVal = Integer.parseInt(binStr.toString(), 2);
                msgStr.append(Character.toString((char)asciiVal));
              //  System.out.println("GOT CHAR"+(char)asciiVal);
            }else{
                System.out.println("USELESS "+ currPos[0] + "    "+currPos[1]+" going "+direction);
                currDistance++;
                // int pixels = img.getRGB(602, 302);
                //  Color colors = new Color(pixels, true);
                //System.out.println("COLOR PIXEL 602 302 IS "+ colors.getRed()+"   "+colors.getGreen()+"     "+colors.getBlue());
                switch (direction){
                    case 'S':
                        currPos[1]++;
                        break;
                    case 'E':
                        currPos[0]++;
                        break;
                    case 'N':
                        currPos[1]--;
                        break;
                    case 'W':
                        currPos[0]--;
                        break;
                }
                if(currDistance==maxLength){
                    currDistance=0;
                    switch (direction){
                        case 'S':
                            direction='E';
                            maxLength++;
                            break;
                        case 'E':
                            direction='N';
                            break;
                        case 'N':
                            maxLength++;
                            direction='W';
                            break;
                        case 'W':
                            direction='S';
                            break;
                    }
                }
            }

            // System.out.println(asBinary.substring(y*3,y*3+3)+"----"+Integer.toString(color.getRed(),2) + "   " + Integer.toString(color.getGreen(),2)+"    "+Integer.toString(color.getBlue(),2));


            //System.out.println(msg.charAt(x));
        }

            System.out.println("FINAL MSG "+msgStr);
        return msgStr.toString();
    }
}
