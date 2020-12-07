# Game Rental Application

## Description

The "Game Rental" application showcases how Axon Framework and Axon Server can be used during software development.
The domain which is focused on is that of rental services from the perspective of a video game store. 

This repository provides just such an application, albeit a demo rather than a full fledged solution.
It serves a personal purpose of having a stepping stone application to live code during application.
My intent is to build upon this sample during consecutive talks, further enhancing its capabilities and implementation as time progresses.
For others, I hope this provides a simple and quick look into what it means to build an Axon-based application.
 
Due to its nature of being based on Axon, it will incorporate DDD, CQRS, Event Sourcing, and an overall messaging solution to communicate between distinct components.
 
## Project Traversal

Distinct branches will be (made) available per public speaking, sharing a start and final solution branch separately.
Additionally, several branches representing the steps throughout the lifecycle of the "Game Rental" application will be present, allowing you to:
* Check out the exact step you are interested in.
* Perform a `git reset --hard step#` to reset your current branch.

Next to providing the convenience of showing the flow, it also serves the purpose of being a back-up during the presentation.

The following steps have been defined for this project:

1. The `core-api`, containing the commands, events, queries and query responses.
2. The `command` model has been created, showing a `Game` aggregate.
3. The application connects to [Axon Cloud](https://console.cloud.axoniq.io/), through the added Axon Server properties to the `application.properties`.
4. The `query` model, a `GameView`, is provided, created/updated and made queryable through the `GameCatalogProjector`.
5. The [Reactor Extension](https://github.com/AxonFramework/extension-reactor) is included and used by the `GameRentalController`.
6. Cleaner distributed exceptional handling is introduced, through an `ExceptionStatusCode` specific exception being thrown in `@ExceptionHandler` annotated functions in the `Game` aggregate and `GameCatalogProjector`.
7. Spring's `@Profile{{profile-name})` annotation has been added to the `Game`, `GameCatalogProjector`, `GameViewRepository` and `GameRentalController`, allowing for application distribution.

## Running and testing the application

As this is a Spring Boot application, simply running the `GameRentalApplication` is sufficient.
However, Spring profiles are present, which allow for running portions of this application.
More specifically there's a `command`, `query` and `ui` profile present, thus segregating the `Game` aggregate, `GameCatalogProjector` and `GameRentalController` into their own runnables.
Furthermore, if Intellij is being use, these Run Configurations can be utilized which are present in the `./.run` folder.

Granted that a connection is made with an Axon Server instance.
Ideally [Axon Cloud](https://console.cloud.axoniq.io/) is used for this, as is shown in step 3.
If you desire to run Axon Server locally, you can download it [here](http://download.axoniq.io/quickstart/AxonQuickstart.zip).

To validate the inner workings of the app, two `.http` files have been provided to the root folder of this project.
The `register-games.http` allows for the registration of several games, to build a base catalog.
The `other-requests.http` can be used to test out all other operations.