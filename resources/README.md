# Reward System Code Challenge [![Build Status](https://travis-ci.org/tavlima/code-challenge-reward-system.svg?branch=master)](https://travis-ci.org/tavlima/code-challenge-reward-system)

A Clojure solution for the Nubank's Reward System Code Challenge.

## Solution specifics

This solution was written in Clojure, with Pedestal as the web framework, Leiningen as build tool and Midje as the testing lib.

I tried to follow some best practices, the [hexagonal/port-adapter architecture](http://alistair.cockburn.us/Hexagonal+architecture) and the "Clojure Way of Building Great Stuff®" but, as I had no previous experience with Clojure nor this particular architecture, I'm pretty sure there's a lot of room to improvement.

### Namespaces

The code is split in 5 namespaces:

* **tree** - tree navigation/operations
* **invitations** - tree node implementation (User) and business rules
* **persistence** - stores the invitations' tree
* **rest** - drives the application by HTTP requests
* **main** - Pedestal boilerplate, routes and input file handling

#### tree

This code was *heavily* inspired by the `clojure.zip` module and functions. I used some protocols to define the interfaces and records implementing them. Only the required functions for this challenge where implemented, so there's no backward navigation or branching control, like in the `clojure.zip` module. All the namespaces here are nested into the `tree.domain` namespace.

* **location** - Defines both the `Location` protocol and the `LocationImpl` record. The record encapsulates all the navigation logic and context.
* **zippable** - Defines the `Zippable` protocol, much like the functions you are required to provide as arguments to `clojure.zip/zipper`, to provide an abstraction to build `LocationImpl` on top and simplify the module usage.
* **tree** - A `Tree` protocol and it's companion record, `TreeImpl`. The protocol specify some search helper functions (`findFirst` and `findFirstByMatcher`) since those can be type and business agnostic. The record has a `uids` hash-set to speed-up exists/contains operations.
* **treenode** - A simple `TreeNode` protocol, on top of which the `TreeImpl` is built. 

#### invitations

This namespace constrains most of the business code, including the domain protocol `User` and the `UserImpl` record.

* **domain.user** - Defines the `UserImpl` record, which extends not only the `User` protocol but also the `tree.domain.zippable/Zippable` and `tree.domain.treenode/TreeNode`.
* **controller** - Functions that implement the business rules by coordinating the tree navigation, checks and updates
* **port** - The "public" interface of this namespace/component. Currently, it just wraps the controller's functions that should be publicly exposed.

#### persistence

This persistence module is backed by a single `atom`, where the current invitation tree is stored and updated. As there's no real persistence here, like a file or a proper datastore, all changes are lost on application shutdown.

#### rest

* **adapter-invitations** - Binds the `invitations`/`persistence` layers and do the required parameters transformation. 
* **controller** - Functions that validate the input, call the `invitations` namespace's functions (through the `adapter-invitations`) and check the results. If ok, do any required transformation and return. Otherwise, return the proper error response.
* **port** - The "public" interface of this namespace/component. It's responsible for extracting the parameters from the HTTP request map and calling the proper controller function. The Pedestal routing map points to this namespace.

#### main

* **server** - Pedestal start-up (dev/prod) and argument handling
* **service** - Pedestal server settings
* **route** - Pedestal routing map
* **bootstrap** - Input file reading/parsing/loading. It calls the `rest.adapter-invitations` functions to handle each invitation record. 

### Tests

I tried to keep the tests as simple as possible, focusing on the `invitations` and `tree` namespace. Besides being mostly straight-forward, the remaining namespaces and functions would require some mocking/integration machinery to be fully tested.

## Build and running

This solution uses Leiningen as it's build tool. The `FILE` argument mentioned in the next sections is optional (more details [here](#input-file)). If no `FILE` is supplied, the registry starts empty.

### REPL

1. `lein repl`
2. `=> (def dev-server (run-dev FILE))`

### Leiningen

`lein run FILE` _OR_ `lein run-dev FILE`

## API

### Get user's info

Gets user's information.

**Request**

`GET /users/:id`

**Parameters**

* `:id` - User id (string)

**Response format**

```
{
  "id": "1",       // :id
  "score": 1.0,    // current score
  "invited": ["2"] // ids of the users invited by this one
}
```

**Return codes**

* `200 OK` - Success
* `400 Bad Request` - `:id` is invalid
* `404 Not Found` - `:id` doesn't exist

### Invite a new user

Invites a new user and returns the updated inviter.

**Request**

`POST /users/:inviter/invite`

**Body**

```
{
  "invitee": :invitee
}
```

**Parameters**

* `:inviter` - The inviter's user id (string)
* `:invitee` - The invitee's user id (string)

**Response format**

```
{
  "id": "1",           // :id
  "score": 1.0,        // current score
  "invited": ["2","3"] // ids of the users invited by this one
}
```

**Return codes**

* `200 OK` - Success
* `400 Bad Request` - `:invitee`/`:inviter` is missing or invalid
* `404 Not Found` - `:inviter` doesn't exist

### Ranking

Gets the current ranking. The response list is sorted by score (desc) and id (asc).

**Request**

`GET /ranking`

**Response format**

```
[
    {"id":"1", "score":1.5},
    {"id":"2", "score":0},
    {"id":"3", "score":0},
    {"id":"4", "score":0}
]
```

**Return codes**

* `200 OK` - Success

## Testing

The tests were implemented using `midje`. The whole test suite can be run using the `lein midje` command.

## Production/Deployment

This code can be deployed in many fashions. Here are some options...

### Heroku

This code can be easily deployed to Heroku. Just click this button:

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

### Docker

1. Build an uberjar: `lein uberjar`
2. Build a Docker image: `docker build -t reward-system .`
3. Run the Docker image: `docker run -p 8080:8080 reward-system`

### JAR

1. Build an uberjar: `lein uberjar`
2. Run it: `java -jar target/reward-system-standalone.jar [FILE]`

### Note

Both Heroku and Docker starts the application with the default [input file](#input-file).

## Input file

Optionally, you can supply an input file from where the application will read the invitation records and populate the registry.

The file should have one invitation per line, in the `inviterId inviteeId` format. Both `inviterId` and `inviteeId` will be handled as strings.

Example:

```
1 2
1 abc
abc c
1 c
2 4
```

This application provides a input file sample, which can be found at `resources/input.txt`.