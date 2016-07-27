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

public class FilenameFilterImpl implements FilenameFilter {

    /**
    * Construction method
    */
    public FilenameFilterImpl(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param   dir    the directory in which the file was found.
     * @param   name   the name of the file.
     * @return  <code>true</code> if and only if the name should be
     * included in the file list; <code>false</code> otherwise.
     */
    public boolean accept(File dir, String name) {
        if (name.endsWith(suffix))
            return true;
        return false;
    }

    // The expected suffix
    String suffix;

}
