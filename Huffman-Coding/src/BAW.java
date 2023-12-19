import java.util.Arrays;

public final class BAW {
    public final byte[] data;

    public BAW(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BAW)) {
            return false;
        }
        return Arrays.equals(data, ((BAW) other).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}