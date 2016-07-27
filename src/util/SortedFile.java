/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author tvnguyen
 */
/** 
 * an ArrayList with sorted annotations
 */
public class SortedFile extends File {

    public SortedFile(String pathname) {
        super(pathname);
    } // SortedFile
    
    @Override
    public File[] listFiles() {
        File[] lst = super.listFiles();
        File[] newList = new File[lst.length];
        for (int i = 0; i < lst.length; i++) {
            String filename = lst[i].getAbsolutePath();
            int j = 0;
            while ( (j < i) && (filename.compareTo(newList[j].getAbsolutePath())) > 0)
                j++;
            if (j == i)
                newList[i] = lst[i];
            else {
                System.arraycopy(newList, j, newList, j + 1, i - j);
                newList[j] = lst[i];
            }
        }
        return newList;
    }
    
    @Override
    public File[] listFiles(FilenameFilter filter) {
        File[] lst = super.listFiles(filter);
        File[] newList = new File[lst.length];
        for (int i = 0; i < lst.length; i++) {
            String filename = lst[i].getAbsolutePath();
            int j = 0;
            while ( (j < i) && (filename.compareTo(newList[j].getAbsolutePath())) > 0)
                j++;
            if (j == i)
                newList[i] = lst[i];
            else {
                System.arraycopy(newList, j, newList, j + 1, i - j);
                newList[j] = lst[i];
            }
        }
        return lst;
    }

} // SortedFile