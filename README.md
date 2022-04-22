# Game Rental Application

## Description

The "Game Rental" application showcases how [Axon Framework](https://github.com/AxonFramework/AxonFramework) 
 and [Axon Server](https://developer.axoniq.io/axon-server/overview) or [AxonIQ Cloud](https://cloud.axoniq.io/) can be used during software development.
The domain focused on is that of rental services from the perspective of a video game store.

This repository provides just such an application, albeit a demo rather than a full-fledged solution.
It serves the personal purpose of having a stepping stone application to live code during application.
I intend to build upon this sample during consecutive talks, further enhancing its capabilities and implementation as time progresses.
For others, I hope this provides a quick and straightforward look into what it means to build an Axon-based application.

Due to its nature of being based on Axon, it incorporates [DDD](https://developer.axoniq.io/domain-driven-design/overview), 
 [CQRS](https://developer.axoniq.io/cqrs/overview), [Event Sourcing](https://developer.axoniq.io/event-sourcing/overview), 
 and an overall message-driven solution to communicate between distinct components.

> **Demo Recordings with different Game Rental implementations**
>
> Since I aim to use this project for some time, it'll change through its lifecycle.
> Most notably, I'll keep it up to date with recent versions of the dependencies.
>
> Due to this, recordings from previous iterations of this project will likely show slight deviations.
> However, the taken steps during those recordings will remain intact.
 
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

## Running and testing the application

As this is a Spring Boot application, simply running the `GameRentalApplication` is sufficient.
Granted that a connection is made with an Axon Server instance.
Ideally [Axon Cloud](https://console.cloud.axoniq.io/) is used for this, as is shown in step 3.
If you desire to run Axon Server locally, you can download it [here](http://download.axoniq.io/quickstart/AxonQuickstart.zip).

Note that any new components introduced in a step include unit tests too.
