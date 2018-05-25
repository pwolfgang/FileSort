/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.temple.cla.wolfgang.filesort;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Paul Wolfgang
 */
public class MainTest {

    public MainTest() {
    }

    /**
     * Test of sort method, of class FileSort.
     */
    @Test
    public void testSort1() throws Exception {
        File dummy = new File("dummy");
        File parent = dummy.getParentFile();
        File tempIn = File.createTempFile("test", "in");
        File tempOut = File.createTempFile("test", "out");
        genFile(10, tempIn);
        FileSort.sort(tempIn.getAbsolutePath(), tempOut.getAbsolutePath(), parent, 20);
        assertTrue(verifySortedFile(tempOut));
    }

    /**
     * Test of sort method, of class FileSort.
     */
    @Test
    public void testSort2() throws Exception {
        File dummy = new File("dummy");
        File parent = dummy.getParentFile();
        File tempIn = File.createTempFile("test", "in");
        File tempOut = File.createTempFile("test", "out");
        System.out.println(tempIn.getAbsolutePath());
        genFile(100, tempIn);
        FileSort.sort(tempIn.getAbsolutePath(), tempOut.getAbsolutePath(), parent, 20);
        assertTrue(verifySortedFile(tempOut));
    }

    /**
     * Test of sort method, of class FileSort.
     */
    @Test
    public void testSort3() throws Exception {
        File dummy = new File("dummy");
        File parent = dummy.getParentFile();
        File tempIn = File.createTempFile("test", "in", parent);
        File tempOut = File.createTempFile("test", "out", parent);
        genFile(3000000, tempIn);
        System.out.println("Input file: " + tempIn.getAbsolutePath());
        System.out.println("Output file: " + tempOut.getAbsolutePath());
        FileSort.sort(tempIn.getAbsolutePath(), tempOut.getAbsolutePath(), parent, Long.MAX_VALUE);
        assertTrue(verifySortedFile(tempOut));
    }
    
    /**
     * Method to generate a file of random integers.
     */
    private void genFile(long numLines, File outFile) {
        Random rand = new Random();
        try {
            PrintWriter out = new PrintWriter(new FileWriter(outFile));
            for (long i = 0; i < numLines; i++) {
                long next = rand.nextLong();
                String line = String.format("%20d", next);
                out.println(line);
            }
            out.flush();
            out.close();
        } catch (IOException ioex) {
            System.err.println("Error creating " + outFile);
        }
    }
    
    /**
     * Method to verify that a file is sorted.
     */
     boolean verifySortedFile(File theFile) {
         try {
             BufferedReader in = new BufferedReader(new FileReader(theFile));
             int lineCount = 0;
             String line1;
             String line2;
             line1 = in.readLine();
             ++lineCount;
             while ((line2 = in.readLine()) != null) {
                 ++lineCount;
                 if (line2.compareTo(line1) < 0) {
                     System.out.println("Error at " + lineCount);
                     System.out.println("line1:" + line1);
                     System.out.println("line2:" + line2);
                     return false;
                 }
             }
         } catch (IOException ioex) {
             System.err.println("Error processing " + theFile);
             System.exit(1);
         }
         return true;
     }
}