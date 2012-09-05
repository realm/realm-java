package my.app;

import com.tightdb.*;

public class Test {

    @Table
    public class Hilbert {
        String fido;
    }

    @Table
    public class Banach {
        String type;
        String number;
    }

    public static void main(String[] args)
    {
        Group db = new Group();

        HilbertTable hilbert = new HilbertTable(db);
        hilbert.add("Odif");

        BanachTable banach = new BanachTable(db);
        banach.add("John", "Doe");
    }
}
