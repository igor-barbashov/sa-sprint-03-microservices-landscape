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
