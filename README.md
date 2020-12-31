# Project Structure
## cli package
* `Command`
* `ItemCommand`

This package includes the interfaces used for command line parsing. Both interfaces have a single `execute` method.

## exceptions package
* `InvalidUseOfItem`: thrown when the player tries to use an item in a place where it cannot be used, like using the wrong key or using a key with no *`Lockable`* object in-front of the player.
* `ItemNotFoundException`: thrown when a requested item does not exist.
* `MapSiteLockedException`: thrown when trying to open a locked object.
* `NoLightsException`: thrown when trying to toggle lights if the room does not have lights.
* `NotEnoughGoldException`: thrown if the player tries to buy items but does not have enough gold.

## item package
* `ItemManager`: Manages item retrieval and removal.
* `Item`: An interface used to define *`Item`* classes
    * `Key`
    * `Flashlight`
* `ItemVisitor`: An interface to define visitors for various `Item`(s)

## mapsite package
Includes various map sites, and their helper interfaces: `AbstractHangable`, `AbstractLockable`, `Checkable`, `CheckableVisitor`, `Chest`, `DarkMapSite`, `Door`, `Hangable`, `Lock`, `Lockable`, `Loot`, `MapSite`, `Mirror`, `Painting`, `Seller`, `SerializeableMapSite`.

## parser package
* `GameParser`: a utility class to parse the game.
* `ItemParser`: parses the items in the game
* `MapSiteParser`: parses the various map sites in the game.

## player package
Player-centric classes, like, `CheckVisitor`, `Inventory`, `Player`, `Position`, `UseItemVisitor`.

## room package
Includes `LightSwitch`, `NoLightSwitch` (special case), and `Room` classes.

## trade package
`TradeHandler` and `TransactionHandlers` used to handle the communication between the player and the seller.

## util package
General utility classes for item formatting, JSON serialization, or checking the validity of a certain action.

## mazegame package
* `Direction`: Enum class to identify and operate on the orientation of the player.
* `GameMaster`: The public API that moves all parts of the game.
* `Interpreter`: The command line parser and interpreter.
* `JsonSerializable`: An interface with a single `toJson` method
* `MazeMap`: Stores the map configurations
* `Response`: A data type that stores the message and a boolean that indicates the success or failure of an operation.
* `State`: Enum class for the state of the game. (EXPLORE, TRADE, WON, LOST)

# How to Use
Write a JSON map or use the included (simple) test map. The format of the map is intuitive if you follow this sample (time is in seconds):
```JSON
{
  "mapConfiguration": {
    "startRoomID": 1,
    "endRoomID": 3,
    "time": 1000,
    "orientation": "north",
    "gold": 2,
    "items": []
  },
  "rooms": [
    {
      "id": 1,
      "lightswitch": {
        "hasLights": true,
        "lightsOn": true
      },
      "north": {
        "siteMap": "Door",
        "roomID": 1,
        "otherRoomID": 2,
        "key": "Dragon Glass",
        "locked": true
      },
      "east": {
        "siteMap": "Chest",
        "loot": {
          "gold": 15,
          "items": [
            {
              "name": "Flashlight",
              "type": "Flashlight"
            },
            {
              "name": "Dragon Glass",
              "type": "Key"
            }
          ]
        },
        "key": "",
        "locked": false
      },
      "south": {
        "siteMap": "Mirror",
        "hiddenKey": ""
      },
      "west": {
        "siteMap": "Wall"
      }
    },
    {
      "id": 2,
      "lightswitch": {
        "hasLights": false,
        "lightsOn": false
      },
      "north": {
        "siteMap": "Door",
        "roomID": 2,
        "otherRoomID": 3,
        "key": "Monkee",
        "locked": true
      },
      "east": {
        "siteMap": "Painting",
        "hiddenKey": ""
      },
      "south": {
        "siteMap": "Door",
        "roomID": 1,
        "otherRoomID": 2,
        "key": "Dragon Glass",
        "locked": true
      },
      "west": {
        "siteMap": "Seller",
        "items": [
          {
            "name": "Monkee",
            "type": "Key"
          }
        ],
        "priceList": [
          {
            "name": "Monkee Key",
            "price": 18
          },
          {
            "name": "Dragon Glass Key",
            "price": 3
          },
          {
            "name": "Flashlight",
            "price": 5
          }
        ]
      }
    },
    {
      "id": 3,
      "lightswitch": {
        "hasLights": true,
        "lightsOn": false
      },
      "north": {
        "siteMap": "Wall"
      },
      "east": {
        "siteMap": "Wall"
      },
      "south": {
        "siteMap": "Door",
        "roomID": 2,
        "otherRoomID": 3,
        "key": "Monkee",
        "locked": true
      },
      "west": {
        "siteMap": "Wall"
      }
    }
  ]
}
```

Place the map file in the *MazeGame* directory, then simply run the project (Interpreter) and enter the file name without the extension. That’s it, now the game is running!

## List of commands:
Commands and names of items are case-insensitive
* Left
* Right
* Forward
* Backward
* Playerstatus
* Look
* Check mirror
* Check painting
* Check chest
* Check door
* Open
* Trade
* List
* Sell \<item>
* Buy \<item>
* Finish trade
* Use \<item>
* Save \<filename>
* Quit
* Restart

