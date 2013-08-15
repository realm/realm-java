// @@Example: ex_java_group_intro @@
package com.tightdb.refdoc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import com.tightdb.*;

public class GroupIntro {

    public static void main(String[] args) {
        // @@Show@@

        //Create a new empty group
        Group group = new Group();

        //Create a new table
        Table table = group.getTable("table1");


        //Specify the column types and names
        table.addColumn(ColumnType.ColumnTypeInt, "ID");
        table.addColumn(ColumnType.ColumnTypeString, "Animal");

        //Add data to the table
        table.add(1, "Lion");
        table.add(2, "Monkey");
        table.add(3, "Elephant");


        //-------------------------------------------------------------------
        //Serialization of the group
        //-------------------------------------------------------------------

        //A file pointing to the location of the database
        File file = new File("mydatabase.tightdb");


        try {
            //Serialize the database to the file
            group.writeToFile(file);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //-------------------------------------------------------------------
        //Initialize a group from a database file
        //-------------------------------------------------------------------

        //Initialize a group object from file
        group = new Group(file);

        //Get the number of tables in the group. In this case, only 1 table has been added
        assert(group.size() == 1);

        //Returns the name of the first (zero-indexed) table in the group. In this case 'table1'
        String tableName = group.getTableName(0);

        //Checks if the group contains the specified table name
        assert(group.hasTable(tableName));

       
        //-------------------------------------------------------------------
        //Writing to byte array and transfer over a socket
        //-------------------------------------------------------------------


        //Write group to byte array
        byte[] byteArray = group.writeToMem();

        //Transfer the byte array using sockets 
        try {
            Socket socket = new Socket("host", 1234);
            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

            
            dOut.writeInt(byteArray.length); // write length of the array
            dOut.write(byteArray);           // write the array


            
            //-------------------------------------------------------------------
            //Receive byte array from socket and initialize group
            //-------------------------------------------------------------------

            DataInputStream dIn = new DataInputStream(socket.getInputStream());

            int length = dIn.readInt();                                     // read length of incoming byte array
            byte[] receivedByteArray = new byte[length];
            dIn.readFully(receivedByteArray, 0, receivedByteArray.length); // read the byte array


            //Initialize group from the received byte array
            Group fromArray = new Group(receivedByteArray);
            
            //Get the number of tables in the group. In this case, only 1 table has been added
            assert(fromArray.size() == 1);


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
//@@EndExample@@