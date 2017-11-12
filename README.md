## Money Dispatcher

Slack app for gathering information about _loans_ between members of the team.

### Usage
#### Commands
Currently there are three commands that can be executed in channel view:
* `money-balance` - show balances between user and all other team members
* `money-history`- show history of all payments between 
* `money-add @user value description` - add new payment between `@me` and `@user` with specific `value`. `Description` is optional field.

#### Bot
There is a special _Bot_ called `Money` that will give notifications about actions that relates to `@me`.

#### Limitations
1. You can create payment between yourself and one other user - by default user that invokes command is a _payer_ and user provided in parameters is _receiver_. To revert the relationship pass negative number as `value`.  
2. There is no distinguish between payment for something and returning the money. You only can write it in `description`.
3. You cannot filter or sort results from `money-history` or `money-balance` commands.
4. It's not possible to delete payment that was accepted by the server. To revert it just make another payment with reverted value. 

### Development
All required configuration is stored in `application-dev-sample.yml` file. Copy it do `application-dev.yml` and add proper configuration information.

To make this file be loaded during startup you need to activate app profile `dev`. Easiest way in local setup is to set environment variable `SPRING_PROFILES_ACTIVE`. More information available at [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html).

#### Neo4J
Application uses Neo4J to store data. Easiest way to install it is by using `docker-compose.yml`.

### Deployment
#### Heroku
Current production version runs on `Heroku` cloud. `Neo4J` is provided by `GrapheneDB` add-on.

To properly run application you need to specified all required env variables. They are listed in `application-heroku.yml` file.
Additionally you need to activate `heroku` profile.
#### Docker
You can also prepare Docker image using provided `Dockerfile`

### Contribution
Pull requests with new features and bugfixes are welcome :)