package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Philipp Kurz
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = posn;
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        set(_permutation.alphabet().toInt(cposn));
    }

    /** Set ringSetting of rotor to POSN. */
    void setRingSetting(int posn) {
        _ringSetting = posn;
    }

    /** Set ringSetting of rotor to character CPOSN. */
    void setRingSetting(char cposn) {
        setRingSetting(_permutation.alphabet().toInt(cposn));
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int intoPerm = _permutation.wrap(p + _setting - _ringSetting);
        if (intoPerm != p + _setting - _ringSetting) {
            int setBr = 0;
        }
        int outOfPerm = _permutation.permute(intoPerm);
        int ret = _permutation.wrap(outOfPerm - _setting + _ringSetting);
        if (ret != outOfPerm - _setting + _ringSetting) {
            int setBr = 0;
        }
        return ret;
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int intoPerm = _permutation.wrap(e + _setting - _ringSetting);
        if (intoPerm != e + _setting - _ringSetting) {
            int setBr = 0;
        }
        int outOfPerm = _permutation.invert(intoPerm);
        int ret = _permutation.wrap(outOfPerm - _setting + _ringSetting);
        if (ret != outOfPerm - _setting + _ringSetting) {
            int setBr = 0;
        }
        return ret;
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** Current setting of the rotor. */
    protected int _setting;

    /** Ring setting of this rotor. */
    protected int _ringSetting;
}
