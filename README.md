# BetterReads Data Loader
This repo is used to load the initial data into data stack 
cassandra instance for the Better Reads application. Further, it's also 
used to synchronize 
it with the updated data on the OpenLibrary.

## Caution
1. Make sure to setup **environment variables**.
2. Although [secure-connect.zip](src/main/resources/secure-connect.zip) is 
useless without username and password, but it's still good practice to fetch it form a
secure vault during the **build** stage instead of including it directly 
in the repository.
