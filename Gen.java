import java.io.*;
import java.util.*;

public class Gen {
    static Set<String> reserved_names = new HashSet<String> () {{
        add ("README.md");
        add ("SUMMARY.md");
        add ("GLOSSARY.md");
        add ("book.json");
        add ("regex.md");
    }};

    boolean apply_filter = false;
    boolean DEBUG = true;

    Map<String, String> matches;

    static String SUMMARY_HEADER = "# Summary\n\n";

    int root_path_len;

    public Gen (String initial_path, boolean b) {
        File root = null;
        try {
            root = new File (initial_path);
        } catch (Exception ex) {
            System.err.printf ("Root path:%s is invalid, aborting\n", initial_path);
            System.exit (1);
        }
        root_path_len = initial_path.length ();
        apply_filter = b;
        File[] files = root.listFiles ((d, name) -> !reserved_names.contains (name) && !name.startsWith ("."));

        if (!new File (initial_path, "README.md").exists ()) {
            System.err.printf ("Serious WARNING: make sure you at least have README.md in the folder. If you don't have one yet, create one after the processing.\n");
        }

        matches = loadRegex ();
        if (DEBUG) System.out.printf ("final regex:%s\n", matches);

        StringBuilder summary = new StringBuilder (SUMMARY_HEADER);
        
        for (File file : files) {
            summary.append (process (file, new StringBuilder (initial_path.equals (".") ? "" : initial_path + "/"), new StringBuilder ()));
        }

        try (BufferedWriter bw = new BufferedWriter (new FileWriter ("SUMMARY.md", false))) {
            bw.write (summary.toString ());
        } catch (Exception ex) {
            System.err.printf ("Can't write to SUMMARY.md\n");
            System.exit (1);
        }
    }

    String process (File input_file, StringBuilder path, StringBuilder indent) {
        String input_file_name = input_file.getName (), split_name = splitName (input_file_name);
        if (DEBUG) System.out.printf ("%sfile:(%s), path:(%s)\n", indent.toString (), input_file_name, path.toString ());
        // Recurse in case this is a directory
        StringBuilder res = new StringBuilder ();
        if (input_file.isDirectory ()) {
            File[] files = input_file.listFiles ();
            boolean has_readme = false;
            for (File file : files) {
                if (!has_readme && file.getName ().equals ("README.md"))
                    has_readme = true;
                if (file.getName ().startsWith ("."))
                    continue;
                int path_old_len = path.length ();
                indent.append ("    ");
                path.append (input_file_name + "/");
                res.append (process (file, path, indent));
                path.setLength (path_old_len);
                indent.setLength (indent.length () - 4);
            }
            return String.format ("* [%s](%s)\n", split_name, has_readme ? (path.toString () + split_name + "/README.md").substring (root_path_len + 1) : "") + res.toString ();
        }
        // Base case: process a file (not a directory)
        String full_path = path.toString () + input_file_name;
        if (!input_file_name.equals ("README.md"))
            res.append (String.format ("%s* [%s](%s)\n", indent, split_name, full_path.substring (root_path_len + 1)));
        // actually process the file content text
        if (apply_filter) {
            StringBuilder processed_content = new StringBuilder ();
            try (BufferedReader br = new BufferedReader (new FileReader (input_file))) {
                String line = "";
                boolean code = false;
                while ((line = br.readLine ()) != null) {
                    line = line.trim ();
                    if (line.startsWith ("```")) {
                        code = !code;
                    }
                    if (line.matches ("http[s]?://www.dropbox.com.*?dl=0")) {
                        line = line.replace ("?dl=0", "?raw=1");
                        if (line.matches (".*\\s+.*")) {
                            String[] tokens = line.split ("\\s+");
                            Integer scale = null;
                            if (tokens.length >= 2) {
                                try {
                                    scale = Integer.parseInt (tokens[1]);
                                } catch (Exception ex) {
                                    System.err.printf ("WARNING: %s is not a valid scaling factor and thus is ignored.\n", tokens[1]);
                                }
                            }
                            line = String.format ("<img src=\"%s\"%s", line, scale != null ? String.format (" width=\"%d\"", scale) : "");
                        }
                    }
                    processed_content.append (line + "\n");
                }
                // ready to write back
                BufferedWriter bw = new BufferedWriter (new FileWriter (full_path, false));
                bw.write (processed_content.toString ());
                bw.close ();
            } catch (Exception ex) {
                System.err.printf ("Unknown I/O error for file %s\n", input_file_name);
                ex.printStackTrace ();
                System.exit (1);
            }
        }
        return res.toString ();
    }

    Map<String, String> loadRegex () {
        Map<String, String> res = new HashMap<> ();
        try (BufferedReader br = new BufferedReader (new FileReader (new File ("regex.md")))) {
            String line = "";
            while ((line = br.readLine ()) != null) {
                String[] tokens = line.split ("\\s+");
                String seperator = " ";
                if (tokens.length >= 2) {
                    seperator = tokens[1];
                }
                if (res.containsKey (tokens[0]))
                    System.err.printf ("WARNING: duplicated regex pair for %s detected\n", tokens[0]);
                res.put (tokens[0], tokens[1]);
            }
        } catch (Exception ex) {
            System.err.printf ("No supplied regex pairs detected (regex.md), default settings applied.\n");
            // TODO: what should \\S be if scanned in from file?
            res.put ("(?<=[^A-Z&&\\S])(?=[A-Z])", " ");
        }
        return res;
    }

    String splitName (String name) {
        if (DEBUG) System.out.printf ("\t\t\t\t\t\t\t\t\tSPLIT (%s) returns ", name);
        if (name.endsWith (".md"))
            name = name.substring (0, name.length () - 3);
        for (String match : matches.keySet ()) {
            String delimiter = matches.get (match);
            name = name.replaceAll (match, delimiter);
        }
        if (DEBUG) System.out.println (name);
        return name;
    }

    public static void main (String[] args) {
        boolean apply_filter = false;
        String path = ".";
        for (int i = 0; i < args.length && i < 2; i++) {
            if (i == 0) {
                path = args[0];
            }
            if (i == 1 && args[1].toLowerCase ().equals ("true")) {
                apply_filter = true;
            }
        }
        new Gen (path, apply_filter);
    }
}
