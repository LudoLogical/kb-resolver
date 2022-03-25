import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {

    /**
     * Adds a new line to the console output required by the project spec
     * corresponding to the clause most recently added to the knowledgeBase
     * @param knowledgeBase the knowledgeBase to draw from
     * @param source1 the first of the two clauses that were combined to make the last clause
     *                in the knowledgeBase, or 0 if that clause was taken directly from the KB file
     * @param source2 the second of the two clauses that were combined to make the last clause
     *                in the knowledgeBase, or 0 if that clause was taken directly from the KB file
     */
    private static void reportMostRecentClause(ArrayList<LinkedHashMap<String, Boolean>> knowledgeBase,
                                               int source1, int source2) {

        LinkedHashMap<String, Boolean> clause = knowledgeBase.get(knowledgeBase.size() - 1);
        StringBuilder output = new StringBuilder(knowledgeBase.size() + ". ");

        // Add the new clause itself to the output
        if (clause.size() > 0) {
            for (Map.Entry<String, Boolean> entry : clause.entrySet()) {
                if (entry.getValue().equals(false)) {
                    output.append("~");
                }
                output.append(entry.getKey()).append(" ");
            }
        } else {
            output.append("Contradiction ");
        }

        // Add the source clause #s to the output
        output.append("{");
        if (source1 > 0 && source2 > 0) {
            output.append(source1).append(", ").append(source2);
        }
        output.append("}");

        System.out.println(output);

    }

    /**
     * Attempts to resolve the input clauses.
     * @param a the first clause to resolve
     * @param b the clause to resolve a with
     * @return either (1) null if a and b resolve to true,
     *         (2) an empty clause if a and b resolve to false,
     *         (3) null if a and b do not resolve, or
     *         (4) the clause (containing at least one literal) that a and b resolve to.
     */
    private static LinkedHashMap<String, Boolean> resolveClauses(LinkedHashMap<String, Boolean> a,
                                                                 LinkedHashMap<String, Boolean> b) {

        // Look for a variable that can be used to resolve a and b

        String match = null;

        for (String aVar : a.keySet()) {

            for (String bVar : b.keySet()) {
                if (!a.get(aVar).equals(b.get(bVar)) && aVar.equals(bVar)) {
                    match = aVar;
                    break;
                }
            }

            if (match != null) {
                break;
            }

        }

        if (match == null) {
            return null; // a and b cannot be resolved
        }

        // Build the resolved clause

        LinkedHashMap<String, Boolean> newClause = new LinkedHashMap<>();

        HashSet<String> relevantVars = new HashSet<>();
        relevantVars.addAll(a.keySet());
        relevantVars.addAll(b.keySet());
        relevantVars.remove(match);
        for (String var : relevantVars) {

            if (a.containsKey(var)) {
                if (b.containsKey(var)) {
                    if (!a.get(var).equals(b.get(var))) {
                        return null; // (var or ~var) evaluates to true!
                    } else {
                        newClause.put(var, a.get(var));
                    }
                } else { // a.contains(var) == true, b.contains(var) == false
                    newClause.put(var, a.get(var));
                }
            } else { // a.contains(var) == false, b.contains(var) == true
                newClause.put(var, b.get(var));
            }

        }

        return newClause;

    }

    /**
     * Searches the input knowledgeBase for a contradiction.
     * @param knowledgeBase the knowledgeBase to search
     * @return true if a contradiction is found, false otherwise
     */
    public static boolean resolveKB(ArrayList<LinkedHashMap<String, Boolean>> knowledgeBase) {

        for (int i = 1; i < knowledgeBase.size(); i++) {

            LinkedHashMap<String, Boolean> currentClause = knowledgeBase.get(i);

            for (int j = 0; j < i; j++) { // check every clause that comes before this one in the KB

                LinkedHashMap<String, Boolean> resolvedClause = resolveClauses(currentClause, knowledgeBase.get(j));

                if (resolvedClause != null) {

                    // Check for logical equivalence to an existing KB entry

                    boolean logicalEquivalentFound = false;
                    for (int k = 0; k < knowledgeBase.size(); k++) {

                        if (k == i || k == j) {
                            continue;
                        }

                        LinkedHashMap<String, Boolean> clauseToCompare = knowledgeBase.get(k);
                        Set<String> resolvedKeySet = resolvedClause.keySet();
                        if (resolvedClause.keySet().containsAll(clauseToCompare.keySet())) {

                            boolean literalsAreIdentical = true;
                            for (String s : resolvedKeySet) {
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

                    // Add the resolved clause to the KB and report doing so to the console
                    if (!logicalEquivalentFound) {
                        knowledgeBase.add(resolvedClause);
                        reportMostRecentClause(knowledgeBase, i+1, j+1);
                        if (resolvedClause.size() == 0) {
                            return true; // a contradiction!
                        }
                    }

                }

            }

        }

        // Exhausted all possible combinations
        return false;

    }

    public static void main(String[] args) {

        // Check that cli format is correct
        if (args.length != 1) {
            System.out.println("Usage: java Main <path_to_kb_file>");
            return;
        }

        File kbFile = new File(args[0]);
        Scanner scan;

        try {
            scan = new Scanner(kbFile);
        } catch (FileNotFoundException e) {
            System.out.println("Something went wrong opening path_to_kb_file.");
            System.exit(0);
            return;
        }

        ArrayList<LinkedHashMap<String, Boolean>> knowledgeBase = new ArrayList<>();

        String[] prevContent;
        try {
            prevContent = scan.nextLine().split(" ");
        } catch (NoSuchElementException e) {
            System.out.println("KB file must have at least one line!");
            System.exit(0);
            return;
        }

        // Add each line except for the last to the KB directly
        while (scan.hasNextLine()) {
            LinkedHashMap<String, Boolean> prevClause = new LinkedHashMap<>();
            for (String s : prevContent) {
                if (s.charAt(0) == '~') {
                    prevClause.put(s.substring(1), false);
                } else {
                    prevClause.put(s, true);
                }
            }
            knowledgeBase.add(prevClause);
            reportMostRecentClause(knowledgeBase, 0, 0);
            prevContent = scan.nextLine().split(" ");
        }
        scan.close();

        // Add the negation of each literal in the last line to the knowledge base as its own clause
        for (String s : prevContent) {
            LinkedHashMap<String, Boolean> newClause = new LinkedHashMap<>();
            if (s.charAt(0) == '~') {
                newClause.put(s.substring(1), true);
            } else {
                newClause.put(s, false);
            }
            knowledgeBase.add(newClause);
            reportMostRecentClause(knowledgeBase, 0, 0);
        }

        // Perform the resolution
        boolean succeeded = resolveKB(knowledgeBase);
        if (succeeded) {
            System.out.println("Valid");
        } else {
            System.out.println("Fail");
        }

    }

}
