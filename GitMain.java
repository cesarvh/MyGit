/**
 * @author
 * This is meant to a sample program implemente while in
 * UC Bekeley's Data Structures (CS61B) course.
 * 
*/

import java.io.File;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;


public class GitMain implements Serializable {
    static MyGit myGit;

    public GitMain() {
        myGit = new MyGit();
    }

    public static void deserialize() {
        try {
            FileInputStream fileIn = new FileInputStream(".gitlet/serCommits/commit.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            myGit = (MyGit) in.readObject();
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

    public static void serialize() {
        try {
            FileOutputStream fileOut = new FileOutputStream(".gitlet/serCommits/commit.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(myGit);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkoutCase(String[] args) {
        if (args.length == 2) {
            myGit.oneCheckout(args[1]);
        }
        if (args.length == 3) {
            myGit.twoCheckout(Integer.parseInt(args[1]), args[2], true);
        }
    }

    public static void canAdd(String[] args) {
        String sToAdd = args[1];
        File fToAdd = new File(sToAdd);
        if (fToAdd.exists()) {
            myGit.stage(sToAdd);
        } else {
            System.out.println("File does not exist.");
            return;
        }
    }

    public static void canCommit(String[] args) {
        if (args.length < 2) {
            System.out.println("You must enter a commit message.");
            return;
        }
        if (args.length > 2) {
            System.out.println("You must enter a commit message.");
            return;
        }
        myGit.commit(args[1]);
    }

    public static void main(String[] args) {
        if (args.length < 0) {
            System.out.println("Please enter a command");
        } 
        switch (args[0]) {
            case "init": 
                GitMain newG = new GitMain();
                myGit.myGitInit();
                serialize();
                break;
            case "add":
                deserialize(); 
                canAdd(args);
                serialize();
                break;
            case "commit": 
                deserialize();
                canCommit(args);
                serialize();
                break;
            case "rm": 
                deserialize();
                myGit.remove(args[1]);
                serialize();
                break;
            case "log": 
                deserialize();
                myGit.log();
                break;
            case "global-log": 
                deserialize();
                myGit.globalLog();
                break;
            case "find":
                deserialize();
                myGit.find(args[1]);
                break;
            case "status": 
                deserialize();
                myGit.status();
                break;
            case "checkout":
                deserialize();
                checkoutCase(args);
                serialize(); 
                break;
            case "branch":
                deserialize();
                myGit.branch(args[1]);
                serialize();
                break;
            case "rm-branch" :
                deserialize();
                myGit.removeBranch(args[1]);
                serialize();
                break; 
            case "reset":
                deserialize();
                myGit.reset(Integer.parseInt(args[1]));
                serialize();
                break; 
            case "merge":
                deserialize();
                myGit.merge(args[1]);
                serialize();
                break; 
            case "rebase":
                deserialize();
                myGit.rebase(args[1]);
                serialize();
                break; 
            case "i-rebase":
                deserialize();
                myGit.iRebase(args[1]);
                serialize();
                break; 
            default:
                System.out.println("Unrecognized command.");
        } 
    }
}   
