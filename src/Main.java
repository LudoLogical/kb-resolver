import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {

    private static class Problem {

        Set<String> variables;
        ArrayList<HashMap<String, Boolean>> knowledgeBase;

        private void addToClause(String variable, boolean isTrue,
                                 HashMap<String, Boolean> clause, Set<String> variables) {

            clause.put(variable, isTrue);
            variables.add(variable);

        }

        public Problem(String sourceFile) {

            File kbFile = new File(sourceFile);
            Scanner scan;

            try {
                scan = new Scanner(kbFile);
            } catch (FileNotFoundException e) {
                System.out.println("Something went wrong opening path_to_kb_file.");
                System.exit(0);
                return;
            }

            variables = new HashSet<>();
            knowledgeBase = new ArrayList<>();

            String[] prevContent;
            try {
                prevContent = scan.nextLine().split(" ");
            } catch (NoSuchElementException e) {
                System.out.println("KB file must have at least one line!");
                System.exit(0);
                return;
            }

            while (scan.hasNextLine()) {
                HashMap<String, Boolean> prevClause = new HashMap<>();
                for (String s : prevContent) {
                    if (s.charAt(0) == '~') {
                        addToClause(s.substring(1), false, prevClause, variables);
                    } else {
                        addToClause(s, true, prevClause, variables);
                    }
                }
                knowledgeBase.add(prevClause);
                prevContent = scan.nextLine().split(" ");
            }
            scan.close();

            for (String s : prevContent) {
                HashMap<String, Boolean> newClause = new HashMap<>();
                if (s.charAt(0) == '~') {
                    addToClause(s.substring(1), true, newClause, variables);
                } else {
                    addToClause(s, false, newClause, variables);
                }
                knowledgeBase.add(newClause);
            }

        }

    }

    private static HashMap<String, Boolean> resolveClauses(HashMap<String, Boolean> a,
                                                                 HashMap<String, Boolean> b) {

        // don't forget to remove repeated literals
        return null; // if the resolved clause is just "true"

    }

    public static boolean resolveProblem(Problem problem) {

        for (int i = 1; i < problem.knowledgeBase.size(); i++) {

            HashMap<String, Boolean> currentClause = problem.knowledgeBase.get(i);

            for (int j = 0; j < i; j++) {

                HashMap<String, Boolean> resolvedClause = resolveClauses(currentClause, problem.knowledgeBase.get(i));

                if (resolvedClause != null) {

                    // Check for logical equivalence to an existing KB entry

                    boolean logicalEquivalentFound = false;
                    for (int k = 0; k < problem.knowledgeBase.size(); k++) {

                        if (k == i || k == j) {
                            continue;
                        }

                        HashMap<String, Boolean> clauseToCompare = problem.knowledgeBase.get(k);
                        Set<String> toCompareKeySet = clauseToCompare.keySet();
                        if (resolvedClause.keySet().containsAll(toCompareKeySet)) {

                            boolean literalsAreIdentical = true;
                            for (String s : toCompareKeySet) {
                                if (!resolvedClause.get(s).equals(clauseToCompare.get(s))) {
                                    literalsAreIdentical = false;
                                    break;
                                }
                            }

                            if (literalsAreIdentical) {
                                logicalEquivalentFound = true;
                                break;
                            }

                        }

                    }

                    if (!logicalEquivalentFound) {
                        problem.knowledgeBase.add(resolvedClause);
                        // print the clause, the line number, and the current values of j and i
                    }

                }

            }

        }

        return false;

    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Main <path_to_kb_file>");
            return;
        }

        Problem problem = new Problem(args[0]);

        System.out.println(problem.variables);
        System.out.println(problem.knowledgeBase);

        boolean succeeded = resolveProblem(problem);

        if (succeeded) {
            System.out.println("Valid");
        } else {
            System.out.println("Fail");
        }

    }

}
