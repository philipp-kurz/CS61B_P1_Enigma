package enigma;

import java.util.HashSet;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Philipp Kurz
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = new HashSet<Integer>();
        if (notches.length() > 0) {
            for (int i = 0; i < notches.length(); i += 1) {
                char notch = notches.charAt(i);
                _notches.add(permutation().alphabet().toInt(notch));
            }
        }
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        return _notches.contains(permutation().wrap(_setting));
    }

    @Override
    void advance() {
        _setting = permutation().wrap(_setting + 1);
    }

    @Override
    public String toString() {
        return "MovingRotor " + name();
    }

    /** Stores notches in a HashSet. */
    private HashSet<Integer> _notches;

}
