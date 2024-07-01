package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;


import static gitlet.Repository.headCommit;
import static gitlet.Utils.readObject;
import static gitlet.Utils.writeObject;


public class StagingArea implements Serializable {

    private Map<String, String> additionFiles = new HashMap<>();
    private Set<String> removalFiles = new HashSet<>();
    private Map<String, String> trackingFiles = new HashMap<>();


    public void add(File file) {
        String fileName = file.getName();
        // The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.
        if (isRemoved(file)) {
            removalFiles.remove(fileName);
        }
// 如果这个文件已经在当前尾部/current Commit中，name无视它

        if (isModified(file, headCommit())) {
            Blob blob = new Blob(file);
            blob.save();
            additionFiles.put(fileName, blob.getBlobID());
        }
        saveStagingArea();

    }


    public Set<String> getRemoval() {
        return removalFiles;
    }

    public void removeFromStage(String fileName) {
        additionFiles.remove(fileName);
    }

    public void addToRemoval(String fileName) {
        removalFiles.add(fileName);
    }

    public boolean isRemoved(File file) {

        return removalFiles.contains(file.getName());
    }

    public boolean isModified(File file, Commit currentCommit) {
        String currentID = Blob.getBlobID(file);
        String oldBlobID = currentCommit.getBlobsID(file);
        return oldBlobID == null || !oldBlobID.equals(currentID);

    }

    public boolean isInStaged(String fileName) {
        return additionFiles.containsKey(fileName);
    }

    public Map<String, String> copyStagedFile() {
        trackingFiles.putAll(additionFiles); //将stagedFile 全部添加到 tracking
        for (String fileName:removalFiles) { //再将需要移除的文件 从中去除
            trackingFiles.remove(fileName);
        }
        setStageClean();
        return trackingFiles;
    }

    public static StagingArea loadStagingArea() {

        return readObject(Repository.STAGE_FILE, StagingArea.class);
    }

    public void saveStagingArea() {
        writeObject(Repository.STAGE_FILE, this);
    }

    public boolean isStagedEmpty() {
        return additionFiles.isEmpty() && removalFiles.isEmpty();
    }

    public Map<String, String> getAdditionFiles() {
        return additionFiles;
    }

    public void setStageClean() {
        additionFiles.clear();
        removalFiles.clear();
    }

    public List<String> getStagedFilename() {
        ArrayList<String> res = new ArrayList<>();
        res.addAll(additionFiles.keySet());
        res.addAll(removalFiles);
        return res;
    }
}
