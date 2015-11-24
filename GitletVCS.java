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
    //final
    // branch --> commit id hashmaps
    // commit id --> branch hashmaps
    CommitTree commitTree;
    String branch;
    CommitTree.CommitNode head;
    int commitID;
    HashSet<String> otherfiles;
    HashSet<String> fileChecker;
    HashSet<String> specificFileChecker;
    HashSet<String> oldfiles;
    HashSet<String> newOldFiles;
    HashSet<String> parentFiles;
    HashMap<String, String> tobeInherited;
    HashMap<String, String> newInherits;
    HashSet<String> stagedQueue = new HashSet<String>();
    HashSet<String> newpaths = new HashSet<String>();
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

    public void rebasedInHistory(CommitTree.CommitNode c, 
                                 CommitTree.CommitNode r, String rBranch) {
        head = id2Node.get(branches.get(rBranch).getID());
        branches.put(branch, head);
        for (String x : head.getAllPaths().keySet()) {
            try {
                String pathX = "./.gitlet/" + head.getAllPaths().get(x);
                String working = "./";
                File fileX = new File(working + x);
                fileX.getParentFile().mkdirs();
                File createX = new File(x);
                if (!createX.exists()) {
                    File tempX = new File(working + x);
                }
                Files.copy((new File(pathX).toPath()), (new File(x).toPath()), 
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }


    public void rebase(String rebasedBranch) {
        if (!rebaseSpecialCase(rebasedBranch)) {
            return;
        }
        if (promptUser().equals("yes")) {
            HashSet<Integer> rebasedIDs = new HashSet<Integer>();
            ArrayList<Integer> nodesToRebase = new ArrayList<Integer>();
            CommitTree.CommitNode rNode = branches.get(rebasedBranch);
            while (rNode != null) {  
                if (rNode.getID() == head.getID()) {
                    rebasedInHistory(rNode, head, rebasedBranch);
                } 
                rebasedIDs.add(rNode.getID());
                rNode = rNode.getParent();
            }  
            CommitTree.CommitNode splitPointNode = head; 
            while (splitPointNode != null) { 
                if (rebasedIDs.contains(splitPointNode.getID())) { 
                    break;
                }
                nodesToRebase.add(0, splitPointNode.getID());                
                splitPointNode = splitPointNode.getParent();
            }
            CommitTree.CommitNode targetRebaseBranch = branches.get(rebasedBranch);
            CommitTree.CommitNode headbackup = head;
            for (Integer i : nodesToRebase) {
                headbackup = head;
                rebasedCommit(i, targetRebaseBranch, false); 
                for (String r : head.getInherited().keySet()) {
                    String hPath = headbackup.getPath(r);
                    String sPath = splitPointNode.getPath(r);
                    String tPath = targetRebaseBranch.getPath(r);
                    System.out.println(r);
                    System.out.println(hPath);
                    System.out.println(sPath);
                    System.out.println(tPath);
                    if (hPath.equals("none") || sPath.equals("none") || tPath.equals("none")) {
                        try {
                            System.out.println("i make it here some day");
                            String frmP = "./.gitlet/" + targetRebaseBranch.getPath(r);
                            File noThereDir = new File(frmP); // fix
                            noThereDir.getParentFile().mkdirs();
                            File notInA = new File("./" + r);
                            Files.copy((new File(frmP).toPath()), (new 
                                File(r).toPath()),  StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!hPath.equals(sPath) && !sPath.equals(tPath) 
                                && !tPath.equals(hPath)) {
                            try {
                                String conflictPath2 = "./.gitlet/" + hPath;
                                File conReplacementFile = new File(conflictPath2);
                                File z = new File("./" + r);
                                z.getParentFile().mkdirs();
                                File xRe = new File(conflictPath2);
                                if (!conReplacementFile.exists()) {
                                    File y =  new File(r);
                                    y.getParentFile().mkdirs();
                                }
                                conReplacementFile.getParentFile().mkdirs();
                                Files.copy((new File(conflictPath2).toPath()), (new 
                                    File("./" + r).toPath()), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (hPath.equals(sPath) && !sPath.equals(tPath) 
                                && !tPath.equals(hPath)) {
                            System.out.println("I am this case, and i am failing.");
                            twoCheckout(targetRebaseBranch.getID(), r, false); //fix


                        } else if (!hPath.equals(sPath) && sPath.equals(tPath) 
                                && !tPath.equals(hPath)) {
                            try {
                                File headReplacementFile = new File("./.gitlet/" + hPath);
                                headReplacementFile.getParentFile().mkdirs();
                                Files.copy((new File("./.gitlet/" + hPath).toPath()), (new 
                                    File(r).toPath()), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } 
                    }
                }
            }
        }
    }

    public void rebasedCommit(int nodeMapping, CommitTree.CommitNode targetBranch, boolean newM) {
        CommitTree.CommitNode rCurNode = id2Node.get(nodeMapping);
        String newTimeStamp = generateTimeStamp();
        HashMap<String, String> newPaths = rCurNode.getInherited();
        HashSet<String> newFiles = rCurNode.getFiles();
        String newMessage = rCurNode.getMsg();
        newInherits = new HashMap<String, String>();
        newOldFiles = new HashSet<String>();

        if (newM) {
            newMessage = newMessagePrompt();
        }

        for (String f : targetBranch.getAllPaths().keySet()) {
            if (targetBranch.getInherited().containsKey(f)) {
                if (!removals.contains(f)) { // if head . parent??
                    newInherits.put(f, "commits/" + commitID + "/" + f);
                    newOldFiles.add(f);
                }
            }
            else if (!targetBranch.getInherited().containsKey(f)) {
                newInherits.put(f, targetBranch.getPath(f));
            } 

        }        
        System.out.println("new inherits: " + newInherits);
        System.out.println("newOldFiles: " + newOldFiles);

        CommitTree.CommitNode newRebasedCommit = commitTree.nodeGetter(commitID, newMessage, 
                                    newTimeStamp, targetBranch, newOldFiles, newInherits);
        head = newRebasedCommit;
        allcommits.add(newRebasedCommit);
        branches.put(branch, head);
        id2Node.put(commitID, newRebasedCommit);

        try {
            for (String rebasedFile : newInherits.keySet()) {
                if (newOldFiles.contains(rebasedFile)){

                String rebasedPath = "./.gitlet/commits/" + rCurNode.parent.getID();
                if (!removals.contains(rebasedFile)) {
                    File temp = new File(rebasedPath + "/" + rebasedFile);
                    // File frm = new File("./.gitlet/" + rCurNode.getAllPaths().get(rebasedFile));
                    // System.out.println(frm);
                    // frm.getParentFile().mkdirs();
                    temp.getParentFile().mkdirs();
                    System.out.println(temp);
                    Files.copy(temp.toPath(), 
                        (new File("./.gitlet/" + newInherits.get(rebasedFile))).toPath());
                }
            }

                }
                        } catch (IOException e) { //closes try and starts catch
            e.printStackTrace();
        } 
//closes catch
        commitID += 1; 
    }

    public void iRebase(String rplayBranch) {
        if (!rebaseSpecialCase(rplayBranch)) {
            return;
        }
        if (promptUser().equals("yes")) {
            HashSet<Integer> rNodeIDs = new HashSet<Integer>(); // w
            ArrayList<Integer> rebaseQueue = new ArrayList<Integer>(); // 

            CommitTree.CommitNode replayNode = branches.get(rplayBranch); // 
            while (replayNode.parent != null) {   // while the given's isnt null  
                if (replayNode.getID() == head.getID()) {  
                    rebasedInHistory(replayNode, head, rplayBranch);
                } 
                rNodeIDs.add(replayNode.getID()); // add the given id to the ids in node
                replayNode = replayNode.getParent(); //reassign getParent()
            }  
            CommitTree.CommitNode iRebaseSplitNode = head;  

            while (iRebaseSplitNode != null) { 
                if (rNodeIDs.contains(iRebaseSplitNode.getID())) { 
                    break;
                }
                rebaseQueue.add(0, iRebaseSplitNode.getID());                
                iRebaseSplitNode = iRebaseSplitNode.getParent();
            }  

            for (Integer i : rebaseQueue) {


                String iAns = iRebaseUserInput();
                if (iAns.equals("c")) {
                    iRebaseLog(id2Node.get(i));
                    rebasedCommit(i, replayNode, false);

                } else if (iAns.equals("m")) {
                    iRebaseLog(id2Node.get(i));
                    rebasedCommit(i, replayNode, true);
                } else if (iAns.equals('s') && rebaseQueue.indexOf(i) != 0 
                    || rebaseQueue.indexOf(i) != rebaseQueue.size()) {
                    System.out.println("skip. Sorry me no finished :(");

                }
            }
        }
    }

    public void iRebaseLog(CommitTree.CommitNode node) {
        System.out.println("Currently replaying:");
        System.out.println("====");
        System.out.println("Commit " + node.getID() + ".");
        System.out.println(node.getTime());
        System.out.println(node.getMsg());
        System.out.println();
    }

    public String iRebaseUserInput() {
        String rebaseAnswer;
        Scanner scannerA = new Scanner(System.in);
        while (true) {
            System.out.println("Would you like to (c)ontinue, (s)kip this commit, "
                + "or change this commit's (m)essage?");
            rebaseAnswer = scannerA.nextLine();
            if (rebaseAnswer.equals("c") || rebaseAnswer.equals("s") 
                || rebaseAnswer.equals("m")) {
                break;
            }
        }
        return rebaseAnswer;

    }

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
