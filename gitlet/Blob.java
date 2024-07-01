package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.MyUtils.getBlobFile;
import static gitlet.Utils.*;

/** Blob class.
 * @author Haifeng
 */

public class Blob implements Serializable {

    private byte[] fileContent;
    private String fileName;
    private String filePath;
    private String blobID;
    private File sha1FilePath;

    /** Constructor.
     * @param sourceFile
     */
    public Blob(File sourceFile) {
        this.fileContent = readContents(sourceFile);
        this.fileName = sourceFile.getName();
        this.filePath = sourceFile.getPath();
        this.blobID = sha1(sourceFile.getName(), fileContent);
        this.sha1FilePath = MyUtils.getBlobFile(blobID);
    }

    /**
     *
     * @return file content
     */

    public byte[] getFileContent() {
        return fileContent;
    }

    /**
     *
     * @param id the blob id
     * @return Blob File
     */
    public static Blob loadBlobFile(String id) {
        return readObject(getBlobFile(id), Blob.class);
    }

    /**
     *
     * @return Saved BlobID
     */
    public String getBlobID() {
        return blobID;
    }

    /**
     *
     * @param file
     * @return a Blob ID of a file
     */
    public static String getBlobID(File file) {
        return sha1(file.getName() + readContentsAsString(file)); }


    /**
     *
     * @return blob file name
     */
    public String getFileName() {
        return fileName;
    }

    /** save blob base on sha1.
     */
    public void save() {
        MyUtils.saveObject(sha1FilePath, this);
    }


}
