import java.util.ArrayList;
import java.util.List;

public class TST<Value> {
    public Node<Value> root;

    public static class Node<Value> {
        public char c;
        public Node<Value> left, mid, right;
        public Value val;

        // empty constructor might come in handy
        Node() {
            this.c = '\u0000';
        }

        // instead of building the whole word in put method, when the proper place is found,
        // new node is generated with the given substring to simplify things a bit more

    }

    // Inserts the key value pair into ternary search tree
    public void put(String key, Value val) {
        if(key.isEmpty()) return;

        // in case we have any turkish characters we fix the key first
//        if (key.contains("ı")) {
//            key = key.replaceAll("ı", "i");
//        }

        // set the tree for the first time
        if(root == null){
            root = new Node<>();
        }
        put(root, key, val);

    }

    private void put(Node<Value> node, String key, Value val) {

        if (key.length() == 1) {
            node.val = val;
            node.c = key.charAt(0);
            return;
        }
//        if (node == null) return;
        if (node.c == '\u0000') {
            node.c = key.charAt(0);
        }
//            node = new Node<>();
////            put(node.mid, key.substring(1), val);
////            node.mid = new Node<>(key.substring(1), val);
//            return; // stop it from getting into recursion
//        }

        int compareResult = Character.compare(key.charAt(0), node.c);
        // if we got a match with the root means we use the root char and place the remaining substring
        if (compareResult == 0) {
            if (node.mid == null) {
                node.mid = new Node<>();
            }
            put(node.mid, key.substring(1), val);
        }
        // the value is smaller than the root's character
        else if (compareResult < 0) {
            if (node.left == null) {
                node.left = new Node<>();
            }
            put(node.left, key, val);
        }
        // else: the value is greater than root and we go right
        else {
            if (node.right == null) {
                node.right = new Node<>();
            }
            put(node.right, key, val);
        }
    }

    // Returns a list of values using the given prefix
    public List<Value> valuesWithPrefix(String prefix) {
        ArrayList<Value> values = new ArrayList<>();
        searchValuesWithPrefix(root, prefix, values);
        return values;
    }

    // TODO :
    private void searchValuesWithPrefix(Node<Value> node, String prefix, List<Value> values) {
        // node will be null if we could not find a matching substring and terminate the recursion
        if (node != null) {
            // when we find a matching case, prefix will be empty,
            // after that we move till the end of all nodes to retrieve the value then we add it to the list
            if (prefix.isEmpty()) {
                if (node.left != null && node.left.val != null) {
                    values.add(node.left.val);
                }
                searchValuesWithPrefix(node.left, prefix, values); //called with an empty prefix

                if (node.mid != null && node.mid.val != null) {
                    values.add(node.mid.val);
                }
                searchValuesWithPrefix(node.mid, prefix, values); //called with an empty prefix

                if (node.right != null && node.right.val != null) {
                    values.add(node.right.val);
                }
                searchValuesWithPrefix(node.right, prefix, values); //called with an empty prefix
            }

            // if we have not found a matching substring, we proceed the call
            // check if the value belongs to the right side of the tree
            else if (prefix.charAt(0) > node.c) {
                searchValuesWithPrefix(node.right, prefix, values);
            }
            // check if the value is present in the node
            else if (prefix.charAt(0) == node.c) {
                searchValuesWithPrefix(node.mid, prefix.substring(1), values);
            }
            // check if the value belongs to the left side of the tree
            else if (prefix.charAt(0) < node.c) {
                searchValuesWithPrefix(node.left, prefix, values);
            }
        }
    }

    private Value getValue(Node<Value> node) {
        if (node == null) return null;
        Node<Value> temp = node;
        while (temp.mid != null) {
            temp = temp.mid;
        }
        return temp.val;
    }
}