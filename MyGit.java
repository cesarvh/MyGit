/**
 * @author Cesar Villalobos-Huizar
 * This is meant to a sample program implemented while in
 * UC Bekeley's Algorithms and Data Structures (CS61B) course.
 * 
*/

import java.io.File;
import java.util.HashSet;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.ArrayList;

public class MyGit implements Serializable {


    /* Creds to: 
     * http://www.java-tips.org/java-se-tips/java.util/how-to-get-current-date-time.html*/
    public String generateTimeStamp() { //called twice during init
        String date;
        Date dateNow = new Date();
        date = dateFormat.format(dateNow);
        return date;
    }

    /* Initializes the MyGit class instance*/
    public MyGit() {

    }

    /* Initializes repository */
    public void myGitInit() { // DONE i think
    }

    public  void stage(String filename) { 
    }


    public void commit(String message) {
       
       
    } //closes method body
    
    public void remove(String filename) { // What aout the failture case? make sure to check
        
    }

    public void log() { // the initial commit isnt printing

    }

    public void globalLog() {
	
    }

    public void find(String message) {
      
    }

    public void status() {
       
    }
    
    public void branchCheckout(String outBranch) { 
 
    }


    public void fileCheckout(String outFile) {

    }

    /* Caller method: If a branch name is given, the brachCheckout method is called,
     * and if a file name is passed in, it calls the fileCheckout method.*/
    public void oneCheckout(String argument) {

    }
    
    public void twoCheckout(int numID, String fName, boolean scannerNeeded) {

    }

    public String promptUser() {

        return null;
    }

    public void branch(String newBranch) {
       
    }

    public void removeBranch(String killedBranch) {
        
    }

    public void reset(int newHead) {
    
    }


    public void merge(String branchToMerge) {
    
    }

    public boolean rebaseSpecialCase(String branchAudit) {
    
    }

    public void rebasedInHistory(CommitTree.CommitNode c, 
                                 CommitTree.CommitNode r, String rBranch) {
    
    }


    public void rebase(String rebasedBranch) {

    }

    public void rebasedCommit(int nodeMapping, CommitTree.CommitNode targetBranch, boolean newM) {

    }

    public void iRebase(String rplayBranch) {

    }

    public void iRebaseLog(CommitTree.CommitNode node) {

    }

    public String iRebaseUserInput() {
		return null;
    }

    public String newMessagePrompt() {
    	return null;	
    }
   
    /*http://cdn2.crunchify.com/wp-content/uploads/2013/07/Serialize-DeSerialize-an-Object.png*/
    public  void deserialize() {
        try {
            FileInputStream fileIn = new FileInputStream(".gitlet/serCommits/commit.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            commitTree = (CommitTree) in.readObject();
            in.close();
            fileIn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    } 
    /*http://cdn2.crunchify.com/wp-content/uploads/2013/07/Serialize-DeSerialize-an-Object.png*/
    public  void serialize() {
        try {
            FileOutputStream fileOut = new FileOutputStream(".gitlet/serCommits/commit.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(commitTree);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}   
