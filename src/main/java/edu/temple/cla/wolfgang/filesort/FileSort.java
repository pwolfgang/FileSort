/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.temple.cla.wolfgang.filesort;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Program to sort a large file of data.
 * @author Paul Wolfgang
 */
public class FileSort {

    /**
     * FileSort program to call the sort method
     * @param args the command line arguments
     * args[0] The input file name
     * args[1] The output file name
     * args[2] Max lines to read at a time (for testing)
     */
    public static void main(String[] args) {
        long maxLines = Long.MAX_VALUE;
        if (args.length >= 3) {
            maxLines = Long.parseLong(args[2]);
        }
        sort(args[0], args[1], null, maxLines);
    }
    
    /** 
     * Method to sort a large text file.  The file is considered to
     * contain lines as read by the BufferedReader.readLine method.
     * The program reads lines until it has used 1/2 of available
     * memory or the max number of lines has been read.  Collections.sort
     * is then used to sort these strings.  They are then written to a
     * temporary file.  The contents of the temporary files are then
     * merged.
     * @param inputFileName The name of the input file
     * @param outputFileName The name of the output file
     * @param tempDir The directory for temporary files (may be null)
     * @param maxLines The maximum number of lines to read. 
     */
    public static void sort(String inputFileName, 
            String outputFileName,
            File tempDir,
            long maxLines) {
        List<String> theList = new ArrayList<>();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(inputFileName));
        } catch (IOException ioex) {
            System.err.println("Error opening " + inputFileName);
            System.exit(1);
        }
        if (readLines(input, theList, maxLines)) {
            List<File> theFiles = new ArrayList<>();
            try {
                File nextFile = File.createTempFile("tempsort", "", tempDir);
                theFiles.add(nextFile);
                sortAndWrite(theList, nextFile);
                boolean notDone;
                do {
                    notDone = readLines(input, theList, maxLines);
                    if (!theList.isEmpty()) {
                        nextFile = File.createTempFile("tempsort", "");
                        theFiles.add(nextFile);
                        sortAndWrite(theList, nextFile);
                    }
                } while (notDone);
                mergeFiles(theFiles, outputFileName);
            } catch (IOException ioex) {
                System.err.println("Error creating temporary sort files");
                System.exit(1);
            }
        } else {
            File outFile = new File(outputFileName);
            sortAndWrite(theList, outFile);
        }
    }
    
    /**
     * Method to read input into an ArrayList until either
     * (1) all input has been read
     * (2) available memory has been reduced by a factor of 1/2
     * (3) maxLines have been read
     * @param in The buffered reader containing the input
     * @param out The ArrayList<String> containing the data read
     * @param maxLines The maximum number of lines to read
     * @returns true if more there is more input to process
     */
    private static boolean readLines(BufferedReader in, List<String> out,
            long maxLines) {
        boolean returnValue = false;
        out.clear();
        Runtime runTime = Runtime.getRuntime();
        // Run the garbage collector
        runTime.gc();
        long availMemoryBase = runTime.freeMemory();
        String line;
        int linesRead = 0;
        try {
            while ((line = in.readLine()) != null) {
                out.add(line);
                if (++linesRead == maxLines) break;
                if (runTime.freeMemory() < availMemoryBase / 2) break;
            }
            returnValue = line != null;
        } catch (IOException ioex) {
            System.err.println("Error reading input file");
            System.exit(1);
        }
        return returnValue;
    }
    
    /**
     * Class to contain Strings and their associated BufferedReaders. The
     * String is the current line in the reader.  The class implements
     * Comparable, where the comparison is based on the value of the string.
     */
    private static class StringReaderPair implements Comparable<StringReaderPair> {
        private String theString;
        private BufferedReader theReader;
        private File theFile;
        
        /**
         * Create a StringReaderPair from a file.  A BufferedReader is created
         * to read the file, and the first line is read into it.
         */
        public StringReaderPair(File theFile) {
            this.theFile = theFile;
            try {
                theReader = new BufferedReader(new FileReader(theFile));
                theString = theReader.readLine();
            } catch (IOException ioex) {
                System.err.println("Error reading from " + theFile.toString());
                ioex.printStackTrace();
                System.exit(1);
            }
        }
        
        /**
         * Method to compare two StringReaderPair objects
         */
        @Override
        public int compareTo(StringReaderPair other) {
            // EOF is bigger than anybody else
            if (theString == null) return 1;
            if (other.theString == null) return -1;
            return theString.compareTo(other.theString);
        }
        
        /**
         * Method to get the current string and read the next one
         */
        public String next() {
            String returnVal = theString;
            try {
                theString = theReader.readLine();
            } catch (IOException ioex) {
                System.err.println("Error reading from " + theFile.toString());
                ioex.printStackTrace();
                System.exit(1);
            }
            return returnVal;
        }
        
        /**
         * Method to determine if there is a next string
         */
        public boolean hasNext() {
            return theString != null;
        }
        
        /**
         * Method to close in BufferedReader and erase the file
         */
        public void erase() {
            try {
                theReader.close();
                theFile.delete();
            } catch (IOException ioex) {
                System.err.println("Error deleting " + theFile.getAbsolutePath());
                ioex.printStackTrace();
                System.exit(1);
            }
        }
    }
    
    /**
     * Method to merge the contents of several files
     * @param inFiles The ArrayList of files to be merged
     * @param outFile The output file
     */
    private static void mergeFiles(List<File> inFiles,
            String outFile) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outFile))) {
            PriorityQueue<StringReaderPair> pq = new PriorityQueue<>();
            inFiles.forEach(aFile -> pq.add(new StringReaderPair(aFile)));
            while (!pq.isEmpty()) {
                StringReaderPair aPair = pq.poll();
                out.println(aPair.next());
                if (aPair.hasNext()) {
                    pq.add(aPair);
                } else {
                    aPair.erase();
                }
            }
            out.flush();
        } catch (IOException ioex) {
            System.err.println("IO Error while merging files");
            ioex.printStackTrace();
            System.exit(1);
        }
    }

    
    /**
     * Method to sort a List and output the results to a file
     * @param theList The list to be sorted
     * @param outFile The output file
     */
    private static void sortAndWrite(List<String> theList, File outFile) {
        Collections.sort(theList);
        try (PrintWriter out = new PrintWriter(new FileWriter(outFile))) {
            theList.forEach(line -> out.println(line));
            out.flush();
        } catch (IOException ioex) {
            System.err.println("Error writing " + outFile);
            System.exit(1);
        }
    }
}
