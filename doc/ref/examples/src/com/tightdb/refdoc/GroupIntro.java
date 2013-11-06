// @@Example: ex_java_group_intro @@
package com.tightdb.refdoc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import com.tightdb.*;

public class GroupIntro {

    public static void main(String[] args) throws IOException {
        // @@Show@@
        // Create a new empty group
        Group group = new Group();

        // Create a new table with 2 columns and add 3 rows of data
        Table table = group.getTable("table1");
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "Animal");
        table.add(1, "Lion");
        table.add(2, "Monkey");
        table.add(3, "Elephant");
 
        // -------------------------------------------------------------------
        // Serialization of the group
        // -------------------------------------------------------------------

        // A _new_ file pointing to the location of the database
        File file = new File("mydatabase.tightdb");
        file.delete();

        // Serializing to a file that already exists is an error
        // and would cause undefined behavior
        if(file.exists() == false){
            //Serialize the database to the file
            group.writeToFile(file);
        }

        // -------------------------------------------------------------------
        // Initialize a group from a database file
        // -------------------------------------------------------------------

        // Initialize a group object from file
        group = new Group(file);

        // Check the number of tables in the group is 1.
        Assert(group.size() == 1);

        // Get the name of the first (zero-indexed) table in the group. 
        // In this case 'table1'
        String tableName = group.getTableName(0);

        // Check if the group contains the specified table name
        Assert(group.hasTable(tableName));

        // -------------------------------------------------------------------
        // Writing to byte array and transfer over a socket
        // -------------------------------------------------------------------

        // Write group to byte array
        byte[] byteArray = group.writeToMem();

        // Transfer the byte array using sockets
        try {
            Socket socket = new Socket("host", 1234);
            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

            dOut.writeInt(byteArray.length); // write length of the array
            dOut.write(byteArray);           // write the array

            // -------------------------------------------------------------------
            // Receive byte array from socket and initialize group
            // -------------------------------------------------------------------

            DataInputStream dIn = new DataInputStream(socket.getInputStream());

            int length = dIn.readInt();                 // read length of incoming byte array
            byte[] receivedByteArray = new byte[length];
            dIn.readFully(receivedByteArray, 0, receivedByteArray.length); // read byte array

            // Initialize group from the received byte array
            Group fromArray = new Group(receivedByteArray);

            // Get a table from the group, 
            // and read the value from column 1, row 2 (zero-indexed)
            Table tableFromArray = fromArray.getTable(tableName);
            String value = tableFromArray.getString(1, 2);
            Assert(value.equals("Elephant")); 

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // @@EndShow@@
    }
    
    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 
// @@EndExample@@
