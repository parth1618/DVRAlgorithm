# DVRAlgorithm

DistanceVector-Routing Alogorithm
This is a sample implementation of distance vector algorithm on routing topology with six routers.

Objective of this project is to implement Distance Vector Routing Algorithm. Project is developed in Java. For working part, project takes an input from text file describing network topology with six router. Then Algorithm calculate distances and present final routing matrix and also update routing table information at every router. Then you can calculate shortest path between any of given router. As part of extension, to check whether this algorithm works properly after making any change to network topology, we also present a scenario when user can shut down any router. Whenever such changes have been made to topology, algorithm recalculate the routes and present new routing matrix.

Programming Language: Java Key methodology: Multi-threading, synchronization, locks, Java Collections

This is sample project with six router scenario. There can be any number of router. Algorithm read topology from an input file called "default.txt". You can create your topology and save it in default.txt and calculate the path.
