# Game Rental Application

## Description

The "Game Rental" application showcases how you can use Axon Framework and Axon Server during software development.
The domain focused on is that of rental services from the perspective of a video game store.

This repository provides just such an application, albeit a demo rather than a full-fledged solution.
It serves the personal purpose of having a stepping stone application to live code during application.
I intend to build upon this sample during consecutive talks, further enhancing its capabilities and implementation as time progresses.
For others, I hope this provides a quick and straightforward look into what it means to build an Axon-based application.

Since Axon is at the basis of the sample, it will incorporate DDD, CQRS, Event Sourcing, and an overall messaging solution to communicate between distinct components.
 
## Project Traversal

Distinct branches will be (made) available per public speaking, sharing a start and final solution branch separately.
Additionally, several branches representing the steps throughout the lifecycle of the "Game Rental" application will be present, allowing you to:
* Check out the exact step that interests you.
* Perform a `git reset --hard step#` to reset your current branch.

Next to providing the convenience of showing the flow, it also serves as a backup during the presentation.

This project currently contains the following steps:

1. The `core-api`, containing the commands, events, queries, and query responses.
2. The `command` model has been created, showing a `Game` aggregate.
3. The application connects to [Axon Cloud](https://console.cloud.axoniq.io/), through the added Axon Server properties to the `application.properties`.
4. The `query` model, a `GameView`, is provided, created/updated and made queryable through the `GameCatalogProjector`.
5. This step includes the [Reactor Extension](https://github.com/AxonFramework/extension-reactor), which is used by the `GameRentalController`.
6. Cleaner distributed exceptional handling is introduced, through an `ExceptionStatusCode` specific exception being thrown in `@ExceptionHandler` annotated functions in the `Game` aggregate and `GameCatalogProjector`.

## Running and testing the application

As this is a Spring Boot application, simply running the `GameRentalApplication` is sufficient.
However, Spring profiles are present, which allow for running portions of this application.
More specifically, there's a `command`, `query`, and `ui` profile present, thus segregating the `Game` aggregate, `GameCatalogProjector`, and `GameRentalController` into their separate runnables.
Furthermore, when you use IntelliJ, you can use the "Run Configurations" from the `./.run` to speed up the startup process.

The application does expect it can make a connection with an Axon Server instance.
Ideally, [Axon Cloud](https://console.cloud.axoniq.io/) is used for this, as is shown in step 3.
If you desire to run Axon Server locally, you can download it [here](http://download.axoniq.io/quickstart/AxonQuickstart.zip).

For validating the application's internals, the projects include tests and two `.http` files (in the root folder of this project). The tests show all the basics, whereas the `.http` files (supported in [IntelliJ Ultimate](https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html) only) allow invocation of the endpoints.
The `register-games.http` allows for the registration of several games to build a base catalog.
The `other-requests.http` file contains all other operations for testing.
