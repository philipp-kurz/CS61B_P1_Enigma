package enigma;

import java.util.ArrayList;
import java.util.HashMap;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Philipp Kurz
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            HashMap<String, Rotor> allRotors) {
        if (numRotors <= pawls || numRotors == 0) {
            throw error("Invalid number of rotors or pawls.");
        }
        _alphabet = alpha;
        _numRotors = numRotors;
        _numPawls = pawls;
        _allRotors = allRotors;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }


    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _rotors = new ArrayList<Rotor>();
        for (String rotor : rotors) {
            Rotor rot = _allRotors.get(rotor);
            if (rot == null) {
                throw error("Could not find rotor.");
            }
            if (_rotors.contains(rot)) {
                throw error("Duplicate rotor detected.");
            }
            _rotors.add(rot);
        }
        checkRotorPositions();
    }

    /** Check if positions in _rotors are correct. */
    private void checkRotorPositions() {
        for (int i = 0; i < _rotors.size(); i += 1) {
            if (_rotors.get(i) instanceof Reflector) {
                if (i != 0) {
                    throw error("Reflector at wrong position detected.");
                }
            } else if (_rotors.get(i) instanceof FixedRotor) {
                if (i == 0 || i >= _numRotors - _numPawls) {
                    throw error("FixedRotor at wrong position detected.");
                }
            } else if (_rotors.get(i) instanceof MovingRotor) {
                if (i < _numRotors - _numPawls) {
                    throw error("MovingRotor at wrong position detected.");
                }
            } else {
                throw error("Invalid rotor type detected.");
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != _numRotors - 1) {
            throw error("Rotor setting has wrong length.");
        }
        for (int i = 1; i < _numRotors; i += 1) {
            char c = setting.charAt(i - 1);
            if (!_alphabet.contains(c)) {
                throw error("Alphabet does not contain character "
                        + "from setting.");
            }
            _rotors.get(i).set(c);
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Set the ring setting to RINGSETTING. */
    void setRingSetting(String ringSetting) {
        if (ringSetting.equals("")) {
            for (int i = 1; i < _numRotors; i += 1) {
                ringSetting += _alphabet.toChar(0);
            }
        }
        for (int i = 1; i < _numRotors; i += 1) {
            _rotors.get(i).setRingSetting(ringSetting.charAt(i - 1));
        }
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        boolean[] canMove = new boolean[_numRotors];
        for (int i = 0; i < _numRotors; i += 1) {
            canMove[i] = (i == _numRotors - 1)
                || (_rotors.get(i).rotates() && _rotors.get(i + 1).atNotch());
        }

        for (int i = 0; i < _numRotors; i += 1) {
            if (canMove[i]) {
                _rotors.get(i).advance();
                if (i < _numRotors - 1) {
                    _rotors.get(i + 1).advance();
                    i += 1;
                }
            }
        }
        c = _plugboard.permute(c);
        for (int i = _numRotors - 1; i >= 0; i -= 1) {
            c = _rotors.get(i).convertForward(c);
        }
        for (int i = 1; i < _numRotors; i += 1) {
            c = _rotors.get(i).convertBackward(c);
        }
        c = _plugboard.invert(c);
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String res = "";
        for (int i = 0; i < msg.length(); i += 1) {
            res += _alphabet.toChar(convert(_alphabet.toInt(msg.charAt(i))));
        }
        return res;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Stores all possible rotors from config file. */
    private final HashMap<String, Rotor> _allRotors;

    /** Stores references to the rotors that have been selected
     *  through the input file. */
    private ArrayList<Rotor> _rotors;

    /** Number of rotors that Enigma machine has. */
    private int _numRotors;

    /** Number of pawls that Enigma machine has. */
    private int _numPawls;

    /** Plugboard of Enigma machine represented as permutation. */
    private Permutation _plugboard;
}
