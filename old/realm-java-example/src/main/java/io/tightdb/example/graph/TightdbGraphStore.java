//
// NOTES:
// It might be possible to eliminate the seperate classes Link and Node - and use below instead
// Disregard the int/Long mismatches differetent places
// Also the Binary "data" column is a String column instead.
//

package io.tightdb.example.graph;

import java.util.ArrayList;
import java.util.Date;

import io.realm.DefineTable;
import io.realm.ReadTransaction;
import io.realm.SharedGroup;
import io.realm.WriteTransaction;

public class TightdbGraphStore {

    SharedGroup db;

    @DefineTable(table = "NodeTable")
    class node {
        int         node_type;
        int         version;
        Date        time;
        String      data;
        boolean     deleted;
        linktype    link_in;
        linktype    link_out;
    }
    @DefineTable(table = "LinktypeTable")
    class linktype {
        int type;
        int link;
    }

    @DefineTable(table = "LinkTable")
    class link {
        int         id1;
        int         link_type;
        int         id2;
        String      data;
        int         version;
        Date        time;
        boolean     deleted;
    }

    @DefineTable()
    class nodeDeleted {
        int id;
    }

    @DefineTable()
    class linkDeleted {
        int id;
    }

    static Link clone_link(LinkRow l) {
        return new Link((int)l.getId1(), (int)l.getLink_type(), (int)l.getId2(), 0, l.getData(),
                (int)l.getVersion(), l.getTime());
    }

    TightdbGraphStore(String filename) {
        db = new SharedGroup(filename);

        // Create initial tables
        WriteTransaction tr = db.beginWrite();
        try {
            if (tr.size() == 0) {
                new NodeTable(tr);
                new LinkTable(tr);
                new NodeDeletedTable(tr);
                new LinkDeletedTable(tr);
                for (int i=0; i<tr.size(); i++)
                    System.out.println(" Table: '" + tr.getTableName(i) + "' created.");
                tr.commit();
            }
        } catch (Exception e) {
            tr.rollback();
            System.err.println("Exception");
        }
    }

    long addNode(Node node) {
        long free_row_ndx = 0;
        WriteTransaction tr = db.beginWrite();
        try {
            NodeTable nodes = new NodeTable(tr);
            NodeDeletedTable deleted_nodes = new NodeDeletedTable(tr);
            if (deleted_nodes.isEmpty()) {
                nodes.add(node.node_type, node.version, node.time, node.data, false, null, null);
                free_row_ndx = nodes.size() - 1; // inserted at end
            } else {
                 free_row_ndx = deleted_nodes.at(0).getId();
                 // (set() method is not yet implemented in typed interface
                 nodes.at(free_row_ndx).setNode_type(node.node_type);
                 nodes.at(free_row_ndx).setVersion(node.version);
                 nodes.at(free_row_ndx).setTime(node.time);
                 nodes.at(free_row_ndx).setData(node.data);
                 nodes.at(free_row_ndx).setDeleted(false);
                 nodes.at(free_row_ndx).setLink_in(null);
                 nodes.at(free_row_ndx).setLink_out(null);

                 deleted_nodes.remove(0);
            }
            tr.commit();
        } catch (Exception e) {
            System.err.println("Exception");
            tr.rollback();
        }
        return free_row_ndx;
    }

    Node getNode(int node_type, int node_id) {
        ReadTransaction tr = db.beginRead();
        NodeTable nodes = new NodeTable(tr);
        NodeRow row = nodes.at(node_id);
        assert(row.getNode_type() == node_type);
        tr.endRead();
        return new Node(node_id, node_type, (int)row.getVersion(), row.getTime(),
                        row.getData());
    }

    boolean updateNode(Node node) {
        WriteTransaction tr = db.beginWrite();
        try {
            NodeTable nodes = new NodeTable(tr);
            NodeRow row = nodes.at(node.id);
            if (row.getDeleted() == false && row.getNode_type() == node.node_type) {
                row.setVersion(node.version);
                row.setTime(node.time);
                row.setData(node.data);
                tr.commit();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Exception");
        }
        tr.rollback();
        return false;
    }

    boolean deleteNode(int node_type, int node_id) {
        WriteTransaction tr = db.beginWrite();
        try {
            NodeTable nodes = new NodeTable(tr);
            NodeDeletedTable deleted_nodes = new NodeDeletedTable(tr);
            NodeRow row = nodes.at(node_id);
            if (row.getDeleted() == false && row.getNode_type() == node_type) {
                row.setDeleted(true);
                tr.commit();
                deleted_nodes.add(node_id);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Exception");
        }
        tr.rollback();
        return false;
    }


    /////////////////////////////////////////////////////
    // Links
    /////////////////////////////////////////////////////

    boolean addLink(Link link) {
        WriteTransaction tr = db.beginWrite();
        try {
            NodeTable nodes = new NodeTable(tr);
            LinkTable links = new LinkTable(tr);
            LinkDeletedTable deleted_links = new LinkDeletedTable(tr);

            // check if link already exists
            // we don't want to search over all links, so we go through the source node
            LinktypeQuery query = nodes.at(link.id1).getLink_out().
                                    where().type.equalTo(link.link_type);
            for (LinktypeRow ref : query.findAll()) {
                LinkRow l = links.at(ref.getLink());
                if (l.getId2() == link.id2) {
                    l.setData(link.data);
                    l.setVersion(link.version);
                    l.setTime(link.time);
                    tr.commit();
                    return false;   // updated existing link
                }
            }

            // create new link
            int link_id;
            if (deleted_links.isEmpty()) {
                links.add(link.id1, link.link_type, link.id2, link.data,
                          link.version, link.time, false);
                link_id = (int) (links.size()-1);       // inserted at end
            } else {
                int free_row_ndx = (int) deleted_links.at(0).getId();
                links.at(free_row_ndx).setId1(link.id1);
                links.at(free_row_ndx).setLink_type(link.link_type);
                links.at(free_row_ndx).setId2(link.id2);
                links.at(free_row_ndx).setData(link.data);
                links.at(free_row_ndx).setVersion(link.version);
                links.at(free_row_ndx).setTime(link.time);
                links.at(free_row_ndx).setDeleted(false);
                deleted_links.remove(0);
                link_id = free_row_ndx;
            }

            // update refs in nodes
            nodes.at(link.id1).getLink_out().add(link.link_type, link_id);
            nodes.at(link.id2).getLink_in().add(link.link_type, link_id);
            tr.commit();
            return true;
        } catch (Exception e) {
            System.err.println("Exception");
        }
        tr.rollback();
        return false;
    }

    // this is very inefficient, in practice I would assume that
    // you would always have a link id, so that you could delete directly
    boolean deleteLink(int id1, int link_type, int id2) {
        WriteTransaction tr = db.beginWrite();
        try {
            NodeTable nodes = new NodeTable(tr);
            LinkTable links = new LinkTable(tr);
            LinkDeletedTable deleted_links = new LinkDeletedTable(tr);

            // we don't want to search over all links, so we go through the source node
            NodeRow srcNode = nodes.at(id1);
            LinktypeQuery query = srcNode.getLink_out().where().type.equalTo(link_type);
            for (LinktypeRow ref : query.findAll()) {
                long refLink = ref.getLink();
                LinkRow l = links.at(refLink);
                if (l.getId2() == id2) {
                    l.setDeleted(true);
                    deleted_links.add((int) refLink);
                    srcNode.getLink_out().where().type.equalTo(refLink).remove();
                    nodes.at(id2).getLink_in().where().type.equalTo(link_type).link.equalTo(refLink).remove();
                    tr.commit();
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Exception");
        }
        tr.rollback();
        return false;  // not found
    }

    ArrayList<Link> getLinkList(int id1, int link_type) {
        ArrayList<Link> link_list = new ArrayList<Link>();

        ReadTransaction tr = db.beginRead();
        NodeTable nodes = new NodeTable(tr);
        LinkTable links = new LinkTable(tr);

        LinktypeView view = nodes.at(id1).getLink_out().where().type.equalTo(link_type).findAll();
        for (LinktypeRow r : view) {
            LinkRow link = links.at(r.getLink());
            link_list.add(clone_link(link));
        }
        tr.endRead();
        return link_list;
    }


    // This is a bonus method (not in linkbench)
    long countLinks(int id1, int link_type) {
        ReadTransaction tr = db.beginRead();
        NodeTable nodes = new NodeTable(tr);
        long cnt = nodes.at(id1).getLink_out().where().type.equalTo(link_type).count();
        tr.endRead();
        return cnt;
    }

    // This is a bonus method (not in linkbench)
    ArrayList<Link> getBacklinkList(int id1, int link_type) {
        ArrayList<Link> link_list = new ArrayList<Link>();

        ReadTransaction tr = db.beginRead();
        NodeTable nodes = new NodeTable(tr);
        LinkTable links = new LinkTable(tr);

        LinktypeView view = nodes.at(id1).getLink_in().where().type.equalTo(link_type).findAll();
        for (LinktypeRow r : view) {
            LinkRow link = links.at(r.getLink());
            link_list.add(clone_link(link));
        }

        tr.endRead();
        return link_list;
    }


}
