package skyfight.lib;

public class Pair<T1, T2> {
    private final T1 item1;
    private final T2 item2;

    public Pair(T1 item1, T2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public T1 get1() {
        return item1;
    }

    public T2 get2() {
        return item2;
    }
}