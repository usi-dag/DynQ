package ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing;

import java.util.Arrays;
import java.util.Objects;

public class ArrayBox {
    public final Object[] data;
    public final int hash;

    public ArrayBox(Object[] key, int hash) {
        this.data = key;
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        ArrayBox that = (ArrayBox) o;
        for (int i = 0; i < data.length; i++) {
            if (!(Objects.equals(this.data[i], that.data[i]))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }

}