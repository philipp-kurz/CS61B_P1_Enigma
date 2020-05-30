package enigma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Philipp Kurz
 */
class Permutation {

    /* A =  0, B =  1, C =  2, D =  3, E =  4,
       F =  5, G =  6, H =  7, I =  8, J =  9,
       K = 10, L = 11, M = 12, N = 13, O = 14,
       P = 15, Q = 16, R = 17, S = 18, T = 19,
       U = 20, V = 21, W = 22, X = 23, Y = 24,
       Z = 25
     */

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = new ArrayList<Cycle>();
        _intToCycleMap = new HashMap<Integer, Cycle>();
        boolean[] charExistence = new boolean[_alphabet.size()];

        Pattern r = Pattern.compile("\\A(\\s*\\(.+\\)\\s*)*\\z");
        Matcher m = r.matcher(cycles);
        if (!m.find()) {
            throw error("Invalid permutation format.");
        }


        r = Pattern.compile("(?<=\\()([^\\)]+)\\)");
        m = r.matcher(cycles);
        while (m.find()) {
            addCycle(m.group(1), charExistence);
        }

        for (int index = 0; index < charExistence.length; index += 1) {
            if (!charExistence[index]) {
                addSingularCycle(index);
            }
        }

    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm.
     *  Add the cycle to the cycle hash map, so that the correct cycle can
     *  be retrieved quickly.
     *  Set all respective elements in CHAREXISTENCE to true. */
    private void addCycle(String cycle, boolean[] charExistence) {
        Cycle newCycle = new Cycle();
        for (int i = 0; i < cycle.length(); i += 1) {
            int charIndex = _alphabet.toInt(cycle.charAt(i));
            if (charExistence[charIndex]) {
                throw error("Duplicate character in permutation "
                        + "cycles detected.");
            }
            charExistence[charIndex] = true;
            newCycle.add(charIndex);
            _intToCycleMap.put(charIndex, newCycle);
        }
        _cycles.add(newCycle);
    }

    /** Add the singular cycle ...c0->c0->c0... where c is the character
     * from the alphabet that corresponds to INDEX. */
    private void addSingularCycle(int index) {
        Cycle newCycle = new Cycle();
        newCycle.add(index);
        _intToCycleMap.put(index, newCycle);
        _cycles.add(newCycle);
    }

    /** Return cycle that includes character which corresponds to INDEX. */
    private Cycle getCycle(int index) {
        return ((Cycle) _intToCycleMap.get(index));
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return getCycle(p).getNext(p);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return getCycle(c).getPrevious(c);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return _alphabet.toChar(permute(_alphabet.toInt(p)));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return _alphabet.toChar(invert(_alphabet.toInt(c)));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int minSize = _cycles.get(0).size();
        for (Cycle cyc : _cycles) {
            minSize = Math.min(minSize, cyc.size());
        }
        return minSize > 1;
    }

    /** Throws EnigmaException if CYCLE is invalid. */
    static void checkCycleValidity(String cycle) {
        if (cycle.isEmpty()) {
            return;
        }
        EnigmaException e = new EnigmaException("Permutation cycles invalid.");
        cycle = cycle.replaceAll(" ", "");
        if (cycle.charAt(0) != '(' || cycle.charAt(cycle.length() - 1) != ')') {
            throw e;
        }
        int bracketCounter = 0;
        int charCounter = 0;
        for (int i = 0; i < cycle.length(); i++) {
            char c = cycle.charAt(i);
            if (c == '(') {
                bracketCounter += 1;
            } else if (c == ')') {
                if (charCounter == 0) {
                    throw e;
                }
                charCounter = 0;
                bracketCounter -= 1;
            } else if ((c >= '!' && c <= '\'') || (c >= '+' && c <= '~')) {
                if (bracketCounter != 1) {
                    throw e;
                }
                charCounter += 1;
            } else {
                throw e;
            }
        }
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Permutation cycles of this permutation. */
    private ArrayList<Cycle> _cycles;

    /** Mapping of char to permutation cycle. */
    private HashMap<Integer, Cycle> _intToCycleMap;


    /** Represents a cycle of a permutation.
     *  Every cycle is basically a circular doubly linked list with a hash map
     *  storing the reference from every index to the respective node.
     *  @author Philipp Kurz
     */
    class Cycle {

        /** Represents a cycle node of a permutation cycle.
         *  @author Philipp Kurz
         */
        private class CycleNode {

            /** Constructs a new cycle node and sets its index to IND. */
            CycleNode(int ind) {
                _index = ind;
            }

            /** Index in alphabet of this node. */
            private int _index;

            /** Returns alphabet index of this node. */
            int getIndex() {
                return _index;
            }

            /** Reference to previous node. */
            private CycleNode _previous;

            /** Returns reference to the previous node. */
            CycleNode getPrevious() {
                return _previous;
            }

            /** Sets reference to previous node to PREV. */
            void setPrevious(CycleNode prev) {
                _previous = prev;
            }

            /** Reference to next node. */
            private CycleNode _next;

            /** Returns reference to next node. */
            CycleNode getNext() {
                return _next;
            }

            /** Sets reference to next node to NEXT. */
            void setNext(CycleNode next) {
                _next = next;
            }
        }

        /** Constructs a new cycle. */
        Cycle() {
            _indexToNodeMap = new HashMap<Integer, CycleNode>();
            _size = 0;
        }
        /** Stores information on in which cycle a specific index lies. */
        private HashMap<Integer, CycleNode> _indexToNodeMap;

        /** Reference to the first node. */
        private CycleNode _first;

        /** Reference to the last node. */
        private CycleNode _last;

        /** Size of the circular linked list. */
        private int _size;

        /** Returns index of node previous to node with INDEX. */
        int getPrevious(int index) {
            return getNode(index).getPrevious().getIndex();
        }

        /** Returns index of node after node with INDEX. */
        int getNext(int index) {
            return getNode(index).getNext().getIndex();
        }

        /** Returns node with INDEX. */
        private CycleNode getNode(int index) {
            return _indexToNodeMap.get(index);
        }

        /** Add new node with INDEX. */
        void add(int index) {
            CycleNode newNode = new CycleNode(index);
            if (_first == null && _last == null) {
                _first = _last = newNode;
            }
            newNode.setPrevious(_last);
            newNode.setNext(_first);
            _last.setNext(newNode);
            _last = newNode;
            _first.setPrevious(_last);
            _indexToNodeMap.put(index, newNode);
            _size += 1;
        }

        /** Return size of circular linked list. */
        int size() {
            return _size;
        }
    }


}
