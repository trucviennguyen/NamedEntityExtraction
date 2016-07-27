/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

/**
 *
 * @author Truc Vien
 */
public final class FileUtils {
    
    public static final int INTERNAL_BUFFER_SIZE  = 16*1024;
    
    public static String readContentFromFile(URL u, String encoding) {
        String content = "";

        try {
            int readLength = 0;
            char[] readBuffer = new char[INTERNAL_BUFFER_SIZE];

            BufferedReader uReader = null;
            StringBuffer buf = new StringBuffer();
            char c;
            long toRead = Long.MAX_VALUE;
            
            if(encoding != null && !encoding.equalsIgnoreCase("")) {
                uReader = new BufferedReader(
                new InputStreamReader(u.openStream(), encoding), INTERNAL_BUFFER_SIZE);
            } 
            else {
                uReader = new BufferedReader(
                new InputStreamReader(u.openStream()), INTERNAL_BUFFER_SIZE);
            }
            
            // read gtom source into buffer
            while (toRead > 0 && (readLength = uReader.read(readBuffer, 0, INTERNAL_BUFFER_SIZE)) != -1) {
                if (toRead <  readLength) {
                    //well, if toRead(long) is less than readLenght(int)
                    //then there can be no overflow, so the cast is safe
                    readLength = (int)toRead;
                }

                buf.append(readBuffer, 0, readLength);
                toRead -= readLength;
            }

            // 4.close reader
            uReader.close();

            content = new String(buf);
        }
        catch (java.net.MalformedURLException urlEx) {
            urlEx.printStackTrace();            
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }

        return content;
    }
    
    public static String readContentFromFile(String inputFilename, String encoding) {
        String content = "", st = "";

        try {
            File df = new File(inputFilename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(df), encoding));
      
            content = reader.readLine();
            while ( (st = reader.readLine()) != null) {
                content += "\n" + st;
            }

            // 4.close reader
            reader.close();
        }
        catch (java.net.MalformedURLException urlEx) {
            urlEx.printStackTrace();            
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }

        return content;
    }
    
    public static String readContentFromFile(InputStream inputStream) {
        String content = "";

    try {
            int readLength = 0;
            char[] readBuffer = new char[INTERNAL_BUFFER_SIZE];

            BufferedReader uReader = null;
            StringBuffer buf = new StringBuffer();
            char c;
            long toRead = Long.MAX_VALUE;
            
            uReader = new BufferedReader(
            new InputStreamReader(inputStream), INTERNAL_BUFFER_SIZE);
            
            // read gtom source into buffer
            while (toRead > 0 && (readLength = uReader.read(readBuffer, 0, INTERNAL_BUFFER_SIZE)) != -1) {
                if (toRead <  readLength) {
                    //well, if toRead(long) is less than readLenght(int)
                    //then there can be no overflow, so the cast is safe
                    readLength = (int)toRead;
                }

                buf.append(readBuffer, 0, readLength);
                toRead -= readLength;
            }

            // 4.close reader
            uReader.close();

            content = new String(buf);
        }
        catch (java.net.MalformedURLException urlEx) {
            urlEx.printStackTrace();            
        }
        catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
        }

        return content;
    }
    
    public static void exportContentToFile(String outputFilename, String fileContent, String encoding) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFilename)), encoding));
            writer.write(fileContent);
            writer.close();
        }
        catch (FileNotFoundException fileEx) {
            fileEx.printStackTrace();
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        
    }
/*
    public static void exportContentToFile(String outputFilename, String fileContent, String encoding) {
        try {
            RandomAccessFile rf = new RandomAccessFile(outputFilename, "rw");
            rf.writeBytes(fileContent);
            rf.close();
        }
        catch (FileNotFoundException fileEx) {
            fileEx.printStackTrace();
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        
    }
*/

}
