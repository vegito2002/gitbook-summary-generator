import java.io.*;
import java.util.*;

public class Gen {
    /* Reserved file names to be excluded from the processing. Some will be
    handled specifically later. */
    static Set<String> reserved_names = new HashSet<String> () {{
        add ("README.md");
        add ("SUMMARY.md");
        add ("GLOSSARY.md");
        add ("book.json");
        add ("regex.md");
    }};
    /* Start boilerplate of the SUMMARY.md to be generated. */
    static String SUMMARY_HEADER = "# Summary\n\n";
    /* Stores whether the user wants the file content to be processed. See README
    for details. */
    boolean apply_filter = false;

    static String USAGE_INFO = "USAGE:\njava Gen [relative path of base folder root] [true/false: whether to apply file content processing]";

    /* Toggle for debug logging */   
    boolean DEBUG = true;
    /* Stores regex pairs to process the file names */
    Map<String, String> matches;
    /* Stores the relative path of the root folder: default to "." */
    String root_path;

    public Gen (String initial_path, boolean b) {
        File root = null;
        try {
            root = new File (initial_path);
        } catch (Exception ex) {
            System.err.printf ("Root relative path:%s is invalid, aborting\n", initial_path);
            System.exit (1);
        }
        root_path = initial_path;
        apply_filter = b;
        // Do not include files like .DS_store, which are usually implicit files.
        File[] files = root.listFiles ((d, name) -> !reserved_names.contains (name) && !name.startsWith ("."));
        // Just a reminder that you should have a README.md there for your book
        if (!new File (initial_path, "README.md").exists ()) {
            System.err.printf ("Serious WARNING: make sure you at least have README.md in the folder. If you don't have one yet, create one after the processing.\n");
        }

        matches = loadRegex ();

        StringBuilder summary = new StringBuilder (SUMMARY_HEADER);
        
        for (File file : files) {
            summary.append (process (file, new StringBuilder (initial_path + "/"), new StringBuilder ()));
        }

        try (BufferedWriter bw = new BufferedWriter (new FileWriter (root_path + "/" + "SUMMARY.md", false))) {
            bw.write (summary.toString ());
        } catch (Exception ex) {
            System.err.printf ("Can't write to SUMMARY.md\n");
            System.exit (1);
        }
    }

    /**
    * Recursively process each file. If a directory, then visit each file within;
    * if a file, apply specific processing if requested.
    * @param input_file: the file to be processed.
    * @param path: path of current file relative to the root_path.
    * @param indent: backtracking indentation for SUMMARY.md indentation
    * @return The proportion in SUMMARY.md that corresponds to this file
    */
    String process (File input_file, StringBuilder path, StringBuilder indent) {
        String input_file_name = input_file.getName (), split_name = splitName (input_file_name);
        if (DEBUG) System.out.printf ("%sfile:(%s), path:(%s)\n", indent.toString (), input_file_name, path.toString ());
        // Recurse in case this is a directory
        StringBuilder res = new StringBuilder ();
        if (input_file.isDirectory ()) {
            File[] files = input_file.listFiles ();
            /* For a directory, a README.md within acts as the cover of the Chapter/Part.
            When we traverse the directory's content, remember whether we found one.
            This would affect the line appended to SUMMARY.md. */
            boolean has_readme = false;
            for (File file : files) {
                if (!has_readme && file.getName ().equals ("README.md"))
                    has_readme = true;
                if (file.getName ().startsWith ("."))
                    continue;
                // recurse down in a backtracking manner
                int path_old_len = path.length ();
                indent.append ("    ");
                path.append (input_file_name + "/");
                res.append (process (file, path, indent));
                path.setLength (path_old_len);
                indent.setLength (indent.length () - 4);
            }
            return String.format ("%s* [%s](%s)\n", indent, split_name, has_readme ? (path.toString () + split_name + "/README.md").substring (root_path.length () + 1) : "") + res.toString ();
        }
        // Base case: process a file (not a directory)
        String full_path = path.toString () + input_file_name;
        if (!input_file_name.equals ("README.md"))
            res.append (String.format ("%s* [%s](%s)\n", indent, split_name, full_path.substring (root_path.length () + 1)));
        // actually process the file content text if requested by user
        if (apply_filter) {
            StringBuilder processed_content = new StringBuilder ();
            try (BufferedReader br = new BufferedReader (new FileReader (input_file))) {
                String line = "";
                boolean code = false;
                while ((line = br.readLine ()) != null) {
                    line = line.trim ();
                    // some processing rules should not be applied in listings: track the status
                    if (line.startsWith ("```")) {
                        code = !code;
                    }
                    // process dropbox picture links
                    if (!code && line.matches ("http[s]?://www.dropbox.com.*((\\?dl=0)|(\\?raw=1)).*")) {
                        line = line.replace ("?dl=0", "?raw=1");
                        Integer scale = null;
                        if (line.matches (".*\\s+.*")) {
                            String[] tokens = line.split ("\\s+");
                            if (tokens.length >= 2) {
                                try {
                                    scale = Integer.parseInt (tokens[1]);
                                } catch (Exception ex) {
                                    System.err.printf ("WARNING: %s is not a valid scaling factor and thus is ignored.\n", tokens[1]);
                                }
                            }
                            line = tokens[0];
                        }
                        line = String.format ("<img src=\"%s\"%s>", line, scale != null ? String.format (" width=\"%d\"", scale) : "");
                    }
                    // Non-listing lines are appended two spaces to avoid github-style markdown newline handling
                    if (!code)
                        line += "  ";
                    processed_content.append (line + "\n");
                }
                // ready to write back
                BufferedWriter bw = new BufferedWriter (new FileWriter (full_path, false));
                bw.write (processed_content.toString ());
                bw.close ();
            } catch (Exception ex) {
                System.err.printf ("I/O error for file %s\n", input_file_name);
                ex.printStackTrace ();
                System.exit (1);
            }
        }
        return res.toString ();
    }

    /**
    * Load the regex pairing from regex.md file. If not found, use the default
    * handling of inserting a space where each caplitalized word begins.
    * The regex pairing should be stored as alternating lines of pattern/seperator
    * @return a map that stores all regex pairing information
    */
    Map<String, String> loadRegex () {
        Map<String, String> res = new HashMap<> ();
        try {
            Scanner scan = new Scanner (new File (root_path + "/regex.md"));
            while (scan.hasNextLine ()) {
                String pat = scan.nextLine ();
                String seperator = " ";
                if (scan.hasNextLine ())
                    seperator = scan.nextLine ();
                if (res.containsKey (pat))
                    System.err.printf ("WARNING: duplicated regex pair for %s detected\n", pat);
                res.put (pat, seperator);
            }
        } catch (Exception ex) {
            System.err.printf ("No supplied regex pairs detected (regex.md), default settings applied.\n");
            res.put ("(?<=[^A-Z&&\\S])(?=[A-Z])", " ");
        }
        return res;
    }

    /**
    * According to the provided regex pairing rules, Split each file name, so
    * that it is more human-readable and suitable for the book.
    * @param name: the name to be split
    * @return split name: eg. TextOne -> Text One by the default rules
    */
    String splitName (String name) {
        if (DEBUG) System.out.printf ("\t\t\t\t\t\t\t\t\tSPLIT (%s) = ", name);
        // Strip out extension, but only for markdown files
        if (name.endsWith (".md"))
            name = name.substring (0, name.length () - 3);
        // Process each regex rule
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
                if (args[i].equals ("-h") || args[i].equals ("-help")) {
                    System.out.println (USAGE_INFO);
                    System.exit (1);
                }
                path = args[0];
            }
            if (i == 1 && args[1].toLowerCase ().equals ("true")) {
                apply_filter = true;
            }
        }
        new Gen (path, apply_filter);
    }
}
