# Reward System Code Challenge [![Build Status](https://travis-ci.org/tavlima/code-challenge-reward-system.svg?branch=master)](https://travis-ci.org/tavlima/code-challenge-reward-system)

A Clojure solution for the Nubank's Reward System Code Challenge.

## Build and running

This solution uses Leiningen as it's build tool. The `FILE` argument mentioned in the next sections is optional (more details [here](#input-file)). If no `FILE` is supplied, the registry starts empty.

### REPL

1. `lein repl`
2. `=> (def dev-server (run-dev FILE))`

### Leiningen

`lein run FILE` _OR_ `lein run-dev FILE`

## API

### Get user's info

Gets user's information. Returns status code `404` if the requested user is invalid (doesn't exist or is not an integer).

**Request**

`GET /users/:id`

**Parameters**

* `:id` - User id

**Response format**

```
{
  "id": 1,       // :id
  "score": 1.0,  // current score
  "invited": [2] // ids of the users invited by this one
}
```

**Return codes**

* `200 OK` - Sucess
* `404 Not Found` - `:id` is invalid (not an integer)

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

* `:inviter` - The inviter's user id (integer)
* `:invitee` - The invitee's user id (integer)

**Response format**

```
{
  "id": 1,         // :id
  "score": 1.0,    // current score
  "invited": [2,3] // ids of the users invited by this one
}
```

**Return codes**

* `200 OK` - Sucess
* `400 Bad Request` - `:invitee` is missing or invalid (not an integer)
* `404 Not Found` - `:inviter` is invalid (not an integer)

### Ranking

Gets the current ranking. The response list is sorted by score (desc) and id (asc).

**Request**

`GET /ranking`

**Response format**

```
[
    {"id":1, "score":1.5},
    {"id":2, "score":0},
    {"id":3, "score":0},
    {"id":4, "score":0}
]
```

**Return codes**

* `200 OK` - Sucess

## Testing

The tests were implemented using `midje`. The whole test suite can be run using the `lein midje` command.

## Production/Deployment

This code can be deployed in many fashions. Here are some options...

### Heroku

This code can be easily deployed to Heroku. Just click this button:

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

### Docker

1. Build an uberjar: `lein uberjar`
2. Build a Docker image: `sudo docker build -t reward-system`
3. Run the Docker image: `docker run -p 8080:8080 reward-system`

### JAR

1. Build an uberjar: `lein uberjar`
2. Run it: `java -jar target/reward-system-standalone.jar [FILE]`

### Note

Both Heroku and Docker starts the application with the default [input file](#input-file).

## Input file

Optionally, you can supply an input file from where the application will read the invitation records and populate the registry.

The file should have one invitation per line, in the `inviterId inviteeId` format. Both `inviterId` and `inviteeId` should be integers.

Example:

```
1 2
1 3
3 4
3 5
2 4
```

This application provides a input file sample, which can be found at `resources/input.txt`.