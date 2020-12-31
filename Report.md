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
Since I consciously tried to avoid this code smell, I have only one instance of it. A utility function takes a StringBuilder and removes the last character. To avoid confusion I return the same output argument after resizing it. The other option would be to copy the whole StringBuilder which would be costly.

```Java
public static StringBuilder removeTrailingChar(StringBuilder stringBuilder) {
    if (stringBuilder.length() > 0) {
      stringBuilder.setLength(stringBuilder.length() - 1);
    }
    return stringBuilder;
  }
```

### *F3: Flag Arguments & G15: Selector Arguments*
I encountered this smell when I wanted to use the *look* command. Since the player might have a flashlight in dark room. However, instead of having a `lookAhead(boolean hasFlashlight)` method which then also needs to check if the flashlight is on, I split it into two methods:
* `lookAhead()`
* `lookAheadWithFlashlight(Flashlight flashlight)`

The second method also checks if the flashlight is on.

## General
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
Now we don't have to copy paste this code for every MapSite possible `lookX()`.

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
One way to identify this issue, is to check if there is a class that does not use some functionality of the base class. For example, my code used to have a *`DarkMapSite`* class that extends *`MapSite`*. I later added a serialization functionality to the *`MapSite`* interface, so I had to implement a `toJson()` method in *`DarkMapSite`* even though it can never be serialized since it is not an actual MapSite, and is only returned when the player cannot see. I fixed this issue by creating another interface called *`SerializableMapSite`* which extends both *`MapSite`* and *`JsonSerializer`*. This ensures that some MapSites (like *`DarkMapSite`*) do not have to be serializable.

```Java
public interface SerializableMapSite extends MapSite, JsonSerializable {}
```

### *G8: Too Much Information*
By default I always make the methods of a class private, and the variables private final unless otherwise needed. For example, looking at the *`Player`* class, it has many public methods. However, this is because I never expose the data directly, instead, only the required information is retrieved as needed. In fact, a lot of the methods like `public void addItemToInventory(Item item)` hide information from the user. The user does not know of the existence of an *`Inventory`* class, nor should they as this creates better decoupling.

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
The *`ItemFormatter`* class suffers from this smell. This is a necessary evil since it decouples *`ItemManager`* from the format.

Another is the *`GameMaster`* class in its interaction with the player class, see the multiple invocations of `player.getMapSiteAhead()`. This is because *`GameMaster`* acts as an interface to the game, so it must know what the player knows (most of the time), otherwise, the Player class would be a “god-tier” class with many responsibilities.


### *G16: Obscured Intent*
The way around this smell, is to expressively name the methods and variables while avoiding magic numbers. For example, the method: `StringBuilder serializeRooms(Room startRoom)` is a classic use of Breadth First Search (BFS), by choosing the correct name, one need not know how to traverse a graph to understand this code.

See [G30](#G30-Functions-Should-Do-One-Thing) for the code.

### *G17: Misplaced Responsibility*
This is apparent in the handling of trade. Initially it was in *`GameMaster`* since this is the interface with the user. However, this is not the responsibility of *`GameMaster`* and can cause confusion. So I separated it into two classes, *`TradeHandler`* which uses an instance of *`TransactionHandler`*. Those two classes deal with the trade, while *`GameMaster`* only acts as an interface (as expected).

### *G18: Inappropriate Static*
All the static methods are in utility classes, and in this case it makes sense because it defines a behaviour that can never be polymorphic. For example, *`JsonSerializer`* has 3 public methods:
* `String serializeGameState(GameMaster gamemaster)`
* `StringBuilder removeTrailingChar(StringBuilder stringBuilder)`
* `StringBuilder listToJSON(List<? extends JSONSerializable> serializableList)`

These 3 methods are not specific for a certain object, and they add the flexibility of changing the behaviour of the serialization without touching the other classes.

The same can be said about *`GameParser`*, you can change the JSON format and only edit that class. This is also not the responsibility of the individual classes.

An example where static didn’t make a lot of sense was the `String toJson()` method inherited from *`JsonSerializable`*. Since the `toJson()` is specific for each class.

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
This point is clearly demonstrated in *`itemManager`*. It has an `Item get(String itemName)` method that simply returns the item, and an `Item takeFromItems(String itemName)` method that returns the item but also removes it from *Items*. Compare also to `void removeByName(String name)` that simply removes the item.

There’s also the `tryToBuy(String itemName)` and the `tryToSell(String itemName)` methods that try to do the operation but might fail (has a try, catch block).
```Java
private String tryToBuy(String itemName) {
  try {
    return tradeHandler.buy(itemName);
  } catch (ItemNotFoundException | NotEnoughGoldException exception) {
    return exception.getMessage();
  }
}
```

### *G23: Prefer Polymorphism to If/Else or Switch/Case*
As I mentioned earlier, the strategy pattern was used extensively to abstract implementation details and achieve high reusability.
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

There is an exception to this rule. `SerializableMapSite parseMapSite(JSONObject mapSiteJson)` has a switch to parse the MapSites. This actually follows the “One Switch” rule that Robert mentions, and in this case it is unavoidable since I have to parse a string and create an object based on the content of that string. The same can be said about `Item parseItem(JSONObject itemJson)`.

So I think it is acceptable in both of these cases, the alternative is creating objects using reflection or mapping a method to a String in Map, which is more confusing and dangerous.


### *G25: Replace Magic Numbers with Named Constants*
Every bit of code that I had to use a specific number or string, I put it in named constant. For example, the string `DESCRIPTION` in MapSite classes holds their description. All flashlights have the name *Flashlight* and this is indicated by the constant String `FLASHLIGHT_NAME`.

When using the schedule method of the *`Timer`* class, it delays the execution by some time in milliseconds. However, the map configuration has the time in seconds, so to convert it I have to multiply by a 1000. In this case there is no need for a named constant because it is already obvious, and the variables around it help explain it:

`long timeInMilliseconds = map.getTimeInSeconds() * 1000;`

### *G26: Be Precise*
*`ActionValidityChecker`* is a great example of this point. This class makes sure that the player can do what the user requests. For example, it checks that the player has initiated trading before doing any buying or selling using the method `Response inTradeMode(State state)`.


The same can be said about *`ItemManager`* and *`GameMaster`*. It is never assumed that the input will be correct all the time. It is checked for any errors that might happen.

There is also the defensive copying used in methods like `Map<Direction, MapSite> getMapSites()`.


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
private boolean shouldToggleLock(boolean locked) {
   return locked && !lock.isLocked() || !locked && lock.isLocked();
}

```

### *G29: Avoid Negative Conditionals*
In this case I have always used the prefered conditional, for example if a function only needs to know if something does not exist then it is has a negative conditional:

```Java
private static boolean hasNotVisitedRoom(Room room) {
   return !visitedRooms.contains(room.getId());
}
```
This is better as a negative conditional because the use of !hasVisitedRoom is even more confusing.

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
               Room nextRoom = door.getNextRoom(currentRoom);
               if(!visitedRooms.contains(nextRoom.getId())) {
                   visitedRooms.add(nextRoom.getId());
                   roomsQueue.add(nextRoom);
               }
           }
       }
   }
}
```
This function is hideous and violates a lot of the rules mentioned (and not mentioned). So I refactored it to the following methods:
```Java
private static void serializeRooms(Room startRoom) {
   visitRoom(startRoom);
   while(!roomsQueue.isEmpty()) {
       Room currentRoom = roomsQueue.remove();
       serializeRoom(currentRoom);
       visitNeighboringRooms(currentRoom);
   }
}

private static void serializeRoom(Room room) {
   roomsJSON.append(room.toJSON());
}

private static void visitNeighboringRooms(Room room) {
   for(Direction direction : Direction.values()) {
       MapSite mapSite = room.getMapSite(direction);
       tryToVisitNextRoom(mapSite, room);
   }
}

private static void tryToVisitNextRoom(MapSite mapSite, Room currentRoom) {
   if(isDoor(mapSite)) {
       Door door = (Door) mapSite;
       Room nextRoom = door.getNextRoom(currentRoom);
       visitRoom(nextRoom);
   }
}

private static boolean isDoor(MapSite mapSite) {
   return mapSite instanceof Door;
}

private static void visitRoom(Room room) {
   if(hasNotVisitedRoom(room)) {
       markRoomAsVisited(room);
       roomsQueue.add(room);
   }
}

private static void markRoomAsVisited(Room room) {
   visitedRooms.add(room.getId());
}

private static boolean hasNotVisitedRoom(Room room) {
   return !visitedRooms.contains(room.getId());
}
```

Now it is clear what each function does. Each function does “one thing”, at least to the appropriate abstraction. Notice that this method is a simple Breadth First Search (BFS) used to traverse the rooms and save their state.


### *G31: Hidden Temporal Couplings*
There was a hidden temporal coupling between `parseRoomsArray` and `setRoomsInDoors` methods.
```Java
private static void parseGameMap(JSONObject gameJson) {
  ...
  parseRoomsArray(roomsJson);
  setRoomsInDoors(rooms);
}
```
so I changed `parseRoomsArray` to return a map of rooms and passed that map to `setRoomsInDoors` method:
```Java
private static void parseGameMap(JSONObject gameJson) {
  ...
  Map<Integer, Room> roomsMap = parseRoomsArray(roomsJson);
  setRoomsInDoors(roomsMap);
}
```
Now the order cannot change!

### *G34: Functions Should Descend Only One Level of Abstraction*
A great example of this was the `serializeRooms` refactoring metioned in [G30](#G30-Functions-Should-Do-One-Thing). It used to descend multiple levels of abstraction. But the refactoring abstracted the operations and separated them so each only descended one level.

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
public static MazeMap parseJSONFile(String pathToFile) throws IOException {
   String jsonString = readWholeFile(pathToFile);
   JSONObject gameJson = new JSONObject(jsonString);
   parseGameMap(gameJson);
   return parseMazeMap(gameJson);
}

private static void parseGameMap(JSONObject gameJson) {
   JSONArray roomsJson = gameJson.getJSONArray("rooms");
   Map<Integer, Room> roomsMap = parseRoomsArray(roomsJson);
   setRoomsInDoors(roomsMap);
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
This is great for decoupling components of a system. In my code, the *`Inventory`*, it is not exposed anywhere, instead a public method of the *`Player`* class has to be invoked:
```Java
public void addItemToInventory(Item item) {
   inventory.addItem(item);
}
```
There's no need to return the actual Inventory and expose implementation details.

## Java
### *J1: Avoid Long Import Lists by Using Wildcards*
At first, I followed this rule everywhere. However, this violates *Google's Java Style*. So I replaced every wildcard with the actual specific imports.

### *J3: Constants versus Enums*
I have used two Enum classes, *`Direction`* and *`State`*. Enums proved to be very useful, especially for the Direction Enum.

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


If you read the code in all of the examples above, I tried my best to choose the best descriptive name possible while also sticking to a convention. See also [G20](#G20-Function-Names-Should-Say-What-They-Do).

### *N3: Use Standard Nomenclature Where Possible*
The `Serializable` interface has a `String toJson()` method, this follows the standard nomenclature `<T> toT()`.

Another example is the Visitor pattern, I have a *`UseItemVisitor`* and a *`CheckVisitor`*. It is obvious what this means for someone familiar with the Visitor pattern.


### *N7: Names Should Describe Side-Effects*
The method `tryToVisitNextRoom(MapSite mapSite, Room currentRoom)` describes that it will only try to visit the next room, since there is a chance that the map site provided is not a door, or that the room is already visited.

`Item takeItemFromInventory(String name)` mentioned above, describes the side-effect of removing the Item while also returning it.


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
   if(isNewKey(name)) {
       pool.put(name, new Key(name));
   }
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

On the other hand, *`TransactionHandler`* uses a static factory to make the name descriptive (start a transaction between a player and a seller) instead of a confusing constructor:
```Java
static TransactionHandler startTransaction(Player player, Seller seller) {
   return new TransactionHandler(player, seller);
}
```

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

One might expect the *`GameMaster`* or the *`Player`* classes to be singletons, since there is only one of each. However, there might not be. If I add a multiplayer option, or a server-client situation then there would need to be more than one *`GameMaster`* or *`Player`*.

### *Item 4: Enforce noninstantiability with a private constructor*
This is relevant in the Singleton classes mentioned above, but also applies to utility classes. *`ActionValidityChecker`*, *`ItemFormatter`*, *`JSONSerializer`*, and *`GameParser`* are all utility classes with private constructors and no static factories.

### *Item 5: Prefer dependency injection to hardwiring resources*
Instead of creating a single map and just reading that same map, the method `MazeMap parseJSONFile(String pathToFile)` accepts a path to a JSON file and returns a *`MazeMap`* which then can be used to define a *`GameMaster`*.

### *Item 6: Avoid creating unnecessary objects*
This is why all of the serialization methods use a `StringBuilder` instead of a String as it is much more efficient.

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
This ensures that the player cannot loot the same items. But it also means that the original loot is collected by the GC when the player is done with it.


## Methods Common to All Objects
### *Item 10: Obey the general contract when overriding equals*
I followed this item by never manually writing the equals and hashCode methods. Instead I generate them using the IDE. For example *`Key`*'s `equals` method is generated as the following:
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
I always first define a method to be private, but only change it if there’s a need. All variables are private (even in abstract classes) except in the case of using a class as a data structure, like the Response class:
```Java
public class Response {
   public final boolean valid;
   public final String message;
   ...
}
```
*`Inventory`*, *`Position`*, *`CheckVisitor`* and *`UseItemVisitor`* are all package private, since only *`Player`* needs to know about them. But the Map Site classes are all public because they need to be accessible across packages and to the public.


### *Item 17: Minimize mutability*
The *`Key`*, *`Loot`*, *`Wall`*, *`DarkMapSite`*, *`Direction`*, and *`Response`* are all immutable classes. While the Flashlight class cannot be made immutable since its state can change (on/off), nothing else can be changed. This rule was applied everywhere, whenever a value is assigned one time it was made final.

### *Item 18: Favor composition over inheritance*
This can be seen in the *`Inventory`* class, instead of inheriting *`ItemManager`* and adding methods for dealing with *`Player`* specific actions. *`ItemManager`* was added as a private field. This has the problem of having to redefine the accessors but hides implementation details and makes it easier to change them. *`Inventory`* is essentially a wrapper class that hides the weakness of *`ItemManager`* while providing an appropriate API for *`Player`*.

### *Item 20: Prefer interfaces to abstract classes*
There are around 10 interfaces in my project, while only 2 abstract classes.
This keeps the design open, so developers can create their own implementations of the interfaces. But the abstract classes provided skeleton implementations that act as a lightweight guide.


### *Item 24: Favor static member classes over nonstatic*
There are three instances of nested-classes. Two of them are builder classes and the third is used to hold the Door information (Key, and rooms' ID). All three of them are static classes with their visibility limited as much as possible.

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
public interface SerializableMapSite extends MapSite, JSONSerializable {}
```
This creates a clear distinction between the two, and doesn’t force non-serializables to implement an empty `toJson` method.


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
This is also an example where values are copied instead of used as is. This ensures that malicious users cannot modify map sites from outside the class. An unmodifiable view is also returned so that it is not changed accidently inside *`Room`*.


### *Item 52: Use overloading judiciously*
I have the following methods to format items to display them to the user. The `formatItems` method is overloaded to accept either a `List` or a `Map`. This does not cause confusion because a `Map` is very different to a `List`. In fact, even if they are confused somehow, they produce the same exact consistent output  (A `List` is internally converted to a `Map`).

```Java
public static String formatItems(List<? extends Item> itemsList) {
   return formatItems(fromListToMap(itemsList));
}

public static String formatItems(Map<String, ? extends List<? extends Item>> items) { … }
```


## General Programming
### *Item 58: Prefer for-each loops to traditional for loops*
Every loop that I wrote was a for-each loop, except the ones in the parser package. Since `JSONArray` only iterates through `Object`, so instead, I choose the safer `JSONArray.getJSONObject(index)` method. Which requires that I use a classic for loop.
```Java
for(int i = 0; i < itemsJSON.length(); i++) {
   JSONObject itemJSON = itemsJSON.getJSONObject(i);
   items.add(parseItem(itemJSON));
}
```

### *Item 63: Beware the performance of string concatenation*
Whenever there’s a need to build a String dynamically a `StringBuilder` should be used, see the `formatItems` method below:

See [G30](#G30-Functions-Should-Do-One-Thing) for an example.

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


## Exceptions
### *Item 69: Use exceptions only for exceptional conditions*
The exceptions that I defined in section TODO are used only as the last line of defense.


### *Item 72: Favor the use of standard exceptions*
While I created my own exception to provide better error checking, I also used standard exceptions when there was an obvious programming or state error. For example when the room provided is not connected to the door, or when the gold is negative.

```Java
if(gold < 0) {
   throw new IllegalArgumentException("gold must be positive");
}
```


# Design Patterns
A lot of the design patterns implemented have been done so unconsciously with some exceptions.

## Strategy Pattern
The Strategy pattern in its most essential form is just polymorphism. This was useful in the look method of the *`MapSite`* interface (see [G5](#G5-Duplication)).There’s also the *`JsonSerializable`* interface with its sole `toJson()` method that is also different per object.


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


## Visitor Pattern
This pattern was very useful even though it requires some boilerplate. I used it two times, both with the *`Player`* class.

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

The reason that these weren’t just interface methods that can be invoked polymorphically is because they require access to the player's *`Inventory`* and *`Position`*, respectively.

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


## Facade Pattern
A facade is a simplified interface to interact with a system. This pattern was useful for *`GameMaster`*, since it is an interface to the whole game with all of the commands.

Here are the public methods in the GameMaster class:
```Java
public State getGameState();
public void turnPlayerLeft();
public void turnPlayerRight();
public String movePlayerForward();
public String movePlayerBackward();
public String getPlayerStatus();
public String look();
public String check();
public String openDoor();
public String initiateTrade();
public String buyItem(String itemName);
public String sellItem(String itemName);
public String listSellerItems();
public String finishTrade();
public String useItem(String name);
public String switchLights();
```

Note, the actual logic is not implemented here, each is done in its respective class (*`Position`* for movement, *`Player`* for other player related commands, *`TradeHandler`* for the trade). Nor is the checking implemented here, the *`ActionValidityChecker`* does that.

In a way, the *`Player`* class is also a facade, since it has very little logic, and is mostly wrapping other classes APIs for more Player-centric methods. This is done by usage of *`Position`*, *`Inventory`*, *`CheckVisitor`*, and *`UseItemVisitor`* classes instead of writing all of that logic in *`Player`*.


## Mediator Pattern
*`TradeHandler`* is a high level class that provides an interface to *`TransactionHandler`*, it is used to translate the low-level interaction into a String to display to the user.

The *`TransactionHandler`* acts as a mediator between the *`TradeHandler`* and the actual implementation of buying or selling between a seller and a player.
*`TransactionHandler`* deals with the communication between the player and the seller so that it is much more seamless to trade in both *`TradeHandler`* and *`GameMaster`*.

In the TransactionHandler class:
```Java
static TransactionHandler startTransaction(Player player, Seller seller) {
   return new TransactionHandler(player, seller);
}

void buy(String itemName) {
   long price = seller.getItemPrice(itemName);
   player.removeGoldFromInventory(price);
   Item itemBought = seller.takeItem(itemName);
   player.addItemToInventory(itemBought);
}

void sell(String itemName) {
   Item itemSold = player.takeItemFromInventory(itemName);
   seller.addItem(itemSold);
   long price = seller.getItemPrice(itemName);
   player.addGoldToInventory(price);
}
```

Then in the TradeHandler class:
```Java
public TradeHandler(Player player, Seller seller) {
   transactionHandler = TransactionHandler.startTransaction(player, seller);
}

public String list() {
   return transactionHandler.listSellerItems() + "\n" + transactionHandler.listSellerPriceList();
}

public String buy(String itemName) {
   transactionHandler.buy(itemName);
   return itemName + " bought and acquired";
}

public String sell(String itemName) {
   transactionHandler.sell(itemName);
   return itemName + " sold";
}
```


# SOLID Principles
## Single-Responsibility Principle
See [G30](#G30-Functions-Should-Do-One-Thing) for a good example on a refactorization for better SRP. One of the refactors was in the `visitRoom` method that serializes the *`Room`*. However, this violates the SRP. Instead, visiting the room should only visit the room in the graph sense (mark the room as visited and add to the queue).


I have also split the *`GameParser`* class into 3 classes: *`GameParser`*, *`ItemParser`*, and *`MapSiteParser`*. Each only deals with what the name implies.

There's also the validation of the state and the requested action where it used to be in the *`GameMaster`* class. However, this would mean that the *`GameMaster`* is an interface to the game AND an error checking class. Instead, I created *`ActionValidityChecker`* which does the error checking.


## Open-Closed Principle
You’ll notice in the `mazegame.util` package, there exists an *`ItemFormatter`* utility class. However, that code was initially inside the *`ItemManager`* class. This violates the SRP and even the OCP since now there are at least two reasons to change the *`ItemManager`*.

See [G5](#G5-Duplication) and [G36](#G36-Avoid-Transitive-Navigation) for more about OCP.


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

See [G6](#G6-Code-at-Wrong-Level-of-Abstraction) and [Item 41](#Item-41-Use-marker-interfaces-to-define-types).


## Dependency Inversion Principle
As I mentioned above, I always wrote my code at the highest level of abstraction possible, weather for a method or a variable. For example, the *`Room`* does not depend on specific Mapsites (every Room must have a door if it is reachable) yet the variable mapSites uses *`SerializableMapSite`*.
```Java
private final Map<Direction, SerializableMapSite> mapSites;
```
This ensures that the specifics can be changed easily without touching this code.

See also [Item 64](#Item-64-Refer-to-objects-by-their-interfaces).


# Google's Java Styling Guide
## Source file basics
These styling guides are obvious things that I already did by default.

## Source file structure
A notable style guide in this section is the prohibition of using wildcard imports, this is the opposite of what Robert says in *J1: Avoid Long Import Lists by Using Wildcards*. However, the styling guide is a requirement for the document to be considered *“Google Style”*.

Also, overloaded constructors always appear after one another, with the highest abstraction at the top.

## Formatting
I was already following most of the things mentioned here, except the 2-spaces indentation (compared to 4) and a column limit of 100 characters which then needs to be wrapped.

## Naming
The only thing I had to do here is to rename my JSON helper classes, methods, and variables, since *JSONSerializer* for example should be *JsonSerializer*.

In the end, I ran my code through a styler just to make sure that it satisfies Google’s Java Style Guide.


# Data Structures
The main data structure used in the game is obviously a graph. The rooms represent the nodes, while the doors represent the edges. The map actually represented a real-life graph.

Map, Set, List, and Queue were also used in various places.

The Map was mainly used to store the items by mapping the name (a String) to the actual *`Item`* object. It was used because of the performance and the actual need of mapping values.

While the Set and Queue were used to mark the visited nodes (rooms) and to queue the order of the visiting, respectively. Sets don’t allow duplication and Hashsets are extremely fast, so it makes sense to use it to mark visited rooms.

Finally, lists were simply used as a way to save a collection of items, since it required no searching; it was the perfect data structure for that.
