package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Scanner;
import static enigma.EnigmaException.*;
import java.util.NoSuchElementException;

/** Enigma simulator.
 *  @author Philipp Kurz
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine mach = readConfig();

        if (!_input.hasNext("(?<=^|\n)\\*.*")) {
            throw error("Invalid start of input file.");
        }

        while (_input.hasNext("(?<=^|\n)\\*.*")) {
            String[] rotors = new String[mach.numRotors()];

            String start = _input.next();
            if (start.equals("*")) {
                rotors[0] = _input.next();
            } else {
                rotors[0] = start.substring(1);
            }

            for (int i = 1; i < mach.numRotors(); i += 1) {
                rotors[i] = _input.next();
            }
            mach.insertRotors(rotors);

            String setting = _input.next();
            setUp(mach, setting);

            String rest = _input.nextLine();
            Scanner sc = new Scanner(rest);
            String ringSetting = "";
            if (sc.hasNext() && !sc.hasNext("(?<!\\()(\\(.+\\))(?!\\))")) {
                ringSetting = sc.next();
            }

            mach.setRingSetting(ringSetting);

            String cycles = "";
            while (sc.hasNext(".*[\\(|\\)]+.*")) {
                cycles += sc.next();
            }
            Permutation.checkCycleValidity(cycles);
            mach.setPlugboard(new Permutation(cycles, _alphabet));

            while (_input.hasNextLine() && !_input.hasNext("(?<=^|\n)\\*.*")) {
                String nextLine = _input.nextLine().replaceAll("[ \t]", "");
                printMessageLine(mach.convert(nextLine));
            }

            if (_input.hasNextLine()) {

                _input.useDelimiter("[ \t*]+");
                while (_input.hasNext("(\r\n)+") || _input.hasNext("(\n)+")) {
                    String str = _input.next();
                    str = str.replaceAll("\r", "");
                    for (int i = 0; i < str.length(); i += 1) {
                        _output.print("\r\n");
                    }
                }
                _input.useDelimiter("\\s+");
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String strAlphabet = _config.next();
            _alphabet = new Alphabet(strAlphabet);

            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();

            HashMap<String, Rotor> rotors = new HashMap<String, Rotor>();
            while (_config.hasNext()) {
                Rotor newRotor = readRotor();
                if (rotors.containsKey(newRotor.name())) {
                    throw error("Duplicate rotor in conf file detected.");
                }
                rotors.put(newRotor.name(), newRotor);
            }
            return new Machine(_alphabet, numRotors, numPawls, rotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            Rotor res;
            String name = _config.next();
            if (name.contains("(") || name.contains(")")) {
                throw error("Characters ( or ) not allowed "
                        + "in rotor name.");
            }
            String info = _config.next();
            String notches = info.substring(1);

            String cycles = "";
            while (_config.hasNext(".*[\\(|\\)]+.*")) {
                cycles += _config.next();
            }
            Permutation.checkCycleValidity(cycles);
            Permutation perm = new Permutation(cycles, _alphabet);
            switch (info.charAt(0)) {
            case 'M':
                if (notches.length() < 1) {
                    throw error("No notch specified for moving rotor.");
                }
                for (int i = 0; i < notches.length(); i += 1) {
                    if (!_alphabet.contains(notches.charAt(i))) {
                        throw error("Notch not found in alphabet.");
                    }

                }
                res = new MovingRotor(name, perm, notches); break;
            case 'N':
                if (notches.length() != 0) {
                    throw error("Notch for fixed rotor detected.");
                }
                res = new FixedRotor(name, perm); break;
            case 'R':
                if (notches.length() != 0) {
                    throw error("Notch for reflector detected.");
                }
                res = new Reflector(name, perm);
                if (!res.permutation().derangement()) {
                    throw error("Reflector is not a derangement.");
                }
                break;
            default:
                throw error("Wrong rotor type. Must be M, N or R.");
            }
            return res;
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        M.setRotors(settings);
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 1) {
            _output.print(msg.charAt(i));
            if (((i + 1) % 5 == 0) && (i != msg.length() - 1)) {
                _output.print(" ");
            }
        }
        _output.print("\r\n");
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}
