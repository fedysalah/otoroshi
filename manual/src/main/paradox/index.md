# Otoroshi

**Otoroshi** is a layer of lightweight api management on top of a modern http reverse proxy written in <a href="https://www.scala-lang.org/" target="_blank">Scala</a> and developped by the <a href="https://maif.github.io" target="_blank">MAIF OSS</a> team that can handle all the calls to and between your microservices without service locator and let you change configuration dynamicaly at runtime.


> *The <a href="https://en.wikipedia.org/wiki/Gazu_Hyakki_Yagy%C5%8D#/media/File:SekienOtoroshi.jpg" target="blank">Otoroshi</a> is a large hairy monster that tends to lurk on the top of the torii gate in front of Shinto shrines. It's a hostile creature, but also said to be the guardian of the shrine and is said to leap down from the top of the gate to devour those who approach the shrine for only self-serving purposes.*

@@@ div { .centered-img }
[![Build Status](https://travis-ci.org/MAIF/otoroshi.svg?branch=master)](https://travis-ci.org/MAIF/otoroshi) [![Join the chat at https://gitter.im/MAIF/otoroshi](https://badges.gitter.im/MAIF/otoroshi.svg)](https://gitter.im/MAIF/otoroshi?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [ ![Download](https://img.shields.io/github/release/MAIF/otoroshi.svg) ](https://dl.bintray.com/maif/binaries/otoroshi.jar/1.4.7-dev/otoroshi.jar)
@@@

@@@ div { .centered-img }
<img src="https://github.com/MAIF/otoroshi/raw/master/resources/otoroshi-logo.png" width="300"></img>
@@@

## Installation

You can download the latest build of Otoroshi as a [fat jar](https://dl.bintray.com/maif/binaries/otoroshi.jar/1.4.7-dev/otoroshi.jar), as a [zip package](https://dl.bintray.com/maif/binaries/otoroshi-dist/1.4.7-dev/otoroshi-dist.zip) or as a @ref:[docker image](./getotoroshi/fromdocker.md).

You can install and run Otoroshi with this little bash snippet

```sh
curl -L -o otoroshi.jar 'https://dl.bintray.com/maif/binaries/otoroshi.jar/1.4.7-dev/otoroshi.jar'
# run the following line if you want to use the admin UI in your browser
sudo echo "127.0.0.1    otoroshi-api.foo.bar otoroshi.foo.bar otoroshi-admin-internal-api.foo.bar" >> /etc/hosts
# Java 8
java -jar otoroshi.jar
# Java 9 and 10
java --add-modules java.xml.bind -jar otoroshi.jar
```

or using docker

```sh
docker run -p "8080:8080" maif/otoroshi:1.4.7-dev
```

now open your browser to <a href="http://otoroshi.foo.bar:8080/" target="_blank">http://otoroshi.foo.bar:8080/</a>, **log in with the credential generated in the logs** and explore by yourself, if you want better instructions, just go to the @ref:[Quick Start](./quickstart.md) or directly to the @ref:[installation instructions](./getotoroshi/index.md)

You can also try Otoroshi online with Google Cloud Shell if you're a user of the Google Cloud Platform

@@@ div { .centered-img }
[![Open in Cloud Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/open?git_repo=https%3A%2F%2Fgithub.com%2Fmathieuancelin%2Fotoroshi-tutorial&page=shell&tutorial=tutorial.md)
@@@

## Documentation

* @ref:[About Otoroshi](./about.md)
* @ref:[Architecture](./archi.md)
* @ref:[Features](./features.md)
* @ref:[Try Otoroshi in 5 minutes](./quickstart.md)
* @ref:[Video tutorials](./videos.md)
* @ref:[Get Otoroshi](./getotoroshi/index.md)
* @ref:[First run](./firstrun/index.md)
* @ref:[Setup Otoroshi](./setup/index.md)
* @ref:[Using Otoroshi](./usage/index.md)
* @ref:[Third party Integrations](./integrations/index.md)
* @ref:[Detailed topics](./topics/index.md)
* @ref:[Embedding Otoroshi](./embedding.md)
* @ref:[Admin REST API](./api.md)
* @ref:[Rust CLI](./cli.md)
* @ref:[Deploy to production](./deploy/index.md)
* @ref:[Connectors](./connectors/index.md)

## Discussion

Join the [Otoroshi](https://gitter.im/MAIF/otoroshi) channel on the [MAIF Gitter](https://gitter.im/MAIF)

## Sources

The sources of Otoroshi are available on [Github](https://github.com/MAIF/otoroshi).

## Logo

You can find the official Otoroshi logo [on GitHub](https://github.com/MAIF/otoroshi/blob/master/resources/otoroshi-logo.png). The Otoroshi logo has been created by François Galioto ([@fgalioto](https://twitter.com/fgalioto))

## Changelog

Every release, along with the migration instructions, is documented on the [Github Releases](https://github.com/MAIF/otoroshi/releases) page.

## Patrons

The work on Otoroshi was funded by <a href="https://www.maif.fr/" target="_blank">MAIF</a> with the help of the community.

## Licence

Otoroshi is Open Source and available under the [Apache 2 License](https://opensource.org/licenses/Apache-2.0)

@@@ index

* [About Otoroshi](about.md)
* [Architecture](archi.md)
* [Features](features.md)
* [Quickstart](quickstart.md)
* [Videos](videos.md)
* [Get otoroshi](getotoroshi/index.md)
* [First run](firstrun/index.md)
* [Setup](setup/index.md)
* [Using Otoroshi](usage/index.md)
* [Integrations](integrations/index.md)
* [Detailed topics](topics/index.md)
* [Admin REST API](api.md)
* [Embedding Otoroshi](./embedding.md)
* [Official Rust CLI](cli.md)
* [Deploy to production](deploy/index.md)
* [Connectors](connectors/index.md)

@@@
