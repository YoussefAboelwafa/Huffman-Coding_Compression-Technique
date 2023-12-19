import java.util.Arrays;

public final class Wrapper {
    public final byte[] data;

    public Wrapper(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Wrapper)) {
            return false;
        }
        return Arrays.equals(data, ((Wrapper) other).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}