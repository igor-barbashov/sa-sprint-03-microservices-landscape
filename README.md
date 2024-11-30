## Настройка Kafka

1. Скачайте последнюю версию Apache Kafka с официального сайта: [Kafka Downloads](https://kafka.apache.org/downloads)

2. Распакуйте архив в удобное место на вашем сервере или локальной машине

3. Команды выполняем в директории с дистрибутивом Kafka
   - Запустите ZooKeeper с использованием предоставленного скрипта
    ```bash
    bin/zookeeper-server-start.sh config/zookeeper.properties
    ```

   - После запуска ZooKeeper запустите Kafka-брокер
    ```bash
    bin/kafka-server-start.sh config/server.properties
    ```

   - Создаем топик `weather-data`
    ```bash
    bin/kafka-topics.sh --create --topic weather-data --bootstrap-server localhost:9092 --partitions 5 --replication-factor 3
    ```

   - Проверяем, что топик был успешно создан
    ```bash
    bin/kafka-topics.sh --list --bootstrap-server localhost:9092
    ```

   - Подробная информация о топике
    ```bash
    bin/kafka-topics.sh --describe --topic weather-data --bootstrap-server localhost:9092
    ```
   
   - Удаление топика
   ```bash
   bin/kafka-topics.sh --delete --topic weather-data --bootstrap-server localhost:9092
   ```

   - Топики в Kafka могут быть настроены с различными параметрами для оптимизации производительности и надёжности
    ```bash
    bin/kafka-configs.sh --alter --topic weather-data --add-config retention.ms=604800000 --bootstrap-server localhost:9092
    ```

    ```bash
    bin/kafka-configs.sh --alter --topic weather-data --add-config cleanup.policy=delete --bootstrap-server localhost:9092
    ```

    ```bash
    bin/kafka-configs.sh --alter --topic weather-data --add-config min.insync.replicas=2 --bootstrap-server localhost:9092
    ```

   - Вы можете добавить разделы к существующему топику с помощью команды
    ```bash
    bin/kafka-topics.sh --alter --topic weather-data --partitions 5 --bootstrap-server localhost:9092
    ```

4. Запуск UI for Apache Kafka
```bash
docker run -it -p 8081:8080 -e DYNAMIC_CONFIG_ENABLED=true provectuslabs/kafka-ui
```

5. Запуск нескольких брокеров Kafka
   - Для каждого брокера создаем свой файл настроек `server-1.properties`
      ```
      broker.id=1
      listeners=PLAINTEXT://0.0.0.0:9091
      advertised.listeners=PLAINTEXT://host.docker.internal:9091
      log.dirs=/tmp/kafka-logs-1
      zookeeper.connect=localhost:2181
      ```
      ```
      broker.id=2
      listeners=PLAINTEXT://0.0.0.0:9092
      advertised.listeners=PLAINTEXT://host.docker.internal:9092
      log.dirs=/tmp/kafka-logs-2
      zookeeper.connect=localhost:2181
      ```
   - Запускаем брокеры
      ```bash
      bin/kafka-server-start.sh config/server-1.properties
      ```
      ```bash
      bin/kafka-server-start.sh config/server-2.properties
      ```
   - Подготовка продьюсеров
      ```java
      props.put("bootstrap.servers", "host.docker.internal:9091,host.docker.internal:9092,host.docker.internal:9093");
      ```

# Базовая настройка

## Запуск minikube

[Инструкция по установке](https://minikube.sigs.k8s.io/docs/start/)

```bash
minikube start
```

## Добавление токена авторизации GitHub

[Получение токена](https://github.com/settings/tokens/new)

При создании токена можно не указывать ни одного `scope` - это будет соответствовать `Grants read-only access to public information (including user profile info, repository info, and gists)`. 

```bash
kubectl create secret docker-registry ghcr --docker-server=https://ghcr.io --docker-username=<github_username> --docker-password=<github_token> -n default
```

## Установка API GW kusk

[Install Kusk CLI](https://docs.kusk.io/getting-started/install-kusk-cli)

В Windows операцию надо выполнять в консоли открытой от имени администратора. 

```bash
kusk cluster install
```

## Смена адреса образа в helm chart

После того как вы сделали форк репозитория и у вас в репозитории отработал GitHub Action. Вам нужно получить адрес образа `https://github.com/<github_username>/<repository_name>/pkgs/container/<docker_image_name>`

Он выглядит таким образом
```ghcr.io/<github_username>/<docker_image_name>:latest```

Замените адрес образа в файлах:
- `helm/smart-home-monolith/values.yaml`
- `.github/workflows/ci.yaml`

на полученное название docker-образа:

```yaml
image:
  repository: ghcr.io/<github_username>/<docker_image_name>
  tag: latest
```

или (это одно и тоже)

```yaml
image:
  repository: ghcr.io/<github_username>/<docker_image_name>:latest
```

## Настройка terraform

[Установите Terraform](https://yandex.cloud/ru/docs/tutorials/infrastructure-management/terraform-quickstart#install-terraform)

Создайте файл `~/.terraformrc`. В Windows файл должен называться `terraform.rc` и находиться в папке `%APPDATA%`.

```hcl
provider_installation {
  network_mirror {
    url = "https://terraform-mirror.yandexcloud.net/"
    include = ["registry.terraform.io/*/*"]
  }
  direct {
    exclude = ["registry.terraform.io/*/*"]
  }
}
```

## Применяем terraform конфигурацию

```bash
cd terraform
terraform init
terraform apply
```

## Настройка API GW

```bash
kusk deploy -i api.yaml
```

## Проверяем работоспособность

```bash
kubectl port-forward svc/kusk-gateway-envoy-fleet -n kusk-system 8080:80
```

```bash
curl localhost:8080/hello
```

Результат в консоли

<img src="/images/cnosole.png" width="640"/>

## Github Actions

Делаем push в репозиторий. Результат выполнения `ci` `action`:

<img src="/images/gh-action.png" width="640"/>

## Delete minikube

```bash
minikube delete
```
