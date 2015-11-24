import java.io.File;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

//final
public class Gitlet implements Serializable {
    static GitletVCS gitletVCS;

    public Gitlet() {
        gitletVCS = new GitletVCS();
    }

    public static void deserialize() {
        try {
            FileInputStream fileIn = new FileInputStream(".gitlet/serCommits/commit.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            gitletVCS = (GitletVCS) in.readObject();
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
    public static void serialize() {
        try {
            FileOutputStream fileOut = new FileOutputStream(".gitlet/serCommits/commit.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(gitletVCS);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void whichCheckout(String[] args) {
        if (args.length == 2) {
            gitletVCS.oneCheckout(args[1]);
        }
        if (args.length == 3) {
            gitletVCS.twoCheckout(Integer.parseInt(args[1]), args[2], true);
        }
    }

    public static void addable(String[] args) {
        String sToAdd = args[1];
        File fToAdd = new File(sToAdd);
        if (fToAdd.exists()) {
            gitletVCS.stage(sToAdd);
        } else {
            System.out.println("File does not exist.");
            return;
        }
    }

    public static void commitable(String[] args) {
        if (args.length < 2) {
            System.out.println("You must enter a commit message.");
            return;
        }
        if (args.length > 2) {
            System.out.println("You must enter a commit message.");
            return;
        }
        gitletVCS.commit(args[1]);
    }

    public static void main(String[] args) {
        if (args.length < 0) {
            System.out.println("Please enter a command");
        } 
        switch (args[0]) {
            case "init": 
                Gitlet newG = new Gitlet();
                gitletVCS.gitletInit();
                serialize();
                break;
            case "add":
                deserialize(); 
                addable(args);
                serialize();
                break;
            case "commit": 
                deserialize();
                commitable(args);
                serialize();
                break;
            case "rm": 
                deserialize();
                gitletVCS.remove(args[1]);
                serialize();
                break;
            case "log": 
                deserialize();
                gitletVCS.log();
                break;
            case "global-log": 
                deserialize();
                gitletVCS.globalLog();
                break;
            case "find":
                deserialize();
                gitletVCS.find(args[1]);
                break;
            case "status": 
                deserialize();
                gitletVCS.status();
                break;
            case "checkout":
                deserialize();
                whichCheckout(args);
                serialize(); 
                break;
            case "branch":
                deserialize();
                gitletVCS.branch(args[1]);
                serialize();
                break;
            case "rm-branch" :
                deserialize();
                gitletVCS.removeBranch(args[1]);
                serialize();
                break; 
            case "reset":
                deserialize();
                gitletVCS.reset(Integer.parseInt(args[1]));
                serialize();
                break; 
            case "merge":
                deserialize();
                gitletVCS.merge(args[1]);
                serialize();
                break; 
            // case "rebase":
            //     deserialize();
            //     gitletVCS.rebase(args[1]);
            //     serialize();
            //     break; 
            // case "i-rebase":
            //     deserialize();
            //     gitletVCS.iRebase(args[1]);
            //     serialize();
            //     break; 
            default:
                System.out.println("Unrecognized command.");
        } 
    }
}   
