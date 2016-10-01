Getting Started
===

# About Kademlia
Kademlia implements a distributed hash table using a simple protocol. Actually it implements an simple overlay network, in which each node is assigned a hash based address.

Routing in this overlay networks happens by via the distance property of the nodes.

# Starting a node
To start a node you will have to first create a Kademlia object. The Kademlia class has two constructors.
`public Kademlia(Key nodeId)` is the simple constructor which takes a minimal set of required arguments and uses defaults for everything else.
`public Kademlia(Configuration config)` is the complex constructor with this, you have much more configuration possibilities.

## Configuration
| Property | Default | Description  |
| --- | --- | --- |
| nodeId | *none* | Address of this node in the Kademlia network. This needs to be unique. |
| listeners | *udp://0.0.0.0:9000 | A comma separated list of listener to listen and accept packages on
| advertised_listeners | *udp://127.0.0.1:9000* | A comma separated list of listeners that will be advertised

# Interacting with the Kademlia network