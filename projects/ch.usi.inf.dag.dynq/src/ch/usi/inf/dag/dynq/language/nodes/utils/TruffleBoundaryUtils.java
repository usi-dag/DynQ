package ch.usi.inf.dag.dynq.language.nodes.utils;

import com.oracle.truffle.api.CompilerDirectives;

import java.util.HashMap;
import java.util.HashSet;


public class TruffleBoundaryUtils {

    // Maps

    @CompilerDirectives.TruffleBoundary(allowInlining = true)
    public static <K, V> V hashMapGet(HashMap<K, V> hashMap, K key) {
        return hashMap.get(key);
    }

    @CompilerDirectives.TruffleBoundary(allowInlining = true)
    public static <K, V> void hashMapPut(HashMap<K, V> hashMap, K key, V value) {
        hashMap.put(key, value);
    }

    // Sets

    @CompilerDirectives.TruffleBoundary(allowInlining = true)
    public static <T> boolean hashSetContains(HashSet<T> hashSet, T value) {
        return hashSet.contains(value);
    }

    @CompilerDirectives.TruffleBoundary(allowInlining = true)
    public static <T> void hashSetAdd(HashSet<T> hashSet, T value) {
        hashSet.add(value);
    }


    // Strings

    @CompilerDirectives.TruffleBoundary(allowInlining = true)
    public static boolean stringEquals(String who, String what) {
        return who.equals(what);
    }

    @CompilerDirectives.TruffleBoundary(allowInlining = true)
    public static char getCharAt(String str, int index) {
        return str.charAt(index);
    }

    @CompilerDirectives.TruffleBoundary
    public static String stringUpper(String string) {
        return string.toUpperCase();
    }
    @CompilerDirectives.TruffleBoundary
    public static double parseDouble(String string) {
        return Double.parseDouble(string);
    }

}
