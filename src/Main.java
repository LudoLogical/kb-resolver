import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {

    private static class Problem {

        Set<String> variables;
        ArrayList<LinkedHashMap<String, Boolean>> clauses;

        private void addToClause(String variable, boolean isTrue,
                                 LinkedHashMap<String, Boolean> clause, Set<String> variables) {

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
            clauses = new ArrayList<>();

            String[] prevContent;
            try {
                prevContent = scan.nextLine().split(" ");
            } catch (NoSuchElementException e) {
                System.out.println("KB file must have at least one line!");
                System.exit(0);
                return;
            }

            while (scan.hasNextLine()) {
                LinkedHashMap<String, Boolean> prevClause = new LinkedHashMap<>();
                for (String s : prevContent) {
                    if (s.charAt(0) == '~') {
                        addToClause(s.substring(1), false, prevClause, variables);
                    } else {
                        addToClause(s, true, prevClause, variables);
                    }
                }
                clauses.add(prevClause);
                prevContent = scan.nextLine().split(" ");
            }
            scan.close();

            for (String s : prevContent) {
                LinkedHashMap<String, Boolean> newClause = new LinkedHashMap<>();
                if (s.charAt(0) == '~') {
                    addToClause(s.substring(1), true, newClause, variables);
                } else {
                    addToClause(s, false, newClause, variables);
                }
                clauses.add(newClause);
            }

        }

    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Main <path_to_kb_file>");
            return;
        }

        Problem problem = new Problem(args[0]);

        System.out.println(problem.variables);
        System.out.println(problem.clauses);

    }

}
