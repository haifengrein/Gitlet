package gitlet;


import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.MyUtils.*;
import static gitlet.Utils.readObject;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Haifeng Hou
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date timeStamp;
    private List<String> parentID;
    private final Map<String, String> trackedBlobsID;

    private final String sha1ID;
    private final File file;

    /**
     * Initialize the first commit
     */
    public Commit() {
        this.message = "initial commit";
        this.timeStamp = new Date(0);
        parentID = new ArrayList<>();
        trackedBlobsID = new HashMap<>();
        sha1ID = generateID();
        file = getCommitFile(sha1ID);
    }

    public Commit(String message, List<String> parentID, Map<String, String> trackedBlobsID) {
        this.message = message;
        this.timeStamp = new Date();
        this.parentID = parentID;
        this.trackedBlobsID = trackedBlobsID;
        sha1ID = generateID();
        file = getCommitFile(sha1ID);
    }

    public Blob getBlobByFileName(String fileName) {
        return Blob.loadBlobFile(trackedBlobsID.get(fileName));
    }

    public List<String> getBlobFileNames() {
        List<String> fileNames = new ArrayList<>();
        for (String fileName : trackedBlobsID.keySet()) {
            fileNames.add(fileName);
        }
        return fileNames;
    }


    public List<String> getFileNames() {
        List<String> fileName = new ArrayList<>();
        List<Blob> blobList = getBlobList();
        for (Blob b : blobList) {
            fileName.add(b.getFileName());
        }
        return fileName;
    }

    private List<Blob> getBlobList() {
        Blob blob;
        List<Blob> blobList = new ArrayList<>();
        for (String id : trackedBlobsID.values()) {
            blob = Blob.loadBlobFile(id);
            blobList.add(blob);
        }
        return blobList;
    }

    public String getMessage() {
        return this.message;
    }


    public String commitToString() {
        StringBuilder log = new StringBuilder();
        log.append("===").append("\n");
        log.append("commit").append(" ").append(sha1ID).append("\n");
        if (parentID.size() == 2) {
            log.append("Merge:");
            for (String parent : parentID) {
                log.append(" ").append(parent, 0, 7);
            }
            log.append("\n");
        }
        log.append("Date:").append(" ").append(dateToTimeStamp()).append("\n");
        log.append(message).append("\n");
        return log.toString();
    }

    public List<String> getParents() {
        return parentID;
    }

    public String getFirstParentID() {
        if (parentID.isEmpty()) {
            System.exit(0);
        }
        return parentID.get(0);
    }

    public String getParentID() {
        if (parentID.isEmpty()) {
            return null;
        }
        return parentID.get(0);
    }


    /**
     * Save the commit to a file
     */
    public void saveCommit() {
        saveObject(file, this);
    }
    /**
     * Return the SHA1 ID
     */
    public String getSHA1ID() {
        return sha1ID;
    }

    public static Commit fromFile(String id) {
        return readObject(getCommitFile(id), Commit.class);
    }

    public String getBlobsID(File file) {
        return getBlobsID(file.getName());
    }

    public String getBlobsID(String fileName) {
        return trackedBlobsID.get(fileName);
    }

    public boolean isContainsBlob(String fileName) {
        return trackedBlobsID.containsKey(fileName);
    }

    public Map<String, String> getTrackedBlobsID() {
        return trackedBlobsID;
    }


    /**
     * Set time stamp to a required format
     */
    public String dateToTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(timeStamp);
    }

    /**
     * SHA1 Generator
     * The Utils.sha1 function only takes String args
     * so need put very thing into string format

     */

    private String generateID() {
        return Utils.sha1(dateToTimeStamp(), message, parentID.toString(), trackedBlobsID.toString());
    }
}
