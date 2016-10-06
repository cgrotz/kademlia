Getting Started
===

# About Kademlia
Kademlia implements a distributed hash table using a simple protocol. Actually it implements a simple overlay network in which each node is assigned a hash based address. Routing in this overlay networks happens by via the distance property of the nodes. A Kademlia can be used to store and retrieve key-value-tuples in the Kademlia network. The key of each tuple is again a 160 Bit hash. The value can be any value.

Ideally, when you use the Kademlia network and you want to store a big file, it's a good idea to split it up on storage in multiple key-value-tuples.

# Starting a node
To start a node you will have to first create a Kademlia object. The Kademlia class has two constructors.
`public Kademlia(Key nodeId)` is the simple constructor which takes a minimal set of required arguments and uses defaults for everything else.
`public Kademlia(Configuration config)` is the complex constructor with this, you have much more configuration possibilities.

## Configuration
| Property | Default | Description  |
| --- | --- | --- |
| nodeId | *none* | Address of this node in the Kademlia network. This needs to be unique. |
| listeners | *udp://0.0.0.0:9000 | list of listener to listen and accept packages on |
| advertised_listeners | *udp://127.0.0.1:9000* | A list of listeners that will be advertised to other nodes |

# Interacting with the Kademlia network
See [Protocol](protocol.md) for more information on the protocol. The Kademlia network understands five basic commands. Ping, Find Value, Find Node, Store and Retrieve.
While Ping is merely a houskeeping