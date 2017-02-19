# Total-and-Fifo-Ordering
Totally and Causally Ordered Group Messenger with a Local Persistent Key-Value Table

This application builds on the simple messenger app. This is a group messenger application that preserves total ordering as well as causal ordering of all messages. In addition, it implements a key-value table that each device uses to individually store all messages on its local storage.

The provider has two columns.

The first column is “key”. This column is used to store all keys.
The second column is “value”. This column is used to store all values.
String datatype is used for storing all keys and values
