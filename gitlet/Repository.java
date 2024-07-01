package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Blob.loadBlobFile;
import static gitlet.Commit.fromFile;
import static gitlet.MyUtils.*;
import static gitlet.StagingArea.loadStagingArea;
import static gitlet.Utils.*;


/** A repository for gitlet
 * @author Haifeng Hou
 * The structure of a Capers Repository is as follows:
 *
 * .capers/ -- top level folder for all persistent data in your lab12 folder
 *    - dogs/ -- folder containing all of the persistent data for dogs
 *    - story -- file containing the current story
 *
 *
 */
public class Repository {


    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** Objects folder stores commit and blob objects (uses hashcode as filename) */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commit");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    private static final File BRANCH_HEADS_DIR = join(REFS_DIR, "heads");

    private static final String DEFAULT_BRANCH_NAME = "master";
    private static final String HEAD_BRANCH_REF_PREFIX = "ref: refs/heads/";
    public static final File STAGE_FILE = join(GITLET_DIR, "index");
    private static final File HEAD = join(GITLET_DIR, "HEAD");

    /*
     *   .gitlet
     *      |--blobs
     *      |--commits
     *      |--refs
     *      |    |--heads
     *      |         |--master
     *      |--HEAD
     *      |--index(StagingArea)
     */

    /**
     * Initialize a repository at the current working directory.
     *
     * <pre>
     * .gitlet
     * ├── HEAD
     * ├── blobs
     * ├── commits
     * └── refs
     *     └── heads
     *         └── master
     * </pre>
     *
     * Create first Commit Object,
     * Store the first commit object to a file,
     * Set HEAD pointer point to master branch and Store first commit SHA1ID to a file.
     * Update HEAD file to track current branch
     *
     */
    public static void initCommand() {
        if (GITLET_DIR.exists()) {
            exit("A Gitlet version-control system already exists in the current directory.");
        }

        GITLET_DIR.mkdir();
        BLOBS_DIR.mkdir();
        COMMIT_DIR.mkdir();
        REFS_DIR.mkdir();
        BRANCH_HEADS_DIR.mkdir();
        Commit initialCommit = new Commit();
        initialCommit.saveCommit();
        setHeadCommitId(DEFAULT_BRANCH_NAME, initialCommit.getSHA1ID());
        setCurrentBranch(DEFAULT_BRANCH_NAME);
        setStagingArea();


    }


    public static void addCommand(String fileName) {
        File file = getFilePath(fileName);
        if (!file.exists()) {
            exit("File does not exist.");
        }
        StagingArea stagingArea = loadStagingArea();
        stagingArea.add(file);
    }


    public static void commitCommand(String message) {
        if (message.isBlank()) {
            exit("Please enter a commit message.");
        }
        commit(message, null);
    }

    /**
     * @param message
     * @param secondParentID
     */

    private static void commit(String message, String secondParentID) {

        StagingArea stagingArea = loadStagingArea();

        if (stagingArea.isStagedEmpty()) {
            exit("No changes added to the commit.");
        }

        List<String> parentID = new ArrayList<>();
        String currentParentID = headCommit().getSHA1ID();
        parentID.add(currentParentID);
        if (secondParentID != null) {
            parentID.add(secondParentID);
        }

        Map<String, String> trackedBlobID = trackedBlobID();
        Commit newCommit =  new Commit(message, parentID, trackedBlobID);
        newCommit.saveCommit();
        stagingArea.getAdditionFiles().clear();
        stagingArea.getRemoval().clear();
        stagingArea.saveStagingArea();
        setHeadCommitId(currentBranchName(), newCommit.getSHA1ID());
    }

    private static Map<String, String> trackedBlobID() {
        Commit currentHeadCommit = headCommit();
        Map<String, String> trackedBlobs = currentHeadCommit.getTrackedBlobsID();
        StagingArea stagingArea = loadStagingArea();
        trackedBlobs.putAll(stagingArea.getAdditionFiles());
        for (String blobName : stagingArea.getRemoval()) {
            trackedBlobs.remove(blobName);
        }
        return trackedBlobs;
    }

    public static void rmCommand(String fileName) {
        //先读取stage file，检查是否有文件被stage。
        StagingArea stagingArea = loadStagingArea();
        //如果在stage中直接添加到removal
        if (stagingArea.isInStaged(fileName)) {
            stagingArea.removeFromStage(fileName);
            stagingArea.saveStagingArea();
            // 如果在current Commit中，从tracking中移除并
        } else if (isInTracking(fileName)) {
            stagingArea.addToRemoval(fileName);
            File file = getFilePath(fileName);
            restrictedDelete(file);
            stagingArea.saveStagingArea();
        } else {
            exit("No reason to remove the file.");
        }
    }

    private static boolean isInTracking(String fileName) {
        return headCommit().isContainsBlob(fileName);
    }

    public static void logCommand() {
        Commit currentHeadCommit = headCommit();
        while (currentHeadCommit != null) {
            System.out.print(currentHeadCommit.commitToString());
            System.out.print("\n");
            currentHeadCommit = fromFile(currentHeadCommit.getFirstParentID());
        }
    }

    public static void globalLogCommand() {
        StringBuilder globalLog = new StringBuilder();
        String[] allCommitsDirs = COMMIT_DIR.list();
        for (String commit:allCommitsDirs) {
            File id = join(COMMIT_DIR, commit);
            for (File fileEntry : id.listFiles()) {
                String fullCommitID = commit + fileEntry.getName();
                Commit targetCommit = fromFile(fullCommitID);
                globalLog.append(targetCommit.commitToString() + "\n");
            }
        } System.out.println(globalLog);
    }

    public static void findCommand(String message) {
        StringBuilder id = new StringBuilder();
        String[] allCommitsDirs = COMMIT_DIR.list();
        for (String commit : allCommitsDirs) {
            File commitID = join(COMMIT_DIR, commit);
            for (File fileEntry : commitID.listFiles()) {
                String fullCommitID = commit + fileEntry.getName();
                Commit targetCommit = fromFile(fullCommitID);
                String cMessage = targetCommit.getMessage();
                if (cMessage.contains(message)) {
                    id.append(targetCommit.getSHA1ID() + "\n");
                }
            }
        }

        if (id.isEmpty()) {
            exit("Found no commit with that message.");
        }
        System.out.println(id);
    }

    public static void statusCommand() {
        branchPrinter();
        stagedFilesPrinter();
        removedFilesPrinter();
        modifiedFilesPrinter();
        unTrackedFilesPrinter();

    }

    private static void branchPrinter() {
        List<String> branchList = plainFilenamesIn(BRANCH_HEADS_DIR);
        String currBranch = currentBranchName();
        System.out.println("=== Branches ===");
        System.out.println("*" + currBranch);
        if (branchList.size() > 1) {
            for (String branch : branchList) {
                if (!branch.equals(currBranch)) {
                    System.out.println(branch);
                }
            }
        }
        System.out.println();
    }

    private static void stagedFilesPrinter() {
        System.out.println("=== Staged Files ===");
        Map<String, String> stagedFile = loadStagingArea().getAdditionFiles();
        for (String filename:stagedFile.keySet()) {
            System.out.println(filename);
        }
        System.out.println();
    }

    private static void removedFilesPrinter() {
        System.out.println("=== Removed Files ===");
        Set<String> removalFile = loadStagingArea().getRemoval();
        for (String filename:removalFile) {
            System.out.println(filename);
        }
        System.out.println();
    }


    private static void modifiedFilesPrinter() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

    }

    private static void unTrackedFilesPrinter() {
        System.out.println("=== Untracked Files ===");
        System.out.println();

    }

    public static void checkoutFileCommand(String fileName) {
        Commit headCommit = headCommit();
        if (!headCommit.isContainsBlob(fileName)) {
            exit("File does not exist in that commit.");
        }
        String blobID = headCommit.getBlobsID(fileName);
        Blob blob = loadBlobFile(blobID);
        saveBlobToCWD(blob);

    }

    public static void checkoutFileWithCommit(String commitID, String fileName) {
        String fullID = getFullCommitID(commitID);
        File commitFile = getCommitFile(fullID);
        if (!commitFile.exists()) {
            exit("No commit with that id exists.");
        }
        Commit targetCommit = fromFile(fullID);
        if (!targetCommit.isContainsBlob(fileName)) {
            exit("File does not exist in that commit.");
        }
        String blobID =  targetCommit.getBlobsID(fileName);
        Blob blob = loadBlobFile(blobID);
        saveBlobToCWD(blob);
    }


    public static void checkoutBranch(String branchName) {
        File branch = getBranchPath(branchName);
        if (!branch.exists()) {
            exit("No such branch exists.");
        }
        if (branchName.equals(currentBranchName())) {
            exit("No need to checkout the current branch.");
        }

        Commit targetCommit = getCommitFromBranch(branchName);
        setCommitTo(targetCommit);
        setCurrentBranch(branchName);
    }

    public static void branchCommand(String newBranchName) {
        File newBranch = getBranchPath(newBranchName);
        if (newBranch.exists()) {
            exit("A branch with that name already exists.");
        } else {
            String copyOfHeadCommitID = getHeadCommitID();
            setHeadCommitId(newBranchName, copyOfHeadCommitID);
        }
    }

    public static void rmBranchCommand(String branchName) {
        File rmBranch = getBranchPath(branchName);
        if (!rmBranch.exists()) {
            exit("A branch with that name does not exist.");
        } else if (branchName.equals(currentBranchName())) {
            exit("Cannot remove the current branch.");
        } else {
            rmBranch.delete();
        }
    }

    public static void resetCommand(String commitID) {
        String fullCommitID = getFullCommitID(commitID);
        File commitPath = getCommitFile(fullCommitID);
        if (!commitPath.exists()) {
            exit("No commit with that id exists.");
        } else {
            Commit targetCommit = fromFile(fullCommitID);
            setCommitTo(targetCommit);
            setHeadCommitId(currentBranchName(), fullCommitID);

        }
    }

    public static void mergeCommand(String branchName) {
        isValidToMerge(branchName);
        Commit currentCommit = headCommit();
        Commit mergeCommit = getCommitFromBranch(branchName);
        Commit splitPointCommit = findSplitPoint(currentCommit, mergeCommit);

        isValidSplitPoint(splitPointCommit, mergeCommit, currentCommit, branchName);

        mergeFile(splitPointCommit, mergeCommit, currentCommit);

        String msg = "Merged " + branchName + " into " + currentBranchName() + ".";
        commit(msg, mergeCommit.getSHA1ID());
    }

    private static void mergeFile(Commit splitPoint, Commit mergeCommit, Commit currentCommit) {
        Map<String, String> splitPointFiles = splitPoint.getTrackedBlobsID();
        Map<String, String> mergeFiles = mergeCommit.getTrackedBlobsID();
        Map<String, String> currentFiles = currentCommit.getTrackedBlobsID();


        List<String> filesOverwrite = checkOverWriteFiles(splitPointFiles, mergeFiles, currentFiles);
        List<String> newFiles =  checkNewFiles(splitPointFiles, mergeFiles, currentFiles);
        List<String> removedFiles = checkRemovedFiles(splitPointFiles, mergeFiles, currentFiles);
        List<String> conflictFiles = checkConflictFiles(splitPointFiles, mergeFiles, currentFiles);

        isAnyUnTrackingFiles(removedFiles, filesOverwrite, conflictFiles, newFiles);
        solveConflictFiles(conflictFiles, currentFiles, mergeFiles);

    }

    private static void isAnyUnTrackingFiles(List remove, List overWrite, List conflict, List newFiles) {
        List<String> untrackedFiles = getUntrackedFiles();
        for (String fileName:untrackedFiles) {
            if (remove.contains(fileName) || overWrite.contains(fileName) || conflict.contains(fileName) || newFiles.contains(fileName)) {
                exit("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
    }

    private static List checkOverWriteFiles(Map<String, String> splitPointFiles,
                                            Map<String, String>  mergeFiles,
                                            Map<String, String>  currentFiles) {
        List<String> filesNeedOverWrite = new ArrayList<>();
        for (String filename:splitPointFiles.keySet()) {
            if (currentFiles.containsKey(filename) && mergeFiles.containsKey(filename)) {
                String splitPointFileID = splitPointFiles.get(filename);
                String currentFileID = currentFiles.get(filename);
                String mergeFileID1 = mergeFiles.get(filename);
                if (splitPointFileID.equals(currentFileID) && !splitPointFileID.equals(mergeFileID1)) {
                    filesNeedOverWrite.add(filename);
                }
            }
        }

        if (!filesNeedOverWrite.isEmpty()) {
            for (String fileName: filesNeedOverWrite) {
                Blob blob = loadBlobFile(mergeFiles.get(fileName));
                saveBlobToCWD(blob);
                addCommand(fileName);
            }
        }
        return filesNeedOverWrite;
    }

    private static List checkNewFiles(Map<String, String> splitPointFiles, Map<String, String>  mergeFiles,Map<String, String>  currentFiles) {
        List<String> newFiles = new ArrayList<>();
        for (String fileName: mergeFiles.keySet()) {
            if (!splitPointFiles.containsKey(fileName) && !currentFiles.containsKey(fileName)) {
                newFiles.add(fileName);
            }
        }
        if (!newFiles.isEmpty()) {
            for (String fileName:newFiles) {
                File file = join(CWD, fileName);
                if (file.exists()) {
                    exit("There is an untracked file in the way; delete it, or add and commit it first.");
                }
                Blob blob = loadBlobFile(mergeFiles.get(fileName));
                saveBlobToCWD(blob);
                addCommand(fileName);
            }
        }
        return newFiles;
    }

    private static List checkRemovedFiles(Map<String, String> splitPointFiles, Map<String, String>  mergeFiles,Map<String, String>  currentFiles) {
        List<String> filesNeedRemove = new ArrayList<>();
        for (String fileName:splitPointFiles.keySet()) {
            String splitPointFileID = splitPointFiles.get(fileName);
            String currentFileID = currentFiles.get(fileName);
            if (splitPointFileID.equals(currentFileID)) {
                if (!mergeFiles.containsKey(fileName)) {
                    filesNeedRemove.add(fileName);
                }
            }
        }

        if (!filesNeedRemove.isEmpty()) {
            for (String fileName:filesNeedRemove) {
                rmCommand(fileName);
            }
        }
        return filesNeedRemove;
    }


    private static void solveConflictFiles (List<String> conflictFiles, Map<String, String> currentFiles, Map<String, String> mergeFiles) {
        if (!conflictFiles.isEmpty()) {
            System.out.println("Encountered a merge conflict.");
        }

        for (String fileName: conflictFiles) {
            String currentFileContents = "";
            if (currentFiles.containsKey(fileName)) {
                Blob currentFile = loadBlobFile(currentFiles.get(fileName));
                currentFileContents = new String(currentFile.getFileContent());
            }
            String mergeFileContents = "";
            if (mergeFiles.containsKey(fileName)) {
                Blob mergeFile = loadBlobFile(mergeFiles.get(fileName));
                mergeFileContents = new String(mergeFile.getFileContent());
            }

            String conflictContents = "<<<<<<< HEAD\n" + currentFileContents + "=======\n" + mergeFileContents + ">>>>>>>\n";
            File conflictFile = join(CWD, fileName);
            writeContents(conflictFile, conflictContents);
        }
    }


    private static List checkConflictFiles(Map<String,String> splitPointFiles, Map<String,String>  mergeFiles, Map<String,String>  currentFiles) {
        List<String> conflictFiles = new ArrayList<>();
        for (String fileName:splitPointFiles.keySet()) {
            if (mergeFiles.containsKey(fileName) && currentFiles.containsKey(fileName)) {
                String splitPointFileID = splitPointFiles.get(fileName);
                String currentFileID = currentFiles.get(fileName);
                String mergeFileID3 = mergeFiles.get(fileName);
                if (!splitPointFileID.equals(currentFileID) && !splitPointFileID.equals(mergeFileID3)) {
                    conflictFiles.add(fileName);
                }
            }
            if (currentFiles.containsKey(fileName) && !mergeFiles.containsKey(fileName)) {
                String splitPointFileID = splitPointFiles.get(fileName);
                String currentFileID = currentFiles.get(fileName);
                if (!splitPointFileID.equals(currentFileID))
                conflictFiles.add(fileName);
            }
        }
        return conflictFiles;
    }

    private static void isValidSplitPoint(Commit splitPoint, Commit mergeCommit, Commit currentCommit, String mergeBranch) {
        if (splitPoint.getSHA1ID().equals(currentCommit.getSHA1ID())) {
            checkoutBranch(mergeBranch);
            exit("Current branch fast-forwarded.");

        }
        if (splitPoint.getSHA1ID().equals(mergeCommit.getSHA1ID())) {
            exit("Given branch is an ancestor of the current branch.");
        }
    }



    private static Commit findSplitPoint(Commit currentCommit, Commit mergeCommit) {
        Map<String, Integer> commitPathWithDepth = getAllCommitsOnPath(mergeCommit, 0);
        Map<String, Integer> targetCommitPathWithDepth = getAllCommitsOnPath(currentCommit, 0);
        String closetCommitID = "";
        Integer closetCommitDepth = 999;

        for (String key: commitPathWithDepth.keySet()) {
            if (targetCommitPathWithDepth.containsKey(key) && targetCommitPathWithDepth.get(key) < closetCommitDepth) {
                closetCommitID = key;
                closetCommitDepth = targetCommitPathWithDepth.get(key);

            }
        }
        Commit x = fromFile(closetCommitID);
        return x;
    }

    private static Map<String, Integer> getAllCommitsOnPath(Commit commit,int length) {
        Map<String, Integer> map = new HashMap<>();
        if (commit.getParents().isEmpty()) {
            map.put(commit.getSHA1ID(), length);
            return map;
        }
        map.put(commit.getSHA1ID(), length);
        length++;
        for (String id : commit.getParents()) {
            Commit parent = fromFile(id);
            map.putAll(getAllCommitsOnPath(parent, length));
        }
        return map;
    }

    private static void isValidToMerge(String branchName) {
        StagingArea stagingArea = loadStagingArea();
        if (!stagingArea.isStagedEmpty()) {
            exit("You have uncommitted changes.");
        } else if (!getBranchPath(branchName).exists()) {
            exit("A branch with that name does not exist.");
        } else if (branchName.equals(currentBranchName())) {
            exit("Cannot merge a branch with itself.");
        }
    }


    private static void setCommitTo(Commit newCommit) {
        List<String> onlyCurrCommitTracked = findOnlyCurrCommitTracked(newCommit);
        List<String> bothCommitTracked = findBothCommitTracked(newCommit);
        List<String> onlyNewCommitTracked = findOnlyNewCommitTracked(newCommit);

        deleteFiles(onlyCurrCommitTracked);
        overwriteFiles(bothCommitTracked, newCommit);
        writeFiles(onlyNewCommitTracked, newCommit);
        StagingArea stage = loadStagingArea();
        stage.setStageClean();
        stage.saveStagingArea();

    }

    private static List<String> findOnlyCurrCommitTracked(Commit newCommit) {
        List<String> newCommitFiles = newCommit.getBlobFileNames();
        List<String> onlyCurrCommitTracked = headCommit().getBlobFileNames();
        for (String s : newCommitFiles) {
            onlyCurrCommitTracked.remove(s);
        }
        return onlyCurrCommitTracked;
    }

    private static List<String> findBothCommitTracked(Commit newCommit) {
        List<String> newCommitFiles = newCommit.getBlobFileNames();
        List<String> currCommitFiles = headCommit().getBlobFileNames();
        List<String> bothCommitTracked = new ArrayList<>();
        for (String s : newCommitFiles) {
            if (currCommitFiles.contains(s)) {
                bothCommitTracked.add(s);
            }
        }
        return bothCommitTracked;
    }

    private static List<String> findOnlyNewCommitTracked(Commit newCommit) {
        List<String> currCommitFiles = headCommit().getBlobFileNames();
        List<String> onlyNewCommitTracked = newCommit.getBlobFileNames();
        for (String s : currCommitFiles) {
            onlyNewCommitTracked.remove(s);
        }
        return onlyNewCommitTracked;
    }

    private static void deleteFiles(List<String> onlyCurrCommitTracked) {
        if (onlyCurrCommitTracked.isEmpty()) {
            return;
        }
        for (String fileName : onlyCurrCommitTracked) {
            File file = join(CWD, fileName);
            restrictedDelete(file);
        }
    }

    private static void overwriteFiles(List<String> bothCommitTracked, Commit newCommit) {
        if (bothCommitTracked.isEmpty()) {
            return;
        }
        for (String fileName : bothCommitTracked) {
            Blob blob = newCommit.getBlobByFileName(fileName);
            saveBlobToCWD(blob);
        }
    }

    private static void writeFiles(List<String> onlyNewCommitTracked, Commit newCommit) {
        if (onlyNewCommitTracked.isEmpty()) {
            return;
        }
        for (String fileName : onlyNewCommitTracked) {
            File file = join(CWD, fileName);
            if (file.exists()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        overwriteFiles(onlyNewCommitTracked, newCommit);
    }






    private static String getFullCommitID(String commitID) {
        if (commitID.length() == 40) {
            return commitID;
        }
        String commitDirName = folderName(commitID);
        File targetCommitDir = join(COMMIT_DIR, commitDirName);
        if (targetCommitDir.exists()) {
            for (String commitName : targetCommitDir.list()) {
                if (commitName.startsWith(fileName(commitID))) {
                    String targetCommitID = commitDirName + commitName;
                    return targetCommitID;
                }
            }
        } else {
            exit("No commit with that id exists.");
        }
        return null;
    }


    private static Commit getCommitFromBranch(String branchName) {
        File branch = getBranchPath(branchName);
        String commitID = readContentsAsString(branch);
        return fromFile(commitID);
    }

    private static void saveBlobToCWD(Blob blob) {
        File file = join(CWD, blob.getFileName());
        writeContents(file, blob.getFileContent());
    }

    private static File getBranchPath(String branchName) {
        File branch = Utils.join(BRANCH_HEADS_DIR, branchName);
        return branch;
    }

    private static String currentBranchName() {
        String name = readContentsAsString(HEAD);
        return name.replace(HEAD_BRANCH_REF_PREFIX, "");
    }

    static Commit headCommit() {
        return fromFile(getHeadCommitID());
    }

    static String getHeadCommitID() {
        String HEADName = currentBranchName(); //先获得head名称
        File branchHeadFile = join(BRANCH_HEADS_DIR, HEADName); //生成path
        String headCommitID = readContentsAsString(branchHeadFile); //解读path下的文件
        return headCommitID;
    }

    private static List<String> getUntrackedFiles() {
        List<String> res = new ArrayList<>();
        List<String> stageFiles = loadStagingArea().getStagedFilename();
        Set<String> headFiles = headCommit().getTrackedBlobsID().keySet();
        for (String filename : plainFilenamesIn(CWD)) {
            if (!stageFiles.contains(filename) && !headFiles.contains(filename)) {
                res.add(filename);
            }
        }
        Collections.sort(res);
        return res;
    }



    /**
     * Throw an error if the command doesn't work
     * @param message The Error message that we want to throw
     * @param args Paired commands
     */

    public static void exit(String message, Object... args) {
        message(message, args);
        System.exit(0);
    }

    /**
     * Return a path of the file
     * @param fileName
     * @return Return a path of the file
     */
    public static File getFilePath(String fileName) {
        File file = Utils.join(CWD, fileName);
        return file;
    }

    /**
     * Update HEAD file to the current branch
     * This file is updating the HEAD file under the .gitlet directory.
     * @param branchName Name of the branch
     */
    private static void setCurrentBranch(String branchName) {
        writeContents(HEAD, HEAD_BRANCH_REF_PREFIX + branchName);
    }

    /**
     * Set the HEAD pointer to point to the new branch
     * and store the commitID in the file named as the new branch.
     * @param branchName Name of the branch；
     * @param commitSHA1ID SHA1 of the commit
     *
     */
    public static void setHeadCommitId(String branchName, String commitSHA1ID) {
        // Create new file base on branch name
        File headFile = Utils.join(BRANCH_HEADS_DIR, branchName);
        // Save the commit SHA1 ID to the file.
        Utils.writeContents(headFile, commitSHA1ID); 
    }

    public static void setStagingArea() {
        writeObject(STAGE_FILE, new StagingArea());
    }

    public static void isGitLetDirExists() {
        if (!GITLET_DIR.exists()) {
            exit("Not in an initialized Gitlet directory.");
        }
    }

    /**
     * check things
     */
    static void messageIncorrectOperands() {
        System.out.println("Incorrect operands.");
        System.exit(0);
    }
}
