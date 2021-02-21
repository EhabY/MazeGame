# Table of Contents
1. [Robert C. Martin’s *Clean Code*](#robert-c-martins-clean-code)
   * [Comments](#comments)
   * [Functions](#functions)
   * [General](#general)
   * [Java](#java)
   * [Names](#names)
   * [Tests](#tests)
2. [Joshua Bloch’s *Effective Java*](#joshua-blochs-effective-java)
   * [Creating and Destroying Objects](#creating-and-destroying-objects)
   * [Methods Common to All Objects](#methods-common-to-all-objects)
   * [Classes and Interfaces](#classes-and-interfaces)
   * [Generics](#generics)
   * [Enums and Annotations](#enums-and-annotations)
   * [Methods](#methods)
   * [General Programming](#general-programming)
   * [Exceptions](#exceptions)
   * [Concurrency](#concurrency)
   * [Serialization](#serialization)
3. [Design Patterns](#design-patterns)
   * [Strategy Pattern](#strategy-pattern)
   * [Special Case Pattern](#null-object-or-special-case-pattern)
   * [Command Pattern](#command-pattern)
   * [Visitor Pattern](#visitor-pattern)
   * [Facade Pattern](#facade-pattern)
   * [Mediator Pattern](#mediator-pattern)
4. [SOLID Principles](#solid-principles)
   * [Single-Responsibility Principle](#single-responsibility-principle)
   * [Open-Closed Principle](#open-closed-principle)
   * [Liskov Substitution Principle](#liskov-substitution-principle)
   * [Interface Segregation Principle](#interface-segregation-principle)
   * [Dependency Inversion Principle](#dependency-inversion-principle)
5. [Google's Java Styling Guide](#googles-java-styling-guide)
6. [Data Structures](#data-structures)
7. [Synchronization](#synchronization)
8. [DevOps](#devops)
8. [Beyond](#beyond)

<br/>

# Robert C. Martin’s *Clean Code*

There was a fair amount of back and forth between writing code and reading Robert Martin’s *Clean Code*. In this section, I’ll go over some of the most relevant points in Chapter 17, *Smells and Heuristics*.

## Comments
I didn’t have to write comments, because everytime I thought I needed a comment I refactored the code and used good method and variable namings. Let's take a look at `visitRoom` method.
```Java
private static void visitRoom(Room room) {
    if (!visitedRooms.contains(room.getId())) {
      visitedRooms.add(room.getId()); // mark room as visited
      roomsQueue.add(room);
    }
  }
```
We can remove the comment by using meaningful naming:
```Java
  private static void visitRoom(Room room) {
    if (hasNotVisitedRoom(room)) {
      markRoomAsVisited(room);
      roomsQueue.add(room);
    }
  }

  private static boolean hasNotVisitedRoom(Room room) {
    return !visitedRooms.contains(room.getId());
  }

  private static void markRoomAsVisited(Room room) {
    visitedRooms.add(room.getId());
  }
```
The code is readable and there is no need to add comments that could actually confuse readers. You’ll find a lot of my code follows this pattern, that’s why comments are not needed at all here!

## Functions
### *F1: Too Many Arguments*
Most of the methods written have no argument, or only one argument because they operate on their objects directly. This also ensures the Single-Responsibility Principle (SRP) since methods operate on their objects only. There are a few methods with 2 arguments, but that’s because they are utility methods.

### *F2: Output Arguments*
Since I consciously tried to avoid this code smell, I have few instances of it. 
For example, a method that takes a door Json and correctly puts the room IDs.
To avoid confusion I return the same output argument after fixing the IDs.


```Java
private JSONObject fixDoorBetweenRooms(JSONObject doorJson, int currentPosition, Direction direction) {
        ...
        doorJson = setRoomIDsInDoor(currentPosition, nextPosition, doorJson);
        ...
        return doorJson;
}
```

Another instance is when there is a chance of adding a flashlight to an items array.
Here it is made clear that this method adds to the array, so there is no confusion.
```Java
private void addFlashlight(JSONArray itemsJson) {
  int chance = random.nextInt(PROBABILITY);
  if (chance < FLASHLIGHT_PROBABILITY) {
    itemsJson.put(ItemGenerator.getFlashlightJson());
  }
}
```

### *F3: Flag Arguments & G15: Selector Arguments*
I encountered this smell when I wanted to use the *look* command. Since the player might have a flashlight in dark room. However, instead of having a `lookAhead(boolean hasFlashlight)` method which then also needs to check if the flashlight is on, I split it into two methods:
* `lookAhead()`
* `lookAheadWithFlashlight(Flashlight flashlight)`

The second method also checks if the flashlight is on.

## General
### *G1: Multiple Languages in One Source File*
The HTML file `match.html` that was served to the user had the CSS and JavaScript within the HTML.
However, this then proved cumbersome to manage and edit, so I moved the CSS to 2 different files under `css` directory, and the JavaScript under the `js` directory.
This is much cleaner and makes it easier to modify any of the code.

### *G5: Duplication*
By using the strategy pattern instead of copying code for every specific case, it becomes a simple call of `object.doThis()`
```Java
public String look() {
    MapSite mapSite;
    if (inventory.hasFlashlight()) {
      mapSite = position.lookAheadWithFlashlight(inventory.getFlashlight());
    } else {
      mapSite = position.lookAhead();
    }

    return mapSite.look();
  }
```
Now we don't have to copy and paste this code for every MapSite possible `lookX()`.

Instead of having item formatters in each class that uses items (like *`Seller`*, *`Player`*, and *`Loot`*), it is unified in an *`ItemFormatter`* utility class, this removes duplication and satisfies both the SRP and OCP since formatting isn’t the responsibility of these classes and shouldn’t be tied to it if we need another format.

Both *`Player`* and *`Seller`* use the *`ItemManager`* class instead of duplicating the same exact functionality of item storage and retrieval. *`ItemManager`*'s use of the Item interface instead of specific classes (Key/Flashlight) makes it reusable and extendable when a new item is added without changing a single line of code.
The main storage Map is defined as such:
```Java
private final Map<String, List<Item>> items = new CaseInsensitiveMap<>();
```

Another way to remove duplication is group identical functionality and use inheritence. Like the *`Mirror`* and *`Painting`* both extend the class *`AbstractHangable`* since their functionality is identical. The same could be said for the lock functionality of *`Chest`* and *`Door`* classes, both extend *`AbstractLockable`* in this case.

Every instance where there could have been duplication, I extracted the code to a higher abstraction level and shared its functionality. This can be seen in the abstract function arguments like `public void add(Collection<? extends Item> items)`

<br/>

### *G6: Code at Wrong Level of Abstraction*
One way to identify this issue, is to check if there is a class that does not use some functionality of the base class. 
For example, my code used to have a *`DarkMapSite`* class that extends *`MapSite`*.
I later added a serialization functionality to the *`MapSite`* interface, so I had to implement a `encodeUsing(Encoder)` method in *`DarkMapSite`* even though it can never be serialized since it is not an actual MapSite, and is only returned when the player cannot see.
I fixed this issue by creating another interface called *`SerializableMapSite`* which extends both *`MapSite`* and *`JsonEncodable`*.
This ensures that some MapSites (like *`DarkMapSite`*) do not have to be serializable.

```Java
public interface SerializableMapSite extends MapSite, JsonEncodable {}
```

### *G8: Too Much Information*
By default, I always make the methods of a class private, and the variables private final unless otherwise needed. For example, looking at the *`Player`* class, it has many public methods. However, this is because I never expose the data directly, instead, only the required information is retrieved as needed. In fact, a lot of the methods like `public void addItemToInventory(Item item)` hide information from the user. The user does not know of the existence of an *`Inventory`* class, nor should they as this creates better decoupling.

### *G9: Dead Code*
I do have some code that is not used in the program. However, It is a telescoping constructor in case a developer wants to create an object with less parameters.

```Java
public Player(Direction startingDirection, Room startingRoom) {
    this(startingDirection, startingRoom, 0);
  }

  public Player(Direction startingDirection, Room startingRoom, long initialGold) {
    this(startingDirection, startingRoom, initialGold, Collections.emptyList());
  }

  public Player(
      Direction startingDirection,
      Room startingRoom,
      long initialGold,
      List<? extends Item> initialItems) {
    this.position = new Position(startingRoom, startingDirection);
    this.inventory = new Inventory(initialGold, initialItems);
    this.useItemVisitor = new UseItemVisitor(this);
    this.checkVisitor = new CheckVisitor(inventory);
  }
```

### *G10: Vertical Separation*
If you read the code then you’ll find I followed this rule religiously. Classes read as a newspaper, where the mention of a method is followed immediately but its implementation. Local variables were also defined in the smallest scope possible and used directly.

### *G11: Inconsistency*
I tried to stick to a convention when naming my variables. For example, JSONObjects were named in the format of objectJson (`chestJson`, `roomJson`, `sellerJson`... etc). An inconsistency I found and fixed immediately was the use of the name “items” in ItemManager to refer to a `Map<String, List<Item>>` when it was also used in the same class as an argument to the constructor but with `List<Item>` type. I fixed this issue by renaming the usage of `List<Item> items` as *ItemsList*, while this concatenates the name with the type, it does so to differentiate itself from more complex *items* variables.


### *G13: Artificial Coupling*
I encountered this problem in *`ItemManager`* for two reasons:
1. It had a `Flashlight getFlashlight` method, when that is a *`Player`*/*`Inventory`* functionality and should not be in *`ItemManager`* that is also used by the Seller. This was fixed by moving this function to the *`Inventory`* class, since it is a *`Player`* specific functionality.
2. The *`ItemFormatter`* static utility class issue mentioned above.

### *G14: Feature Envy*
The way around this smell is create adapters between classes. For example, the *`PlayerConfiguration`* does not expose its members or provide getters, instead it provides methods that specifically serve the need and only the need of the upper class.


*`PlayerConfiguration`* only has 2 public methods:
```Java
private final Interpreter interpreter;
private final MatchCreator matchCreator;

...

public Response executeCommand(String command) {
 return interpreter.execute(command);
}

public String getMazeMapJson() {
 return matchCreator.getMazeMapJson();
}
```

### *G16: Obscured Intent*
Expressively naming the methods and variables while avoiding magic numbers fixes this issue.
For example, the method: `JSONArray serializeRooms(Room startRoom)` is a classic use of Breadth First Search (BFS), by choosing the correct name, one need not know how to traverse a graph to understand this code.

See [G30 (Functions Should Do One Thing)](#G30-Functions-Should-Do-One-Thing) for the code.

### *G17: Misplaced Responsibility*
Initially, I implemented all the commands in the *`PlayerController`* class. This made the class huge with many responsibilities.
So instead, I used the *Command Pattern* to implement the game commands. This resulted in separate class for each command, which is a separate responsibility.

### *G18: Inappropriate Static*
All the static methods are either utility methods or static factories. 
For example, *`MapGenerator`*, *`JsonSerializer`*, and *`Key`* each have one public method:
* `public static String generateMap(MapConfiguration mapConfiguration)`
* `public static String serializeGameState(PlayerController playerController)`
* `public static Key fromString(String name)`

Notice, all these 3 class have only 1 main responsibility which is why there is no need to manually instantiate, and a single static method is enough.

The same can be said about *`GameParser`*, you can change the JSON format and only edit that class. This is also not the responsibility of the individual classes.

### *G19: Use Explanatory Variables*
See other examples above, but also in the *`Seller`* class, we have the following loop:
```Java
for (Map.Entry<String, Long> listing : priceList.entrySet()) {
  String itemName = listing.getKey();
  long itemPrice = listing.getValue();
  ...
}
```
This clearly explains what the key and value actually mean!

### *G20: Function Names Should Say What They Do*
This point is clearly demonstrated in *`itemManager`*. It has an `Item get(String itemName)` method that simply returns the item, and an `Item takeFromItems(String itemName)` method that returns the item but also removes it from *Items*.
Compare also to `void removeByName(String name)` that simply removes the item.

There’s also the `tryToBuy(String itemName)` and the `tryToSell(String itemName)` methods that try to do the operation but might fail.
```Java
private String tryToBuy(String itemName) {
  TradeHandler tradeHandler = playerController.getTradeHandler();
  if (tradeHandler.buy(itemName)) {
    return itemName + " bought and acquired";
  } else {
    return tradeHandler.getReason();
  }
}
```

### *G23: Prefer Polymorphism to If/Else or Switch/Case*
As I mentioned earlier, the strategy pattern was used extensively to abstract implementation details and achieve high re-usability.
There’s also the *`Lockable`* interface. A key can be used on either a door, chest, or potentially another lockable object.

```Java
public void visit(Key key) {
    if (isLockableAhead()) {
      Lockable lockable = (Lockable) player.getMapSiteAhead();
      lockable.toggleLock(key);
    } else {
      throw new InvalidUseOfItem("Not in front of lockable object");
    }
  }
```

There is an exception to this rule. `SerializableMapSite parseMapSite(JSONObject mapSiteJson)` has a switch to parse the MapSites. This actually follows the “One Switch” rule that Robert mentions, and in this case it is unavoidable since I have to parse a string and create an object based on the content of that string.
The same can be said about `Item parseItem(JSONObject itemJson)`.

So I think it is acceptable in both of these cases, the alternative is creating objects using reflection or mapping a method to a String in Map, which is more confusing and dangerous.


### *G25: Replace Magic Numbers with Named Constants*
Every bit of code that I had to use a specific number or string, I put it in named constant. For example, the string `DESCRIPTION` in MapSite classes holds their description.
All flashlights have the name *Flashlight* and this is indicated by the constant String `FLASHLIGHT_NAME`.

The `mapgenerator` package is notorious for this, every probability is abtly named.
In *`ChestGenerator`*:
```Java
private static final int PROBABILITY = 100;
private static final int LOCKED_PROBABILITY = 50;
private static final int ITEM_PROBABILITY = 40;
private static final int FLASHLIGHT_PROBABILITY = 20;
private static final int GOLD_BOUND = 10;
```

When using the schedule method of the *`Timer`* class, it delays the execution by some time in milliseconds. However, the map configuration has the time in seconds, so to convert it I have to multiply by a 1000.
In this case there is no need for a named constant because it is already obvious, and the variables around it help explain it:

`long timeInMilliseconds = map.getTimeInSeconds() * 1000;`

### *G26: Be Precise*
*`ActionValidityChecker`* is a great example of this point.
This class makes sure that the player can do what the user requests.
For example, it checks that the player has initiated trading before doing any buying or selling using the method `ValidityResponse inTradeMode(State state)`.

The same can be said about *`ItemManager`* and the *`Command`* implementations.
It is never assumed that the input will be correct all the time.
It is checked for any errors that might happen.

There is also the defensive copying used in methods like `getAllNames()`.
```Java
public List<String> getAllNames() {
 return new ArrayList<>(this.names);
}
```

### *G28: Encapsulate Conditionals*
I lost count of how many times I have followed this advice:
```Java
boolean hasItem(String name) {
   return items.containsKey(name) && items.get(name).size() > 0;
}
boolean isLockableAhead() {
   return player.getMapSiteAhead() instanceof Lockable;
}
boolean hasFlashlight() {
   return itemManager.hasItem(Flashlight.FLASHLIGHT_NAME);
}
private boolean shouldToggleLock(Key key, boolean locked) {
  return !key.equals(Key.NO_KEY) && (locked && !lock.isLocked() || !locked && lock.isLocked());
}

```

### *G29: Avoid Negative Conditionals*
In this case I have always used the preferred conditional, for example if a function only needs to know if something does not exist then it is has a negative conditional:

```Java
private static boolean hasNotVisitedRoom(Room room) {
   return !visitedRooms.contains(room.getId());
}
```
This is better as a negative conditional because the use of `!hasVisitedRoom` is even more confusing.

### *G30: Functions Should Do One Thing*
If you read through the code, you’ll notice that the methods are very small. This makes sure that they do one thing only. In fact, some "large" methods couldn’t be split without passing a lot of variables as parameters.

I had to refactor many large methods and split them into smaller “one thing” methods, for example:
```Java
private static void serializeRooms(Room startRoom) {
  StringBuilder roomsJSON = new StringBuilder();
  visitedRooms.add(startRoom.getId());
  roomsQueue.add(startRoom);
  while(!roomsQueue.isEmpty()) {
    Room currentRoom = roomsQueue.remove();
    roomsJSON.append(currentRoom.toJSON());
   
    for(Direction direction : Direction.values()) {
      MapSite mapSite = currentRoom.getMapSite(direction);
      if(isDoor(mapSite)) {
        Door door = (Door) mapSite;
        
        boolean toggled = false;
        if (door.isLocked()) {
          door.toggleLock(Key.MASTER_KEY);
          toggled = true;
        }
        
        Room nextRoom = door.getNextRoom(currentRoom);
        
        if (toggled) {
          door.toggleLock(Key.MASTER_KEY);
        }
        
        if(!visitedRooms.contains(nextRoom.getId())) {
          visitedRooms.add(nextRoom.getId());
          roomsQueue.add(nextRoom);
        }
      }
    }
  }
}
```
This function is hideous and violates a lot of the rules mentioned (and not mentioned). So I refactored it to the following methods and used `JSONArray`/`JSONObject` instead of `StringBuilder`:
```Java
private JSONArray serializeRooms(Room startRoom) {
  visitRoom(startRoom);
  while (!roomsQueue.isEmpty()) {
    Room currentRoom = roomsQueue.remove();
    serializeRoom(currentRoom);
    visitNeighboringRooms(currentRoom);
  }
  return roomsJson;
}

private void serializeRoom(Room room) {
  JSONObject roomJson = new JSONObject(room.encodeUsing(encoder));
  roomsJson.put(roomJson);
}

private void visitNeighboringRooms(Room room) {
  for (Direction direction : Direction.values()) {
    MapSite mapSite = room.getMapSite(direction);
    tryToVisitNextRoom(mapSite, room);
  }
}

private void tryToVisitNextRoom(MapSite mapSite, Room currentRoom) {
  if (isDoor(mapSite)) {
   Door door = (Door) mapSite;
   Room nextRoom = getRoomOnOtherSide(door, currentRoom);
   visitRoom(nextRoom);
 }
}

private boolean isDoor(MapSite mapSite) {
  return mapSite instanceof Door;
}

private Room getRoomOnOtherSide(Door door, Room room) {
  boolean toggled = toggleIfLocked(door);
  Room otherRoom = door.getNextRoom(room);

  if (toggled) {
    door.toggleLock(Key.MASTER_KEY);
  }
  
  return otherRoom;
}

private boolean toggleIfLocked(Door door) {
  if (door.isLocked()) {
    door.toggleLock(Key.MASTER_KEY);
    return true;
  }
  
  return false;
}

private void visitRoom(Room room) {
  if (hasNotVisitedRoom(room)) {
    markRoomAsVisited(room);
    roomsQueue.add(room);
  }
}

private boolean hasNotVisitedRoom(Room room) {
  return !visitedRooms.contains(room.getId());
}

private void markRoomAsVisited(Room room) {
  visitedRooms.add(room.getId());
}
```

Now it is clear what each function does. Each function does “one thing”, at least to the appropriate abstraction. Notice that this method is a simple Breadth First Search used to traverse the rooms and save their state.

Notice also, that here was [inappropriate static methods](#g18-inappropriate-static). Only the public method needed to be static, the other ones shouldn't be.

### *G31: Hidden Temporal Couplings*
There was a hidden temporal coupling between `parseRoomsArray` and `setRoomsInDoors` methods.
```Java
private static void parseGameMap(JSONObject gameJson) {
    JSONArray roomsJson = gameJson.getJSONArray("rooms");
    parseRoomsArray(roomsJson);
    setRoomsInDoors(roomsMap);
}
```
so I changed `parseRoomsArray` to return a map of rooms and passed that map to `setRoomsInDoors` method:
```Java
private void parseAllRooms(JSONObject gameJson) {
  JSONArray roomsJson = gameJson.getJSONArray("rooms");
  Map<Integer, Room> roomsMap = parseRoomsArray(roomsJson);
  setRoomsInDoors(roomsMap);
}
```
Now the order cannot change!

### *G34: Functions Should Descend Only One Level of Abstraction*
A great example of this was the `serializeRooms` refactoring metioned in [G30 (Functions Should Do One Thing)](#G30-Functions-Should-Do-One-Thing).
It used to descend multiple levels of abstraction. But the refactoring abstracted the operations and separated them so each only descended one level.

Another example within the same class is the `parseJsonFile` method:
```Java
public static MazeMap parseJsonFile(String pathToFile) throws IOException {
   String jsonString = readWholeFile(pathToFile);
   JSONObject jsonObject = new JSONObject(jsonString);

   JSONArray roomsJson = jsonObject.getJSONArray("rooms");
   Map<Integer, Room> roomsMap = parseRoomsArray(roomsJson);
   setRoomsInDoors(roomsMap);

   JSONObject mapConfigurationJSON = jsonObject.getJSONObject("mapConfiguration");
   return parseMazeMap(mapConfigurationJSON);
}
```

Notice that `parseJsonFile` reads a file, converts it into a JSON object, and parses the JSON. So this method was split into:
```Java
public static MazeMap parseJsonFile(String pathToFile) throws IOException {
  String jsonString = readWholeFile(pathToFile);
  return parseJson(jsonString);
}

public static MazeMap parseJson(String jsonString) {
  GameParser gameParser = new GameParser();
  JSONObject gameJson = new JSONObject(jsonString);
  gameParser.parseAllRooms(gameJson);
  return gameParser.parseMazeMap(gameJson);
}

private static String readWholeFile(String pathToFile) throws IOException {
  return new String(Files.readAllBytes(Paths.get(pathToFile)), StandardCharsets.UTF_8);
}
```
Now, `public static MazeMap parseJson(String jsonString)` can also be used on its own when the Json is already read/generated.

In the *`MapGenerator`* class. Most of the methods are a couple of lines long because they only do one thing and descend one level of abstraction:
```Java
private JSONArray generateRooms() {
  pathGenerator.generateStartingPaths();
  Queue<Integer> roomsQueue = roomGenerator.getCreatedRoomsQueue();

  while (!roomsQueue.isEmpty()) {
    int currentPosition = roomsQueue.remove();
    addMapSitesToRoom(currentPosition);
  }

  return roomGenerator.getAllRooms();
}

private void addMapSitesToRoom(int roomID) {
  for (Direction direction : Direction.values()) {
    addRandomMapSiteIfUndefined(roomID, direction);
  }
}

private void addRandomMapSiteIfUndefined(int roomID, Direction direction) {
  if (!roomGenerator.roomHasMapSite(roomID, direction)) {
    addRandomMapSite(roomID, direction);
  }
}

private void addRandomMapSite(int currentPosition, Direction direction) {
  JSONObject mapSiteJson = getValidMapSite(currentPosition, direction);
  if (isMapSiteDoor(mapSiteJson)) {
    mapSiteJson = fixDoorBetweenRooms(mapSiteJson, currentPosition, direction);
  }
  addMapSiteToRoomInDirection(mapSiteJson, currentPosition, direction);
}
```

### *G35: Keep Configurable Data at High Levels*
The description of each MapSite mentioned above is an example of this rule. Take the Mirror class:
```Java
public class Mirror extends AbstractHangable {
   private static final String DESCRIPTION = "You See a silhouette of you";
   ...
}
```
This can be easily changed since it’s at the top of the highest level of each concept.


### *G36: Avoid Transitive Navigation*
This is great for decoupling components of a system. In my code, the *`Inventory`*, it is not exposed anywhere, instead public methods in the *`Player`* class have to be invoked:
```Java
public void addItemToInventory(Item item) {
  inventory.addItem(item);
}

public void useItem(String itemName) {
  Item item = inventory.getItem(itemName);
  item.accept(useItemVisitor);
}
...
```
There's no need to return the actual Inventory and expose implementation details.

## Java
### *J1: Avoid Long Import Lists by Using Wildcards*
At first, I followed this rule everywhere. However, this violates *Google's Java Style*. So I replaced every wildcard with the actual specific imports.

### *J3: Constants versus Enums*
I have used three Enum classes, *`Direction`*, *`State`*, *`GameEvent`*. Enums proved to be very useful, especially for the Direction Enum.

* `public abstract Direction left();`
* `public abstract Direction right();`

These *`Direction`* methods added functionality to the Enum that made it easy to turn left or right, this would have been harder using Integer or String constants. There’s also the use of `Direction.values()` when looping over the Directions. In this case it would be impossible to do it with constants.

Either way, Enums made things clear and easy. They also make it convenient to create Maps (`EnumMap`) for *`MapSite`* and unlike String or Integer constants it limits the number of entries in the Map to the number of Enum constants.

## Names
### *N1: Choose Descriptive Names*
```Java
public Room getNextRoom(Room roomFrom) {
   if(roomFrom.equals(this.room)) {
       return this.otherRoom;
   } else if(roomFrom.equals(this.otherRoom)) {
       return this.room;
   }

   throw new IllegalArgumentException("Invalid room provided");
}
```
By using names such as *roomFrom*, *room*, *otherRoom*. It is clear that this method takes a room and returns the one on the other side of the door.

Other examples that come to mind:
* `addRandomMapSiteIfUndefined`
* `addMapSiteToRoomInDirection`
* `generateRandomPathFrom`
* `createDoorBetweenRooms`
* `createRoomIfNull`

If you read the code in all the examples above, I tried my best to choose the best descriptive name possible while also sticking to a convention. See also [G20 (Function Names Should Say What They Do)](#G20-Function-Names-Should-Say-What-They-Do).

### *N3: Use Standard Nomenclature Where Possible*
`MatchListener` and `StateListener` interfaces both have methods that follow the standard nomenclature of `onEvent()`.

Another example is the Visitor pattern, I have a *`UseItemVisitor`* and a *`CheckVisitor`*. It is obvious what this means for someone familiar with the Visitor pattern.


### *N7: Names Should Describe Side-Effects*
The method `tryToVisitNextRoom(MapSite mapSite, Room currentRoom)` describes that it will only try to visit the next room, since there is a chance that the map site provided is not a door, or that the room is already visited.

`Item takeItemFromInventory(String name)` mentioned above, describes the side-effect of removing the Item while also returning it.

See the names in [N1 (Choose Descriptive Names)](#n1-choose-descriptive-names).

<br/>

## Tests
### *T3: Don’t Skip Trivial Tests* & *T5: Test Boundary Conditions*
I actually caught an error I didn't realize I had by writing a trivial test.
Namely, the *`Lock`* class threw an error if the `Key` name was an empty String.
This error, is also considered a boundary condition. 
The reason I didn't notice it because I used the public static variable `Key.NO_KEY` which is a `Key` with an empty `String`. 

Testing also helped uncover a huge problem I overlook.
In *`MapSiteParser`*, I used to create a door and if the other side needs a door it gets the previously created door.
However, I accidentally used to provide the `Map<Integer, Integer>` with the wrong order. So I created the following method:
```Java
private ImmutablePair<Integer, Integer> getSortedPair(int num1, int num2) {
  int min = Math.min(num1, num2);
  int max = Math.max(num1, num2);
  return new ImmutablePair<>(min, max);
}
```

# Joshua Bloch’s *Effective Java*
In this section of the report, I will mention the most relevant points regarding Joshua Bloch's *Effective Java*. In the end of this section, I will also provide the list of items that I followed to the letter throughout the code.

If an item is not mentioned below, then I either followed it to the letter, or did not use it in my code at all.
## Creating and Destroying Objects

### *Item 1: Consider static factory methods instead of constructors*
A great example of this item is the *`Key`* class, since the only property of a *`Key`* is its name, it’s better to store the keys in a pool and keep only one copy of a key in the pool instead of always returning a new object.

Then use a static factory with a descriptive name:
```Java
private Key(String name) {
   this.name = Objects.requireNonNull(name);
}

public static Key fromString(String name) {
  pool.putIfAbsent(name, new Key(name));
  return pool.get(name);
}
```


*`DarkMapSite`* and *`Wall`* classes use a static factory since they are singletons, and the factory returns the instance.
```Java
private static final DarkMapSite INSTANCE = new DarkMapSite();

private DarkMapSite() {}

public static DarkMapSite getInstance() {
   return INSTANCE;
}
```

On the other hand, *`MapGenerator`* uses a static factory to make the name descriptive and because there is no use for an instance (there is only 1 public method):
```Java
public static String generateMap(MapConfiguration mapConfiguration) {
  MapGenerator mapGenerator = new MapGenerator(mapConfiguration);
  return mapGenerator.generateMap();
}
```

See also [G18 (Inappropriate Static)](#g18-inappropriate-static)

### *Item 2: Consider a builder when faced with many constructor parameters*
While I have used the telescoping pattern a fair amount, when the maximum number of arguments was 3-4. However, I have then used the Builder pattern for the *`MapMaze`* class, since it has a lot of information, some of it of the same type, others are optional.

So when parsing the map I simply pass the arguments as chained method calls:
```Java
return new MazeMap.Builder(
            Direction.valueOf(orientation), rooms.get(startRoomID), rooms.get(endRoomID))
        .startingGold(gold)
        .initialItems(initialItems)
        .time(timeInSeconds)
        .build();
```


I also used the builder pattern for a different reason. When parsing the JSON file, there’s a circular dependency between the *`Door`* and *`Room`* classes. Since rooms have doors, and doors connect rooms.

The way I fixed this issue is by creating a Door Builder with a private *`Door`* instance that gets initialized with a *`Key`* only. When the rooms are parsed, the incomplete doors are assigned to the rooms. After all the rooms have been parsed, I loop over the Door Builders and start adding both rooms to each door.
```Java
private static void setRoomsInDoors(Map<Integer, Room> rooms) {
   for(DoorInfo doorInfo : doors) {
       int roomID = doorInfo.roomID;
       int otherRoomID = doorInfo.otherRoomID;
       Door.Builder doorBuilder = doorInfo.doorBuilder;

       doorBuilder.setRoom(rooms.get(roomID));
       doorBuilder.setOtherRoom(rooms.get(otherRoomID));
   }
}
```
The builder fixed this dependency issue by temporary letting the parser not specify the rooms. After the parsing is done, the GC collects all the DoorInfo (and Door.Builder instances) so it’s impossible to change the rooms after parsing!


### *Item 3: Enforce the singleton property with a private constructor or an enum type*
As mentioned above, both *`Wall`* and *`DarkMapSite`* are singleton classes with a private constructor. This is better for memory usage and there is no need for another instance when they represent the same exact thing.

### *Item 4: Enforce noninstantiability with a private constructor*
This is relevant in the Singleton classes mentioned above, but also applies to utility classes.
*`ActionValidityChecker`* is a utility class with private constructors and no static factories.

### *Item 5: Prefer dependency injection to hardwiring resources*
Instead of creating a single map and just reading that same map, the method `MazeMap parseJSONFile(String pathToFile)` accepts a path to a JSON file and returns a *`MazeMap`* which then can be used to define a *`PlayerController`*.

Another great example, is the `public static String generateMap(MapConfiguration mapConfiguration)` method, where the *`MapConfiguration`* can be injected into the *`MapGenerator`* instead of making it constant.

### *Item 6: Avoid creating unnecessary objects*
All the serialization methods use `JSONObject` instead of a `String` as it is much more efficient and easier to use.

There is also the fact that the `Door` instance is shared between two rooms, instead of creating two separate `Door` instances.

`Key.fromString(string)` and the singletons also conform to this item.

### *Item 7: Eliminate obsolete object references*
When the player acquires a chest's loot, they take these items. So the items should be eliminated from their source object. This is done by the following code:
```Java
public Loot acquireLoot() {
   ...
   Loot loot = this.loot;
   this.loot = Loot.EMPTY_LOOT;
   return loot;
}
```
This ensures that the player cannot loot the same items. It also means that the original loot is collected by the GC when the player is done with it.


## Methods Common to All Objects
### *Item 10: Obey the general contract when overriding equals*
I followed this item by never manually writing the equals and hashCode methods. Instead, I generate them using the IDE. For example *`Key`*'s `equals` method is generated as the following:
```Java
public boolean equals(Object o) {
   if (this == o) return true;
   if (o == null || getClass() != o.getClass()) return false;
   Key key = (Key) o;
   return name.equals(key.name);
}
```
Which follows the guidelines provided in the item.


### *Item 12: Always override toString*
This was easily done using the IDE to generate the `toString` method. There were exceptions when the information was too large to summarize it. Like the *`Room`* class returns the id of the room along with the lightswitch state.


## Classes and Interfaces
### *Item 16: In public classes, use accessor methods, not public fields*
I always first define a method to be private, but only change it if there’s a need. All variables are private (even in abstract classes) except in the case of using a class as a data structure, like the *`Response`* class:
```Java
public class Response {

  public final JsonEncodable encodable;
  public final String message;

  public Response(String message) {
    this(message, null);
  }

  public Response(String message, JsonEncodable encodable) {
    this.encodable = encodable;
    this.message = message;
  }
}
```

*`Inventory`*, *`Position`*, *`CheckVisitor`* and *`UseItemVisitor`* are all package private, since only *`Player`* needs to know about them. But the Map Site classes are all public because they need to be accessible across packages and to the public.


### *Item 17: Minimize mutability*
The *`Key`*, *`Loot`*, *`Wall`*, *`DarkMapSite`*, *`Direction`*, *`State`*, *`GameEvent`*, *`ValidityResponse`*, *`Response`* are all immutable classes. While the Flashlight class cannot be made immutable since its state can change (on/off), nothing else can be changed.
This rule was applied everywhere, whenever a value is assigned one time it was made final. When a `Collection` is passed then it is made unmodifiable using `Collections.UnmodifiableX()`.

### *Item 18: Favor composition over inheritance*
This can be seen in the *`Inventory`* class, instead of inheriting *`ItemManager`* and adding methods for dealing with *`Player`* specific actions. *`ItemManager`* was added as a private field. This has the problem of having to redefine the accessors but hides implementation details and makes it easier to change them. *`Inventory`* is essentially a wrapper class that hides the weaknesses of *`ItemManager`* while providing an appropriate API for *`Player`*.

### *Item 20: Prefer interfaces to abstract classes*
There are around 20 interfaces in my project, while only 4 abstract classes.
This keeps the design open, so developers can create their own implementations of the interfaces. 
The abstract classes provided skeleton implementations that act as a lightweight guide.


### *Item 24: Favor static member classes over nonstatic*
There are a few nested-classes. Most of them are builder classes, while some are used to hold data specific for that class. 
However, all of them are static classes with their visibility limited as much as possible.

## Generics
### *Item 31: Use bounded wildcards to increase API flexibility*
I used this extensively when dealing with a List or a *`Collection`* of *`Item`*(s).

In the *`ItemManager`* class this method accepts any single type parameter *`Collection`* with a type that extends *`Item`* (or is *`Item`*).
```Java
public void add(Collection<? extends Item> itemsList) {
   for(Item item : itemsList) {
       add(item);
   }
}
```
These classes are now much flexible without increasing the complexity!


## Enums and Annotations
### *Item 41: Use marker interfaces to define types*
I used marker interfaces to separate the serializable Map Sites from the non-serialized Map Sites.
```Java
public interface SerializableMapSite extends MapSite, JsonEodable {}
```
This creates a clear distinction between the two, and doesn't force non-serializables to implement to add an empty `encodeUsing(Encoder)` method.


## Methods
### *Item 49: Check parameters for validity*
Most of the code that accepts objects as parameters either uses `Objects.requireNotNull()` or invokes a method on the object that returns `NullPointerException` immediately so that an error is detected as soon as possible. For example:
```Java
public Room(int id, Map<Direction, SerializableMapSite> mapSites, LightSwitch lightSwitch) {
   this.id = id;
   this.mapSites = Collections.unmodifiableMap(new EnumMap<>(mapSites));
   this.lightSwitch = Objects.requireNonNull(lightSwitch);
}
```
This is also an example where values are copied instead of used as is. This ensures that malicious users cannot modify map sites from outside the class. An unmodifiable view is also returned so that it is not changed accidentally inside *`Room`*.


### *Item 52: Use overloading judiciously*
The only use of overloading is when adding listeners, and the arguments are of different types so this will not ever be a problem.
```Java
public void addListener(MatchListener listener) {
  matchListeners.add(listener);
}

public void addListener(StateListener listener) {
  stateListeners.add(listener);
}
```


## General Programming
### *Item 58: Prefer for-each loops to traditional for loops*
Every loop that I wrote was a for-each loop, except the ones in the parser package. Since `JSONArray` only iterates through `Object`, so instead, I choose the safer `JSONArray.getJSONObject(index)` method. Which requires that I use a classic for-loop.
```Java
for(int i = 0; i < itemsJSON.length(); i++) {
   JSONObject itemJSON = itemsJSON.getJSONObject(i);
   items.add(parseItem(itemJSON));
}
```

### *Item 63: Beware the performance of string concatenation*
Whenever there’s a need to build a String dynamically a `StringBuilder` should be used, or a higher level abstraction like `JSONObject` and `JSONArray`.

See [G30 (Functions Should Do One Thing)](#G30-Functions-Should-Do-One-Thing) for an example.

### *Item 64: Refer to objects by their interfaces*
This can be seen in field variables, method arguments, and even return types. The highest viable abstraction is always used. Even when referring to a mirror or a painting, a *`Hangable`* is used since it covers both cases without any loss of information.

See in the *`CheckVisitor`*:
```Java
public String visit(Hangable hangable) {
   Key foundKey = hangable.takeHiddenKey();
   if(foundKey.equals(Key.NO_KEY)) {
       return "";
   } else {
       inventory.addItem(foundKey);
       return "The " + foundKey.getName() + " was acquired";
   }
}
```

This method returns one of 5 subtypes of the *`Message`* interface.
```Java
public Message getResponseFromMessage(Session user, String messageAsJson);
```

## Exceptions
### *Item 69: Use exceptions only for exceptional conditions*
The exceptions that I defined in `README.md` are used only as the last line of defense.


### *Item 72: Favor the use of standard exceptions*
While I created my own exception to provide better error checking, I also used standard exceptions when there was an obvious programming or state error. For example when the room provided is not connected to the door, or when the gold is negative.

```Java
if(gold < 0) {
   throw new IllegalArgumentException("gold must be positive");
}
```

<br/>

## Concurrency
### *Item 78: Synchronize access to shared mutable data*
See [the synchronization section](#synchronization).

### *Item 79: Avoid excessive synchronization*
In the *`MovementManager`* when synchronizing the movement of players it is done only on the involved rooms, to reduce the amount of unnecessary synchronization.

```Java
public void removePlayerFromRoom(Room previousRoom) {
  synchronized (locks.get(previousRoom)) {
    roomToPlayerMap.remove(previousRoom);
  }
}

public boolean addPlayerToRoom(PlayerController playerController) {
  fightManager.notifyPlayerIfWaiting(playerController);
  Room room = playerController.getCurrentRoom();
  synchronized (locks.get(room)) {
    return putPlayerInMap(playerController);
  }
}
```

There are as many locks as there are rooms, each lock is unique to each room.

### *Item 80: Prefer executors, tasks, and streams to threads*
When a player moves, they can enter into a fight if there's another player in the same room.
So, *`EventHandler`* triggers the `onMove` events using an `ExecutorService` since that operation is blocking, this is also much cleaner than using threads directly.

```Java
private final ExecutorService executor = Executors.newSingleThreadExecutor();
...
public void triggerMoveEvent(Room previousRoom) {
  for (MatchListener listener : matchListeners) {
    executor.execute(() -> listener.onMove(previousRoom));
  } 
}
```

### *Item 81: Prefer concurrency utilities to wait and notify*
I have always tried to use thread-safe data structure or let the system handle synchronization for me (like the handling of WebSocket/HTTP requests in Spring).
In fact, I have never used `wait` or `notify`, instead depended on data structures like `ConcurrentHashMap`, and `BlockingDeque`. 
Along with synchronized blocks and methods.


## Serialization
### *Item 85: Prefer alternatives to Java serialization*
As mentioned above, the serialization was done using Json.
This was extremely convenient since we also had to implement and send data to users using the Web which is very Json friendly.
It also made it possible to manually create the maps, or edit the state of a player.

# Design Patterns
A lot of the design patterns implemented have been done so unconsciously with some exceptions.

## Strategy Pattern
The Strategy pattern in its most essential form is just polymorphism. This was useful in the look method of the *`MapSite`* interface. See [G5 (Duplication)](#G5-Duplication).

## Builder Pattern
See [item 2](Item-2-Consider-a-builder-when-faced-with-many-constructor-parameters).

Inside MazeMap:
```Java
public static class Builder {
  ...
  public Builder(Direction startingOrientation, Room startRoom, Room endRoom) {
    ...
  }

  public Builder startingGold(long gold) {
    ...
    return this;
  }

  public Builder initialItems(Collection<? extends Item> initialItems) {
    ...
    return this;
  }

  public Builder time(long seconds) {
    ...
    return this;
  }

  public MazeMap build() {
    return new MazeMap(this);
  }
}

```

## Null Object or Special Case Pattern
Instead of returning or storing null in an object, it is better to use this pattern and have the object execute some default behavior.

In a room, there might be a lightswitch. So to deal with this, I cam add a flag to indicate that the room has lights. Instead, I used the *Special Case* pattern, by having a *`NoLightswitch`* extending *`Lightswitch`* and overriding the `toggleLights` method to throw an exception. I also made this object a singleton since it has no state.
```Java
@Override
public void toggleLights() {
   throw new NoLightsException("No lights to switch");
}
```

There’s also a special Key with an empty string as a name. While this is not a textbook null object pattern. It is used when there are no keys, that way we do not deal with nulls and it can be used to indicate that the lock doesn’t require a key or that a painting does not have a hidden key.
```Java
public static final Key NO_KEY = Key.fromString("");
```

## Command Pattern
This was one of the biggest changes that were extremely rewarding.
In the previous revision of the game, the *`GameMaster`* implemented all the commands and was enormous.
So, instead, I used the command pattern. Created a separate class for every Command, and used a `Map<String, Command>` to map the command to the implementation. This made the *`GameMaster`* class obsolete, so I removed it!

Another advantage, is that now commands can be added easily. Just create a new class that implements either Command or ItemCommand and then just add it to the *`Interpreter`*.

Here's an example of the `BuyItem` command:
```Java
public class BuyItem implements ItemCommand {

  private final PlayerController playerController;

  public BuyItem(PlayerController playerController) {
    this.playerController = Objects.requireNonNull(playerController);
  }

  @Override
  public Response execute(String itemName) {
    ValidityResponse response = ActionValidityChecker.inTradeMode(playerController.getGameState());
    String message;
    if (response.valid) {
      message = tryToBuy(itemName);
    } else {
      message = response.message;
    }
    return new Response(message);
  }

  private String tryToBuy(String itemName) {
    TradeHandler tradeHandler = playerController.getTradeHandler();
    if (tradeHandler.buy(itemName)) {
      return itemName + " bought and acquired";
    } else {
      return tradeHandler.getReason();
    }
  }
}
```

Or the `Quit` command:
```Java
public class Quit implements Command {

  private final PlayerController playerController;

  public Quit(PlayerController playerController) {
    this.playerController = Objects.requireNonNull(playerController);
  }

  @Override
  public Response execute() {
    Player player = playerController.getPlayer();
    Room room = player.getCurrentRoom();
    room.addLoot(player.getLoot());
    playerController.quitMatch();
    return new Response("You quit the match!");
  }
}
```

## Visitor Pattern
This pattern was very useful even though it requires some boilerplate. I used it three times, both of these with the *`Player`* class.

The first is the *`CheckVisitor`* which implements *`CheckableVisitor`*, that deals with the “Check” logic for each of the *`Checkable`* objects (*Door*, *Chest*, *Mirror*, and *Painting*).
``` Java
public interface CheckableVisitor {
   String visit(Hangable hangable);
   String visit(Chest chest);
   String visit(Door door);
}
```

The second visitor is the *`UseItemVisitor`* that implements *`ItemVisitor`*. This visitor specifies how to “use” an item.

```Java
public interface ItemVisitor {
   void visit(Key key);
   void visit(Flashlight flashlight);
}
```

The reason that these weren't just interface methods that can be invoked polymorphically is because they require access to the player's *`Inventory`* and *`Position`*, respectively.

By using the visitor pattern instead of defining these methods one-by-one in *`Player`* is that it makes it easier to add more *`Checkables`* or more *`Items`*. Because then you do not have to modify *`Player`* or *`Inventory`*, you only have to add methods to the Visitors.
Along with added flexibility, this also rightly removes this responsibility from the Player class.

For example, here's the Visitor for *`Door`* and *`Chest`*:
```Java
public String visit(Chest chest) {
  Loot loot = chest.acquireLoot();
  inventory.addGold(loot.getGold());
  inventory.addItems(loot.getItems());

  return loot.toString();
}

public String visit(Door door) {
  if (door.isLocked()) {
    return "Door is locked, " + door.getKeyName() + " is needed to unlock";
  } else {
    return "Door is open";
  }
}
```

The third visitor used was the *`Encoder`*/*`Encodable`*. This is useful since the implementation of the serialization can change. In fact, it can also be used for things other than Json like XML.
In fact, the *`JsonEncoder`* and *`JsonSerializer`* go hand-in-hand. Both use the same format, and they can also be replaced or modified easily.

The Encoder interace:
```Java
public interface Encoder {
  String visit(Flashlight flashlight);
  String visit(Key key);
  String visit(Chest chest);
  String visit(Door door);
  String visit(Loot loot);
  String visit(Mirror mirror);
  String visit(Painting painting);
  String visit(Seller seller);
  String visit(Wall wall);
  String visit(Player player);
  String visit(PlayerController playerController);
  String visit(Room room);
}
```

## Facade Pattern
A facade is a simplified interface to interact with a system. This pattern was useful for *`Player`*, since it is an interface to *`Position`*, *`Inventory`*, *`CheckVisitor`*, and *`UseItemVisitor`*.
*`Player`* wraps other classes' APIs for more Player-centric methods.

Here are the public methods in the *`Player`* class:
```Java
public void turnLeft();
public void turnRight();
public Loot moveForward();
public Loot moveBackward();
public Room getCurrentRoom();
public MapSite getMapSiteAhead();
public MapSite getMapSiteBehind();
public String look();
public Response checkAhead();
public void switchLight();
public Direction getDirection();
public long getGold();
public void addGoldToInventory(long gold);
public void removeGoldFromInventory(long gold);
public boolean hasItem(String name);
public void addItemToInventory(Item item);
public Item takeItemFromInventory(String name);
public void useItem(String itemName);
public void addLoot(Loot loot);
public Loot getLoot();
```

Note, the actual logic is not implemented here, each is done in its respective class.
In fact, most of the methods are only 1-2 lines long.

## Mediator Pattern
The *`TradeHandler`* acts as a mediator between a `Seller` and a `Player`, it handles the buying and selling.
In the *`TradeHandler`* class:
```Java
public static TradeHandler startTransaction(Player player, Seller seller) {
  return new TradeHandler(player, seller);
}

public boolean buy(String itemName) {
  boolean bought = canBuy(itemName);
  if (bought) {
    long price = seller.getItemPrice(itemName);
    player.removeGoldFromInventory(price);
    Item itemBought = seller.takeItem(itemName);
    player.addItemToInventory(itemBought);
  }
  return bought;
}

public boolean sell(String itemName) {
  boolean sold = canSell(itemName);
  if (sold) {
    Item itemSold = player.takeItemFromInventory(itemName);
    seller.addItem(itemSold);
    long price = seller.getItemPrice(itemName);
    player.addGoldToInventory(price);
  }
  return sold;
}
```

`boolean canBuy()` and `boolean canSell()` are both private methods the check the validity of the operation. 
The return value indicates if the operation (either buy or sell) was valid.

<br/>

# SOLID Principles
## Single-Responsibility Principle
See [G30 (Functions Should Do One Thing)](#G30-Functions-Should-Do-One-Thing) for a good example on a refactorization for better SRP. One of the refactors was in the `visitRoom` method that serializes the *`Room`*. However, this violates the SRP. Instead, visiting the room should only visit the room in the graph sense (mark the room as visited and add to the queue).


I have also split the *`GameParser`* class into 3 classes: *`GameParser`*, *`ItemParser`*, and *`MapSiteParser`*. Each only deals with what the name implies.

There's also the validation of the state, and the requested action where it used to be in the implementation of each command. However, this would mean that the command does both the error checking and executing of the actual command. Instead, I created *`ActionValidityChecker`* which does the error checking.

If you take a loot at the `mapgenerator` package. You can see that every functionality is split into a different class.
For example, each MapSite has its own generator. The Room, Path each are generated in a separate classes. The `PositionManager` handles position related functionality.
All of these satisfy both the SRP as now there is one reason for change.

See also [G34 (Functions Should Descend Only One Level of Abstraction)](#g34-functions-should-descend-only-one-level-of-abstraction)

## Open-Closed Principle
The *`MapConfiguration`* is a great example of this principle. Instead of modifying the `MapGenerator`, we can simply pass a different configuration object.
This also applies to the *`RandomNameGenerator`* where you can pass a different list of names.

Since each functionality is modularized, and with the use of Interfaces there is no need for modification in a lot of instances. Instead, just implement a new class with a new functionality.
For example, the *`ConflictResolver`* class uses the `long calculateScore(PlayerController)` method defined in *`ScoreCalculator`* interface to calculate each player's score to determine the winner.
If there is a tie then `PlayerController breakTie(PlayerController, PlayerController)` method in *`TieBreaker`* interface can be used to implement whatever tie breaking mini-game (Rock-paper-scissors was used here).
Both of these interfaces can be used to implement custom logic, without changing anything, whether for calculating the score used to determine the winner, or the tiebreaker in case of equal scores.


See [G5 (Duplication)](#G5-Duplication) and [G36 (Avoid Transitive Navigation)](#G36-Avoid-Transitive-Navigation) for more about OCP.


## Liskov Substitution Principle
Everywhere in the code an object can be replaced with its subtype. In fact, I consciously took this into consideration.
```Java
public ItemManager(Collection<? extends Item> itemsList) {
   add(itemsList);
}
```
Here the ItemManager accepts a `Collection` of `Item` or any of its subtypes.
The same can be said about the method `listToJson`:
```Java
public static StringBuilder listToJson(List<? extends JsonSerializable> serializableList) {
   StringBuilder json = new StringBuilder();
   for (JsonSerializable serializable : serializableList) {
       json.append(serializable.toJson()).append(",");
   }

   return json;
}
```


## Interface Segregation Principle
Never reuse an interface just because it has the right methods, instead it should be a type.

See [G6 (Code at Wrong Level of Abstraction)](#G6-Code-at-Wrong-Level-of-Abstraction) and [Item 41 (Use marker interfaces to define types)](#Item-41-Use-marker-interfaces-to-define-types).


## Dependency Inversion Principle
As I mentioned above, I always wrote my code at the highest level of abstraction possible, weather for a method or a variable. For example, the *`Room`* does not depend on specific Mapsites (every Room must have a door if it is reachable) yet the variable mapSites uses *`SerializableMapSite`*.
```Java
private final Map<Direction, SerializableMapSite> mapSites;
```
This ensures that the specifics can be changed easily without touching this code.

See also [Item 64](#Item-64-Refer-to-objects-by-their-interfaces).

<br/>

# Google's Java Styling Guide
## Source file basics
These styling guides are obvious things that I already did by default.

## Source file structure
A notable style guide in this section is the prohibition of using wildcard imports, this is the opposite of what Robert says in *J1: Avoid Long Import Lists by Using Wildcards*. However, the styling guide is a requirement for the document to be considered *“Google Style”*.

Also, overloaded constructors always appear after one another, with the highest abstraction at the top.

## Formatting
I was already following most of the things mentioned here, except the 2-spaces indentation (compared to 4) and a column limit of 100 characters which then needs to be wrapped.

## Naming
The only thing I had to do here is to rename my Json helper classes, methods, and variables, since *JSONSerializer* for example should be *JsonSerializer*.

In the end, I ran my code through a styler just to make sure that it satisfies Google’s Java Style Guide.

<br/>

# Data Structures
The main data structure used in the game is obviously a graph. The rooms represent the nodes, while the doors represent the edges. The map actually represented a real-life graph.

Map, Set, List, and Queue were also used in various places.

The Map was mainly used to store the items by mapping the name (a String) to the actual *`Item`* object. It was used because of the performance, and the actual need of mapping values.

While the Set and Queue were used to mark the visited nodes (rooms) and to queue the order of the visiting, respectively. Sets don’t allow duplication and Hashsets are extremely fast, so it makes sense to use it to mark visited rooms.

A special type of Queue was also used, which is a `BlockingDeque`.
It was used to in the tiebreaker since the mini-game can be customized.
There was a need to block and wait for input, this essentially made it possible without the use of `wait` and `notify`.

Finally, lists were simply used as a way to save a collection of items, since it required no searching; it was the perfect data structure for that.

See also the Back-End section in the `README.md`.

<br/>

# Synchronization

Since there isn't many shared data between players, there was little modification needed to the game itself.
For example, the Door lock can be accessed by 2 players (each from a different side), so the access and the toggling is synchronized.

In *`Lock`*:
```Java
public void toggleLock(Key key) {
  if (canToggleLock(key)) {
    synchronized (this) {
      locked = !locked;
    }
  } else {
    throw new InvalidUseOfItem("Key doesn't match the lock");
  }
}

@Override
public synchronized boolean isLocked() {
  return locked;
}
```

In *`Room`*:
```Java
@Override
  public synchronized Loot acquireLoot() {
    Loot loot = this.loot;
    this.loot = Loot.EMPTY_LOOT;
    return loot;
  }
```

Another small modification is the use of `ConcurrentHashMap`, which is a thread-safe `HashMap` with atomic and multi-threaded specific operations.

There was also the use of `CopyOnWriteArrayList` in the `EventHandler` class.
It was appropriate because modifications are very rare, mainly in the beginning of the match and at the end.
While traversals are very frequent (every few seconds).

The real synchronization problem was related to entering or exiting a room. For example, when entering a room, the method `acquireLoot()` is synchronized so only the first player gets the loot there.
Of course, since the fighting happens when 2 (or more) players enter a room, the operation of removing a player from a room or adding them to a room is synchronized on a lock unique for that room so that other operations in other rooms aren’t impacted.
When adding a player to a room, a check happens to see if there already exists another player in the room.
If there is a player then a fight is commenced using `ConflictResolver` which uses `ScoreCalculator` and `TieBreaker` to determine the winner and finally release the lock.

If there was a third player that entered the same room, then they are notified that there will be a fight while they wait for the other 2 players.
When a player is victorious then the lock is released, and the 3rd player can then enter the room and fight the winner of the previous fight.

Whenever a player loses (either quits or loses in a fight) the rest of the players are notified of the new player list and gold is distributed accordingly to them all.

See also [Item 79 (Avoid Excessive Synchronization)](#item-79-avoid-excessive-synchronization).

# DevOps
The use of Maven to manage the project made it easy to include dependencies, and share the project with more people.
Another advantage is the structure of the project, where the resources are separated from the Java files. Even the tests are separated in their own directory.

When I started writing the tests, I noticed that I had bugs I never considered. In fact, it caught a major bug in the serializer when I wrote an integration test that serialized a map then parsed it and continued playing normally.

The automation of it all makes it very convenient to make changes, because you aren't afraid of breaking the code. The usage of Git also contributes to this, there were many times that I made a mistake and had to reset the head or rollback the changes.
Both of these (git + JUnit) permit me to clean the code, refactor it and even reimplement it, since I can always go back, and I can always rerun the tests to check the validity of my solution.

# Beyond
When I first read the requirements for the website, I thought hard about making the website scalable and after some research I reached a great solution.
My solution was to store all the game data (`Loot` in `Chest`, `Lock` on a `Door`, etc...) in a database, and use a DAO to read the data and do the required operations instead of the current system where POJOs (Plain Old Java Objects) are used.

This effectively makes means that there is no need for synchronization or shared access, since the data is all in a database, and databases are internally synchronized.
The website itself can be containerized and scaled infinitely (since all instances just read data from the same database anyway).

After reading and learning about AWS, one can use AWS Bean Stalk to run the website. This ensures scalability since it auto-scaled and manages load-balancing.
The bean stalk instances then take requests from users, connect to AWS DynamoDB (a distributed database) and request the data, process it and return the response to the user.
In fact, AWS DynamoDB can handle 20 million requests per second, has a latency of a few milliseconds.

By using Bean Stalk and DynamoDB, it is guaranteed that as long as AWS can handle the load, the website will function correctly and scale forever.

There was also a small edit that I could do to my current design, since every match is independent of each other. If matches are run in a single container then they can also be scaled infinitely as long as a single computer can handle the requests for a single match.

Sadly, I didn't have enough time to implement all of this (especially with the finals).


<br/>
