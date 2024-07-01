package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class MyUtils {




    /**
     * Save the serializable object to the file path
     * @param filePath the File path
     * @param object the serializable object
     */

    public static void saveObject(File filePath, Serializable object) {
        File dir = filePath.getParentFile(); // Get the parent path
        if (!dir.exists()) { // Generate a new dir if the parent path doesn't exist
            dir.mkdir();
        }
        writeObject(filePath, object);
    }

    /**
     * Create the blob file path base on SHA1
     * Folder Name is base on the first two digits of SHA1
     * File Name is base on the remaining SHA1 digits
     * @param sha1id the SHA1 of the object
     */

    public static File getBlobFile(String sha1id) {
        String folderName = folderName(sha1id);
        String fileName = fileName(sha1id);
        return join(Repository.BLOBS_DIR, folderName, fileName);
    }

    /**
     * Create the commit file path base on SHA1
     * Folder Name is base on the first two digits of SHA1
     * File Name is base on the remaining SHA1 digits
     * @param sha1id the SHA1 of the object
     */

    public static File getCommitFile(String sha1id) {
        String folderName = folderName(sha1id);
        String fileName = fileName(sha1id);
        return join(Repository.COMMIT_DIR, folderName, fileName);
    }

    public static String folderName(String sha1id) {
        return sha1id.substring(0,2);
    }

    public static String fileName(String sha1id) {
        return sha1id.substring(2, sha1id.length());
    }
}
