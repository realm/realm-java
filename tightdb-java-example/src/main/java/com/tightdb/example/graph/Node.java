package com.tightdb.example.graph;

import java.util.Date;

public class Node {
    int         id;
    int         node_type;
    int         version;
    Date        time;
    String  data;

    Node(int id, int node_type, int version, Date time, String data) {
        this.id = id;
        this.node_type = node_type;
        this.version = version;
        this.time = time;
        this.data = data;
    }
}
