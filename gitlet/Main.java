package gitlet;


import static gitlet.Repository.exit;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Haifeng
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            exit("Please enter a command.");
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                Repository.initCommand();
                break;
            case "add":
                Repository.isGitLetDirExists();
                String addFileName = args[1];
                Repository.addCommand(addFileName);
                break;
            case "rm":
                Repository.isGitLetDirExists();
                String removalFileName = args[1];
                Repository.rmCommand(removalFileName);
                break;
            case "commit":
                Repository.isGitLetDirExists();
                String message = args[1];
                Repository.commitCommand(message);
                break;
            case "log":
                Repository.isGitLetDirExists();
                Repository.logCommand();
                break;
            case "global-log":
                Repository.isGitLetDirExists();
                Repository.globalLogCommand();
                break;
            case "find":
                Repository.isGitLetDirExists();
                String message1 = args[1];
                Repository.findCommand(message1);
                break;
            case "status":
                Repository.isGitLetDirExists();
                Repository.statusCommand();
                break;
            case "branch":
                Repository.isGitLetDirExists();
                String branchName = args[1];
                Repository.branchCommand(branchName);
                break;
            case "rm-branch":
                Repository.isGitLetDirExists();
                String rmBranchName = args[1];
                Repository.rmBranchCommand(rmBranchName);
                break;
            case "reset":
                Repository.isGitLetDirExists();
                String resetCommitID = args[1];
                Repository.resetCommand(resetCommitID);
                break;
            case "merge":
                Repository.isGitLetDirExists();
                String mergeBranch= args[1];
                Repository.mergeCommand(mergeBranch);
                break;
            case "checkout":
                Repository.isGitLetDirExists();
                Repository repository = new Repository();
                switch (args.length) {
                    case 3 -> {
                        if (!args[1].equals("--")) {
                            exit("Incorrect operands.");
                        }
                        String fileName = args[2];
                        repository.checkoutFileCommand(fileName);
                    }
                    case 4 -> {
                        if (!args[2].equals("--")) {
                            exit("Incorrect operands.");
                        }
                        String commitId = args[1];
                        String fileName = args[3];
                        repository.checkoutFileWithCommit(commitId, fileName);
                    }
                    case 2 -> {
                        String branch = args[1];
                        repository.checkoutBranch(branch);
                    }
                    default -> exit("Incorrect operands.");
                }
                break;
            default:
                exit("No command with that name exists.");
            }
        }
    }
