package storage.mongo

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import auth.AuthConfigsDataStore
import cluster.{ClusterStateDataStore, InMemoryClusterStateDataStore}
import com.typesafe.config.ConfigFactory
import env.Env
import events.{AlertDataStore, AuditDataStore, HealthCheckDataStore}
import gateway.{InMemoryRequestsDataStore, RequestsDataStore}
import models._
import otoroshi.script.{InMemoryScriptDataStore, ScriptDataStore}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json._
import play.api.{Configuration, Environment, Logger}
import reactivemongo.api.{MongoConnection, MongoDriver}
import ssl.{
  CertificateDataStore,
  ClientCertificateValidationDataStore,
  InMemoryClientCertificateValidationDataStore,
  RedisClientCertificateValidationDataStore
}
import storage.inmemory._
import storage.{DataStoreHealth, DataStores, RedisLike, RedisLikeStore}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Failure

class MongoDataStores(configuration: Configuration, environment: Environment, lifecycle: ApplicationLifecycle, env: Env)
    extends DataStores {

  lazy val logger = Logger("otoroshi-mongo-datastores")

  lazy val uri: String         = configuration.getOptional[String]("app.mongo.uri").get
  lazy val database: String    = configuration.getOptional[String]("app.mongo.database").getOrElse("default")
  lazy val strictMode: Boolean = configuration.getOptional[Boolean]("app.mongo.strict").getOrElse(false)

  lazy val parsedUri      = MongoConnection.parseURI(uri).get
  lazy val dbName: String = parsedUri.db.getOrElse(database)

  lazy val statsItems: Int = configuration.getOptional[Int]("app.mongo.windowSize").getOrElse(99)

  lazy val actorSystem = ActorSystem(
    "otoroshi-mongo-system",
    configuration
      .getOptional[Configuration]("app.actorsystems.datastore")
      .map(_.underlying)
      .getOrElse(ConfigFactory.empty)
  )

  lazy val driver                      = new MongoDriver(Some(configuration.underlying), None)
  lazy val connection: MongoConnection = driver.connection(parsedUri, strictMode).get

  lazy val redis = new MongoRedis(actorSystem, connection, dbName)

  override def before(configuration: Configuration, environment: Environment, lifecycle: ApplicationLifecycle) = {
    logger.info(s"Now using Mongo DataStores dbName:$dbName, uri:$parsedUri")
    redis.start()
    if (configuration.getOptional[Boolean]("app.mongo.testMode").getOrElse(false)) {
      logger.warn("Flushing DB as in test mode")
      Await.result(redis.flushall(), 5.second)
    }
    Await.result(redis.initIndexes(), 5.second)
    _serviceDescriptorDataStore.startCleanup(env)
    _certificateDataStore.startSync()
    FastFuture.successful(())
  }

  override def after(configuration: Configuration, environment: Environment, lifecycle: ApplicationLifecycle) = {

    import actorSystem.dispatcher

    _serviceDescriptorDataStore.stopCleanup()
    _certificateDataStore.stopSync()
    Await.ready(
      connection
        .askClose()(10.seconds)
        .map { _ =>
          logger.debug("Mongo connections are stopped")
        }
        .andThen {
          case Failure(reason) =>
            reason.printStackTrace()
            driver.close() // Close anyway
          case _ => driver.close()
        },
      12.seconds
    )
    redis.stop()
    actorSystem.terminate()
    FastFuture.successful(())
  }

  private lazy val _privateAppsUserDataStore    = new InMemoryPrivateAppsUserDataStore(redis, env)
  private lazy val _backOfficeUserDataStore     = new InMemoryBackOfficeUserDataStore(redis, env)
  private lazy val _serviceGroupDataStore       = new InMemoryServiceGroupDataStore(redis, env)
  private lazy val _globalConfigDataStore       = new InMemoryGlobalConfigDataStore(redis, env)
  private lazy val _apiKeyDataStore             = new InMemoryApiKeyDataStoreWrapper(redis, env)
  private lazy val _serviceDescriptorDataStore  = new InMemoryServiceDescriptorDataStoreWrapper(redis, statsItems, env)
  private lazy val _u2FAdminDataStore           = new InMemoryU2FAdminDataStore(redis)
  private lazy val _simpleAdminDataStore        = new InMemorySimpleAdminDataStore(redis, env)
  private lazy val _alertDataStore              = new InMemoryAlertDataStore(redis)
  private lazy val _auditDataStore              = new InMemoryAuditDataStore(redis)
  private lazy val _healthCheckDataStore        = new InMemoryHealthCheckDataStore(redis, env)
  private lazy val _errorTemplateDataStore      = new InMemoryErrorTemplateDataStore(redis, env)
  private lazy val _requestsDataStore           = new InMemoryRequestsDataStore()
  private lazy val _canaryDataStore             = new InMemoryCanaryDataStore(redis, env)
  private lazy val _chaosDataStore              = new InMemoryChaosDataStore(redis, env)
  private lazy val _jwtVerifDataStore           = new InMemoryGlobalJwtVerifierDataStore(redis, env)
  private lazy val _globalOAuth2ConfigDataStore = new InMemoryAuthConfigsDataStore(redis, env)
  private lazy val _certificateDataStore        = new InMemoryCertificateDataStore(redis, env)

  private lazy val _clusterStateDataStore                   = new InMemoryClusterStateDataStore(redis, env)
  override def clusterStateDataStore: ClusterStateDataStore = _clusterStateDataStore

  private lazy val _clientCertificateValidationDataStore = new InMemoryClientCertificateValidationDataStore(redis, env)
  override def clientCertificateValidationDataStore: ClientCertificateValidationDataStore =
    _clientCertificateValidationDataStore

  private lazy val _scriptDataStore             = new InMemoryScriptDataStore(redis, env)
  override def scriptDataStore: ScriptDataStore = _scriptDataStore

  override def privateAppsUserDataStore: PrivateAppsUserDataStore               = _privateAppsUserDataStore
  override def backOfficeUserDataStore: BackOfficeUserDataStore                 = _backOfficeUserDataStore
  override def serviceGroupDataStore: ServiceGroupDataStore                     = _serviceGroupDataStore
  override def globalConfigDataStore: GlobalConfigDataStore                     = _globalConfigDataStore
  override def apiKeyDataStore: ApiKeyDataStore                                 = _apiKeyDataStore
  override def serviceDescriptorDataStore: ServiceDescriptorDataStore           = _serviceDescriptorDataStore
  override def u2FAdminDataStore: U2FAdminDataStore                             = _u2FAdminDataStore
  override def simpleAdminDataStore: SimpleAdminDataStore                       = _simpleAdminDataStore
  override def alertDataStore: AlertDataStore                                   = _alertDataStore
  override def auditDataStore: AuditDataStore                                   = _auditDataStore
  override def healthCheckDataStore: HealthCheckDataStore                       = _healthCheckDataStore
  override def errorTemplateDataStore: ErrorTemplateDataStore                   = _errorTemplateDataStore
  override def requestsDataStore: RequestsDataStore                             = _requestsDataStore
  override def canaryDataStore: CanaryDataStore                                 = _canaryDataStore
  override def health()(implicit ec: ExecutionContext): Future[DataStoreHealth] = redis.health()(ec)
  override def chaosDataStore: ChaosDataStore                                   = _chaosDataStore
  override def globalJwtVerifierDataStore: GlobalJwtVerifierDataStore           = _jwtVerifDataStore
  override def certificatesDataStore: CertificateDataStore                      = _certificateDataStore
  override def authConfigsDataStore: AuthConfigsDataStore                       = _globalOAuth2ConfigDataStore
  override def rawExport(
      group: Int
  )(implicit ec: ExecutionContext, mat: Materializer, env: Env): Source[JsValue, NotUsed] =
    throw new RuntimeException("Cluster mode not supported for Mongo datastore")
}

class InMemoryApiKeyDataStoreWrapper(redisCli: RedisLike, _env: Env) extends InMemoryApiKeyDataStore(redisCli, _env) {

  private val customFmt = new Format[ApiKey] {
    override def reads(json: JsValue): JsResult[ApiKey] = {
      ApiKey._fmt.reads(json).map(a => a.copy(metadata = a.metadata.map(t => (t._1.replaceAll("_dot_", "."), t._2))))
    }

    override def writes(o: ApiKey): JsValue = {
      ApiKey._fmt.writes(o.copy(metadata = o.metadata.map(t => (t._1.replaceAll("\\.", "_dot_"), t._2))))
    }
  }

  override def fmt: Format[ApiKey] = customFmt
}

class InMemoryServiceDescriptorDataStoreWrapper(redisCli: RedisLike, maxQueueSize: Int, _env: Env)
    extends InMemoryServiceDescriptorDataStore(redisCli, maxQueueSize, _env) {

  private val customFmt = new Format[ServiceDescriptor] {
    override def reads(json: JsValue): JsResult[ServiceDescriptor] = {
      ServiceDescriptor._fmt
        .reads(json)
        .map(a => a.copy(metadata = a.metadata.map(t => (t._1.replaceAll("_dot_", "."), t._2))))
    }

    override def writes(o: ServiceDescriptor): JsValue = {
      ServiceDescriptor._fmt.writes(o.copy(metadata = o.metadata.map(t => (t._1.replaceAll("\\.", "_dot_"), t._2))))
    }
  }

  override def fmt: Format[ServiceDescriptor] = customFmt
}
