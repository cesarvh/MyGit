import java.util.HashMap;
import java.util.HashSet;
import java.io.Serializable;
import java.io.File;

//final
public class CommitTree implements Serializable {
    protected CommitNode initialCommit;

    public CommitTree(int id, String message, String timestamp,  CommitNode parent, 
                    HashSet<String> files, HashMap<String, String> inheritedFiles) {   
        initialCommit = new CommitNode(id, message, timestamp, parent, files, inheritedFiles);
    }

    public static class CommitNode implements Serializable {
        String msg;
        int nodeID;
        String time;
        CommitNode parent;
        HashSet<String> toCommit;
        HashMap<String, String> toInherit;

        public CommitNode(int id, String message, String timestamp,  CommitNode parent,
            HashSet<String> files, HashMap<String, String> inFiles) {
            nodeID = id;
            msg = message;
            this.parent = parent;
            time = timestamp;
            toCommit = files;
            toInherit = inFiles;

            String idString = Integer.toString(id);
            File newCommit = new File(".gitlet/commits/" + idString);
            newCommit.mkdir();
        }

        public HashSet<String> getFiles() {
            return this.toCommit;
        }  

        public HashMap<String, String> getInherited() {
            return this.toInherit;
        }

        public CommitNode getParent() {
            return this.parent;
        }

        public String getTime() {
            return this.time;
        }

        public int getID() {
            return this.nodeID;
        }

        public String getMsg() {
            return this.msg;
        }

        public HashMap<String, String> getAllPaths() {
            HashMap<String, String> allPaths = new HashMap<String, String>();
            if (this.nodeID != 0) {
                if (this.toInherit.keySet().size() > 0) {
                    for (String ofile : this.toInherit.keySet()) {
                        allPaths.put(ofile, this.toInherit.get(ofile));
                    } 
                }

                if (this.toCommit.size() > 0) {
                    for (String ifile : this.toCommit) {
                        allPaths.put(ifile, "commits/" + this.nodeID + "/" + ifile);
                    }
                }
            }
            return allPaths;
        }

        public String getPath(String file) {
            HashMap<String, String> totalPaths = getAllPaths();
            if (totalPaths.containsKey(file)) {
                return totalPaths.get(file);
            }
            return "none";
        }

        public CommitTree.CommitNode getParent(CommitTree.CommitNode n) {
            return n.parent;
        }
    }

    public CommitNode nodeGetter(int num, String msg, String ts, CommitNode par, 
                                HashSet<String> f, HashMap<String, String> o) {
        CommitNode newNode = new CommitNode(num, msg, ts, par, f, o);
        return newNode;
    }
}
