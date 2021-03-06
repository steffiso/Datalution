"CloudDatalution - A Datalog-based Tool for Schema Evolution in NoSQL Databases", Version 2.0, 09/22/2016
====================================================================================================

GENERAL USAGE NOTES
----------------------------------------------------------------------------------------------------
- CloudDatalution is a tool for migrating schema changes to NoSQL databases lazily with Googles
  Datastore as a NoSQL database backend
- In the background CloudDatalution uses Datalog rules to make lazy migration for operations
  like adding, deleting, copying and moving entities complete and efficient

- Datalution is based on the theoretical background of following paper:
  "Scherzinger, S., St�rl, U., & Klettke, M.: "A Datalog-based Protocol for 
   Lazy Data Migration in agile NoSQL Application Development". In Proc. DBPL'15, 2015

GETTING STARTED
----------------------------------------------------------------------------------------------------
- To demonstrate one use case of this tool, the project "CloudDatalutionUseCase" provides 
	two different sights:
	- User- Console (/user): to get and put entities
	- Admin- Console (/admin): to make schema changes or add new entity types
- Supported commands have the following syntax: e.g.
	1. In the user console:
	- get Player.id=1
	- put Player(4,"Maggie", 100) - (only integer values for id accepted)
	2. In the admin console:
	- add Player.points = 100 or add Player.home="Springfield"
	- delete Player.points
	- copy Player.score to Mission where Player.id=Mission.pid
	- move Player.score to Mission where Player.id=Mission.pid
	- new Job 
	- start (leads to an sample dataset in Datastore of three Player and three Mission entities)
- Additional informations/ limitations:
	- the tool only accepts attribute "id" for get commands
	- commands for a kind that doesn't exist will be ignored 
	  Improper commands can lead to uncorrect behaviour

BACKGROUND INFORMATION
---------------------------------------------------------------------------------------------------
 - If you want to add a new entity with a new kind, these are the working steps:
   (e.g. to add a new entity type "Job")
	1. Add a new schema with the following command in the admin console: "new Job"
	2. Now you can put entities in the user console: e.g. "put Job(1)"
	   Or add attributes to your new entity in the admin console: 
		e.g. "add Job.name="defaultName""
 
 - For CloudDatalution the strong consistency properties of Entity groups is used 
   (more information about strong consistency in Datastore is provided in the docu:
   https://cloud.google.com/appengine/docs/java/datastore/structuring_for_strong_consistency)
 - To guarantee that the schema evolution steps provide strong consistency, each entity
   and its several schema versions are connected in one entity group (e.g. Player1(id=1),
   Player2(id=1) and Player3(id=1) are within the same entity group)

CONTACT
-------------------------------------------------------------------------------------
Katharina Wiech, katharina.wiech@web.de

Stephanie Sombach, steffi.sombach@t-online.de
