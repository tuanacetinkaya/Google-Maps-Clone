"# Google-Maps-Clone" 
This is an assignment held in BBM204 Algorithms Course of Hacettepe University. Code was initially imported from a given template to build the website front-end. I coded the main data structures and algorithms required to run the system. You can see the work under src/main/java folder to see my code in detail. Test classes are imported from the template code. 

# Implemented Milestones #
** Serving the Images **
Every parcel of the map is stored under the img folder and named accordingly by their correctly places bits. root.png includes the whole map and that map is divided into four pieces named 1.png, 2.png and so on. Each bit is recursively divided into 4 and append the same naming convention to itself (i.e 1.png has smaller images named 11.png 12.png 13.png and 14.png) 

![image](https://user-images.githubusercontent.com/47085047/123252559-18052b00-d4f5-11eb-953c-2ac29c7dadbc.png)


From that point I built a tree recursively to store each image and as an attribute their 4 children as north-west nort-east south-west and south-east. 

Using that data structure, when the user zoomed into or moved around the map I parse different pieces of the tree as a 2D array and showed those images for higher quality. Program is using a query box to check the boundaries, therefore saves space by not parsing the whole map each time.

** Parsing the XML Data **
From the given XML file "berkeley.osm", I parsed the data to find which points in those images includes a way or a place and places them inside my graph accordingly.

** Auto-Complete Feature ** 
Program has a search bar to find places inside the map. And as you type the TST structure automatically completes the written string with the actual places exists on map.  And places a red dot to the found place(s) on the map.

![image](https://user-images.githubusercontent.com/47085047/123251925-649c3680-d4f4-11eb-813d-8fd471ed8865.png)
![image](https://user-images.githubusercontent.com/47085047/123251988-72ea5280-d4f4-11eb-873d-eaca0a951304.png)

** Djkstra's Shortest Path for Routing **
When click two places in map the program calls the path finding function. In the previouslu built graph, I use the Djsktra's Algorithm to find the shortest path between two nodes and return the found path into the system.

It is also possible to add random stops on map in the order you'd like to visit them by holding the cursor on some point and pressing 's'.

![image](https://user-images.githubusercontent.com/47085047/123252495-04f25b00-d4f5-11eb-9c4e-0e403dfc2fef.png)
