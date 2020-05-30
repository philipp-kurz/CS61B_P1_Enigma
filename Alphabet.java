package enigma;

import java.util.HashMap;
import static enigma.EnigmaException.*;


/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Philipp Kurz
 */

class Alphabet {

    /** A new alphabet containing CHARS. Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        if (chars.length() < 2) {
            throw error("Alphabet has length zero.");
        }
        _alphArray = new char[chars.length()];
        _alphHashMap = new HashMap<Character, Integer>();
        for (int i = 0; i < _alphArray.length; i += 1) {
            char c = chars.charAt(i);
            if (!((c >= '!' && c <= '\'') || (c >= '+' && c <= '~'))) {
                throw error("Invalid character in alphabet.");
            }
            _alphArray[i] = c;
            _alphHashMap.put(c, i);
        }
        if (_alphHashMap.size() != _alphArray.length) {
            throw error("Duplicate alphabet characters are not allowed!");
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _alphArray.length;
    }

    /** Returns true if preprocess(CH) is in this alphabet. */
    boolean contains(char ch) {
        return (_alphHashMap.get(ch) != null);
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        try {
            return _alphArray[index];
        } catch (IndexOutOfBoundsException e) {
            throw error("Integer could not be converted to char.");
        }
    }

    /** Returns the index of character preprocess(CH), which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        try {
            return (int) _alphHashMap.get(ch);
        } catch (NullPointerException e) {
            throw error("Char could not be converted to int.");
        }
    }

    /**
     * Contains all characters at their respective position. */
    private char[] _alphArray;

    /**
     * Contains the actual alphabet.
     * Key is the index of the character, value is the character itself. */
    private HashMap<Character, Integer> _alphHashMap;
}
