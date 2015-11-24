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

public class GitletVCS implements Serializable {

    CommitTree commitTree;
    String branch;
    CommitTree.CommitNode head;
    int commitID;
    HashSet<String> otherfiles;
    HashSet<String> oldfiles;
    HashMap<String, String> tobeInherited;
    HashSet<String> stagedQueue = new HashSet<String>();
    HashSet<String> removals = new HashSet<String>();
    HashSet<CommitTree.CommitNode> allcommits = new HashSet<CommitTree.CommitNode>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
    HashMap<String, CommitTree.CommitNode> branches = 
                        new HashMap<String, CommitTree.CommitNode>();
    HashMap<Integer, CommitTree.CommitNode> id2Node = 
                        new HashMap<Integer, CommitTree.CommitNode>();

    /* Creds to: 
     * http://www.java-tips.org/java-se-tips/java.util/how-to-get-current-date-time.html*/
    public String generateTimeStamp() { //called twice during init
        String date;
        Date dateNow = new Date();
        date = dateFormat.format(dateNow);
        return date;
    }

    public GitletVCS() {
        String generatedTS = generateTimeStamp();
        commitTree = new CommitTree(commitID, "initial commit", generatedTS, null, null, null);
        commitID += 1;
        branch = "master";
        head = commitTree.initialCommit;
        allcommits.add(head);
        branches.put(branch, head);
    }

    public void gitletInit() { // DONE i think

        File gitletFolder = new File("./.gitlet");
        File commitsFolder = new File("./.gitlet/commits");
        File serCommitsFolder = new File("./.gitlet/serCommits");

        if (!gitletFolder.exists()) {
            gitletFolder.mkdir();
            commitsFolder.mkdir();
            serCommitsFolder.mkdir(); 
            GitletVCS initialGit = new GitletVCS();
        } else if (gitletFolder.exists()) {
            System.out.println("A gitlet version control system already exists in the " 
                + "current directory.");
            return;
        }  
    }

    public  void stage(String filename) { 
        CommitTree.CommitNode pointer = head;
        try {

            while (pointer != null && pointer.getParent() != null) {
                otherfiles = pointer.getFiles(); 
                if (otherfiles.size() == 0) {
                    stagedQueue.add(filename);
                    if (removals.contains(filename)) {
                        removals.remove(filename);
                    }
                    return;
                } else if (otherfiles.contains(filename) && otherfiles.size() > 0) {
                    for (String s : otherfiles) {
                        if (filename.equals(s)) {
                            String otherfile = "./.gitlet/commits/" + pointer.getID() + "/" + s;
                            byte[] currBytes = 
                                Files.readAllBytes(new File(filename).toPath());
                            byte[] otherBytes = 
                                Files.readAllBytes(new File(otherfile).toPath());
                            File hi = new File(s);
                            File yo = new File(filename);
                            if (Arrays.equals(currBytes, otherBytes)) {
                                System.out.println("File has not been " 
                                    + "modified since the last commit.");
                                return;
                            } else {
                                stagedQueue.add(filename);
                                if (removals.contains(filename)) {
                                    removals.remove(filename);
                                }
                            } 
                            break;
                        }
                    }
            
                }
                pointer = pointer.getParent();
            } 
            if (pointer.getParent() == null) {
                if (removals.contains(filename)) {
                    removals.remove(filename);
                }
                stagedQueue.add(filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void commit(String message) {
        if (stagedQueue.size() == 0 && removals.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        } else {
            oldfiles = new HashSet<String>();
            tobeInherited = new HashMap<String, String>();
            if (head.getInherited() != null) { // only iterates if 2nd --> commit
                for (String inherit : head.getInherited().keySet()) {
                    if (!removals.contains(inherit)) {
                        tobeInherited.put(inherit, head.getInherited().get(inherit));
                    }
                }
            }

            if (head.getFiles() != null) {
                for (String oldFile : head.getFiles()) {
                    if (!removals.contains(oldFile)) {
                        String otherPath = "commits/" + head.getID() + "/" + oldFile;
                        tobeInherited.put(oldFile, otherPath);
                    }
                }
            }

            for (String file : stagedQueue) {
                if (tobeInherited.containsKey(file)) {
                    String rePath = "commits/" + commitID + "/" + file;
                    tobeInherited.put(file, rePath);
                }
                oldfiles.add(file);
            } 

            String ts = generateTimeStamp();
            CommitTree.CommitNode newCommit = commitTree.nodeGetter(commitID, message, ts, head, 
                                                oldfiles, tobeInherited);
            head = newCommit;

            allcommits.add(newCommit);
            branches.put(branch, head);
            id2Node.put(commitID, newCommit);

            try {
                String path = "./.gitlet/commits/" + commitID + "/";
                for (String file : stagedQueue) {
                    if (!removals.contains(file)) {
                        File temp = new File(path + file);
                        temp.getParentFile().mkdirs();
                        Files.copy((new File(file).toPath()), 
                            (new File(path + "/" + file).toPath()));
                    }
                }


            } catch (IOException e) { //closes try and starts catch
                e.printStackTrace();
            } //closes catch
            stagedQueue.clear();
            removals.clear();
            commitID += 1;
        }  // closes else 
    } //closes method body
    
    public void remove(String filename) { // What aout the failture case? make sure to check


        if (!stagedQueue.contains(filename) && head != null 
            && !head.getAllPaths().containsKey(filename) && head.getAllPaths() != null) {
            System.out.println("No reason to remove the file.");
            return;
        } else {
            if (stagedQueue.contains(filename)) {
                stagedQueue.remove(filename);
            } 
            removals.add(filename);
        }
    }

    public void log() { // the initial commit isnt printing
        CommitTree.CommitNode p = head;
        while (p.getParent() != null) {
            String tempMessage = p.getMsg();
            String tempTimeStamp = p.getTime();
            int tempID = p.getID();
            System.out.println("====");
            System.out.println("Commit " + tempID + ".");
            System.out.println(tempTimeStamp);
            System.out.println(tempMessage);
            System.out.println();
            p = p.getParent();
        }
        System.out.println("====");
        System.out.println("Commit " + p.getID() + ".");
        System.out.println(p.getTime());
        System.out.println(p.getMsg());
        System.out.println();
    }

    public void globalLog() {
        for (CommitTree.CommitNode node : allcommits) {
            System.out.println("===");
            System.out.println("Commit " + node.getID() + ".");
            System.out.println(node.getTime());
            System.out.println(node.getMsg());
            System.out.println();
        }
    }

    public void find(String message) {
        HashSet<Integer> idSet = new HashSet<Integer>();
        for (CommitTree.CommitNode cn : allcommits) {
            if (cn.getMsg().equals(message)) {
                int foundID = cn.getID();
                idSet.add(foundID);
            }
        } 
        if (idSet.size() > 0) {
            for (int id : idSet) {
                System.out.println(id);
            }
        } else if (idSet.size() == 0) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    public void status() {
        System.out.println("=== Branches ===");

        for (String br : branches.keySet()) { // only star it if it equals the branch!!!

            if (branch.equals(br)) {
                System.out.println("*" + br); // * is on both when newly created.
            } else {
                System.out.println(br);
            }

        }
        System.out.println(); // empty line

        System.out.println("=== Staged Files ===");
        for (String aFile : stagedQueue) {
            System.out.println(aFile);
        }
        System.out.println(); //empty line for next input

        System.out.println("=== Files Marked for Removal ==="); 
        for (String rFile : removals) {
            System.out.println(rFile);
        }
        System.out.println();
    }
    
    public void branchCheckout(String outBranch) { 
        String answerB = promptUser();
        if (answerB.equals("yes")) {
            String cMsg = "The file does not exist in the most recent commit, " 
                + "or no such branch exists.";
            if (outBranch.equals(branch)) {
                System.out.println("No need to checkout the current branch.");
                return;
            } else {
                head = branches.get(outBranch);
                if (branches.containsKey(outBranch) && branches.keySet().size() > 0) { 
                    CommitTree.CommitNode outBranchNode = branches.get(outBranch); 
                    if (outBranchNode.getAllPaths().size() > 0 && outBranchNode != null) {
                        for (String file : outBranchNode.getAllPaths().keySet()) {
                            try {
                                String pthB = "./.gitlet/" + outBranchNode.getAllPaths().get(file);
                                String otherCurPath = "./";
                                File pB = new File(pthB);
                                pB.getParentFile().mkdirs();
                                File tFile = new File(file);
                                

                                if (!tFile.exists()) {
                                    File tempFile = new File(otherCurPath + file);
                                } 
                                Files.copy((new File(pthB).toPath()), (new File(file).toPath()), 
                                                            StandardCopyOption.REPLACE_EXISTING); 
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        branch = outBranch;
                        branches.put(branch, head);
                    } else if (outBranchNode.parent == null) {
                        branch = outBranch;
                        branches.put(branch, head);
                    }
                } else {
                    System.out.println(cMsg);
                    return;
                }
            }
        }
    }


    public void fileCheckout(String outFile) {
        String answerA = promptUser();
        if (answerA.equals("yes")) { // starts main copy body
            if (head.getAllPaths().keySet() != null) {
                if (head.getAllPaths().containsKey(outFile)) {
                    try {
                        String pathA = "./.gitlet/" + head.getAllPaths().get(outFile);
                        String currPath = "./";
                        File mkdi = new File(pathA);
                        mkdi.getParentFile().mkdirs();
                        File a = new File(outFile);
                        if (!a.exists()) {
                            File aTemp = new File(currPath + outFile);
                        } 
                        Files.copy((mkdi.toPath()), (new File(outFile).toPath()), 
                                    StandardCopyOption.REPLACE_EXISTING);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("File does not exist in the most recent commit, " 
                                        + " or no such branch exists.");
                }
            }
        }
    }

    /* Caller method: If a branch name is given, the brachCheckout method is called,
     * and if a file name is passed in, it calls the fileCheckout method.*/
    public void oneCheckout(String argument) {
        if (branches.size() > 0) {
            if (branches.containsKey(argument)) {
                branchCheckout(argument); 
            } else {
                fileCheckout(argument);
            }
        }
    }
    
    public void twoCheckout(int numID, String fName, boolean scannerNeeded) {
        String twoCheckoutAns = "yes";
        if (scannerNeeded) {
            twoCheckoutAns = promptUser();
            // twoCheckoutAns = promptUser();
        } 

        // String twoCheckoutAns = promptUser();
        if (twoCheckoutAns.equals("yes")) {
            if (id2Node.containsKey(numID)) {
                CommitTree.CommitNode checkoutIDNode = id2Node.get(numID);
                if (checkoutIDNode.getAllPaths().containsKey(fName)) {
                    try { 
                        String pathC = "./.gitlet/" + checkoutIDNode.getAllPaths().get(fName);
                        String otherPath = "./";
                        File pC = new File(pathC);
                        pC.getParentFile().mkdirs();
                        File createFile = new File(fName);
                        if (!createFile.exists()) {
                            File temp = new File(otherPath + fName);
                        } 
                        Files.copy((new File(pathC).toPath()), 
                            (new File(fName).toPath()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) { 
                        e.printStackTrace();
                    } 
                } else { 
                    System.out.println("File does not exist in that commit.");
                    return;
                }
            } else { 
                System.out.println("No commit with that id exists.");
                return;
            }     
        } 
    }

    public String promptUser() {
        String userAnswer;
        Scanner scannerA = new Scanner(System.in);
        while (true) {
            System.out.println("Warning: The The command you entered may alter "
                + "files in your working directory.");
            System.out.print("Uncommited changes may be lost. Are you sure you " 
                + "want to continue? (yes/no)");
            userAnswer = scannerA.nextLine();
            if (userAnswer.equals("yes") || userAnswer.equals("no")) {
                break;
            }
        }
        return userAnswer;
    }

    public void branch(String newBranch) {
        if (branches.containsKey(newBranch)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        branches.put(newBranch, head);
    }

    public void removeBranch(String killedBranch) {
        if (killedBranch.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        } else if (!branches.containsKey(killedBranch)) {
            System.out.println("A branch with that name does not exist");
            return;
        } else { 
            branches.remove(killedBranch);
        }
    }

    public void reset(int newHead) {
        String ans = promptUser(); 
        if (ans.equals("yes")) {
            if (!id2Node.containsKey(newHead)) {
                System.out.println("No commit with that id exists.");
                return;
            } else { 
                CommitTree.CommitNode tempNode = id2Node.get(newHead);
                head = tempNode;
                branches.put(branch, head);
                for (String resetFile : head.getAllPaths().keySet()) {
                    try {
                        String pathR = "./.gitlet/" + tempNode.getAllPaths().get(resetFile);
                        String otherR = "./"; 
                        File fileR = new File(resetFile);
                        File pR = new File(otherR + resetFile);
                        pR.getParentFile().mkdirs();
                        if (!fileR.exists()) {
                            File temp = new File(otherR + resetFile);
                        }
                        Files.copy((new File(pathR).toPath()), (new File(resetFile).toPath()), 
                                                        StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void merge(String branchToMerge) {
        String mAnswer = promptUser();
        if (mAnswer.equals("yes")) {
            if (!branches.containsKey(branchToMerge)) {
                System.out.println("A branch with that name does not exist.");
                return;
            } else if (branchToMerge.equals(branch)) {
                System.out.println("Cannot merge a branch with itself");
                return;
            } else {    
                HashSet<Integer> branchCIDs = new HashSet<Integer>();
                CommitTree.CommitNode branchC = branches.get(branchToMerge);
                while (branchC != null) {
                    int nodeCID = branchC.getID();
                    branchCIDs.add(nodeCID);
                    branchC = branchC.getParent();
                }   
                CommitTree.CommitNode branchA = head;
                while (branchA != null) {
                    if (branchCIDs.contains(branchA.getID())) {
                        break;
                    }
                    branchA = branchA.getParent();
                }
                CommitTree.CommitNode splitNode = branchA; 
                CommitTree.CommitNode currentNode = head;
                CommitTree.CommitNode givenNode = branches.get(branchToMerge);
                String wdPath = "./";

                for (String givenPath : givenNode.getAllPaths().keySet()) {
                    String aPath = currentNode.getPath(givenPath); 
                    String bPath = splitNode.getPath(givenPath);
                    String cPath = givenNode.getPath(givenPath); 
                    if (aPath.equals("none") || bPath.equals("none")) {
                        try {
                            String notInAPath = "./.gitlet/" + cPath;
                            File mkd = new File(notInAPath);
                            mkd.getParentFile().mkdirs();
                            File notInA = new File(wdPath + givenPath);
                            Files.copy((new File(notInAPath).toPath()), 
                                        (new File(givenPath).toPath()), 
                                            StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else { // if they all pass the check above... then:
                        if (!aPath.equals(bPath) && !bPath.equals(cPath) && !cPath.equals(aPath)) {
                            try { // copy files... GO!
                                String conflictPath = "./.gitlet/" + cPath;
                                File conflictdr = new File(wdPath + givenPath);
                                conflictdr.getParentFile().mkdirs();
                                String conflictFile = givenPath + ".conflicted";
                                // File conflictFile = new File(givenPath + 
                                // ".conflicted"); // wd?? ever used?
                                Files.copy((new File(conflictPath).toPath()), 
                                            (new File(conflictFile).toPath()), 
                                            StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (aPath.equals(bPath) && !bPath.equals(cPath) 
                                && !cPath.equals(aPath)) {
                            try {
                                String newAfilePath = "./.gitlet/" + cPath;
                                File newApath = new File(wdPath + givenPath);
                                newApath.getParentFile().mkdirs();
                                Files.copy((new File(newAfilePath).toPath()), 
                                    (new File(givenPath).toPath()),
                                    StandardCopyOption.REPLACE_EXISTING);
                               
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean rebaseSpecialCase(String branchAudit) {
        if (!branches.containsKey(branchAudit)) {
            System.out.println("A branch with that name does not exist.");
            return false;
        }
        if (branch.equals(branchAudit)) {
            System.out.println("Cannot rebase a branch onto itself.");
            return false;
        }

        if (head.equals(branches.get(branchAudit))) {
            System.out.println("Already up-to-date.");
            return false;
        } else {
            return true;
        }
    }
r

    public String newMessagePrompt() {
        String newMessage;
        Scanner scannerC = new Scanner(System.in);
        System.out.println("Please enter a new message for this commit.");
        newMessage = scannerC.nextLine();
        return newMessage;

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
